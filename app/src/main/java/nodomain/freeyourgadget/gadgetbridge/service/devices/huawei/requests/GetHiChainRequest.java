package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

 import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.NoSuchPaddingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupport;
import nodomain.freeyourgadget.gadgetbridge.util.CryptoUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig.HiCHain;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig.HiChainStep;

public class GetHiChainRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetHiChainRequest.class);
    private byte operationCode = 0x02;
    private byte step;
    private byte[] seed = null;
    private byte[] authIdSelf = null;
    private byte[] authIdPeer = null;
    private byte[] randSelf = null;
    private byte[] randPeer = null;
    private long requestId = 0x00;
    private JSONObject json = null;
    private byte[] challenge = null;
    private byte[] psk = null;


    public GetHiChainRequest(HuaweiSupport support, boolean firstConnection) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.HiCHain.id;
        if (firstConnection) {
            operationCode = 0x01;
        }
        this.step = 0x01;
    }

    public GetHiChainRequest(
            HuaweiSupport support,
            TransactionBuilder builder,
            JSONObject json
    ) {
        super(support, builder);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.HiCHain.id;
        try {
            this.requestId = json.getLong("requestId");
            json.remove("requestId");
            this.operationCode = (byte)json.getInt("operationCode");
            json.remove("operationCode");
            this.step = (byte)json.getInt("step");
            json.remove("step");
            if (json.has("authIdSelf")) this.authIdSelf = ((String)json.remove("authIdSelf")).getBytes(StandardCharsets.UTF_8);
            if (json.has("authIdPeer")) this.authIdPeer = ((String)json.remove("authIdPeer")).getBytes(StandardCharsets.UTF_8);
            if (json.has("randSelf")) this.randSelf = ((String)json.getString("randSelf")).getBytes(StandardCharsets.UTF_8);
            if (json.has("randPeer")) this.randPeer = ((String)json.getString("randPeer")).getBytes(StandardCharsets.UTF_8);
            if (json.has("psk")) this.psk = ((String)json.getString("psk")).getBytes(StandardCharsets.UTF_8);
            // this.seed = json.getString("seed").getBytes(StandardCharsets.UTF_8);
            // if (json.has("challenge")) this.challenge = json.getString("challenge").getBytes(StandardCharsets.UTF_8);
            this.json = json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        if (requestId == 0x00) {
            requestId = System.currentTimeMillis();
        }

        LOG.debug("Request operationCode: " + operationCode + " - step: " + step);
        HiCHain.Request req = new HiCHain.Request(operationCode, requestId, support.getAndroidId(), HuaweiConstants.GROUP_ID );
        try {
            if (step == 0x01) {
                seed = new byte[32];
                new Random().nextBytes(seed);
                randSelf = new byte[16];
                new Random().nextBytes(randSelf);
                HiCHain.Request.StepOne stepOne = req.new StepOne(support.paramsProvider, randSelf, seed );
                return stepOne.serialize();
            } else if (step == 0x02) {
                byte[] message = ByteBuffer
                        .allocate(randPeer.length + randSelf.length + authIdSelf.length + authIdPeer.length)
                        .put(randSelf)
                        .put(randPeer)
                        .put(authIdPeer)
                        .put(authIdSelf)
                        .array();
                byte[] selfToken = CryptoUtils.calcHmacSha256(psk, message);
                HiCHain.Request.StepTwo stepTwo = req.new StepTwo(support.paramsProvider, selfToken);
                return stepTwo.serialize();
            } else if (step == 0x03) {
                byte[] salt = ByteBuffer
                    .allocate( randSelf.length + randPeer.length)
                    .put(randSelf)
                    .put(randPeer)
                    .array();
                byte[] info = "hichain_iso_session_key".getBytes(StandardCharsets.UTF_8);
                byte[] sessionKey = CryptoUtils.hkdfSha256(psk, salt, info, 32);
                byte[] nonce = new byte[12];
                new Random().nextBytes(nonce);
                challenge = new byte[16];
                new Random().nextBytes(challenge);
                byte[] aad = "hichain_iso_exchange".getBytes(StandardCharsets.UTF_8);
                byte[] encData = CryptoUtils.encryptAES_GCM_NoPad(challenge, sessionKey, nonce, aad); //aesGCMNoPadding encrypt(sessionKey as key, challenge to encrypt, nonce as iv)
                HiCHain.Request.StepThree stepThree = req.new StepThree(support.paramsProvider, nonce, encData);
                return stepThree.serialize();
            } else if (step == 0x04) {
                byte[] nonce = new byte[12];
                new Random().nextBytes(nonce);
                byte[] input = new byte[]{4 * 0x00};
                byte[] aad = "hichain_iso_result".getBytes(StandardCharsets.UTF_8);
                byte[] encResult = CryptoUtils.encryptAES_GCM_NoPad(input, psk, nonce, aad);
                HiCHain.Request.StepFour stepFour = req.new StepFour(support.paramsProvider, nonce, encResult);
                return stepFour.serialize();

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
        return null;
    }

    @Override
    protected void processResponse() throws GBException {
        if (!(receivedPacket instanceof DeviceConfig.HiCHain.Response)) return;

        //if (step == 0x02 && operationCode == 0x02) step += 0x01;
        String jsonStr = new String(((DeviceConfig.HiCHain.Response) receivedPacket).json);
        LOG.debug("Response operationCode: " + operationCode + " - step: " + step);
        if (step == 0x04 && jsonStr == "com.huawei.health") {
            //Operation is finished go to next
            operationCode += 0x01;
            if (operationCode == 0x02) {
                GetHiChainRequest nextRequest = new GetHiChainRequest(this.support, false);
                this.support.addInProgressRequest(nextRequest);
                this.nextRequest(nextRequest);
            }
        } else {
            try {
                json = new JSONObject(jsonStr);
                JSONObject payload = json.getJSONObject("payload");
                if (step == 0x01) {
                    byte[] key = null;
                    authIdSelf = support.getAndroidId();
                    authIdPeer = GB.hexStringToByteArray(payload.getString("peerAuthId"));
                    randPeer = GB.hexStringToByteArray(payload.getString("isoSalt"));
                    byte[] peerToken = GB.hexStringToByteArray(payload.getString("token"));
                    // GeneratePsk
                    if (operationCode == 0x01) {
                        LOG.debug("pincode returned: " + StringUtils.bytesToHex(pastRequest.getValueReturned()));
                        String pincodeHexStr = StringUtils.bytesToHex(pastRequest.getValueReturned());
                        byte[] pincode = pincodeHexStr.getBytes(StandardCharsets.UTF_8);
                        key = CryptoUtils.digest(pincode);
                    } else {
                        key = support.getSecretKey();
                    }
                    psk = CryptoUtils.calcHmacSha256(key, seed);
                    LOG.debug("randPeer: " + GB.hexdump(randPeer) + " and randSelf: " + GB.hexdump(randSelf));
                    LOG.debug("authIdPeer: " + GB.hexdump(authIdPeer) + " and authIdSelf: " + GB.hexdump(authIdSelf));
                    byte[] message = ByteBuffer
                        .allocate(randPeer.length + randSelf.length + authIdSelf.length + authIdPeer.length)
                        .put(randPeer)
                        .put(randSelf)
                        .put(authIdSelf)
                        .put(authIdPeer)
                        .array();
                    byte[] tokenCheck = CryptoUtils.calcHmacSha256(psk, message);
                    if (!Arrays.equals(peerToken, tokenCheck)) {
                        LOG.debug("tokenCheck: " + GB.hexdump(tokenCheck) + " is different than " + GB.hexdump(peerToken));
                        throw new RequestCreationException();
                    }
                } else if (step == 0x02) {
                    byte[] returnCodeMac = GB.hexStringToByteArray(payload.getString("returnCodeMac"));
                    byte[] returnCodeMacCheck = CryptoUtils.calcHmacSha256(psk, new byte[]{4 * 0x00});
                    if (!Arrays.equals(returnCodeMacCheck, returnCodeMac)) {
                        LOG.debug("returnCodeMacCheck: " + GB.hexdump(returnCodeMacCheck) + " is different than " + GB.hexdump(returnCodeMac));
                        throw new RequestCreationException();
                    }
                    if (operationCode == 0x02) step +=1;
                } else if (step == 0x03) {
                    if (operationCode == 0x01) {
                        byte[] nonce = payload.getString("nonce").getBytes(StandardCharsets.UTF_8);
                        byte[] encAuthToken = payload.getString("encAuthToken").getBytes(StandardCharsets.UTF_8);
                        byte[] authToken = CryptoUtils.decryptAES_GCM_NoPad(encAuthToken, psk, nonce, challenge);
                        support.setSecretKey(authToken);
                    }
                }
                // Use the JSONObject to transmit data to the new next request
                json
                    .put("requestId", requestId)
                    .put("operationCode", operationCode)
                    .put("step", step + 1);
                    // .put("seed", GB.hexdump(seed))
                if (authIdSelf != null) json.put("authIdSelf", GB.hexdump(authIdSelf));
                if (authIdPeer != null) json.put("authIdPeer", GB.hexdump(authIdPeer));
                if (randSelf != null) json.put("randSelf", GB.hexdump(randSelf));
                if (randPeer != null) json.put("randPeer", GB.hexdump(randPeer));
                // if (challenge != null) json.put("challenge", GB.hexdump(challenge));
                if (psk != null) json.put("psk", GB.hexdump(psk));
            } catch (Exception e) {
                e.printStackTrace();
            }
            GetHiChainRequest nextRequest = new GetHiChainRequest(
                    this.support,
                    this.builder,
                    json
                );
            this.support.addInProgressRequest(nextRequest);
            this.nextRequest(nextRequest);
            //nextRequest.pastRequest(this.pastRequest);
        }
    }
}
