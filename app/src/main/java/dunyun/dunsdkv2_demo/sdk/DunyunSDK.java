package dunyun.dunsdkv2_demo.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;


import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dunyun.dunsdkv2_demo.beans.DYLockDevice;
import dunyun.dunsdkv2_demo.beans.LockInfo;
import dunyun.dunsdkv2_demo.beans.LockParameter;
import dunyun.dunsdkv2_demo.beans.LockRecord;
import dunyun.dunsdkv2_demo.beans.LockUser;
import dunyun.dunsdkv2_demo.bluetooth.BleBluetooth;
import dunyun.dunsdkv2_demo.bluetooth.BleConstant;
import dunyun.dunsdkv2_demo.bluetooth.BluetoothCode;
import dunyun.dunsdkv2_demo.bluetooth.LockUtils;
import dunyun.dunsdkv2_demo.bluetooth.TimeoutRunnable;
import dunyun.dunsdkv2_demo.callback.Callback;
import dunyun.dunsdkv2_demo.callback.ConnectCallback;
import dunyun.dunsdkv2_demo.callback.ListCallback;
import dunyun.dunsdkv2_demo.utils.AesEncryptionUtil;
import dunyun.dunsdkv2_demo.utils.CrcUtil;
import dunyun.dunsdkv2_demo.utils.HexUtil;
import dunyun.dunsdkv2_demo.utils.LogUtil;

/**
 * <DL>
 * <DD>盾云SDK.</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/21
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public class DunyunSDK {
    public static final String TAG = DunyunSDK.TAG;

    private static byte[] passwd = {(byte) 0xa2, 0x56, 0x46, (byte) 0x8b, 0x35, 0x56, 0x5c, 0x25, 0x55, 0x13, 0x2e, 0x12, 0x37, (byte) 0xf3, (byte) 0x91, (byte) 0xf3};

    /**
     * 添加钥匙
     */
    private boolean addKey = false;

    /**
     * 超时时间
     */
    private static final int timeout = 2000;
    //    private static final int timeout = 1500;
    private static final int timeout2 = 3000;
    private static final int searchTimeout = 10 * 1000;

    private static DunyunSDK instance = null;
    private static BleBluetooth bleBluetooth;
    private static NumberFormat numberFormat;
    private BluetoothManager mBluetoothManager;
    private static Context mContext;
    /**
     * 用户身份信息
     */
    private LockUser lockUser;
    /**
     * 版本更新数据文件
     */
    private byte[] dataBytes;
    /**
     * 版本更新数据文件长度
     */
    private int dataBytesLength;
    private byte[] versionLength;
    /**
     * 更新偏移
     */
    private int offset;
    private int number = 1;
    private int numberTotal = 0;
    private int numberLast = 0;
    /**
     * 更新包当个包长度
     */
    private int packageLength = 70;
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * 接收数据回调
     */
    private Callback<byte[]> receiveDataCallback;
    /**
     * 握手认证回调
     */
    private Callback<String> authCallback;
    /**
     * 获取用户的回调
     */
    private Callback<List<LockUser>> getLockUsersCallback;
    /**
     * 开锁回调
     */
    private Callback<LockInfo> openLockPwdCallback;
    /**
     * 获取时间回调
     */
    private Callback<LockUser> getLockTimeCallback;
    /**
     * 添加用户回调
     */
    private Callback<LockUser> addLockUserCallback;
    /**
     * 删除用户回调
     */
    private Callback<Integer> delLockUserCallback;
    /**
     * 获取电量回调
     */
    private Callback<LockInfo> getBatteryPowerCallback;
    /**
     * 修改开锁密码回调
     */
    private Callback<LockUser> updateOpenLockPwdCallback;
    /**
     * 更新时间回调
     */
    private Callback<String> updateTimeCallback;
    /**
     * 读取锁回调
     */
    private Callback<String> readTimeCallback;
    /**
     * 读取版本回调
     */
    private Callback<String> readVersionCallback;
    /**
     * 开锁前握手回调
     */
    private Callback<String> openLockAuthCallback;
    /**
     * 读取开关门回调
     */
    private ListCallback<LockRecord> readRecordsCallback;
    /**
     * 读取状态
     */
    private Callback<LockParameter> readStatusCallback;
    /**
     *
     */
    private Callback<String> disconnectCallback;

    /**
     * 设置状态
     */
    private Callback<LockParameter> setStatusCallback;
    /**
     * 更新版本回调
     */
    private Callback<String> updateVersionCallback;

    private Callback<List<DYLockDevice>> searchDevicesCallback;

    private List<LockUser> lockUser1;
    private List<LockUser> lockUser2;

    /**
     * 获取电量
     */
    private final static int methodStatus_getBatteryPower = 1;
    /**
     * 获取锁用户
     */
    private final static int methodStatus_queryUser1 = 0;
    /**
     * 操作状态
     */
    private int methodStatus = methodStatus_queryUser1;

    private final static int addUser = 0;
    private final static int updateUser = 1;

    private int addOrUpdateUser = addUser;

    private final static int VERSIONUPDATE_FIRST = 0;
    private final static int VERSIONUPDATE_SENCOND = 1;
    private final static int VERSIONUPDATE_THREE = 3;
    private final static int VERSIONUPDATE_FOUR = 4;
    private int versionUpdate = VERSIONUPDATE_FIRST;

    private TimeoutRunnable auth_TimeoutRunnable;
    private TimeoutRunnable openLockAuth_TimeoutRunnable;
    private TimeoutRunnable getBatteryPower_TimeoutRunnable;
    private TimeoutRunnable getRssi_TimeoutRunnable;
    private TimeoutRunnable getTime_TimeoutRunnable;
    private TimeoutRunnable addLockUser_TimeoutRunnable;
    private TimeoutRunnable openLock_TimeoutRunnable;
    private TimeoutRunnable updateOpenLockPwd_TimeoutRunnable;
    private TimeoutRunnable delLockUser_TimeoutRunnable;
    private TimeoutRunnable getLockUsers_TimeoutRunnable;
    private TimeoutRunnable updateTime_TimeoutRunnable;
    private TimeoutRunnable readTime_TimeoutRunnable;
    private TimeoutRunnable readStatus_TimeoutRunnable;
    private TimeoutRunnable setStatus_TimeoutRunnable;
    private TimeoutRunnable readRecords_TimeoutRunnable;
    private TimeoutRunnable readVersion_TimeoutRunnable;
    private TimeoutRunnable updateVersion_TimeoutRunnable;
    private TimeoutRunnable disconnect_TimeoutRunnable;

    public static int version = 10;

    private Runnable searchTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (searchDevicesCallback != null) {
                searchDevicesCallback.onFailed("end");
            }
            bleBluetooth.stopScan();
        }
    };

    /**
     * 获取用户信息0-5
     */
    Callback<List<LockUser>> queryUser1Callback = new Callback<List<LockUser>>() {
        @Override
        public void onSuccess(List<LockUser> data) {
            lockUser1 = data;
            queryUser2();
        }

        @Override
        public void onFailed(String error) {

        }
    };

    /**
     * 获取用户信息6-12
     */
    Callback<List<LockUser>> queryUser2Callback = new Callback<List<LockUser>>() {
        @Override
        public void onSuccess(List<LockUser> data) {
            lockUser2 = data;
            if (lockUser1 != null) {
                lockUser1.addAll(lockUser2);
            }
            getLockUsersCallback.onSuccess(lockUser1);
        }

        @Override
        public void onFailed(String error) {

        }
    };

    /***
     * 初始化
     */
    public void init() {

    }

    public DYLockDevice getDYLockDevice() {
        //return bleBluetooth.getDYLockDevice();
        return null;
    }

    /**
     * 蓝牙是否打开
     */
    public boolean bluetoothIsOpen() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    /***
     * DunyunSDK
     *
     * @return
     */
    public static DunyunSDK getInstance(Context context) {
        if (instance == null) {
            mContext = context;
            synchronized (DunyunSDK.class) {
                if (instance == null) {
                    instance = new DunyunSDK();
                    bleBluetooth = new BleBluetooth(context);
                }
            }
        }

        numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

        LogUtil.d(mContext, "getInstance");

        return instance;
    }

    /***
     * 搜索设备
     *
     * @param searchDevicesCallback 搜索的回调
     */
    public void startSearchDevices(Callback<List<DYLockDevice>> searchDevicesCallback) {
        if (bluetoothIsOpen()) {
            this.searchDevicesCallback = searchDevicesCallback;
            bleBluetooth.startScan(searchDevicesCallback, false);
            dunyunHandler.removeCallbacks(searchTimeoutRunnable);
            dunyunHandler.postDelayed(searchTimeoutRunnable, searchTimeout);
        } else {
            searchDevicesCallback.onFailed(BleConstant.BLUETOOTH_NOT_OPEN);
        }
    }

    /***
     * 搜索所有设备
     *
     * @param searchDevicesCallback 搜索的回调
     */
    public void startSearchDevices(Callback<List<DYLockDevice>> searchDevicesCallback, boolean allDevice) {
        if (bluetoothIsOpen()) {
            this.searchDevicesCallback = searchDevicesCallback;
            bleBluetooth.startScan(searchDevicesCallback, allDevice);
            dunyunHandler.removeCallbacks(searchTimeoutRunnable);
            dunyunHandler.postDelayed(searchTimeoutRunnable, searchTimeout);
        } else {
            searchDevicesCallback.onFailed(BleConstant.BLUETOOTH_NOT_OPEN);
        }
    }

    /***
     * 停止搜索设备
     */
    public void stopSearchDevices() {
        dunyunHandler.removeCallbacks(searchTimeoutRunnable);
        bleBluetooth.stopScan();
    }

    public void setAddKey(boolean add){
        addKey = add;
    }





    /***
     * 连接设备
     *
     * @param device          设备
     * @param connectCallback 连接设备的回调
     */
    public void connect(DYLockDevice device, ConnectCallback connectCallback) {
        String name = device.getName();
        LogUtil.d(mContext,"-----锁硬件版本--------"+version+",name="+name);

        receiveDataCallback = new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                process(data);
            }

            @Override
            public void onFailed(String error) {

            }
        };

        if (bluetoothIsOpen()) {
            bleBluetooth.connectDevice(device, connectCallback, receiveDataCallback);
        } else {
            connectCallback.onFailed(new DYLockDevice(),"蓝牙未打开");
        }
    }

    /***
     * 握手认证
     *
     * @param device
     * @param authCallback
     */
    public void auth(DYLockDevice device, LockUser lockUser, Callback<String> authCallback) {
        this.authCallback = authCallback;
        //sendData(BluetoothCode.AUTH_1(lockUser));
        auth_TimeoutRunnable = new TimeoutRunnable(authCallback);
        dunyunHandler.postDelayed(auth_TimeoutRunnable, timeout);
    }

    /***
     * 开锁之前握手
     *
     * @param openLockAuthCallback 回调
     */
    public void openLockAuth(LockUser lockUser, Callback<String> openLockAuthCallback) {
        this.lockUser = lockUser;
        this.openLockAuthCallback = openLockAuthCallback;
        if (isConnected()) {
            auth3();
            openLockAuth_TimeoutRunnable = new TimeoutRunnable(openLockAuthCallback);
            dunyunHandler.postDelayed(openLockAuth_TimeoutRunnable, timeout2);
        } else {
            openLockAuthCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 是否连接
     *
     * @return 是否连接成功
     */
    public boolean isConnected() {
        return bleBluetooth.isConnected();
    }

    /***
     * 获取电量
     *
     * @param getBatteryPowerCallback 电量回调
     */
    public void getBatteryPower(LockUser lockUser, Callback<LockInfo> getBatteryPowerCallback) {
        this.lockUser = lockUser;
        this.getBatteryPowerCallback = getBatteryPowerCallback;
        if (isConnected()) {
            getBatteryPower();
            getBatteryPower_TimeoutRunnable = new TimeoutRunnable(getBatteryPowerCallback);
            dunyunHandler.postDelayed(getBatteryPower_TimeoutRunnable, timeout);
        } else {
            getBatteryPowerCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 获取信号值
     *
     * @param getRssiCallback 信号值回调
     */
    public void getRssi(LockUser lockUser, Callback<Integer> getRssiCallback) {
        this.lockUser = lockUser;
        if (isConnected()) {
            bleBluetooth.readRemoteRssi(getRssiCallback);
            getRssi_TimeoutRunnable = new TimeoutRunnable(getRssiCallback);
            dunyunHandler.postDelayed(getRssi_TimeoutRunnable, timeout);
        } else {
            getRssiCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }



    /***
     * 添加锁用户
     *
     * @param userId              用户标示
     * @param openLockPwd         开锁密码
     * @param addLockUserCallback 回调
     */
    public void addLockUser(String userId, String openLockPwd, Callback<LockUser> addLockUserCallback) {
        this.addLockUserCallback = addLockUserCallback;
        addOrUpdateUser = addUser;
        LockUser addLockUser = new LockUser();
        addLockUser.setUserIndex(0);
        addLockUser.setUserId(userId);
        addLockUser.setOpenLockPwd(openLockPwd);
        lockUser = addLockUser;
        if (isConnected()) {
            addUser();
            addLockUser_TimeoutRunnable = new TimeoutRunnable(addLockUserCallback);
            dunyunHandler.postDelayed(addLockUser_TimeoutRunnable, timeout);
        } else {
            addLockUserCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 获得锁内时间
     * @param lockUser
     * @param getLockTimeCallback 回调
     */
    public void GetLockTime(LockUser lockUser, Callback<LockUser> getLockTimeCallback) {
        this.getLockTimeCallback = getLockTimeCallback;
        this.lockUser = lockUser;
        if (isConnected()) {
            //addUser();
            sendData(BluetoothCode.getLockTime(lockUser));
            getTime_TimeoutRunnable = new TimeoutRunnable(getLockTimeCallback);
            dunyunHandler.postDelayed(getTime_TimeoutRunnable, timeout);
        } else {
            getLockTimeCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 添加锁用户
     *
     * @param addLockUser
     * @param addLockUserCallback 回调
     */
    public void addLockUser(LockUser addLockUser, Callback<LockUser> addLockUserCallback) {
        this.addLockUserCallback = addLockUserCallback;
        addOrUpdateUser = addUser;
        lockUser = addLockUser;
        if (isConnected()) {
            addUser();
            addLockUser_TimeoutRunnable = new TimeoutRunnable(addLockUserCallback);
            dunyunHandler.postDelayed(addLockUser_TimeoutRunnable, timeout);
        } else {
            addLockUserCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 开锁
     *
     * @param lockUser
     * @param openLockPwdCallback
     */
    public void openLock(LockUser lockUser, Callback<LockInfo> openLockPwdCallback) {
        this.lockUser = lockUser;
        this.openLockPwdCallback = openLockPwdCallback;
        if (isConnected()) {
            openLock();
            openLock_TimeoutRunnable = new TimeoutRunnable(openLockPwdCallback);
            dunyunHandler.postDelayed(openLock_TimeoutRunnable, timeout);
        } else {
            openLockPwdCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 更新开锁密码
     *
     * @param lockUser
     * @param newPwd
     * @param updateOpenLockPwdCallback
     */
    public void updateOpenLockPwd(LockUser lockUser, String newPwd, Callback<LockUser> updateOpenLockPwdCallback) {
        this.lockUser = lockUser;
        this.updateOpenLockPwdCallback = updateOpenLockPwdCallback;
        addOrUpdateUser = updateUser;

        lockUser.setOpenLockPwd(newPwd);
        if (isConnected()) {
            addUser();
            updateOpenLockPwd_TimeoutRunnable = new TimeoutRunnable(updateOpenLockPwdCallback);
            dunyunHandler.postDelayed(updateOpenLockPwd_TimeoutRunnable, timeout);
        } else {
            updateOpenLockPwdCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 删除锁
     *
     * @param lockUser
     * @param delIndex
     * @param delLockUserCallback
     */
    public void delLockUser(LockUser lockUser, int delIndex, Callback<Integer> delLockUserCallback) {
        this.delLockUserCallback = delLockUserCallback;
        this.lockUser = lockUser;
        if (isConnected()) {
            delUserReq(delIndex);
            delLockUser_TimeoutRunnable = new TimeoutRunnable(delLockUserCallback);
            dunyunHandler.postDelayed(delLockUser_TimeoutRunnable, timeout);
        } else {
            delLockUserCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 设置使能
     *
     * @param lockUser
     * @param lockParameter
     * @param setLockCallback
     */
    public void setLock(LockUser lockUser, LockParameter lockParameter, Callback<String> setLockCallback) {

    }

    /***
     * 获取用户列表
     *
     * @param lockUser
     * @param getLockUsersCallback
     */
    public void getLockUsers(LockUser lockUser, Callback<List<LockUser>> getLockUsersCallback) {
        this.lockUser = lockUser;
        this.getLockUsersCallback = getLockUsersCallback;
        if (isConnected()) {
            queryUser1();
            getLockUsers_TimeoutRunnable = new TimeoutRunnable(getLockUsersCallback);
            dunyunHandler.postDelayed(getLockUsers_TimeoutRunnable, timeout2);
        } else {
            getLockUsersCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 更新时间
     *
     * @param lockUser
     * @param time
     * @param updateTimeCallback
     */
    public void updateTime(LockUser lockUser, String time, Callback<String> updateTimeCallback) {
        this.lockUser = lockUser;
        this.updateTimeCallback = updateTimeCallback;
        if (isConnected()) {
           // sendData(BluetoothCode.UPDATE_TIME(lockUser, time));
            updateTime_TimeoutRunnable = new TimeoutRunnable(updateTimeCallback);
            dunyunHandler.postDelayed(updateTime_TimeoutRunnable, timeout);
        } else {
            updateTimeCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 读取时间
     *
     * @param lockUser
     * @param readTimeCallback
     */
    public void readTime(LockUser lockUser, Callback<String> readTimeCallback) {
        this.lockUser = lockUser;
        this.readTimeCallback = readTimeCallback;
        if (isConnected()) {
            //sendData(BluetoothCode.READ_TIME(lockUser));
            readTime_TimeoutRunnable = new TimeoutRunnable(readTimeCallback);
            dunyunHandler.postDelayed(readTime_TimeoutRunnable, timeout);
        } else {
            readTimeCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 读取状态
     *
     * @param lockUser
     * @param readStatusCallback
     */
    public void readStatus(LockUser lockUser, Callback<LockParameter> readStatusCallback) {
        this.lockUser = lockUser;
        this.readStatusCallback = readStatusCallback;
        if (isConnected()) {
            //sendData(BluetoothCode.READ_STATUS(lockUser));
            readStatus_TimeoutRunnable = new TimeoutRunnable(readStatusCallback);
            dunyunHandler.postDelayed(readStatus_TimeoutRunnable, timeout);
        } else {
            readStatusCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    public void disconnectDevice(Callback<String> disconnectCallback) {
        this.disconnectCallback = disconnectCallback;
        if (isConnected()) {
            deviceDisconnect();
            disconnect_TimeoutRunnable = new TimeoutRunnable(disconnectCallback);
            dunyunHandler.postDelayed(disconnect_TimeoutRunnable, timeout);
        } else {
            disconnectCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 设置状态
     *
     * @param lockUser
     * @param lockParameter
     */
    public void setStatus(LockUser lockUser, LockParameter lockParameter, Callback<LockParameter> setStatusCallback, boolean isEnable) {
        this.lockUser = lockUser;
        this.setStatusCallback = setStatusCallback;
        if (isConnected()) {
            if (isEnable) {
               // sendData(BluetoothCode.LOCK_ABLED(lockUser, lockParameter.toByte()));
            } else {
                //sendData(BluetoothCode.LOCK_ENABLED(lockUser, lockParameter.toByte()));
            }
            setStatus_TimeoutRunnable = new TimeoutRunnable(setStatusCallback);
            dunyunHandler.postDelayed(setStatus_TimeoutRunnable, timeout);
        } else {
            setStatusCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 读取记录
     *
     * @param lockUser
     * @param readRecordsCallback
     */
    public void readRecords(LockUser lockUser, ListCallback<LockRecord> readRecordsCallback) {
        this.lockUser = lockUser;
        this.readRecordsCallback = readRecordsCallback;
        if (isConnected()) {
            //sendData(BluetoothCode.READ_RECORD(lockUser));
            readRecords_TimeoutRunnable = new TimeoutRunnable(readRecordsCallback);
            dunyunHandler.postDelayed(readRecords_TimeoutRunnable, timeout2);
        } else {
            readRecordsCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 获取版本
     *
     * @param lockUser
     * @param readVersionCallback
     */
    public void readVersion(LockUser lockUser, Callback<String> readVersionCallback) {
        this.lockUser = lockUser;
        this.readVersionCallback = readVersionCallback;
        if (isConnected()) {
            //sendData(BluetoothCode.READ_VERSION(lockUser));
            readVersion_TimeoutRunnable = new TimeoutRunnable(readVersionCallback);
            dunyunHandler.postDelayed(readVersion_TimeoutRunnable, timeout);
        } else {
            readVersionCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    /***
     * 更新版本信息
     *
     * @param lockUser
     * @param dataBytes             更新的数据
     * @param updateVersionCallback
     */
    public void updateVersion(LockUser lockUser, byte[] dataBytes, byte[] versionCrc, Callback<String> updateVersionCallback) {
        this.lockUser = lockUser;
        this.updateVersionCallback = updateVersionCallback;
        this.dataBytes = dataBytes;
        dataBytesLength = dataBytes.length;
        versionLength = HexUtil.intToBytes(dataBytes.length);
        offset = 0;

        number = 1;
        numberTotal = 0;
        numberLast = 0;
        packageLength = 70;

        numberTotal = dataBytesLength / packageLength;
        numberLast = dataBytesLength % packageLength;

        versionUpdate = VERSIONUPDATE_FIRST;
        if (isConnected()) {
            //sendData(BluetoothCode.UPDATE_VERSION(lockUser, versionLength, versionCrc));
//            updateVersion_TimeoutRunnable = new TimeoutRunnable(updateVersionCallback);
//            dunyunHandler.postDelayed(updateVersion_TimeoutRunnable, timeout);
        } else {
            updateVersionCallback.onFailed(BleConstant.NOT_CONNECTED);
        }
    }

    private void sendUpdateVersion() {
        byte[] datas = new byte[packageLength];
        System.arraycopy(dataBytes, offset, datas, 0, packageLength);
        //sendData(BluetoothCode.UPDATE_VERSION_DATA(lockUser, HexUtil.intToBytes(offset), datas));
    }

    private void sendUpdateVersionUpdate() {
        //sendData(BluetoothCode.UPDATE_VERSION_UPDATE(lockUser));
    }

    private void sendUpdateVersionUpdateLast() {
        //sendData(BluetoothCode.UPDATE_VERSION_UPDATE_LAST(lockUser));
    }

    /***
     * 发送数据
     *
     * @param data
     */
    private void sendData(byte[] data) {
        LogUtil.d("-sendData-------version-"+version+"-------addKey--"+addKey+"-----data[0]"+data[0]);
        if (version == 10) {//老版本
            bleBluetooth.sendData(data);
        } else {//新版本
            int startCode = 0xFF & data[0];
            if ((addKey && (startCode == 0x80 || startCode == 0x89)) || (startCode == 0x81)) {//添加钥匙时读取锁内用户，加密1
                byte[] content = Arrays.copyOfRange(data, 2, data.length - 2);
                //时间戳
                int time = (int) (System.currentTimeMillis()/1000+ 8*60*60);
                byte[] timeArray = HexUtil.intToBytes(time);
                byte[] needContent = new byte[content.length + 4];
                System.arraycopy(content,0,needContent,0,content.length);
                needContent[needContent.length - 1] = timeArray[3];
                needContent[needContent.length - 2] = timeArray[2];
                needContent[needContent.length - 3] = timeArray[1];
                needContent[needContent.length - 4] = timeArray[0];
                //加密
                byte[] encodeData = AesEncryptionUtil.encrypt(needContent, passwd);

                byte[] endData = new byte[4 + encodeData.length];
                endData[0] = data[0];
                endData[1] = (byte)((data[1] & 0xFF) + 0x10);//改变索引号
                System.arraycopy(encodeData, 0, endData, 2, encodeData.length);
                //CRC校验
                byte[] crc = CrcUtil.getCrc16(endData, endData.length-2);
                endData[endData.length-1] = crc[1];
                endData[endData.length-2] = crc[0];

                bleBluetooth.sendData(endData);
            }else if ((startCode == 0x8c) || (startCode == 0x8d)
                    || (startCode == 0x88)|| (startCode == 0x82)
                    || (startCode == 0x93)|| (startCode == 0x95)) {//读取固件版本，固件刷新，不加密
                bleBluetooth.sendData(data);
            } else {//其他指令，加密2
                byte[] content = Arrays.copyOfRange(data, 2, data.length - 2);
                byte[] passwd = new byte[16];
                System.arraycopy(data, 2, passwd, 0, 11);
                //时间戳
                int time = (int) (System.currentTimeMillis()/1000+ 8*60*60);
                byte[] timeArray = HexUtil.intToBytes(time);
                byte[] needContent = new byte[content.length + 4];
                System.arraycopy(content,0,needContent,0,content.length);
                needContent[needContent.length - 1] = timeArray[3];
                needContent[needContent.length - 2] = timeArray[2];
                needContent[needContent.length - 3] = timeArray[1];
                needContent[needContent.length - 4] = timeArray[0];
                //加密
                byte[] encodeData = AesEncryptionUtil.encrypt(needContent, passwd);

                byte[] endData = new byte[4 + encodeData.length];
                endData[0] = data[0];
                endData[1] = data[1];
                System.arraycopy(encodeData, 0, endData, 2, encodeData.length);
                //CRC校验
                byte[] crc = CrcUtil.getCrc16(endData, endData.length-2);
                endData[endData.length-1] = crc[1];
                endData[endData.length-2] = crc[0];

                bleBluetooth.sendData(endData);
            }
        }
    }

    /***
     * 握手1
     */
    private void auth1() {
        //sendData(BluetoothCode.AUTH_1(lockUser));
    }

    /***
     * 握手2
     */
    private void auth2(byte[] data) {
        //sendData(BluetoothCode.AUTH_2(data, lockUser));
    }

    /***
     * 握手3
     */
    private void auth3() {
        //sendData(BluetoothCode.AUTH_3(lockUser, LockUtils.randomData()));
    }

    /***
     * 查询电量
     */
    private void getBatteryPower() {
        methodStatus = methodStatus_getBatteryPower;
        //sendData(BluetoothCode.QUERY_USER_1(lockUser));
    }

    /***
     * 查询0-5用户
     */
    private void queryUser1() {
        methodStatus = methodStatus_queryUser1;
        //sendData(BluetoothCode.QUERY_USER_1(lockUser));
    }

    /***
     * 查询6-12用户
     */
    private void queryUser2() {
       // sendData(BluetoothCode.QUERY_USER_2(lockUser));
    }

    /**
     * 开锁
     */
    private void openLock() {
       // sendData(BluetoothCode.OPEN_LOCK(lockUser));
    }

    /**
     * 添加用户
     */
    private void addUser() {
        //sendData(BluetoothCode.ADD_USER(lockUser));
    }

    /**
     * 删除用户请求
     */
    private void delUserReq(int delIndex) {
        //sendData(BluetoothCode.DEL_REQ(lockUser, delIndex));
    }

    /**
     * 断开设备
     */
    private void deviceDisconnect() {
        //sendData(BluetoothCode.DISCONNECT());
    }

    /**
     * 删除用户
     */
    private void delUser(int delIndex) {
        //sendData(BluetoothCode.DEL(lockUser, delIndex));
    }

    /**
     * 设置使能
     */
    private void setting(int setData) {
        //sendData(BluetoothCode.SETTING(lockUser, setData));
    }

    /**
     * 处理接收到的数据
     */
    private void process(byte[] data) {
        int startCode = 0xFF & data[0];
        switch (startCode) {
            case BleConstant.AUTH_1_SUCCESS:  //第一次握手成功
                auth2(data);
                break;
            case BleConstant.AUTH_2_SUCCESS: //第二次握手成功
                if (authCallback != null) {
                    authCallback.onSuccess("握手成功");
                }
                break;
            case BleConstant.QUERY_USER_1_SUCCESS: //查询锁内用户返回0-5用户、获取电量成功
                List<LockUser> lockUsers_query1 = LockUtils.getLockUsers(5, data);
                if (data.length == 69) {
                    if (methodStatus == methodStatus_queryUser1) {
                        queryUser1Callback.onSuccess(lockUsers_query1);
                    } else {
                        dunyunHandler.removeCallbacks(getBatteryPower_TimeoutRunnable);
                        getBatteryPowerCallback.onSuccess(LockUtils.getBatteryPower(data));
                    }
                }
                break;
            case BleConstant.QUERY_USER_2_SUCCESS: //查询锁内用户返回6-12用户
                List<LockUser> lockUsers_query2 = LockUtils.getLockUsers(6, data);
                if (data.length == 78) {
                    queryUser2Callback.onSuccess(lockUsers_query2);
                    dunyunHandler.removeCallbacks(getLockUsers_TimeoutRunnable);
                }
                break;
            case BleConstant.OPEN_LOCK_SUCCESS: //开锁成功
                if (data.length == 27) {
                    openLockPwdCallback.onSuccess(LockUtils.getLockInfo(data));
                    dunyunHandler.removeCallbacks(openLock_TimeoutRunnable);
                }
                break;
            case BleConstant.ADD_USER_SUCCESS: //添加用户、修改密码成功
                if (data.length == 16 || data.length == 21) {//21字节
                    if (addOrUpdateUser == addUser) {
                        if (LockUtils.isAddUser(data)) {
                            addLockUserCallback.onSuccess(LockUtils.getLockUser(data));
                        } else {
                            addLockUserCallback.onFailed("该锁已禁止添加用户");
                        }
                        dunyunHandler.removeCallbacks(addLockUser_TimeoutRunnable);
                    } else {
                        if (LockUtils.isAddUser(data)) {
                            updateOpenLockPwdCallback.onSuccess(lockUser);
                        } else {
                            updateOpenLockPwdCallback.onFailed("该锁已禁止修改密码");
                        }
                        dunyunHandler.removeCallbacks(updateOpenLockPwd_TimeoutRunnable);
                    }
                }
                break;
            case BleConstant.DEL_REQ_SUCCESS: //删除用户请求成功
                delUser(LockUtils.getDelLockUserIndex(data));
                break;
            case BleConstant.DEL_SUCCESS: //删除用户成功
                if (data.length == 14) {
                    delLockUserCallback.onSuccess(LockUtils.getDelLockUserIndex(data));
                    dunyunHandler.removeCallbacks(delLockUser_TimeoutRunnable);
                }
                break;
            case BleConstant.UPDATE_TIME_SUCCESS://更新系统时间成功
                if (data.length == 13) {
                    updateTimeCallback.onSuccess("success");
                    dunyunHandler.removeCallbacks(updateTime_TimeoutRunnable);
                }
                break;
            case BleConstant.READ_TIME_SUCCESS://获取系统时间成功
                if (data.length == 19) {
                    dunyunHandler.removeCallbacks(readTime_TimeoutRunnable);
                }
                break;
            case BleConstant.AUTH_3_SUCCESS://握手3成功
                if (data.length == 20) {
                    openLockAuthCallback.onSuccess("success");
                    dunyunHandler.removeCallbacks(openLockAuth_TimeoutRunnable);
                }
                break;
            case BleConstant.READ_RECORD_SUCCESS://门锁记录
                readRecordsCallback.onSuccess(LockUtils.lockRecords(data));
                dunyunHandler.removeCallbacks(readRecords_TimeoutRunnable);
                break;
            case BleConstant.STATUS_ENABLE_SUCCESS://设置开启
                if (data.length == 14) {
                    setStatusCallback.onSuccess(LockUtils.getLockParameter(data));
                    dunyunHandler.removeCallbacks(setStatus_TimeoutRunnable);
                }
                break;
            case BleConstant.STATUS_ABLE_SUCCESS://设置关闭
                if (data.length == 14) {
                    setStatusCallback.onSuccess(LockUtils.getLockParameter(data));
                    dunyunHandler.removeCallbacks(setStatus_TimeoutRunnable);
                }
                break;
            case BleConstant.READ_VERSION_SUCCESS://获取版本成功
                if (data.length == 20) {
                    try{
                        readVersionCallback.onSuccess(LockUtils.getVersion(data));
                        dunyunHandler.removeCallbacks(readVersion_TimeoutRunnable);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case BleConstant.STATUS_SUCCESS://使能状态获取成功
                if (data.length == 14) {
                    readStatusCallback.onSuccess(LockUtils.getLockParameter(data));
                    dunyunHandler.removeCallbacks(readStatus_TimeoutRunnable);
                }
                break;
            case BleConstant.DISCONNECT_SUCCESS:
                if (data.length == 4) {
                    disconnectCallback.onSuccess("success");
                    dunyunHandler.removeCallbacks(disconnect_TimeoutRunnable);
                }
                break;
            case BleConstant.UPDATE_VERSION_SUCCESS://版本更新成功
                if (versionUpdate == VERSIONUPDATE_FIRST) {
                    offset = 0;
                    packageLength = 70;
                    versionUpdate = VERSIONUPDATE_SENCOND;
                    sendUpdateVersion();
                } else if (versionUpdate == VERSIONUPDATE_SENCOND) {
                    if (number < numberTotal) {
                        if (LockUtils.isUpdateSuccess(data)) {
                            offset = number * packageLength;
                            sendUpdateVersion();
                            number++;

                            String result = numberFormat.format((float) offset / (float) dataBytesLength * 100);
                            updateVersionCallback.onSuccess(result);
                        } else {
                            sendUpdateVersion();
                        }
                    } else if (numberLast > 0) {
                        offset = number * packageLength;
                        packageLength = numberLast;
                        sendUpdateVersion();
                        numberLast = 0;
                        String result = numberFormat.format((float) offset / (float) dataBytesLength * 100);
                        updateVersionCallback.onSuccess(result);
                    } else {
                        versionUpdate = VERSIONUPDATE_THREE;
                        sendUpdateVersionUpdate();
                    }
                } else if (versionUpdate == VERSIONUPDATE_THREE) {
                    versionUpdate = VERSIONUPDATE_FOUR;
                    readUpdate();
                } else if (versionUpdate == VERSIONUPDATE_FOUR) {
                    LogUtil.d(mContext, "-------更新成功-----");

                    count = 0;
                    if (timer != null)
                        timer.cancel();

                    if (LockUtils.isUpdateSuccess(data)) {
                        updateVersionCallback.onSuccess("更新完成");
                    } else {
                        updateVersionCallback.onSuccess("更新失败");
                    }
                }
                break;
            default:LogUtil.e("unknow process");break;
        }
    }

    private int count = 0;

    TimerTask timerTask;
    Timer timer;

    private void readUpdate() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (count < 5) {
                    sendUpdateVersionUpdateLast();
                    count++;
                } else {
                    updateVersionCallback.onSuccess("更新失败");
                }
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 2000, 2000);
    }

    public void destroy() {
        bleBluetooth.stopScan();
        bleBluetooth.destroy();
    }

    public void stopAll(){
        bleBluetooth.stopAll();
    }

    public boolean isWorking(){
        return bleBluetooth.isWorking();
    }

    public static Handler dunyunHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
            }
        }
    };

}
