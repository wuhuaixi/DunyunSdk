package dunyun.dunsdkv2_demo.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by GuoWen on 2019/10/18.
 * description
 */

public class RandomUtil {

    public static byte[] getRandKey() {
        KeyGenerator kg = null;
        byte[] randKeyByte = new byte[16];

        try {
            kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            SecretKey sk = kg.generateKey();
            randKeyByte = sk.getEncoded();
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();

            for(int i = 0; i < 16; ++i) {
                randKeyByte[i] = (byte)((int)(Math.random() * 10.0D));
            }
        }

        return randKeyByte;
    }

    public static String getRandKeyString() {
        return HexUtil.byteToStringclean(getRandKey());
    }

    public static String getRandPWD() {
        String randPWDstirng = "";

        for(int i = 0; i < 6; ++i) {
            randPWDstirng = randPWDstirng + String.valueOf((int)(Math.random() * 10.0D));
        }

        return randPWDstirng;
    }

}
