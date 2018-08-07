package dhxz.session.transport;

import dhxz.session.core.PacketContext;
import dhxz.session.core.Session;
import dhxz.session.spi.PacketProcessor;
import poggyio.dataschemas.Packet;
import poggyio.lang.Langs;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class PacketASEProcessor implements PacketProcessor {

    private static final int ENCRYPT_TYPE = 1;

    @Override
    public Packet upstream(PacketContext context, Packet packet) {
        Session.Metadata metadata = context.session().metadata();
        if (metadata.encryptType() != ENCRYPT_TYPE) {
            return packet;
        }
        try {
            String secret = metadata.clientSecret();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKSC5Padding");
            cipher.init(Cipher.DECRYPT_MODE,initAESKey(secret),initAESIv(secret));
            byte[] body = cipher.doFinal(packet.body());
            return packet.body(body).bodySize(body.length);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidAlgorithmParameterException
                | InvalidKeyException e) {
            throw Langs.toUncheck(e);
        }
    }

    private IvParameterSpec initAESIv(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(secret.getBytes());
            return new IvParameterSpec(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw Langs.toUncheck(e);
        }
    }

    private Key initAESKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(secret.getBytes());
            return new SecretKeySpec(digest.digest(),"AES");
        } catch (NoSuchAlgorithmException e) {
            throw Langs.toUncheck(e);
        }
    }

    @Override
    public Packet downstream(PacketContext context, Packet packet) {
        try {
            Session.Metadata metadata = context.session().metadata();
            if (metadata.encryptType() != ENCRYPT_TYPE) {
                return packet;
            }
            String secret = metadata.clientSecret();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,initAESKey(secret),initAESIv(secret));
            byte[] body = cipher.doFinal(packet.body());
            return packet.body(body).bodySize(body.length);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            throw Langs.toUncheck(e);
        }
    }
}
