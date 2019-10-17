package dunyun.dunsdkv2_demo.bluetooth;


import com.psoft.bluetooth.utils.TimeStampUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dunyun.dunsdkv2_demo.beans.LockUser;
import dunyun.dunsdkv2_demo.utils.AesEncryptionUtil;
import dunyun.dunsdkv2_demo.utils.CrcUtil;
import dunyun.dunsdkv2_demo.utils.HexUtil;

import static com.psoft.bluetooth.bluetooth.BluetoothCode.timeCheck;

/**
 * <DL>
 * <DD>类、接口说明.</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/25
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public class BluetoothCode {
    /**
     * 添加钥匙
     */

    private boolean addKey = false;
    private static Map lastSendmap = new HashMap();

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
    public static byte[] getLockTime(LockUser lockUser) {
        byte[] openCodeBytes = new byte[]{2, 55};
        return noEncrypted(openCodeBytes);
    }
    public static byte[] MergePackets(byte[] operationBytes, LockUser mergeUser) {
        LockUser getUser = new LockUser();
        getUser.setUserIndex(mergeUser.getUserIndex());
        getUser.setOpenLockPwd(mergeUser.getOpenLockPwd());
        getUser.setOpenPwdKey(mergeUser.getOpenPwdKey());
        getUser.setUserId(mergeUser.getUserId());
        byte[] addPassword = new byte[]{18, 52, 86};
        byte[] password = HexUtil.HexString2Bytes(getUser.getOpenLockPwd());
        int time = (int) TimeStampUtil.getCurrentTimeStamp() + Integer.parseInt(timeCheck);
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
//    public static native byte[] auth1(int index, byte[] user, byte[] password);
//    /***
//     * 握手1
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] AUTH_1(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = auth1(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//
//    public static native byte[] auth2(byte[] receiveData, byte[] password, byte key1, byte key2);
//    /***
//     * 握手2
//     *
//     * @param receiveData
//     * @param lockUser
//     * @return
//     */
//    public static byte[] AUTH_2(byte[] receiveData, LockUser lockUser) {
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//        int[] keys = CrcUtil.key(0xFF & receiveData[11], 0xFF & receiveData[12]);
//
//        byte[] bytes = auth2(receiveData, password, (byte) (keys[0] & 0xFF), (byte) (keys[1] & 0xFF));
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] auth3(int index, byte[] randomData);
//
//    /***
//     * 握手3
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] AUTH_3(LockUser lockUser, byte[] randomData) {
//        int index = lockUser.getUserIndex();
//
//        byte[] bytes = auth3(index, randomData);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] QUERYUSER1(int index, byte[] user, byte[] password);
//    /***
//     * 查询0-5的用户
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] QUERY_USER_1(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = QUERYUSER1(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] QUERYUSER2(int index, byte[] user, byte[] password);
//    /***
//     * 查询用户6-12的用户
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] QUERY_USER_2(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = QUERYUSER2(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] OPENLOCK(int index, byte[] user, byte[] password);
//    /***
//     * 开锁
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] OPEN_LOCK(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = OPENLOCK(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] ADDUSER(int index, byte[] user, byte[] password);
//    /***
//     * 添加用户
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] ADD_USER(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = ADDUSER(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] UPDATETIME(int index, byte[] user, byte[] password, byte[] timeBytes);
//    /***
//     * 更新锁系统时间
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] UPDATE_TIME(LockUser lockUser, String time) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//        byte[] timeBytes = HexUtil.HexString2Bytes(time);
//
//        byte[] bytes = UPDATETIME(index, user, password, timeBytes);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] READTIME(int index, byte[] user, byte[] password);
//    /***
//     * 读取系统时间
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] READ_TIME(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = READTIME(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] READVERSION(int index, byte[] user, byte[] password);
//    /***
//     * 读取版本
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] READ_VERSION(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = READVERSION(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//
//    public static native byte[] LOCKENABLED(int index, byte[] user, byte[] password, byte data);
//    /***
//     * 禁止功能
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] LOCK_ENABLED(LockUser lockUser, byte enableData) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = LOCKENABLED(index, user, password, enableData);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//
//    public static native byte[] LOCKABLED(int index, byte[] user, byte[] password, byte data);
//    /***
//     * 开启功能
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] LOCK_ABLED(LockUser lockUser, byte data) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = LOCKABLED(index, user, password, data);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] UPDATEVERSION(int index, byte[] user, byte[] password, byte[] versionLength, byte[] versionCrc);
//    /***
//     * 更新版本
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] UPDATE_VERSION(LockUser lockUser,
//                                        byte[] versionLength, byte[] versionCrc) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = UPDATEVERSION(index, user, password, versionLength, versionCrc);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//
//    }
//
//    public static native byte[] UPDATEVERSIONDATA(int index, byte[] user, byte[] password, byte[] offset, byte[] updateData);
//
//    public static byte[] UPDATE_VERSION_DATA(LockUser lockUser, byte[] offset,
//                                             byte[] updateData) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = UPDATEVERSIONDATA(index, user, password, offset, updateData);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] UPDATEVERSIONUPDATE(int index, byte[] user, byte[] password);
//    /***
//     * 更新版本
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] UPDATE_VERSION_UPDATE(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = UPDATEVERSIONUPDATE(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] UPDATEVERSIONUPDATELAST(int index, byte[] user, byte[] password);
//    /***
//     * 更新版本
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] UPDATE_VERSION_UPDATE_LAST(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = UPDATEVERSIONUPDATELAST(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] READSTATUS(int index, byte[] user, byte[] password);
//    /***
//     * 读取功能
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] READ_STATUS(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = READSTATUS(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//    public static native byte[] READRECORD(int index, byte[] user, byte[] password);
//    /***
//     * 读取功能
//     *
//     * @param lockUser
//     * @return
//     */
//    public static byte[] READ_RECORD(LockUser lockUser) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = READRECORD(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//
//    public static native byte[] DELREQ(int index, byte[] user, byte[] password, byte delIndex);
//    /***
//     * 删除请求
//     *
//     * @param lockUser
//     * @param delIndex
//     * @return
//     */
//    public static byte[] DEL_REQ(LockUser lockUser, int delIndex) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = DELREQ(index, user, password, (byte)delIndex);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//
//    public static native byte[] DISCONNECT(byte[] dyBytes);
//    /***
//     * 断开
//     *
//     * @return
//     */
//    public static byte[] DISCONNECT() {
//        byte[] dyBytes = "DYshut".getBytes();
//
//        byte[] bytes = DISCONNECT(dyBytes);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//   // public static native byte[] DEL(int index, byte[] user, byte[] password);
//    /***
//     * 删除
//     *
//     * @param lockUser
//     * @param delIndex
//     * @return
//     */
//    public static byte[] DEL(LockUser lockUser, int delIndex) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = DEL(index, user, password);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//        return bytes;
//    }
//
//
//   // public static native byte[] SETTING(int index, byte[] user, byte[] password, byte setdata);
//    /***
//     * 键盘、密码开锁使能
//     *
//     * @param lockUser
//     * @param setData
//     * @return
//     */
//    public static byte[] SETTING(LockUser lockUser, int setData) {
//        int index = lockUser.getUserIndex();
//        byte[] user = HexUtil.HexString2Bytes(lockUser.getUserId());
//        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
//
//        byte[] bytes = SETTING(index, user, password, (byte)setData);
//        //crc
//        byte[] crc = CrcUtil.getCrc16(bytes, bytes.length - 2);
//        bytes[bytes.length - 2] = crc[0];
//        bytes[bytes.length - 1] = crc[1];
//
//        return bytes;
//    }

}
