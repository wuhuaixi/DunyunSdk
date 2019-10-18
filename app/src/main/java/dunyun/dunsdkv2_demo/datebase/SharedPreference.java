package dunyun.dunsdkv2_demo.datebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;


import org.json.JSONException;
import org.json.JSONObject;

import dunyun.dunsdkv2_demo.beans.LockUser;
import dunyun.dunsdkv2_demo.utils.TimeStampUtil;

/**
 * Created by GuoWen on 2019/10/18.
 * description
 */

public class SharedPreference {

    public void saveTimeCheck(Context context, String checkTime) {
        long mobileTime = TimeStampUtil.getCurrentTimeStamp();
        long serverTime = TimeStampUtil.getDateToint(checkTime);
        SharedPreferences pref = context.getSharedPreferences("TimeCheckMessage", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("checkTime", String.valueOf(serverTime - mobileTime));
        editor.commit();
    }

    public String getTimeCheck(Context context) {
        String getCheckTime = "0";
        SharedPreferences pref = context.getSharedPreferences("TimeCheckMessage", 0);
        getCheckTime = pref.getString("checkTime", "0");
        return getCheckTime;
    }

    public void saveKeyMessage(Context context, LockUser lockUser) {
        SharedPreferences pref = context.getSharedPreferences("savedKeyMessage", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("index", lockUser.getUserIndex() + "");
        editor.putString("id", lockUser.getUserId());
        editor.putString("lock_pwd", lockUser.getOpenLockPwd());
        editor.putString("mac", lockUser.getbleMac());
        editor.putString("pwd_key", lockUser.getOpenPwdKey());
        editor.commit();
    }

    public LockUser getKeyMessage(Context context) {
        SharedPreferences pref = context.getSharedPreferences("savedKeyMessage", 0);
        String index = pref.getString("index", "0");
        String id = pref.getString("id", "018376686270");
        String lock_pwd = pref.getString("lock_pwd", "123456");
        String mac = pref.getString("mac", "04:A3:16:65:4C:52");
        String pwd_key = pref.getString("pwd_key", "00000000000000000000000000");
        LockUser lockUser = new LockUser();
        lockUser.setUserIndex(Integer.parseInt(index));
        lockUser.setUserId(id);
        lockUser.setOpenLockPwd(lock_pwd);
        lockUser.setbleMac(mac);
        lockUser.setOpenPwdKey(pwd_key);
        return lockUser;
    }

    public void saveUserMessage(Context context, JSONObject jsonObject) {
        String moblie = "9999999999";
        String companyId = "999999999999";
        String phoneModel = "dunyun";
        String systemV = "android 1.1";
        String sdkV = "2.0";
        String lockLongitude = "0.0";
        String lockLatitude = "0.0";
        SharedPreferences pref = context.getSharedPreferences("saveUserMessage", 0);
        SharedPreferences.Editor editor = pref.edit();

        try {
            moblie = jsonObject.get("mobile").toString();
            companyId = jsonObject.get("companyId").toString();
            systemV = "android:" + Build.VERSION.RELEASE + "/SDK:" + Build.VERSION.SDK_INT;
            phoneModel = Build.BRAND + "/" + Build.MODEL;
            sdkV = "2.0";
            lockLongitude = "0.0";
            lockLatitude = "0.0";
        } catch (JSONException var13) {
            var13.printStackTrace();
        }

        editor.putString("moblie", moblie);
        editor.putString("companyId", companyId);
        editor.putString("phoneModel", phoneModel);
        editor.putString("systemV", systemV);
        editor.putString("sdkV", sdkV);
        editor.putString("lockLongitude", lockLongitude);
        editor.putString("lockLatitude", lockLatitude);
        editor.putString("useTimes", "10");
        editor.commit();
    }

    public JSONObject getUserMessage(Context context, Boolean isLogin) {
        SharedPreferences pref = context.getSharedPreferences("saveUserMessage", 0);
        JSONObject getUserJson = new JSONObject();

        try {
            getUserJson.put("moblie", pref.getString("moblie", "9999999999"));
            getUserJson.put("companyId", pref.getString("companyId", "9999999999"));
            getUserJson.put("phoneModel", pref.getString("phoneModel", "dunyun"));
            getUserJson.put("systemV", pref.getString("systemV", "android 1.1"));
            getUserJson.put("sdkV", pref.getString("sdkV", "2.0"));
            getUserJson.put("lockLongitude", pref.getString("lockLongitude", "0.0"));
            getUserJson.put("lockLatitude", pref.getString("lockLatitude", "0.0"));
            getUserJson.put("useTimes", pref.getString("useTimes", "10"));
            SharedPreferences.Editor editor = pref.edit();
            int times = Integer.parseInt(pref.getString("useTimes", "0"));
            if(times > 0) {
                --times;
                editor.putString("useTimes", String.valueOf(times));
                editor.commit();
            }
        } catch (JSONException var7) {
            var7.printStackTrace();
        }

        return getUserJson;
    }

}
