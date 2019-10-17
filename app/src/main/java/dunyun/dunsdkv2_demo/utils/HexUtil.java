package dunyun.dunsdkv2_demo.utils;


import java.math.BigDecimal;

public class HexUtil {
    /**
     * "EF"--> 0xEF
     *
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    /**
     * "2B44EFD9"> byte[]{0x2B, 0x44, 0xEF,
     * 0xD9}
     *
     * @param src String
     * @return byte[]
     */
    public static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < tmp.length / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /**
     * <p>
     * </p>
     *
     * @param degree
     * @param scale
     * @return
     */
    public static int centigrade2Fahrenheit(double degree, int scale) {
        double d = 32 + degree * 1.8;
        double temp = new BigDecimal(d).setScale(scale, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return (int) temp;
    }

    /**
     * <p>
     * </p>
     *
     * @param degree
     * @param scale
     * @return
     */
    public static int fahrenheit2Centigrade(double degree, int scale) {
        double d = (degree - 32) / 1.8;
        double temp = new BigDecimal(d).setScale(scale, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return (int) temp;
    }

    public static String binaryString2hexString(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }

    public static int StringToInt(String str) {
        try {
            int result = 0x00;
            int strInt = Integer.parseInt(str);

            result = Integer.valueOf(Integer.toHexString(strInt), 16);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0x00;
            // TODO: handle exception
        }
    }

    public static byte[] stringToByte(String s) {
        byte[] b = s.getBytes();
        int[] in = new int[b.length];
        for (int i = 0; i < in.length; i++) {
            in[i] = b[i] & 0xff;
        }
        for (int j = 0; j < in.length; j++) {
            System.out.println(Integer.toString(in[j], 0x10));
        }
        return b;
    }

    public static String byteToString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            String strTemp = Integer.toHexString(0xFF & byteArray[i]);
            if (strTemp.length() < 2) {
                strTemp = "0" + strTemp;
            }
            sb.append(strTemp.toUpperCase() + " ");
        }
        return sb.toString();
    }

    public static byte[] intToBytes(int number){
        byte[] a = new byte[4];
        a[3] = (byte) (0xff & number);
        a[2] = (byte) ((0xff00 & number) >> 8);
        a[1] = (byte) ((0xff0000 & number) >> 16);
        a[0] = (byte) ((0xff000000 & number) >> 24);
        return a;
    }

    public static void main(String[] args) {
        System.out.println(com.psoft.bluetooth.utils.HexUtil.fahrenheit2Centigrade(129, 0));
    }
}
