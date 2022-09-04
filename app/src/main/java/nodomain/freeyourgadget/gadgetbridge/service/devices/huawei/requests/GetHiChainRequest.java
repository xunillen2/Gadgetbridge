package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

 import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

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
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig.HiCHain;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.DeviceConfig.HiChainStep;

public class GetHiChainRequest extends Request {
    private static final Logger LOG = LoggerFactory.getLogger(GetHiChainRequest.class);
    private int step;
    private byte[] seed = null;
    private byte[] isoSalt = null;
    private byte[] requestId = null; //new byte[8];
    private JSONObject json = null;


    public GetHiChainRequest(HuaweiSupport support) {
        super(support);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.HiCHain.id;
        this.step = HiChainStep.begin;
    }

    public GetHiChainRequest(HuaweiSupport support, TransactionBuilder builder, byte[] requestId, int step, byte[] seed, byte[] isoSalt, JSONObject json) {
        super(support, builder);
        this.serviceId = DeviceConfig.id;
        this.commandId = DeviceConfig.HiCHain.id;
        this.requestId = requestId;
        this.step = step;
        this.seed = seed;
        this.isoSalt = isoSalt;
        this.json = json;
    }

    @Override
    protected byte[] createRequest() throws RequestCreationException {
        if (requestId == null) {
            requestId = new byte[8];
            new Random().nextBytes(requestId);
        }
        HiCHain.Request req = new HiCHain.Request(requestId, support.getAndroidId());
        try {
            if (step == HiChainStep.begin) {
                seed = new byte[32];
                new Random().nextBytes(seed);
                isoSalt = new byte[16];
                new Random().nextBytes(isoSalt);
                byte[] serviceType = GB.hexStringToByteArray(HuaweiConstants.SERVICE_TYPE);
                HiCHain.Request.Start start = req.new Start(support.secretsProvider, isoSalt, seed, serviceType );
                return start.serialize();
            } else if (step == HiChainStep.inter) {
                // GeneratePSK - needed ?
                // byte[] serviceType = GB.hexStringToByteArray(HuaweiConstants.SERVICE_TYPE);
                // byte[] pkgName = HuaweiConstants.PKG_NAME.getBytes(StandardCharsets.UTF_8);
                // byte[] serviceId = ByteBuffer
                //     .allocate(pkgName.length + serviceType.length)
                //     .put(pkgName)
                //     .put(serviceType)
                //     .array();
                // serviceId = CryptoUtils.digest(serviceId);
                // byte[] authId = GB.hexStringToByteArray(json.getJSONObject("payload").getString("peerAuthId")); //or authId == selfAuthId
                // byte[] keyType = HuaweiConstants.KEY_TYPE;
                // byte[] keyAlias = ByteBuffer
                //     .allocate(serviceId.length + keyType.length + authId.length)
                //     .put(serviceId)
                //     .put(keyType)
                //     .put(authId)
                //     .array();
                // keyAlias = CryptoUtils.digest(keyAlias);
                byte[] authIdSelf = support.getAndroidId().getBytes(StandardCharsets.UTF_8);
                JSONObject payload = json.getJSONObject("payload");
                byte[] authIdPeer = GB.hexStringToByteArray(payload.getString("peerAuthId")); //or authId == selfAuthId
                byte[] randPeer = GB.hexStringToByteArray(payload.getString("isoSalt"));
                byte[] message = ByteBuffer
                    .allocate(randPeer.length + isoSalt.length + authIdSelf.length + authIdPeer.length)
                    .put(isoSalt)
                    .put(randPeer)
                    .put(authIdPeer)
                    .put(authIdSelf)
                    .array();
                byte[] token = CryptoUtils.calcHmacSha256(support.getSecretKey(), message);
                HiCHain.Request.Inter inter = req.new Inter(support.secretsProvider, token);
                return inter.serialize();
            } else if (step == HiChainStep.end) {
                byte[] nonce = new byte[0];
                byte[] encResult = new byte[0];
                HiCHain.Request.End end = req.new End(support.secretsProvider, nonce, encResult);
                return end.serialize();
            }
        } catch (HuaweiPacket.CryptoException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new RequestCreationException();
        }
        return null;
    }

    @Override
    protected void processResponse() throws GBException {
        if (!(receivedPacket instanceof DeviceConfig.HiCHain.Response)) {
            // TODO: exception
            return;
        }

        try {
            json = new JSONObject(new String(((DeviceConfig.HiCHain.Response) receivedPacket).json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        GetHiChainRequest nextRequest = new GetHiChainRequest(this.support, this.builder, requestId, step + 1, seed, isoSalt, json);
        this.support.addInProgressRequest(nextRequest);
        this.nextRequest(nextRequest);
    }
}
