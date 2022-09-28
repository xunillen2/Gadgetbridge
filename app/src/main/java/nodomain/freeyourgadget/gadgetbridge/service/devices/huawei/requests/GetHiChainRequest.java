package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

 import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
    private byte[] randSelf = null;
    private byte[] randPeer = null;
    private long requestId = 0x00;
    private JSONObject json = null;


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
            this.operationCode = (byte)json.getInt("operationCode");
            this.step = (byte)json.getInt("step");
            this.seed = json.getString("seed").getBytes(StandardCharsets.UTF_8);
            this.randSelf = json.getString("randSelf").getBytes(StandardCharsets.UTF_8);
            if (json.has("randPeer")) this.randPeer = json.getString("randPeer").getBytes(StandardCharsets.UTF_8);
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

        LOG.debug("operationCode: " + operationCode + " - step: " + step);
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
                byte[] key = null;
                JSONObject payload = json.getJSONObject("payload");
                byte[] authIdPeer = GB.hexStringToByteArray(payload.getString("peerAuthId")); //or authId == selfAuthId
                // GeneratePsk
                if (operationCode == 0x01) {
                    LOG.debug("pincode returned: " + StringUtils.bytesToHex(pastRequest.getValueReturned()));
                    String pincodeHexStr = StringUtils.bytesToHex(pastRequest.getValueReturned());
                    byte[] pincode = pincodeHexStr.getBytes(StandardCharsets.UTF_8);
                    // System.arraycopy(pastRequest.getValueReturned(), 0, pincode, 0, pincode.length);
                    key = CryptoUtils.digest(pincode);
                } else {
                    byte[] pkgName = HuaweiConstants.PKG_NAME.getBytes(StandardCharsets.UTF_8);
                    byte[] serviceType = GB.hexStringToByteArray(HuaweiConstants.GROUP_ID);
                    byte[] serviceId = ByteBuffer
                        .allocate(pkgName.length + serviceType.length)
                        .put(pkgName)
                        .put(serviceType)
                        .array();
                    serviceId = CryptoUtils.digest(serviceId);
                    byte[] keyType = HuaweiConstants.KEY_TYPE;
                    key = ByteBuffer
                        .allocate(serviceId.length + keyType.length + authIdPeer.length)
                        .put(serviceId)
                        .put(keyType)
                        .put(authIdPeer)
                        .array();
                    key = CryptoUtils.digest(key);
                }
                byte[] psk = CryptoUtils.calcHmacSha256(key, seed);
                support.setSecretKey(psk);
                //Calculate token and check peerToken ??
                byte[] authIdSelf = support.getAndroidId();
                randPeer = GB.hexStringToByteArray(payload.getString("isoSalt"));
                LOG.debug("randPeer: " + GB.hexdump(randPeer) + " and randSelf: " + GB.hexdump(randSelf));
                LOG.debug("authIdPeer: " + GB.hexdump(authIdPeer) + " and authIdSelf: " + GB.hexdump(authIdSelf));
                byte[] message = ByteBuffer
                    .allocate(randPeer.length + randSelf.length + authIdSelf.length + authIdPeer.length)
                    .put(randSelf)
                    .put(randPeer)
                    .put(authIdPeer)
                    .put(authIdSelf)
                    .array();
                byte[] token = CryptoUtils.calcHmacSha256(psk, message);
                HiCHain.Request.StepTwo stepTwo = req.new StepTwo(support.paramsProvider, token);
                return stepTwo.serialize();
            } else if (step == 0x03) { // Only for operationCode == 0x01
                                       // Should check returnCodeMac ? hmac(secretKey, 0)
                byte[] nonce = new byte[0]; //generateRandom
                byte[] salt = ByteBuffer
                    .allocate( randSelf.length + randPeer.length)
                    .put(randSelf)
                    .put(randPeer)
                    .array();
                byte[] sessionKey = CryptoUtils.hkdfSha256(support.getSecretKey(), salt, 32);
                byte[] challenge = new byte[0]; //generateRandom
                byte[] aad = "hichain_iso_exchange".getBytes(StandardCharsets.UTF_8);
                byte[] encData = CryptoUtils.encryptAES_GCM_NoPad(challenge, sessionKey, nonce, aad); //aesGCMNoPadding encrypt(sessionKey as key, challenge to encrypt, nonce as iv)
                HiCHain.Request.StepThree stepThree = req.new StepThree(support.paramsProvider, nonce, encData);
                return stepThree.serialize();
            } else if (step == 0x04) {
                byte[] nonce = new byte[0];
                byte[] encResult = new byte[0];
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

        if (step == 0x02 && operationCode == 0x02) step += 0x01;
        try {
            json = new JSONObject(new String(((DeviceConfig.HiCHain.Response) receivedPacket).json));
            // Use the JSONObject to transmit data
            json
                .put("requestId", requestId)
                .put("operationCode", operationCode)
                .put("step", step + 1)
                .put("seed", GB.hexdump(seed))
                .put("randSelf", GB.hexdump(randSelf));
            if (randPeer != null) json.put("randPeer", GB.hexdump(randPeer));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        GetHiChainRequest nextRequest = new GetHiChainRequest(
                this.support,
                this.builder,
                json
            );
        this.support.addInProgressRequest(nextRequest);
        this.nextRequest(nextRequest);
        nextRequest.pastRequest(this.pastRequest);
    }
}
