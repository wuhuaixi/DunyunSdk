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
    // /** 算法/模式/填充 **/
    private static final String CipherMode = "AES/CBC/PKCS5Padding";
    private static final byte[] iv = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30};

    // /** 创建密钥 **/
    private static SecretKeySpec createKey(byte[] data) {
        return new SecretKeySpec(data, "AES");
    }

    private static IvParameterSpec createIV(byte[] data) {
        return new IvParameterSpec(data);
    }

    // /** 加密字节数据 **/
    private static byte[] localEncrypt(byte[] content, byte[] password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key, createIV(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // /** 加密(结果为16进制字符串) **/
    public static byte[] encrypt(byte[] content, byte[] password) {
        byte[] data = localEncrypt(content, password);
        return data;
    }

    // /** 解密字节数组 **/
    private static byte[] localDecrypt(byte[] content, byte[] password) {
        try {
            SecretKeySpec key = createKey(password);
            Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, key, createIV(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // /** 解密 **/
    public static byte[] decrypt(byte[] content, byte[] password) {
        byte[] data = localDecrypt(content, password);
        if (data == null)
            return null;
        return data;
    }
}
