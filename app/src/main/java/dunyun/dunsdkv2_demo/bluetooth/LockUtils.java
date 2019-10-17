package dunyun.dunsdkv2_demo.bluetooth;



import java.util.ArrayList;
import java.util.List;

import dunyun.dunsdkv2_demo.beans.LockInfo;
import dunyun.dunsdkv2_demo.beans.LockParameter;
import dunyun.dunsdkv2_demo.beans.LockRecord;
import dunyun.dunsdkv2_demo.beans.LockUser;

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
public class LockUtils {

    public static List<LockUser> getLockUsers(int number, byte[] lockUserInfo) {
        List<LockUser> lockUsers = new ArrayList<LockUser>();
        try {
            LockUser lockUser = new LockUser();
            for (int j = 0; j <= number; j++) {
                if ((lockUserInfo[15 + j * 9] != (byte) 0xff) && (lockUserInfo[16 + j * 9] != (byte) 0xff)
                        && (lockUserInfo[17 + j * 9] != (byte) 0xff) && (lockUserInfo[18 + j * 9] != (byte) 0xff)) {
                    lockUser = new LockUser();
                    int userNum = 0xFF & lockUserInfo[10 + j * 9];

                    String strTemp1 = Integer.toHexString(0xFF & lockUserInfo[15 + j * 9]);
                    String strTemp2 = Integer.toHexString(0xFF & lockUserInfo[16 + j * 9]);
                    String strTemp3 = Integer.toHexString(0xFF & lockUserInfo[17 + j * 9]);
                    String strTemp4 = Integer.toHexString(0xFF & lockUserInfo[18 + j * 9]);

                    String strTemp5 = Integer.toHexString(0xFF & lockUserInfo[11 + j * 9]);
                    String strTemp6 = Integer.toHexString(0xFF & lockUserInfo[12 + j * 9]);
                    String strTemp7 = Integer.toHexString(0xFF & lockUserInfo[13 + j * 9]);
                    String strTemp8 = Integer.toHexString(0xFF & lockUserInfo[14 + j * 9]);


                    if (strTemp1.length() < 2) {
                        strTemp1 = "0" + strTemp1;
                    }
                    if (strTemp2.length() < 2) {
                        strTemp2 = "0" + strTemp2;
                    }
                    if (strTemp3.length() < 2) {
                        strTemp3 = "0" + strTemp3;
                    }
                    if (strTemp4.length() < 2) {
                        strTemp4 = "0" + strTemp4;
                    }
                    //
                    if (strTemp5.length() < 2) {
                        strTemp5 = "0" + strTemp5;
                    }
                    if (strTemp6.length() < 2) {
                        strTemp6 = "0" + strTemp6;
                    }
                    if (strTemp7.length() < 2) {
                        strTemp7 = "0" + strTemp7;
                    }
                    if (strTemp8.length() < 2) {
                        strTemp8 = "0" + strTemp8;
                    }

                    lockUser.setUserIndex(userNum);
                    StringBuffer sb = new StringBuffer();
                    sb.append(strTemp5);
                    sb.append(strTemp6);
                    sb.append(strTemp7);
                    sb.append(strTemp8);
                    sb.append(strTemp1);
                    sb.append(strTemp2);
                    sb.append(strTemp3);
                    sb.append(strTemp4);

                    lockUser.setUserId(sb.toString());

                    lockUsers.add(lockUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return lockUsers;
    }

    public static String getVersion(byte[] dataBytes) {
        String version = "";
        if (dataBytes.length == 20) {
            version = (dataBytes[15] & 0xFF) + "" + (dataBytes[16] & 0xFF) + "" + (dataBytes[17] & 0xFF);
        }
        return version;
    }

    public static LockInfo getBatteryPower(byte[] data) {
        if (data.length > 5) {
            LockInfo lockInfo = new LockInfo();
            int power = data[data.length - 4] & 0xff;
            int status = data[data.length - 3] & 0xff;
            String bin = "00000000" + Integer.toBinaryString(status);
            String binStr = bin.substring(bin.length() - 8);
            if ((binStr.charAt(5) + "" + binStr.charAt(6)).equals("00")) {
                lockInfo.lockStatus = "0";
            } else {
                lockInfo.lockStatus = "1";
            }
            lockInfo.batteryPower = power + "";
            return lockInfo;
        } else {
            return null;
        }
    }

    public static LockInfo getLockInfo(byte[] data) {
        return new LockInfo();
    }

    public static String byteToString(byte b) {
        String strTemp8 = Integer.toHexString(0xFF & b);
        if (strTemp8.length() < 2) {
            strTemp8 = "0" + strTemp8;
        }
        return strTemp8;
    }

    public static LockUser getLockUser(byte[] data) {
        return new LockUser();
    }

    public static boolean isAddUser(byte[] data) {
        if (data.length == 16 || data.length == 21) {
            if ((data[data.length-3] & 0xFF) == 0x44) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static int getDelLockUserIndex(byte[] data) {
        if (data.length == 14) {
            return 0xFF & data[data.length - 4];
        }
        return -1;
    }

    public static List<LockRecord> lockRecords(byte[] data) {
        List<LockRecord> lockRecords = new ArrayList<LockRecord>();
        if (data.length >= 22) {
            int total = data.length;
            int number = 0;
            while (total > 22) {
                number++;
                total -= 9;
            }
            while (number >= 0) {
                LockRecord lockRecord = new LockRecord();
                boolean isOpenRecord = false;
                if (data[11 + 9 * number] == (byte) 0x4F) {
                    isOpenRecord = true;
                }
                lockRecord.setIsOpenRecord(isOpenRecord);
                lockRecord.setUserIndex(data[12 + 9 * number] & 0xFF);
                StringBuffer sb = new StringBuffer();
                sb.append(byteToString(data[13 + 9 * number]));
                sb.append(byteToString(data[14 + 9 * number]) + "-");
                sb.append(byteToString(data[15 + 9 * number]) + "-");
                sb.append(byteToString(data[16 + 9 * number]) + " ");
                sb.append(byteToString(data[17 + 9 * number]) + ":");
                sb.append(byteToString(data[18 + 9 * number]) + ":");
                sb.append(byteToString(data[19 + 9 * number]));

                lockRecord.setTime(sb.toString());
                lockRecords.add(lockRecord);
                number--;
            }
        }
        return lockRecords;
    }

    //
    public static LockParameter getLockParameter(byte[] data) {
        LockParameter lockParameter = new LockParameter();
        if (data.length == 14) {
            String parameterString = Integer.toBinaryString(data[11] & 0xFF);
            int total = 8 - parameterString.length();
            while (total > 0) {
                parameterString = "0" + parameterString;
                total--;
            }
            char[] parameter = parameterString.toCharArray();

            if (parameter[parameter.length - 1] == '1') {
                lockParameter.setADD_KEY(LockParameter.OPEN);
            }
            if (parameter[parameter.length - 2] == '1') {
                lockParameter.setDEL_KEY(LockParameter.OPEN);
            }
            if (parameter[parameter.length - 3] == '1') {
                lockParameter.setCLEAR_KEY(LockParameter.OPEN);
            }
            if (parameter[parameter.length - 4] == '1') {
                lockParameter.setKEYBOARD(LockParameter.OPEN);
            }
            if (parameter[parameter.length - 5] == '1') {
                lockParameter.setPOWER(LockParameter.OPEN);
            }
        }
        return lockParameter;
    }

    public static byte[] randomData() {
        byte[] bytes = new byte[15];
        for (int i = 0; i < 15; i++) {
            int random = (int) (Math.random() * 15) + 1;
            bytes[i] = (byte) random;
        }
        return bytes;
    }

    public static boolean isUpdateSuccess(byte[] data) {
        if (data.length == 13) {
            if ((0xff & data[10]) == 0x00) {
                return true;
            }
        }
        return false;
    }

}
