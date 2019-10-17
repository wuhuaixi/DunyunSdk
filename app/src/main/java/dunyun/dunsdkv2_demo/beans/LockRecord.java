package dunyun.dunsdkv2_demo.beans;

import java.io.Serializable;

/**
 * <DL>
 * <DD>锁记录</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/21
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public class LockRecord implements Serializable,Comparable<com.psoft.bluetooth.beans.LockRecord>{
    /**用户在锁内位置*/
    private int userIndex;
    /**是否为开门记录*/
    private boolean isOpenRecord;
    /**时间*/
    private String time;

    public int getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(int userIndex) {
        this.userIndex = userIndex;
    }

    public boolean isOpenRecord() {
        return isOpenRecord;
    }

    public void setIsOpenRecord(boolean isOpenRecord) {
        this.isOpenRecord = isOpenRecord;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "LockRecord{" +
                "userIndex=" + userIndex +
                ", isOpenRecord=" + isOpenRecord +
                ", time='" + time + '\'' +
                '}';
    }

    @Override
    public int compareTo(com.psoft.bluetooth.beans.LockRecord o) {
        if(o!=null){
            if(this.getTime().compareTo(o.getTime()) == 1 ){
                return 1;
            }else if(this.getTime().equals(o.getTime())){
                return 0;
            }
        }
        return -1;
    }
}
