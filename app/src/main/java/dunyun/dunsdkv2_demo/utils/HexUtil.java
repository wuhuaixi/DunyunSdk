package dunyun.dunsdkv2_demo.utils;


import java.math.BigDecimal;

public class HexUtil {
    public HexUtil() {
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte)(_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte)(_b0 ^ _b1);
        return ret;
    }

    public static byte[] HexString2Bytes(String src) {
        src.replace(" ", "");
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();

        for(int i = 0; i < tmp.length / 2; ++i) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }

        return ret;
    }

    public static int centigrade2Fahrenheit(double degree, int scale) {
        double d = 32.0D + degree * 1.8D;
        double temp = (new BigDecimal(d)).setScale(scale, 4).doubleValue();
        return (int)temp;
    }

    public static int fahrenheit2Centigrade(double degree, int scale) {
        double d = (degree - 32.0D) / 1.8D;
        double temp = (new BigDecimal(d)).setScale(scale, 4).doubleValue();
        return (int)temp;
    }

    public static String binaryString2hexString(String bString) {
        if(bString != null && !bString.equals("") && bString.length() % 8 == 0) {
            StringBuffer tmp = new StringBuffer();
            for(int i = 0; i < bString.length(); i += 4) {
                int iTmp = 0;

                for(int j = 0; j < 4; ++j) {
                    iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << 4 - j - 1;
                }

                tmp.append(Integer.toHexString(iTmp));
            }

            return tmp.toString();
        } else {
            return null;
        }
    }

    public static int StringToInt(String str) {
        try {
            int strInt = Integer.parseInt(str);
            int result = Integer.valueOf(Integer.toHexString(strInt), 16).intValue();
            return result;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0;
        }
    }

    public static byte[] stringToByte(String s) {
        byte[] b = s.getBytes();
        int[] in = new int[b.length];

        int j;
        for(j = 0; j < in.length; ++j) {
            in[j] = b[j] & 255;
        }

        for(j = 0; j < in.length; ++j) {
            System.out.println(Integer.toString(in[j], 16));
        }

        return b;
    }

    public static String byteToString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < byteArray.length; ++i) {
            String strTemp = Integer.toHexString(255 & byteArray[i]);
            if(strTemp.length() < 2) {
                strTemp = "0" + strTemp;
            }

            sb.append(strTemp.toUpperCase() + " ");
        }

        return sb.toString();
    }

    public static String byteToStringclean(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < byteArray.length; ++i) {
            String strTemp = Integer.toHexString(255 & byteArray[i]);
            if(strTemp.length() < 2) {
                strTemp = "0" + strTemp;
            }

            sb.append(strTemp.toUpperCase());
        }

        return sb.toString();
    }

    public static byte[] intToBytes(int number) {
        byte[] a = new byte[]{(byte)((-16777216 & number) >> 24), (byte)((16711680 & number) >> 16), (byte)(('\uff00' & number) >> 8), (byte)(255 & number)};
        return a;
    }

    public static long bytesToInt(byte[] bytes) {
        StringBuffer sb = new StringBuffer();

        String srt16;
        for(int i = 0; i < bytes.length; ++i) {
            srt16 = Integer.toHexString(255 & bytes[i]);
            if(srt16.length() < 2) {
                srt16 = "0" + srt16;
            }

            sb.append(srt16.toUpperCase());
        }

        String get16str = sb.toString();
        srt16 = "0123456789ABCDEF";
        char[] hexstr = get16str.toCharArray();
        long results = 0L;

        for(int i = 0; i < get16str.length(); ++i) {
            int digit = srt16.indexOf(hexstr[i]);
            results = results * 16L + (long)digit;
        }

        return results;
    }

    public static String bytesToAny(byte[] bytes, int any) {
        StringBuffer sb = new StringBuffer();

        String srt16;
        for(int i = 0; i < bytes.length; ++i) {
            srt16 = Integer.toHexString(255 & bytes[i]);
            if(srt16.length() < 2) {
                srt16 = "0" + srt16;
            }

            sb.append(srt16.toUpperCase());
        }

        String get16str = sb.toString();
        srt16 = "0123456789ABCDEF";
        char[] hexstr = get16str.toCharArray();
        long results = 0L;

        for(int i = 0; i < get16str.length(); ++i) {
            int digit = srt16.indexOf(hexstr[i]);
            results = results * 16L + (long)digit;
        }

        long Quotient = results;
        String resultesAny;
        int Remainder;
        for(resultesAny = ""; Quotient >= (long)any; resultesAny = Remainder + resultesAny) {
            Remainder = (int)(Quotient % (long)any);
            Quotient /= (long)any;
        }

        resultesAny = Quotient + resultesAny;
        return resultesAny;
    }

    public static String[] byteTo01(byte mbyte) {
        String[] in = new String[8];
        String parameterString = Integer.toBinaryString(mbyte & 255);

        for(int total = 8 - parameterString.length(); total > 0; --total) {
            parameterString = "0" + parameterString;
        }

        System.out.println("数据类型转换：" + parameterString);
        char[] parameter = parameterString.toCharArray();

        for(int i = 0; i < parameter.length; ++i) {
            in[i] = String.valueOf(parameter[i]);
        }

        return in;
    }

    public static byte[] MergeBytes(byte[] a, byte[] b) {
        if(b == null) {
            return a;
        } else {
            byte[] ab = new byte[a.length + b.length];
            System.arraycopy(a, 0, ab, 0, a.length);
            System.arraycopy(b, 0, ab, a.length, b.length);
            return ab;
        }
    }

    public static byte[] InterceptBytes(byte[] a, int begin, int end) {
        byte[] data = new byte[end - begin];
        System.arraycopy(a, begin, data, 0, data.length);
        return data;
    }

    public static void main(String[] args) {
        System.out.println(fahrenheit2Centigrade(129.0D, 0));
    }
}
