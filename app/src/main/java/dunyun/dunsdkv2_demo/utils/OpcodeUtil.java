package dunyun.dunsdkv2_demo.utils;

import android.util.Log;

/**
 * Created by GuoWen on 2019/10/17.
 * description
 */

public class OpcodeUtil {
    public static final int getSerialNum = 1;
    public static final int getHardwareVersion = 2;
    public static final int getSoftwareVersion = 3;
    public static final int getProductionDate = 4;
    public static final int updateSoftMessage = 8;
    public static final int updateSoftData = 9;
    public static final int updateStart = 10;
    public static final int updateResult = 11;
    public static final int addKey = 17;
    public static final int openLock = 33;
    public static final int breakConnection = 12;
    public static final int changePasswd = 18;
    public static final int deleteKey = 19;
    public static final int getAllKey = 20;
    public static final int settingAdminID = 21;
    public static final int getAddminID = 22;
    public static final int getPower = 49;
    public static final int getStatus = 50;
    public static final int openEnable = 51;
    public static final int closeEnable = 52;
    public static final int getEnable = 53;
    public static final int settime = 54;
    public static final int getLockTime = 55;
    public static final int getLockRecord = 56;

    public static String getErrorReason(byte error) {
        String reason = "";
        int check = error & 255;
        Log.i("OpcodeUtil", "错误码=" + check);
        switch(check) {
            case 1:
                reason = "格式错误";
                break;
            case 2:
                reason = "数据长度错误";
                break;
            case 3:
                reason = "钥匙ID错误";
                break;
            case 4:
                reason = "钥匙不存在";
                break;
            case 5:
                reason = "解密数据错误";
                break;
            case 6:
                reason = "钥匙密码错误";
                break;
            case 7:
                reason = "此为重发数据，无效";
                break;
            case 8:
                reason = "时间误差不在允许的范围以内";
                break;
            case 9:
                reason = "不在授权的时间范围以内";
                break;
            case 10:
                reason = "授权的次数已经用尽";
                break;
            case 11:
                reason = "应用层数据长度未达到最小数据长度";
                break;
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            default:
                reason = "未知错误";
                break;
            case 17:
                reason = "未收到程序信息";
                break;
            case 18:
                reason = "程序大小错误";
                break;
            case 19:
                reason = "偏移地址错误";
                break;
            case 20:
                reason = "程序错误";
                break;
            case 21:
                reason = "程序校验错误";
                break;
            case 22:
                reason = "无程序更新";
                break;
            case 23:
                reason = "程序更新中";
                break;
            case 32:
                reason = "命令错误";
                break;
            case 33:
                reason = "需要加密";
                break;
            case 34:
                reason = "需要加密或者设置状态";
                break;
            case 35:
                reason = "需要设置状态";
                break;
            case 36:
                reason = "需要管理员权限";
                break;
            case 37:
                reason = "初始密码类型错误";
                break;
            case 38:
                reason = "密码格式错误";
                break;
            case 39:
                reason = "钥匙已经满了";
                break;
            case 40:
                reason = "数据错误";
                break;
            case 41:
                reason = "剩余数据长度不够";
                break;
            case 42:
                reason = "禁止删除管理员";
                break;
            case 43:
                reason = "管理员ID错误";
                break;
            case 44:
                reason = "无管理员";
        }

        return reason;
    }

    public static String getRecordOperation(int operation) {
        String reason = "";
        Log.i("OpcodeUtil", "getRecordOperation=" + operation);
        switch(operation) {
            case 0:
                reason = "管理员蓝牙开锁";
                break;
            case 1:
                reason = "普通用户蓝牙开锁";
                break;
            case 2:
                reason = "授权用户蓝牙开锁";
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            default:
                reason = "未更新解析字段";
                break;
            case 16:
                reason = "管理员密码开锁";
                break;
            case 17:
                reason = "普通用户密码开锁";
                break;
            case 32:
                reason = "未知类型开锁（天地锁、斜舌）";
                break;
            case 33:
                reason = "上锁（天地锁上锁）";
                break;
            case 34:
                reason = "斜舌动作（关门、未反锁状态下内把 手开锁，未反锁状态钥匙开锁）";
                break;
            case 48:
                reason = "恢复出厂设置";
                break;
            case 49:
                reason = "开启使能";
                break;
            case 50:
                reason = "禁止使能";
                break;
            case 51:
                reason = "设置时间";
                break;
            case 52:
                reason = "固件刷新";
                break;
            case 53:
                reason = "进入设置状态";
                break;
            case 54:
                reason = "退出设置状态";
                break;
            case 64:
                reason = "添加钥匙（第一把钥匙）";
                break;
            case 65:
                reason = "添加钥匙（非第一把钥匙）";
                break;
            case 66:
                reason = "删除钥匙";
                break;
            case 67:
                reason = "修改密码";
                break;
            case 68:
                reason = "设置管理员ID";
        }

        return reason;
    }

}
