package dunyun.dunsdkv2_demo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by GuoWen on 2019/10/18.
 * description
 */

public class TimeStampUtil {

    public static String dateCheck(String date) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(date);
        String newdate = m.replaceAll("").trim();
        return newdate;
    }

    public static byte[] getCurrentTimeBytes() {
        long time = System.currentTimeMillis() / 1000L + 28800L;
        byte[] currTime = com.psoft.bluetooth.utils.HexUtil.intToBytes((int)time);
        return currTime;
    }

    public static long getCurrentTimeStamp() {
        long time = System.currentTimeMillis() / 1000L + 28800L;
        return time;
    }

    public static long getTimeStamp(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date Date = new Date();

        try {
            Date = sdf.parse(dateCheck(date));
        } catch (ParseException var5) {
            var5.printStackTrace();
        }

        long time = Date.getTime() / 1000L + 28800L;
        return time;
    }

    public static byte[] getTimeStrToByte(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date Date = new Date();

        try {
            Date = sdf.parse(dateCheck(date));
        } catch (ParseException var6) {
            var6.printStackTrace();
        }

        long time = Date.getTime() / 1000L + 28800L;
        byte[] strTime = com.psoft.bluetooth.utils.HexUtil.intToBytes((int)time);
        return strTime;
    }

    public static String getCurTimeStr() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(Long.valueOf(time));
    }

    public static String getDate(byte[] timeByte) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long d = com.psoft.bluetooth.utils.HexUtil.bytesToInt(timeByte);
        double ddddd = (double)((d - 28800L) * 1000L);
        String x = sdf.format(Double.valueOf(ddddd));
        return x;
    }

    public static long getDateToint(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date Date = new Date();

        try {
            Date = sdf.parse(dateCheck(date));
        } catch (ParseException var5) {
            var5.printStackTrace();
        }

        long time = Date.getTime() / 1000L + 28800L;
        return time;
    }

    public static String getIntToDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        double ddddd = (double)((date - 28800L) * 1000L);
        String x = sdf.format(Double.valueOf(ddddd));
        return x;
    }

}
