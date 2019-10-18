package dunyun.dunsdkv2_demo.bluetooth;


import com.psoft.bluetooth.utils.TimeStampUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dunyun.dunsdkv2_demo.beans.LockUser;
import dunyun.dunsdkv2_demo.utils.AesEncryptionUtil;
import dunyun.dunsdkv2_demo.utils.CrcUtil;
import dunyun.dunsdkv2_demo.utils.HexUtil;
import dunyun.dunsdkv2_demo.utils.LogUtil;


/**
 * <DL>
 * <DD>类、接口说明.</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/25
 * 修改记录:
 * 初始化
 */
public class BluetoothCode {
    private String TAG = "BluetoothCode";
    public static byte[] lastBytes;
    public static byte[] sendedBytes;
    public static String timeCheck = "0";
    private static String lastSendData = "";
    private static Map lastSendmap = new HashMap();

    public static void setCheckTime(String time) {
        timeCheck = time;
    }

    public static byte[] GetSerialNum() {
        byte[] openCodeBytes = new byte[]{2, 1};
        sendedBytes = noEncrypted(openCodeBytes);
        return sendedBytes;
    }

    public static byte[] GetHardwareVersion() {
        byte[] openCodeBytes = new byte[]{2, 2};
        sendedBytes = noEncrypted(openCodeBytes);
        return sendedBytes;
    }

    public static byte[] GetSoftwareVersion() {
        byte[] openCodeBytes = new byte[]{2, 3};
        sendedBytes = noEncrypted(openCodeBytes);
        return sendedBytes;
    }

    public static byte[] GetUpdateResults() {
        byte[] openCodeBytes = new byte[]{2, 11};
        sendedBytes = noEncrypted(openCodeBytes);
        return sendedBytes;
    }

    public static byte[] BreakConnection() {
        byte[] openCodeBytes = new byte[]{2, 12};
        sendedBytes = noEncrypted(openCodeBytes);
        return sendedBytes;
    }

    public static byte[] GetLockTime(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 55};
        return noEncrypted(openCodeBytes);
    }

    public static byte[] getLockTime() {
        byte[] openCodeBytes = new byte[]{6, 0, 2, 55, 0, 0};
        byte[] crc = CrcUtil.getCrc16(openCodeBytes, openCodeBytes.length - 2);
        openCodeBytes[4] = crc[0];
        openCodeBytes[5] = crc[1];
        return openCodeBytes;
    }

    public static byte[] UpdateVersionMessage(LockUser lockUser, byte[] softBytes, String softVersion) {
       return null;
    }

    public static byte[] UpdateVersionData(LockUser lockUser, byte[] softBytes, int currentLength) {
        byte[] begin = new byte[6];
        byte[] openCodeBytes = HexUtil.MergeBytes(begin, softBytes);
        byte[] currentBytelength = HexUtil.intToBytes(currentLength);
        openCodeBytes[0] = (byte)(openCodeBytes.length & 255);
        openCodeBytes[1] = 9;
        openCodeBytes[2] = currentBytelength[0];
        openCodeBytes[3] = currentBytelength[1];
        openCodeBytes[4] = currentBytelength[2];
        openCodeBytes[5] = currentBytelength[3];
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] UpdateVersionStart(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 10};
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] GetProductionDate(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 4};
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] AddKey(LockUser lockUser, byte[] randKey) {
        lockUser.setUserIndex(15);
        LogUtil.i("AddKey", "setOpenPwdKey=" + lockUser.getOpenPwdKey());
        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
        int t = (int)TimeStampUtil.getCurrentTimeStamp() + Integer.parseInt(timeCheck);
        byte[] timeArray = HexUtil.intToBytes(t);
        List<Byte> byteList = new ArrayList();
        byteList.add((byte)0x00);
        byteList.add((byte)0x11);
        byteList.add((byte)0x01);
        byteList.add(Byte.valueOf(user[0]));
        byteList.add(Byte.valueOf(user[1]));
        byteList.add(Byte.valueOf(user[2]));
        byteList.add(Byte.valueOf(user[3]));
        byteList.add(Byte.valueOf(user[4]));
        byteList.add(Byte.valueOf(user[5]));

        for(int i = 0; i < password.length; ++i) {
            byteList.add(Byte.valueOf(password[i]));
        }

        byte[] OpenPwdKeyByets = randKey;

        int i;
        for(i = 0; i < OpenPwdKeyByets.length; ++i) {
            byteList.add(Byte.valueOf(OpenPwdKeyByets[i]));
        }

        for(i = 0; i < timeArray.length; ++i) {
            byteList.add(Byte.valueOf(timeArray[i]));
        }
        String comply="2becde9154824a3aa7c226393c916298";
        byte[] comBytes = HexUtil.HexString2Bytes(comply);
        for(i = 0; i < comBytes.length; i++) {
            byteList.add(Byte.valueOf(comBytes[i]));
        }


        byte[] addBytes = new byte[byteList.size()];

        for(i = 0; i < byteList.size(); ++i) {
            addBytes[i] = ((Byte)byteList.get(i)).byteValue();
        }


        addBytes[0] = (byte)(255 & addBytes.length);
        byte[] getPower = new byte[]{2, 49, 2, 2, 2, 3, 2, 1};
        byte[] allAddBytes = HexUtil.MergeBytes(getPower, addBytes);
        sendedBytes = MergePackets(allAddBytes, lockUser);
        lastSendmap.put("type", "1");
        return sendedBytes;
    }

    public static byte[] OpenLock(LockUser ddd) {
        byte[] openCodeBytes = new byte[]{3, 33, 0};
        byte[] getPower = new byte[]{2, 49};
        byte[] allAddBytes = HexUtil.MergeBytes(getPower, openCodeBytes);
        lastBytes = allAddBytes;
        sendedBytes = MergePackets(allAddBytes, ddd);
        lastSendmap.put("type", "2");
        return sendedBytes;
    }

    public static byte[] OpenLockAuthorization(LockUser lockUser, String AuthorizationId, String startTime, String endTime, String times) {
        byte[] timesBytes = new byte[]{0, 2};
        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
        byte[] userid = HexUtil.HexString2Bytes(lockUser.getUserId());
        byte[] authorizationId = HexUtil.HexString2Bytes(AuthorizationId);
        byte[] strattime = TimeStampUtil.getTimeStrToByte(startTime);
        byte[] endtime = TimeStampUtil.getTimeStrToByte(endTime);
        int time = (int)TimeStampUtil.getCurrentTimeStamp() + Integer.parseInt(timeCheck);
        LogUtil.i("OpenLockAuthorization", "" + TimeStampUtil.getIntToDate((long)time));
        byte[] timeArray = HexUtil.intToBytes(time);
        byte[] openCodeBytes = new byte[23];
        openCodeBytes[0] = password[0];
        openCodeBytes[1] = password[1];
        openCodeBytes[2] = password[2];

        int i;
        for(i = 0; i < 6; ++i) {
            openCodeBytes[3 + i] = authorizationId[i];
        }

        for(i = 0; i < 4; ++i) {
            openCodeBytes[9 + i] = strattime[i];
            openCodeBytes[13 + i] = endtime[i];
            openCodeBytes[19 + i] = timeArray[i];
        }

        openCodeBytes[17] = timesBytes[0];
        openCodeBytes[18] = timesBytes[1];
        byte[] open = new byte[]{3, 33, 0};
        byte[] getPower = new byte[]{2, 49};
        byte[] appliacation = HexUtil.MergeBytes(getPower, open);
        byte[] unEncrpt = HexUtil.MergeBytes(openCodeBytes, appliacation);
        byte[] allData = MergeAuth(unEncrpt, lockUser);
        lastSendmap.put("type", "4");
        return allData;
    }

    public static byte[] MergeAuth(byte[] unEncrpt, LockUser lockUser) {
        byte[] OpenPwdKeyByets = lockUser.getOpenPwdKeyBytes();
        byte[] encryptedBytes = AesEncryptionUtil.encrypt(unEncrpt, OpenPwdKeyByets);
        byte[] MergePacketsBytes = new byte[encryptedBytes.length + 5];
        System.arraycopy(encryptedBytes, 0, MergePacketsBytes, 3, encryptedBytes.length);
        MergePacketsBytes[0] = (byte)(MergePacketsBytes.length & 255);
        MergePacketsBytes[1] = 104;
        MergePacketsBytes[2] = (byte)(lockUser.getUserIndex() & 255);
        byte[] crc = CrcUtil.getCrc16(MergePacketsBytes, MergePacketsBytes.length - 2);
        MergePacketsBytes[MergePacketsBytes.length - 2] = crc[0];
        MergePacketsBytes[MergePacketsBytes.length - 1] = crc[1];
        LogUtil.i("OpenLockAuthorization", HexUtil.byteToString(unEncrpt));
        saveLastData(unEncrpt, lockUser);
        return MergePacketsBytes;
    }

    public static byte[] ChangePasswd(LockUser lockUser, byte[] passwd) {
        byte[] openCodeBytes = new byte[]{5, 18, passwd[0], passwd[1], passwd[2]};
        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] DeleteKey(LockUser lockUser, int index) {
        byte[] openCodeBytes = new byte[]{3, 19, (byte)(255 & index)};
        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] GetAllKey(LockUser lockUser, int begin, int length) {
        byte[] openCodeBytes = new byte[]{4, 20, (byte)(255 & begin), (byte)(255 & length)};
        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] SettingAdminID(LockUser lockUser, int index) {
        byte[] openCodeBytes = new byte[]{3, 21, (byte)(255 & index)};
        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] GetAddminID(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 22};
        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] GetPower(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 49};
        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] GetStatus(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 50};
        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] OpenEnable(LockUser lockUser, String enable) {
        byte[] openCodeBytes = new byte[]{4, 51, 0, 0};
        String[] first = new String[8];
        String[] second = new String[8];

        for(int i = 0; i < 8; ++i) {
            first[i] = enable.substring(i, i + 1);
            second[i] = enable.substring(8 + i, i + 9);
        }

        byte[] enableBytes = new byte[]{-128, 64, 32, 16, 8, 4, 2, 1};

        for(int i = 0; i < enableBytes.length; ++i) {
            if(first[i].equals("1")) {
                openCodeBytes[2] += enableBytes[i];
            }

            if(second[i].equals("1")) {
                openCodeBytes[3] += enableBytes[i];
            }
        }

        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] CloseEnable(LockUser lockUser, String enable) {
        byte[] openCodeBytes = new byte[]{4, 52, 0, 0};
        String[] first = new String[8];
        String[] second = new String[8];

        for(int i = 0; i < 8; ++i) {
            first[i] = enable.substring(i, i + 1);
            second[i] = enable.substring(8 + i, i + 9);
        }

        byte[] enableBytes = new byte[]{-128, 64, 32, 16, 8, 4, 2, 1};

        for(int i = 0; i < enableBytes.length; ++i) {
            if(first[i].equals("1")) {
                openCodeBytes[2] += enableBytes[i];
            }

            if(second[i].equals("1")) {
                openCodeBytes[3] += enableBytes[i];
            }
        }

        lastBytes = openCodeBytes;
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] ReadEnable(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 53, 0};
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] Settime(LockUser lockUser) {
        byte[] lockTime = TimeStampUtil.getTimeStrToByte(lockUser.getCurrentTime());
        byte[] openCodeBytes = new byte[]{6, 54, lockTime[0], lockTime[1], lockTime[2], lockTime[3]};
        byte[] settimeBytes = MergePackets(openCodeBytes, lockUser);
        lastSendmap.put("type", "3");
        return settimeBytes;
    }

    public static byte[] GetLockRecord(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 56};
        sendedBytes = MergePackets(openCodeBytes, lockUser);
        return sendedBytes;
    }

    public static byte[] noEncrypted(byte[] operationBytes) {
        int length = operationBytes.length + 4;
        byte[] noEncryptedBytes = new byte[length];
        noEncryptedBytes[0] = (byte)(noEncryptedBytes.length & 255);
        noEncryptedBytes[1] = 0;

        for(int i = 0; i < operationBytes.length; ++i) {
            noEncryptedBytes[2 + i] = operationBytes[i];
        }

        byte[] crc = CrcUtil.getCrc16(noEncryptedBytes, noEncryptedBytes.length - 2);
        noEncryptedBytes[length - 2] = crc[0];
        noEncryptedBytes[length - 1] = crc[1];
        lastSendmap.put("type", "0");
        return noEncryptedBytes;
    }

    public static byte[] MergePackets(byte[] operationBytes, LockUser mergeUser) {
        LockUser getUser = new LockUser();
        getUser.setUserIndex(mergeUser.getUserIndex());
        getUser.setOpenLockPwd(mergeUser.getOpenLockPwd());
        getUser.setOpenPwdKey(mergeUser.getOpenPwdKey());
        getUser.setUserId(mergeUser.getUserId());
        byte[] addPassword = new byte[]{18, 52, 86};
        byte[] password = HexUtil.HexString2Bytes(getUser.getOpenLockPwd());
        int time = (int)TimeStampUtil.getCurrentTimeStamp() + Integer.parseInt(timeCheck);
        byte[] timeArray = HexUtil.intToBytes(time);
        if(getUser.getUserIndex() == 15) {
            password = addPassword;
            getUser.setOpenLockPwd("000000");
            getUser.setOpenPwdKey("00000000000000000000000000");
            mergeUser.setOpenLockPwd("000000");
            mergeUser.setOpenPwdKey("00000000000000000000000000");
        }

        byte[] commonBytes = new byte[]{password[0], password[1], password[2], timeArray[0], timeArray[1], timeArray[2], timeArray[3]};
        byte[] unencryptedBytes = new byte[operationBytes.length + commonBytes.length];
        System.arraycopy(commonBytes, 0, unencryptedBytes, 0, commonBytes.length);
        System.arraycopy(operationBytes, 0, unencryptedBytes, commonBytes.length, operationBytes.length);
        saveLastData(unencryptedBytes, getUser);
        byte[] allData = Merge(unencryptedBytes, getUser);
        return allData;
    }

    public static byte[] Merge(byte[] unencryptedBytes, LockUser lockUser) {
        byte[] OpenPwdKeyByets = lockUser.getOpenPwdKeyBytes();
        byte[] encryptedBytes = AesEncryptionUtil.encrypt(unencryptedBytes, OpenPwdKeyByets);
        byte[] MergePacketsBytes = new byte[encryptedBytes.length + 4];
        System.arraycopy(encryptedBytes, 0, MergePacketsBytes, 2, encryptedBytes.length);
        MergePacketsBytes[0] = (byte)(MergePacketsBytes.length & 255);
        MergePacketsBytes[1] = (byte)(64 + (byte)(255 & lockUser.getUserIndex()));
        byte[] crc = CrcUtil.getCrc16(MergePacketsBytes, MergePacketsBytes.length - 2);
        MergePacketsBytes[MergePacketsBytes.length - 2] = crc[0];
        MergePacketsBytes[MergePacketsBytes.length - 1] = crc[1];
        return MergePacketsBytes;
    }

    private static void saveLastData(byte[] sendData, LockUser saveUser) {
        lastSendmap.put("application", HexUtil.byteToString(sendData));
        lastSendmap.put("openPwdKey", saveUser.getOpenPwdKey());
        lastSendmap.put("openPwd", saveUser.getOpenLockPwd());
        lastSendmap.put("index", Integer.valueOf(saveUser.getUserIndex()));
        lastSendmap.put("type", "0");
    }

    public static Map getLastData() {
        return lastSendmap;
    }

    public static byte[] sendSettingAgain(byte[] lockTime) {
        int type = Integer.parseInt(lastSendmap.get("type").toString());
        byte[] allData = null;
        byte[] application = HexUtil.HexString2Bytes(lastSendmap.get("application").toString());
        LockUser savedUser = new LockUser();
        savedUser.setUserIndex(Integer.parseInt(lastSendmap.get("index").toString()));
        savedUser.setOpenLockPwd(lastSendmap.get("openPwd").toString());
        savedUser.setOpenPwdKey(lastSendmap.get("openPwdKey").toString());
        switch(type) {
            case 0:
                System.out.println("未有发送过的数据");
                break;
            case 1:
            case 2:
            case 3:
                application[3] = lockTime[0];
                application[4] = lockTime[1];
                application[5] = lockTime[2];
                application[6] = lockTime[3];
                allData = Merge(application, savedUser);
                break;
            case 4:
                application[19] = lockTime[0];
                application[20] = lockTime[1];
                application[21] = lockTime[2];
                application[22] = lockTime[3];
                allData = MergeAuth(application, savedUser);
                break;
            default:
                System.out.println("未识别的type");
        }

        lastSendmap.put("type", "0");
        lastSendmap = new HashMap();
        return allData;
    }
}
