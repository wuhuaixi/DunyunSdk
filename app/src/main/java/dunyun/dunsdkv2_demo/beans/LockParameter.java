package dunyun.dunsdkv2_demo.beans;

/**
 * <DL>
 * <DD>使能设置.</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/22
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public class LockParameter {
    public static final byte OPEN = 1;
    public static final byte CLOSE = 0;
    /**添加钥匙*/
    private byte ADD_KEY = CLOSE;
    /**删除钥匙*/
    private byte DEL_KEY = CLOSE;
    /**清空功能*/
    private byte CLEAR_KEY = CLOSE;
    /**键盘功能*/
    private byte KEYBOARD = CLOSE;
    /**电量*/
    private byte POWER = CLOSE;

    public byte getADD_KEY() {
        return ADD_KEY;
    }

    public void setADD_KEY(byte ADD_KEY) {
        this.ADD_KEY = ADD_KEY;
    }

    public byte getDEL_KEY() {
        return DEL_KEY;
    }

    public void setDEL_KEY(byte DEL_KEY) {
        this.DEL_KEY = DEL_KEY;
    }

    public byte getCLEAR_KEY() {
        return CLEAR_KEY;
    }

    public void setCLEAR_KEY(byte CLEAR_KEY) {
        this.CLEAR_KEY = CLEAR_KEY;
    }

    public byte getKEYBOARD() {
        return KEYBOARD;
    }

    public void setKEYBOARD(byte KEYBOARD) {
        this.KEYBOARD = KEYBOARD;
    }

    public byte getPOWER() {
        return POWER;
    }

    public void setPOWER(byte POWER) {
        this.POWER = POWER;
    }

    public byte toByte(){
        StringBuffer sb = new StringBuffer();
        sb.append(CLOSE);
        sb.append(CLOSE);
        sb.append(CLOSE);
        sb.append(POWER);
        sb.append(KEYBOARD);

        sb.append(CLEAR_KEY);
        sb.append(DEL_KEY);
        sb.append(ADD_KEY);
        return (byte)Integer.parseInt(sb.toString(), 2);
    }

    @Override
    public String toString() {
        return "LockParameter{" +
                "ADD_KEY=" + ADD_KEY +
                ", DEL_KEY=" + DEL_KEY +
                ", CLEAR_KEY=" + CLEAR_KEY +
                ", KEYBOARD=" + KEYBOARD +
                ", POWER=" + POWER +
                '}';
    }
}
