package dunyun.dunsdkv2_demo.utils;

/**
 * @author chenzp
 * @date 2016/7/19
 * @Copyright:重庆平软科技有限公司
 */

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密解密算法
 *
 * @author long
 */
public class AesEncryptionUtil {
    private static final String CipherMode = "AES/CBC/PKCS5Padding";
    private static final byte[] iv = new byte[]{48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48};
    private static SecretKeySpec createKey(byte[] data) {
        return new SecretKeySpec(data, "AES");
    }

    private static IvParameterSpec createIV(byte[] data) {
        return new IvParameterSpec(data);
    }

    private static byte[] localEncrypt(byte[] content, byte[] password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
            cipher.init(1, key, createIV(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public static byte[] encrypt(byte[] content, byte[] password) {
        byte[] data = localEncrypt(content, password);
        return data;
    }

    private static byte[] localDecrypt(byte[] content, byte[] password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, key, createIV(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public static byte[] decrypt(byte[] content, byte[] password) {
        byte[] data = localDecrypt(content, password);
        return data == null?null:data;
    }
}
