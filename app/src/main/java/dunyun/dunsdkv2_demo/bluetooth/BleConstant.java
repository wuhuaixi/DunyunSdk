package dunyun.dunsdkv2_demo.bluetooth;

/**
 * Created by chenzp on 2016/4/29.
 */
public class BleConstant {
    /**查询锁内用户返回0-5用户*/
    public static final int QUERY_USER_1_SUCCESS = 0x00;
    /**添加用户成功*/
    public static final int ADD_USER_SUCCESS = 0x01;
    /**更新时间成功*/
    public static final int UPDATE_TIME_SUCCESS = 0x03;
    /**删除用户请求成功*/
    public static final int DEL_REQ_SUCCESS = 0x04;
    /**删除用户*/
    public static final int DEL_SUCCESS = 0x05;
    /**开锁成功*/
    public static final int OPEN_LOCK_SUCCESS = 0x06;
    /**一次握手成功*/
    public static final int AUTH_1_SUCCESS = 0x07;
    /**二次握手成功*/
    public static final int AUTH_2_SUCCESS = 0x08;
    /**查询锁内用户返回6-12用户*/
    public static final int QUERY_USER_2_SUCCESS = 0x09;
    /**版本更新成功*/
    public static final int UPDATE_VERSION_SUCCESS = 0x0C;
    /**获取版本*/
    public static final int READ_VERSION_SUCCESS = 0x0D;
    /**关闭设置状态*/
    public static final int STATUS_ABLE_SUCCESS = 0x0E;
    /**开启设置状态*/
    public static final int STATUS_ENABLE_SUCCESS = 0x0F;
    /**远程使能或者键盘密码开关*/
    public static final int SETTING_SUCCESS = 0x10;
    /**读取状态*/
    public static final int STATUS_SUCCESS = 0x11;
    /**读取门锁记录成功*/
    public static final int READ_RECORD_SUCCESS = 0x12;
    /**握手3成功*/
    public static final int AUTH_3_SUCCESS = 0x13;
    /**读取系统时间成功*/
    public static final int READ_TIME_SUCCESS = 0x14;
    /**读取系统时间成功*/
    public static final int DISCONNECT_SUCCESS = 0x15;

    public static final String BLUETOOTH_NOT_OPEN = "蓝牙未开启";
    public static final String NOT_CONNECTED = "设备未连接";
    public static final String TIMEOUT = "超时";
}
