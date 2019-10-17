package dunyun.dunsdkv2_demo.beans;

import com.psoft.bluetooth.utils.TimeStampUtil;

import dunyun.dunsdkv2_demo.utils.HexUtil;

/**
 * <DL>
 * <DD>锁用户信息</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/21
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public class LockUser {
    private int userIndex;
    private String userId;
    private String openLockPwd;
    private String openPwdKey;
    private String addTime;
    private String currentTime;
    private String bleMac;
    private String bleName;

    public LockUser() {
    }

    public int getUserIndex() {
        return this.userIndex;
    }

    public void setUserIndex(int userIndex) {
        this.userIndex = userIndex;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        if(userId.length() == 11) {
            this.userId = "0" + userId;
        } else {
            this.userId = userId;
        }

    }

    public String getOpenLockPwd() {
        return this.openLockPwd;
    }

    public void setOpenLockPwd(String openLockPwd) {
        this.openLockPwd = openLockPwd;
    }

    public String getOpenPwdKey() {
        return this.openPwdKey + this.openLockPwd;
    }

    public byte[] getOpenPwdKeyBytes() {
        byte[] getOpenPwdKeyBytes = HexUtil.HexString2Bytes(this.openPwdKey + this.openLockPwd);
        return getOpenPwdKeyBytes;
    }

    public void setOpenPwdKey(String openPwdKeystr) {
        if(openPwdKeystr.length() > 26) {
            openPwdKeystr = openPwdKeystr.substring(0, 26);
        }

        this.openPwdKey = openPwdKeystr;
    }

    public String getaddTime() {
        return this.addTime;
    }

    public void setaddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getCurrentTime() {
        return this.currentTime;
    }

    public String getbleName() {
        return this.bleName;
    }

    public void setbleName(String bleName) {
        this.bleName = bleName;
    }

    public String getbleMac() {
        return this.bleMac;
    }

    public void setbleMac(String bleMac) {
        this.bleMac = bleMac;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public void setCurrentTime(byte[] currentTime) {
        this.currentTime = TimeStampUtil.getDate(currentTime);
    }

    public String toString() {
        return "{userIndex=" + this.userIndex + '\'' + ", userId='" + this.userId + '\'' + ", openLockPwd='" + this.openLockPwd + '\'' + ", addTime='" + this.addTime + '\'' + ", currentTime='" + this.currentTime + '\'' + '}';
    }
}
