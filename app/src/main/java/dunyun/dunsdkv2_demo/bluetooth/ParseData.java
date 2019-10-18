package dunyun.dunsdkv2_demo.bluetooth;

import android.content.Context;
import android.util.Log;

import com.psoft.bluetooth.utils.OpcodeUtil;
import com.psoft.bluetooth.utils.TimeStampUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import dunyun.dunsdkv2_demo.beans.DYLockDevice;
import dunyun.dunsdkv2_demo.beans.LockUser;
import dunyun.dunsdkv2_demo.utils.AesEncryptionUtil;
import dunyun.dunsdkv2_demo.utils.CrcUtil;
import dunyun.dunsdkv2_demo.utils.HexUtil;

/**
 * Created by GuoWen on 2019/10/17.
 * description
 */

public class ParseData {
    private JSONObject rebackJason;
    private Boolean isFinish;
    private ArrayList<String> receiveSavedData;
    private LockUser lockUser;
    private LockUser addUser;
    private int reSendType;
    private int consumtime;
    private DYLockDevice device;
    Context mContext;


    public void init(LockUser addUser) {
        this.addUser = addUser;
    }

    public JSONObject start(byte[] data, LockUser mlock, int consumtime, DYLockDevice device, Context mContext) {
        this.mContext = mContext;
        this.lockUser = mlock;
        this.rebackJason = new JSONObject();
        this.receiveSavedData = new ArrayList();
        this.isFinish = Boolean.valueOf(true);
        this.reSendType = 0;
        this.consumtime = consumtime;
        this.device = device;
        this.Decryption(data);
        return this.rebackJason;
    }

    public int getReSendType() {
        return this.reSendType;
    }

    public Boolean getIsFinish() {
        return this.isFinish;
    }

    private boolean checkData(byte[] data) {
        int startCode = 255 & data[0];
        if(startCode != data.length) {
            this.onFail("107checkData返回数据的长度与实际长度不相同");
            return false;
        } else if(data.length < 5) {
            this.onFail("108check2返回数据的长度小于5");
            return false;
        } else if(!CrcUtil.checkCrc16(data)) {
            this.onFail("109check3Crc校验失败");
            return false;
        } else {
            return true;
        }
    }

    private void onFail(String reason) {
        this.setReback(this.rebackJason, "result", "fail");
        this.setReback(this.rebackJason, "reason", reason);
    }

    public void setReback(JSONObject drebackjson, String code, String data) {
        try {
            drebackjson.put(code, data);
        } catch (JSONException var5) {
            var5.printStackTrace();
            System.out.println("setRebackd的JSONObject失败");
        }

    }

    private void Decryption(byte[] data) {
        if(this.checkData(data)) {
            String[] dataType = HexUtil.byteTo01(data[1]);
            byte[] test;
            String error_reason;
            if(dataType[1].equals("0")) {
                test = new byte[data.length - 4];
                System.arraycopy(data, 2, test, 0, test.length);
                Log.i("DUNyunsdk", "未加密数据包");
                if(dataType[3].equals("1")) {
                    error_reason = OpcodeUtil.getErrorReason(data[2]);
                    Log.i("DUNyunsdk", "错误原因:" + error_reason);
                    this.onFail("101" + error_reason);
                } else {
                    this.application(test, "00000000000000");
                }
            } else {
                byte[] time;
                String date;
                byte[] apprecation;
                byte[] EncryptBytes;
                if(dataType[2].equals("0")) {
                    if(dataType[3].equals("0")) {
                        test = new byte[data.length - 4];
                        System.arraycopy(data, 2, test, 0, test.length);
                        if(test.length % 16 == 0) {
                            EncryptBytes = AesEncryptionUtil.decrypt(test, HexUtil.HexString2Bytes(this.lockUser.getOpenPwdKey()));
                            time = new byte[]{EncryptBytes[0], EncryptBytes[1], EncryptBytes[2], EncryptBytes[3]};
                            new SimpleDateFormat("yyyyMMddHHmmss");
                            date = TimeStampUtil.getDate(time);
                            this.setReback(this.rebackJason, "Locktime", date);
                            Log.i("DUNyunsdk", " 锁内的当前时间time=" + date);
                            if(EncryptBytes.length >= 6) {
                                apprecation = new byte[EncryptBytes.length - 4];
                                System.arraycopy(EncryptBytes, 4, apprecation, 0, apprecation.length);
                                this.application(apprecation, date);
                            } else {
                                Log.e("DUNyunsdk", " 返回信息数据过小=" + date);
                                this.onFail(" 102返回信息数据过小=" + HexUtil.byteToString(data));
                            }
                        }
                    } else {
                        if(dataType[2].equals("1") && dataType[4].equals("1") && dataType[3].equals("0")) {
                            ;
                        }

                        if(data[2] == 8) {
                            Log.e("DUNyunsdk", "时间戳不匹配,执行校正命令");
                            int savedType = Integer.parseInt(com.psoft.bluetooth.bluetooth.BluetoothCode.getLastData().get("type").toString());
                            if(savedType != 0) {
                                this.reSendType = 1;
                                this.isFinish = Boolean.valueOf(false);
                            } else {
                                this.onFail("103时间戳不匹配");
                            }
                        } else {
                            error_reason = OpcodeUtil.getErrorReason(data[2]);
                            Log.e("DUNyunsdk", "错误原因=" + error_reason + HexUtil.byteToString(data));
                            this.onFail("104error" + error_reason);
                        }
                    }
                } else if(dataType[2].equals("1")) {
                    if(data.length != 6) {
                        test = new byte[data.length - 5];
                        System.arraycopy(data, 3, test, 0, test.length);
                        if(test.length % 16 == 0) {
                            EncryptBytes = AesEncryptionUtil.decrypt(test, HexUtil.HexString2Bytes(this.lockUser.getOpenPwdKey()));
                            time = new byte[]{EncryptBytes[0], EncryptBytes[1], EncryptBytes[2], EncryptBytes[3]};
                            new SimpleDateFormat("yyyyMMddHHmmss");
                            date = TimeStampUtil.getDate(time);
                            this.setReback(this.rebackJason, "Locktime", date);
                            if(EncryptBytes.length >= 6) {
                                apprecation = new byte[EncryptBytes.length - 4];
                                System.arraycopy(EncryptBytes, 4, apprecation, 0, apprecation.length);
                                this.application(apprecation, date);
                            } else {
                                Log.e("DUNyunsdk", " 返回信息数据过小=" + date);
                                this.onFail(" 102返回信息数据过小=" + HexUtil.byteToString(data));
                            }
                        }
                    } else {
                        error_reason = String.valueOf(data[2] & 255);
                        error_reason = OpcodeUtil.getErrorReason(data[3]);
                        this.onFail("索引号:" + error_reason + ",错误原因:" + error_reason);
                    }
                } else {
                    this.onFail("105错误未收录");
                }
            }
        }

    }

    public void application(byte[] data, String locktime) {
        int len = data.length;
        int length = data[0] & 255;
        int i = 0;
        Boolean isCallHttp = Boolean.valueOf(false);
        List<byte[]> applicationList = new ArrayList();
        if(length > len) {
            this.onFail("106应用层数据长度不对");
        } else {
            while(true) {
                if(i >= len) {
                    this.setReback(this.rebackJason, "Locktime", locktime);
                    this.isFinish = Boolean.valueOf(true);

                    for(int j = 0; j < applicationList.size(); ++j) {
                        int x = ((byte[])applicationList.get(j))[1] & 255;
                        int errorCode = ((byte[])applicationList.get(j))[2] & 255;
                        if(errorCode != 0) {
                            if((((byte[])applicationList.get(j))[2] & 255) != 8 && (((byte[])applicationList.get(j))[2] & 255) != 10) {
                                String error_reason = OpcodeUtil.getErrorReason(((byte[])applicationList.get(j))[2]);
                                Log.e("DUNyunsdk", "应用层错误原因" + ((byte[])applicationList.get(j))[2] + "=" + error_reason + HexUtil.byteToString(data));
                                this.setReback(this.rebackJason, "result", "fail");
                                this.setReback(this.rebackJason, "operation", (x & 255) + "");
                                this.setReback(this.rebackJason, "errorcode=" + (((byte[])applicationList.get(j))[2] & 255), OpcodeUtil.getErrorReason(((byte[])applicationList.get(j))[2]));
                            } else {
                                this.isFinish = Boolean.valueOf(false);
                                this.reSendType = 1;
                                Log.e("DUNyunsdk", "时间错误=" + OpcodeUtil.getErrorReason(((byte[])applicationList.get(j))[2]) + HexUtil.byteToString(data));
                            }
                        } else {
                            this.setReback(this.rebackJason, "result", "success");
                            int i1;
                            int savedDatalength;
                            byte[] record;
                            String operationTimeStr;
                            byte[] getHardwareVersion;
                            switch(x) {
                                case 1:
                                    Log.i("DUNyunsdk", "读取锁内序列号完成");
                                    if(((byte[])applicationList.get(j)).length > 3) {
                                        getHardwareVersion = HexUtil.InterceptBytes((byte[])applicationList.get(j), 3, ((byte[])applicationList.get(j)).length);
                                        this.setReback(this.rebackJason, "SerialNum", HexUtil.byteToString(getHardwareVersion).replace(" ", ""));
                                    } else {
                                        this.setReback(this.rebackJason, "SerialNum", "null");
                                    }
                                    break;
                                case 2:
                                    if(((byte[])applicationList.get(j)).length > 3) {
                                        getHardwareVersion = HexUtil.InterceptBytes((byte[])applicationList.get(j), 3, ((byte[])applicationList.get(j)).length);
                                        this.setReback(this.rebackJason, "HardwareVersion", HexUtil.byteToString(getHardwareVersion).replace(" ", ""));
                                    } else {
                                        this.setReback(this.rebackJason, "HardwareVersion", "null");
                                    }

                                    Log.i("DUNyunsdk", "读取硬件版本号");
                                    break;
                                case 3:
                                    getHardwareVersion = HexUtil.InterceptBytes((byte[])applicationList.get(j), 3, ((byte[])applicationList.get(j)).length);
                                    this.setReback(this.rebackJason, "SoftwareVersion", HexUtil.byteToString(getHardwareVersion).replace(" ", ""));
                                    Log.i("DUNyunsdk", "读取锁内固件版本号");
                                    break;
                                case 4:
                                    this.setReback(this.rebackJason, "ProductionDate", "未完成");
                                    Log.i("DUNyunsdk", "读取生产日期");
                                case 5:
                                case 6:
                                case 7:
                                case 13:
                                case 14:
                                case 15:
                                case 16:
                                case 23:
                                case 24:
                                case 25:
                                case 26:
                                case 27:
                                case 28:
                                case 29:
                                case 30:
                                case 31:
                                case 32:
                                case 34:
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
                                case 48:
                                default:
                                    break;
                                case 8:
                                    Log.i("DUNyunsdk", "发送程序信息");
                                    System.out.println("发送程序信息成功，开始发送程序的数据");
                                    this.isFinish = Boolean.valueOf(false);
                                    this.reSendType = 4;
                                    break;
                                case 9:
                                    this.isFinish = Boolean.valueOf(false);
                                    this.reSendType = 4;
                                    Log.i("DUNyunsdk", "发送程序数据");
                                    break;
                                case 10:
                                    Log.i("DUNyunsdk", "开始更新程序");
                                    this.setReback(this.rebackJason, "updateStart", "success");
                                    break;
                                case 11:
                                    record = HexUtil.InterceptBytes((byte[])applicationList.get(j), 4, ((byte[])applicationList.get(j)).length);
                                    byte[] updateResultVersion = HexUtil.InterceptBytes(record, 0, 4);
                                    byte[] updateResultTime = HexUtil.InterceptBytes(record, 4, record.length);
                                    StringBuffer updatedVersion = new StringBuffer();

                                    for(int z = 0; z < updateResultVersion.length; ++z) {
                                        operationTimeStr = String.valueOf(255 & updateResultVersion[z]);
                                        if(operationTimeStr.length() < 2) {
                                            operationTimeStr = "0" + operationTimeStr;
                                        }

                                        updatedVersion.append(operationTimeStr.toUpperCase());
                                    }

                                    String updateResultDate = TimeStampUtil.getDate(updateResultTime);
                                    this.setReback(this.rebackJason, "updatedVersion", updatedVersion.toString());
                                    this.setReback(this.rebackJason, "updatedDate", updateResultDate);
                                    Log.i("DUNyunsdk", "固件刷新结果：版本=" + updatedVersion.toString() + "时间" + updateResultDate);
                                    break;
                                case 12:
                                    Log.i("DUNyunsdk", "断开连接");
                                    break;
                                case 17:
                                    int index = ((byte[])applicationList.get(j))[((byte[])applicationList.get(j)).length - 1] & 255;
                                    this.addUser.setUserIndex(index);
                                    this.lockUser = this.addUser;
                                    Log.i("DUNyunsdk", "添加钥匙完成" + this.lockUser.toString() + index);
                                    isCallHttp = Boolean.valueOf(true);
                                    break;
                                case 18:
                                    this.setReback(this.rebackJason, "changePasswd", "success");
                                    Log.i("DUNyunsdk", "修改钥匙密码");
                                    isCallHttp = Boolean.valueOf(true);
                                    break;
                                case 19:
                                    this.setReback(this.rebackJason, "deleteKey", "success");
                                    Log.i("DUNyunsdk", "删除钥匙");
                                    break;
                                case 20:
                                    this.setReback(this.rebackJason, "getAllKey", "success");
                                    String[] isFinshed = HexUtil.byteTo01(((byte[])applicationList.get(j))[4]);
                                    byte[] LockKey = HexUtil.InterceptBytes((byte[])applicationList.get(j), 5, ((byte[])applicationList.get(j)).length);
                                    int LockKey_length = LockKey.length;

                                    int save;
                                    for(boolean var23 = false; LockKey_length > 0 && LockKey_length % 12 == 0; LockKey_length -= 12) {
                                        byte[] index_key = HexUtil.InterceptBytes(LockKey, LockKey_length - 12, LockKey_length);
                                        int lastindex = index_key[0] & 255;
                                        save = index_key[1] & 255;
                                        byte[] userID = HexUtil.InterceptBytes(index_key, 2, 8);
                                        byte[] time = HexUtil.InterceptBytes(index_key, 8, index_key.length);
                                        String timeStr = TimeStampUtil.getDate(time);
                                        String str = lastindex + "," + save + "," + HexUtil.byteToStringclean(userID) + "," + timeStr;
                                        this.receiveSavedData.add(str);
                                    }

                                    if(isFinshed[0].equals("1")) {
                                        savedDatalength = this.receiveSavedData.size();

                                        for(save = 0; save < savedDatalength; ++save) {
                                            this.setReback(this.rebackJason, "Lokuser" + save, (String)this.receiveSavedData.get(save));
                                        }
                                    } else {
                                        this.reSendType = 3;
                                        this.isFinish = Boolean.valueOf(false);
                                    }

                                    Log.i("DUNyunsdk", "读取钥匙信息完成" + HexUtil.byteToString(LockKey));
                                    break;
                                case 21:
                                    Log.i("DUNyunsdk", "设置管理员ID");
                                    break;
                                case 22:
                                    i1 = ((byte[])applicationList.get(j))[((byte[])applicationList.get(j)).length - 1] & 255;
                                    this.setReback(this.rebackJason, "lockAdminID", String.valueOf(i1));
                                    Log.i("DUNyunsdk", "读取管理员ID");
                                    break;
                                case 33:
                                    this.setReback(this.rebackJason, "openLock", "success");
                                    Log.i("DUNyunsdk", "开锁");
                                    break;
                                case 49:
                                    savedDatalength = ((byte[])applicationList.get(j))[((byte[])applicationList.get(j)).length - 1] & 255;
                                    this.setReback(this.rebackJason, "Power", String.valueOf(savedDatalength));
                                    Log.i("DUNyunsdk", "读取电量");
                                    break;
                                case 50:
                                    String[] lockStatus = HexUtil.byteTo01(((byte[])applicationList.get(j))[((byte[])applicationList.get(j)).length - 2]);
                                    String str = new String();

                                    for(int z = 0; z < lockStatus.length; ++z) {
                                        str = lockStatus[z] + str;
                                    }

                                    this.setReback(this.rebackJason, "lockStatus", str);
                                    Log.i("DUNyunsdk", "锁状态:" + str);
                                    break;
                                case 51:
                                    this.setReback(this.rebackJason, "openEnable", "success");
                                    Log.i("DUNyunsdk", "开启使能");
                                    break;
                                case 52:
                                    this.setReback(this.rebackJason, "closeEnable", "success");
                                    Log.i("DUNyunsdk", "禁止使能");
                                    break;
                                case 53:
                                    String[] firstEnable = HexUtil.byteTo01(((byte[])applicationList.get(j))[3]);
                                    String[] secondEnable = HexUtil.byteTo01(((byte[])applicationList.get(j))[4]);
                                    String getedEnable1 = new String();
                                    String getedEnable2 = new String();

                                    for(i1 = 0; i1 < firstEnable.length; ++i1) {
                                        getedEnable1 = getedEnable1 + firstEnable[i1];
                                        getedEnable2 = getedEnable2 + secondEnable[i1];
                                    }

                                    this.setReback(this.rebackJason, "lockEnable", getedEnable1 + getedEnable2);
                                    Log.i("DUNyunsdk", "读取使能位" + getedEnable1 + getedEnable2);
                                    break;
                                case 54:
                                    this.setReback(this.rebackJason, "settime", "success");
                                    this.isFinish = Boolean.valueOf(true);
                                    Log.i("DUNyunsdk", "设置锁内时间");
                                    break;
                                case 55:
                                    byte[] getLockTime = new byte[4];
                                    getLockTime = HexUtil.InterceptBytes((byte[])applicationList.get(j), 3, ((byte[])applicationList.get(j)).length);
                                    int savedType = Integer.parseInt(com.psoft.bluetooth.bluetooth.BluetoothCode.getLastData().get("type").toString());
                                    this.setReback(this.rebackJason, "Locktime", TimeStampUtil.getDate(getLockTime));
                                    if(savedType != 0) {
                                        this.reSendType = 2;
                                        this.isFinish = Boolean.valueOf(false);
                                    } else {
                                        String lockTime = TimeStampUtil.getDate(getLockTime);
                                        Log.i("DUNyunsdk", "获取说内时间完成" + lockTime);
                                        this.setReback(this.rebackJason, "getLockTime", TimeStampUtil.getDate(getLockTime));
                                    }
                                    break;
                                case 56:
                                    if(((byte[])applicationList.get(j)).length == 4) {
                                        this.setReback(this.rebackJason, "getLockRecord", "noRecord");
                                    } else {
                                        this.setReback(this.rebackJason, "getLockRecord", "success");
                                        String[] LockRecordFinshed = HexUtil.byteTo01(((byte[])applicationList.get(j))[3]);
                                        byte[] LockRecord = HexUtil.InterceptBytes((byte[])applicationList.get(j), 4, ((byte[])applicationList.get(j)).length);
                                        int LockRecordlength = LockRecord.length;
                                        int recordlength;
                                        int recordLoca = 0;
                                        int recordTimes = 0;

                                        while(recordLoca < LockRecordlength) {
                                            recordlength = LockRecord[recordLoca] & 255;
                                            record = new byte[recordlength];
                                            System.arraycopy(LockRecord, recordLoca, record, 0, recordlength);
                                            recordLoca += recordlength;
                                            int operationType = record[1] & 255;
                                            String recordType = OpcodeUtil.getRecordOperation(operationType);
                                            byte[] operationTime = HexUtil.InterceptBytes(record, 2, 6);
                                            operationTimeStr = TimeStampUtil.getDate(operationTime);
                                            String content = null;
                                            String operationUser = null;
                                            String recordAllStr = null;
                                            int operationIndex;
                                            if(recordlength == 13) {
                                                operationUser = HexUtil.byteToStringclean(HexUtil.InterceptBytes(record, 7, 13));
                                                operationIndex = record[6] & 255;
                                                recordAllStr = operationTimeStr + "," + recordType + "," + operationUser + "," + content + "," + operationType + "," + operationIndex;
                                            }

                                            if(recordlength > 13) {
                                                content = HexUtil.byteToStringclean(HexUtil.InterceptBytes(record, 13, recordlength));
                                                operationIndex = record[6] & 255;
                                                operationUser = HexUtil.byteToString(HexUtil.InterceptBytes(record, 7, 13));
                                                recordAllStr = operationTimeStr + "," + recordType + "," + operationUser + "," + content + "," + operationType + "," + operationIndex;
                                            }

                                            ++recordTimes;
                                            this.setReback(this.rebackJason, "Record" + recordTimes, recordAllStr);
                                            System.out.println(recordAllStr);
                                        }

                                        if(LockRecordFinshed[0].equals("1")) {
                                            this.setReback(this.rebackJason, "isFinish", "success");
                                        } else {
                                            this.setReback(this.rebackJason, "isFinish", "fail");
                                        }

                                        Log.i("DUNyunsdk", "读取门锁记录");
                                    }
                            }
                        }
                    }

                    if(this.isFinish.booleanValue()) {
                        if(isCallHttp.booleanValue()) {
                            try {
                                this.rebackJason.put("Index", this.lockUser.getUserIndex());
                                this.rebackJason.put("Mac", this.lockUser.getbleMac());
                                this.rebackJason.put("Name", this.device.getName());
                                this.rebackJason.put("Rssi", this.device.getRssi() + "");
                                this.rebackJason.put("OpenPwdKey", this.lockUser.getOpenPwdKey());
                                this.rebackJason.put("OpenPwd", this.lockUser.getOpenLockPwd());
                                this.rebackJason.put("consumTime", this.consumtime + "");
                                this.rebackJason.put("reason", "success");
                            } catch (JSONException var42) {
                                var42.printStackTrace();
                            }
                        }

                        //this.setLoadMessage(this.rebackJason, ((byte[])applicationList.get(0))[1]);
                    }
                    break;
                }

                length = data[i] & 255;
                byte[] applicationBytes = new byte[length];
                System.arraycopy(data, i, applicationBytes, 0, length);
                applicationList.add(applicationBytes);
                i += length;
            }
        }

    }

    private void setLoadMessage(JSONObject jsonObject, byte bbb) {

    }
}
