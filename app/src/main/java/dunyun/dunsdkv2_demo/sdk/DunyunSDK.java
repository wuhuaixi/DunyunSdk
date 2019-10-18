package dunyun.dunsdkv2_demo.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dunyun.dunsdkv2_demo.beans.DYLockDevice;
import dunyun.dunsdkv2_demo.beans.LockInfo;
import dunyun.dunsdkv2_demo.beans.LockParameter;
import dunyun.dunsdkv2_demo.beans.LockRecord;
import dunyun.dunsdkv2_demo.beans.LockUpdate;
import dunyun.dunsdkv2_demo.beans.LockUser;
import dunyun.dunsdkv2_demo.bluetooth.BleBluetooth;
import dunyun.dunsdkv2_demo.bluetooth.BleConstant;
import dunyun.dunsdkv2_demo.bluetooth.BluetoothCode;
import dunyun.dunsdkv2_demo.bluetooth.LockUtils;
import dunyun.dunsdkv2_demo.bluetooth.ParseData;
import dunyun.dunsdkv2_demo.bluetooth.TimeoutRunnable;
import dunyun.dunsdkv2_demo.callback.Callback;
import dunyun.dunsdkv2_demo.callback.ConnectCallback;
import dunyun.dunsdkv2_demo.datebase.SharedPreference;
import dunyun.dunsdkv2_demo.utils.AesEncryptionUtil;
import dunyun.dunsdkv2_demo.utils.ConsumTimeUtil;
import dunyun.dunsdkv2_demo.utils.CrcUtil;
import dunyun.dunsdkv2_demo.utils.HexUtil;
import dunyun.dunsdkv2_demo.utils.LogUtil;
import dunyun.dunsdkv2_demo.utils.RandomUtil;
import dunyun.dunsdkv2_demo.utils.TimeStampUtil;

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
    public String TAG = "DunyunSDK";
    public static String SDKVersion = "2.0";
    private static BleBluetooth bleBluetooth;
    private static NumberFormat numberFormat;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private static Context mContext;
    private static DunyunSDK instance = null;
    private LockUser lockUser;
    private LockUser addLockUser;
    private LockUser connectedLocUser = new LockUser();
    private int isRunning = 0;
    private TimeoutRunnable receiveTimeoutRunnable;
    public int Rssi = 0;
    private ConsumTimeUtil consumTimeUtil = new ConsumTimeUtil();
    private static final int timeout = 10000;
    private static final int searchTimeout = 10000;
    private Callback<String> searchDevicesCallback;
    private Callback<String> getSerialNumCallback;
    private Callback<String> getAdminIDCallback;
    private Callback<String> settingAdminIDCallback;
    private Callback<String> getLockUsersCallback;
    private Callback<String> updateTimeCallback;
    private Callback<String> getLockTimeCallback;
    private Callback<String> openEnableCallback;
    private Callback<String> getEnableCallback;
    private Callback<String> closeEnableCallback;
    private Callback<String> getLockPowerCallback;
    private Callback<String> getStatusCallback;
    private Callback<String> openLockPwdCallback;
    private Callback<String> addLockUserCallback;
    private Callback<String> delLockUserCallback;
    private Callback<String> changeLockPwdCallback;
    private Callback<String> getSoftwareVersionCallback;
    private Callback<String> getHardwareVersionCallback;
    private Callback<String> getRecordsCallback;
    private Callback<String> disconnectCallback;
    Callback<List<DYLockDevice>> onlysearchDevicesCallback;
    private Callback<String> SDKCallback = null;
    private Callback<byte[]> receiveDataCallback = new Callback<byte[]>() {
        public void onSuccess(byte[] data) {
            DunyunSDK.this.PARSED_DATA(data);
        }

        public void onFailed(String error) {
        }
    };
    public static Handler dunyunHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1:
                default:
            }
        }
    };
    private Callback<List<DYLockDevice>> addDevicesCallback = new Callback<List<DYLockDevice>>() {
        public void onSuccess(List<DYLockDevice> devices) {
            Log.e(DunyunSDK.this.TAG, "搜索到设置状态的锁" + ((DYLockDevice)devices.get(0)).getName() + "+" + ((DYLockDevice)devices.get(0)).getMac());
            DunyunSDK.this.lockUser.setbleMac(((DYLockDevice)devices.get(0)).getMac());
            DunyunSDK.this.lockUser.setbleName(((DYLockDevice)devices.get(0)).getName());
            DunyunSDK.dunyunHandler.removeCallbacks(DunyunSDK.this.searchTimeoutRunnable);
            DunyunSDK.this.stopSearchDevices();
            DunyunSDK.this.connectBLe(DunyunSDK.this.lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.SDKCallback = DunyunSDK.this.searchDevicesCallback;
                    byte[] randomKey = RandomUtil.getRandKey();
                    String randomPWD = RandomUtil.getRandPWD();
                    DunyunSDK.this.addLockUser = new LockUser();
                    DunyunSDK.this.addLockUser.setOpenLockPwd(randomPWD);
                    DunyunSDK.this.addLockUser.setOpenPwdKey(HexUtil.byteToStringclean(randomKey));
                    DunyunSDK.this.addLockUser.setbleMac(DunyunSDK.this.lockUser.getbleMac());
                    DunyunSDK.this.lockUser.setOpenLockPwd(randomPWD);
                    DunyunSDK.bleBluetooth.sendData(BluetoothCode.AddKey(lockUser, randomKey));
                }

                public void onFailed(String error) {
                    Log.e("连接失败", "error=" + error);
                }
            });
        }

        public void onFailed(String error) {
            Log.e(DunyunSDK.this.TAG, "搜索失败：");
        }
    };
    private int reSearchTimes = 2;
    private Runnable searchTimeoutRunnable = new Runnable() {
        public void run() {
            Log.e(DunyunSDK.this.TAG, "当前搜索的次数：" + DunyunSDK.this.reSearchTimes);
            if(DunyunSDK.this.searchDevicesCallback != null) {
                DunyunSDK.bleBluetooth.stopScan();
                DunyunSDK.this.reSearchTimes--;
                DunyunSDK.this.searchDevicesCallback.onFailed("搜索时间到");
            }

            if(DunyunSDK.this.onlysearchDevicesCallback != null) {
                DunyunSDK.this.onlysearchDevicesCallback.onFailed("搜索时间到");
                DunyunSDK.this.stopSearchDevices();
            }
        }
    };
    private Boolean isLogined = Boolean.valueOf(true);
    private LockUpdate lockUpdate;
    private Boolean isFinish = Boolean.valueOf(true);
    private ArrayList<String> receiveSavedData = new ArrayList();

    public DunyunSDK() {
    }

    public boolean bluetoothIsOpen() {
        if(this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if(this.mBluetoothManager == null) {
                LogUtil.e(this.TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        if(this.mBluetoothAdapter == null) {
            this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        }

        return this.mBluetoothAdapter.isEnabled();
    }

    public void destroy() {
        bleBluetooth.stopScan();
        bleBluetooth.destroy();
        this.isRunning = 0;
    }

    public void operationStart() {
        this.consumTimeUtil.startConsumtime();
    }

    public String getBleStatus() {
        String BleStatus = "";
        switch(this.isRunning) {
            case 0:
                BleStatus = "isWaiting";
                break;
            case 1:
                BleStatus = "isSearching";
                break;
            case 2:
                BleStatus = "startConnect";
                break;
            case 3:
                BleStatus = "endConnect";
                break;
            case 4:
                BleStatus = "isSendingData";
                break;
            default:
                BleStatus = "unknowStatus";
        }

        return BleStatus;
    }

    public DYLockDevice getCurrentDevice() {
        return bleBluetooth.getCurrentDevice();
    }

    public static DunyunSDK getInstance(Context context) {
        if(instance == null) {
            mContext = context;
            Class var1 = DunyunSDK.class;
            synchronized(DunyunSDK.class) {
                if(instance == null) {
                    instance = new DunyunSDK();
                    bleBluetooth = new BleBluetooth(context);
                }
            }
        }

        SharedPreference sh = new SharedPreference();
        BluetoothCode.setCheckTime(sh.getTimeCheck(mContext));
        numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        LogUtil.d(mContext, "getInstance");
        return instance;
    }

    public void startSearchDevices(int timeout, Callback<List<DYLockDevice>> onlysearchDevicesCallback) {
        this.onlysearchDevicesCallback = onlysearchDevicesCallback;
        bleBluetooth.startScan(onlysearchDevicesCallback, false);
        dunyunHandler.removeCallbacks(this.searchTimeoutRunnable);
        dunyunHandler.postDelayed(this.searchTimeoutRunnable, (long)timeout);
    }

    public void connectBLe(final LockUser lockUser, final Callback<String> connectBLeCallback) {
        this.receiveTimeoutRunnable = new TimeoutRunnable(connectBLeCallback);
        this.lockUser = lockUser;
        if(this.isLogined.booleanValue()) {
            ConnectCallback mConnectCallback = new ConnectCallback() {
                public void onSuccess(DYLockDevice device) {
                    DunyunSDK.this.connectedLocUser = lockUser;
                    DunyunSDK.this.isRunning = 3;
                    LogUtil.d(DunyunSDK.mContext, "mConnectCallback成功连接" + DunyunSDK.this.Rssi);
                    connectBLeCallback.onSuccess("成功连接");
                }

                public void onFailed(DYLockDevice device, String reason) {
                    LogUtil.d(DunyunSDK.mContext, "mConnectCallbackonFailed");
                    connectBLeCallback.onFailed("201" + reason);
                    DunyunSDK.this.destroy();
                }

                public void onDescoverServiceFailed(DYLockDevice device) {
                    LogUtil.d(DunyunSDK.mContext, "mConnectCallbackonDescoverServiceFailed");
                    connectBLeCallback.onFailed("202onDescoverServiceFailed");
                    DunyunSDK.this.destroy();
                }

                public void onDataReceive(byte[] data) {
                    System.out.println("onDataReceive：");
                    connectBLeCallback.onFailed("203onDataReceive:");
                    DunyunSDK.this.destroy();
                }

                public void onDisconnected(DYLockDevice device) {
                    System.out.println("SDK:onDisconnected：");
                }
            };
            LogUtil.d(mContext, "-----待连接锁-mac=" + lockUser.getbleMac());
            if(this.bluetoothIsOpen()) {
                this.stopSearchDevices();
                this.isRunning = 2;
                bleBluetooth.connectBLe(lockUser.getbleMac(), mConnectCallback, this.receiveDataCallback);
            } else {
                mConnectCallback.onFailed(new DYLockDevice(), "蓝牙未打开");
            }
        } else {
            connectBLeCallback.onFailed("error:202未登录");
        }

    }

    public void stopSearchDevices() {
        System.out.println("stopSearchDevices");
        dunyunHandler.removeCallbacks(this.searchTimeoutRunnable);
        this.reSearchTimes = 2;
        bleBluetooth.stopScan();
        this.isRunning = 0;
    }

    private void sendData(byte[] sendingData, Callback<String> sendCallback) {
        this.SDKCallback = sendCallback;
        if(sendingData.length > 1) {
            dunyunHandler.removeCallbacks(this.receiveTimeoutRunnable);
            this.receiveTimeoutRunnable = new TimeoutRunnable(SDKCallback);
            dunyunHandler.postDelayed(this.receiveTimeoutRunnable, 10000L);
            this.isRunning = 4;
            bleBluetooth.sendData(sendingData);
            System.out.println("正常发送数据");
        } else {
            System.out.println("数据已经发送完毕");
        }

    }

    public void addLockUser(LockUser lockUser, Callback<String> searchDevicesCallback) {
        this.operationStart();
        if(this.bluetoothIsOpen()) {
            this.searchDevicesCallback = searchDevicesCallback;
            this.SDKCallback = searchDevicesCallback;
            this.lockUser = lockUser;
            Boolean isSearchSetting = Boolean.valueOf(true);
            bleBluetooth.startScan(this.addDevicesCallback, isSearchSetting.booleanValue());
            dunyunHandler.removeCallbacks(this.searchTimeoutRunnable);
            this.reSearchTimes = 1;
            dunyunHandler.postDelayed(this.searchTimeoutRunnable, 10000L);
        } else {
            searchDevicesCallback.onFailed("蓝牙未开启");
        }

    }

    public void openLock(final LockUser lockUser, final Callback<String> openLockCallback) {
        this.openLockPwdCallback = openLockCallback;
        this.operationStart();
        this.lockUser = lockUser;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.OpenLock(lockUser), openLockCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.OpenLock(lockUser), openLockCallback);
                }

                public void onFailed(String error) {
                    openLockCallback.onFailed(error);
                }
            });
        }

    }

    public void updateOpenLockPwd(final LockUser lockUser, final String newPwd, final Callback<String> changeLockPwdCallback) {
        this.lockUser = lockUser;
        this.changeLockPwdCallback = changeLockPwdCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.ChangePasswd(lockUser, HexUtil.HexString2Bytes(newPwd)), changeLockPwdCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.ChangePasswd(lockUser, HexUtil.HexString2Bytes(newPwd)), changeLockPwdCallback);
                }

                public void onFailed(String error) {
                    changeLockPwdCallback.onFailed(error);
                }
            });
        }

    }

    public void delLockUser(final LockUser lockUser, final int delIndex, final Callback<String> delLockUserCallback) {
        this.delLockUserCallback = delLockUserCallback;
        this.lockUser = lockUser;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.DeleteKey(lockUser, delIndex), delLockUserCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.DeleteKey(lockUser, delIndex), delLockUserCallback);
                }

                public void onFailed(String error) {
                    delLockUserCallback.onFailed(error);
                }
            });
        }

    }

    public void GetLockTime(final LockUser lockUser, final Callback<String> getLockTimeCallback) {
        this.lockUser = lockUser;
        this.getLockTimeCallback = getLockTimeCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetLockTime(lockUser), getLockTimeCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetLockTime(lockUser), getLockTimeCallback);
                }

                public void onFailed(String error) {
                    getLockTimeCallback.onFailed(error);
                }
            });
        }

    }

    public void GetSerialNum(LockUser lockUser, final Callback<String> getSerialNumCallback) {
        this.lockUser = lockUser;
        this.getSerialNumCallback = getSerialNumCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetSerialNum(), getSerialNumCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetSerialNum(), getSerialNumCallback);
                }

                public void onFailed(String error) {
                    getSerialNumCallback.onFailed(error);
                }
            });
        }

    }

    public void GetSoftwareVersion(LockUser lockUser, final Callback<String> getSoftwareVersionCallback) {
        this.lockUser = lockUser;
        this.getSoftwareVersionCallback = getSoftwareVersionCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetSoftwareVersion(), getSoftwareVersionCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetSoftwareVersion(), getSoftwareVersionCallback);
                }

                public void onFailed(String error) {
                    getSoftwareVersionCallback.onFailed(error);
                }
            });
        }

    }

    public void GetProductionDate(LockUser lockUser, Callback<String> getProductionDateCallback) {
    }

    public void getBatteryPower(final LockUser lockUser, final Callback<String> getLockPowerCallback) {
        this.lockUser = lockUser;
        this.getLockPowerCallback = getLockPowerCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetPower(lockUser), getLockPowerCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetPower(lockUser), getLockPowerCallback);
                }

                public void onFailed(String error) {
                    getLockPowerCallback.onFailed(error);
                }
            });
        }

    }

    public void ReadEnable(final LockUser lockUser, final Callback<String> getEnableCallback) {
        this.getEnableCallback = getEnableCallback;
        this.lockUser = lockUser;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.ReadEnable(lockUser), getEnableCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.ReadEnable(lockUser), getEnableCallback);
                }

                public void onFailed(String error) {
                    getEnableCallback.onFailed(error);
                }
            });
        }

    }

    public void openEnable(final LockUser lockUser, final String openEnable, final Callback<String> openEnableCallback) {
        this.openEnableCallback = openEnableCallback;
        this.lockUser = lockUser;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.OpenEnable(lockUser, openEnable), openEnableCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.OpenEnable(lockUser, openEnable), openEnableCallback);
                }

                public void onFailed(String error) {
                    openEnableCallback.onFailed(error);
                }
            });
        }

    }

    public void closeEnable(final LockUser lockUser, final String closeEnable, final Callback<String> closeEnableCallback) {
        this.closeEnableCallback = closeEnableCallback;
        this.lockUser = lockUser;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.CloseEnable(lockUser, closeEnable), closeEnableCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.CloseEnable(lockUser, closeEnable), closeEnableCallback);
                }

                public void onFailed(String error) {
                    closeEnableCallback.onFailed(error);
                }
            });
        }

    }

    public void openLockAuth(final LockUser lockUser, final String AuthorizationId, final String startTime, final String endTime, final String times, final Callback<String> getAdminIDCallback) {
        this.getAdminIDCallback = getAdminIDCallback;
        this.lockUser = lockUser;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.OpenLockAuthorization(lockUser, AuthorizationId, startTime, endTime, times), getAdminIDCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.OpenLockAuthorization(lockUser, AuthorizationId, startTime, endTime, times), getAdminIDCallback);
                }

                public void onFailed(String error) {
                    getAdminIDCallback.onFailed(error);
                }
            });
        }

    }

    public void settingAdminID(final LockUser lockUser, final int index, final Callback<String> settingAdminIDCallback) {
        this.settingAdminIDCallback = settingAdminIDCallback;
        this.lockUser = lockUser;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.SettingAdminID(lockUser, index), settingAdminIDCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.SettingAdminID(lockUser, index), settingAdminIDCallback);
                }

                public void onFailed(String error) {
                    settingAdminIDCallback.onFailed(error);
                }
            });
        }

    }

    public void updateTime(final LockUser updateUser, String date, final Callback<String> updateTimeCallback) {


    }

    private void updateTimeItem(LockUser updateUser, String date, final Callback<String> updateTimeCallback) {
        System.out.println(":" + date);
        Log.i("updateTimeItem", "需要更新的时间" + date);
        updateUser.setCurrentTime(date);
        BluetoothCode.Settime(updateUser);
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(updateUser.getbleMac())) {
            this.sendData(BluetoothCode.getLockTime(), updateTimeCallback);
        } else {
            this.connectBLe(updateUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.getLockTime(), updateTimeCallback);
                }

                public void onFailed(String error) {
                    updateTimeCallback.onFailed(error);
                }
            });
        }

    }

    public void readStatus(final LockUser lockUser, final Callback<String> getStatusCallback) {
        this.lockUser = lockUser;
        this.getStatusCallback = getStatusCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetStatus(lockUser), getStatusCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetStatus(lockUser), getStatusCallback);
                }

                public void onFailed(String error) {
                    getStatusCallback.onFailed(error);
                }
            });
        }

    }

    public void disconnectDevice(Callback<String> disconnectCallback) {
        this.disconnectCallback = disconnectCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(this.lockUser.getbleMac())) {
            this.sendData(BluetoothCode.BreakConnection(), disconnectCallback);
        } else {
            disconnectCallback.onSuccess("已断开");
        }

    }

    public void GetHardwareVersion(LockUser lockUser, final Callback<String> getHardwareVersionCallback) {
        this.lockUser = lockUser;
        this.getHardwareVersionCallback = getHardwareVersionCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetHardwareVersion(), getHardwareVersionCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetHardwareVersion(), getHardwareVersionCallback);
                }

                public void onFailed(String error) {
                    getHardwareVersionCallback.onFailed(error);
                }
            });
        }

    }

    public void getLockUsers(final LockUser lockUser, final Callback<String> getLockUsersCallback) {
        this.lockUser = lockUser;
        this.getLockUsersCallback = getLockUsersCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetAllKey(lockUser, 0, 10), getLockUsersCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetAllKey(lockUser, 0, 10), getLockUsersCallback);
                }

                public void onFailed(String error) {
                    getLockUsersCallback.onFailed(error);
                    DunyunSDK.this.destroy();
                }
            });
        }

    }

    public void readRecords(final LockUser lockUser, final Callback<String> getRecordsCallback) {
        this.lockUser = lockUser;
        this.getRecordsCallback = getRecordsCallback;
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetLockRecord(lockUser), getRecordsCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetLockRecord(lockUser), getRecordsCallback);
                }

                public void onFailed(String error) {
                    getRecordsCallback.onFailed(error);
                    DunyunSDK.this.destroy();
                }
            });
        }

    }

    public void updateVersion(final LockUser lockUser, final byte[] softBytes, final String softVersion, final Callback<String> updateVersionCallback) {
        this.lockUser = lockUser;
        this.lockUpdate = new LockUpdate(softBytes, 80);
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.UpdateVersionMessage(lockUser, softBytes, softVersion), updateVersionCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.UpdateVersionMessage(lockUser, softBytes, softVersion), updateVersionCallback);
                }

                public void onFailed(String error) {
                    updateVersionCallback.onFailed(error);
                    DunyunSDK.this.destroy();
                }
            });
        }

    }

    public void updateVersionData(String date) {
        if(this.lockUpdate.isNext().booleanValue()) {
            byte[] newBytes = this.lockUpdate.getNextBytes();
            int currentLength = this.lockUpdate.getCurrentLength();
            this.SDKCallback.onSuccess(currentLength + "");
            LogUtil.d("程序的偏移量为：" + currentLength);
            bleBluetooth.sendData(BluetoothCode.UpdateVersionData(this.lockUser, newBytes, currentLength));
        } else {
            System.out.println("程序发送完毕，更新开始");
            this.updateVersionStart(date);
        }

    }

    public void updateVersionStart(String date) {
        this.lockUser.setCurrentTime(TimeStampUtil.getCurTimeStr());
        bleBluetooth.sendData(BluetoothCode.UpdateVersionStart(this.lockUser));
    }

    public void updateVersionGetResult(LockUser lockUser, final Callback<String> updatedResultsCallback) {
        if(bleBluetooth.isConnected() && this.connectedLocUser.getbleMac().equals(lockUser.getbleMac())) {
            this.sendData(BluetoothCode.GetUpdateResults(), updatedResultsCallback);
        } else {
            this.connectBLe(lockUser, new Callback<String>() {
                public void onSuccess(String data) {
                    DunyunSDK.this.sendData(BluetoothCode.GetUpdateResults(), updatedResultsCallback);
                }

                public void onFailed(String error) {
                    updatedResultsCallback.onFailed(error);
                    DunyunSDK.this.destroy();
                }
            });
        }

    }

    public String getTemporarypwd(LockUser lockUser, String time) {
        SharedPreference sh = new SharedPreference();
        int dd = Integer.parseInt(sh.getTimeCheck(mContext));
        int timeInt;
        if(time != null) {
            timeInt = (int)TimeStampUtil.getTimeStamp(time);
        } else {
            timeInt = (int)TimeStampUtil.getCurrentTimeStamp() + dd;
        }

        int timeUse = timeInt / 300;
        byte[] timeBytes = HexUtil.intToBytes(timeUse);
        byte index = (byte)(255 & lockUser.getUserIndex());
        byte[] password = HexUtil.HexString2Bytes(lockUser.getOpenLockPwd());
        byte[] time_pwd = HexUtil.MergeBytes(password, timeBytes);
        byte[] unencryptedBytes = new byte[8];
        unencryptedBytes[0] = index;

        for(int i = 0; i < time_pwd.length; ++i) {
            unencryptedBytes[1 + i] = time_pwd[i];
        }

        byte[] encryptedBytes = AesEncryptionUtil.encrypt(unencryptedBytes, HexUtil.HexString2Bytes(lockUser.getOpenPwdKey()));
        byte[] down4 = HexUtil.InterceptBytes(encryptedBytes, 12, encryptedBytes.length);
        long d = HexUtil.bytesToInt(down4);
        String usepwd = String.valueOf(d);
        System.out.println("得到的授权密码结果" + usepwd);
        String results = "";
        if(usepwd.length() > 8) {
            results = usepwd.substring(usepwd.length() - 8, usepwd.length());
        } else {
            if(usepwd.length() < 8) {
                int difference = 8 - usepwd.length();

                for(int i = 0; i < difference; ++i) {
                    usepwd = "0" + usepwd;
                }
            }

            results = usepwd;
        }

        return results;
    }


    public void setReback(JSONObject drebackjson, String code, String data) {
        try {
            drebackjson.put(code, data);
        } catch (JSONException var5) {
            var5.printStackTrace();
            System.out.println("setRebackd的JSONObject失败");
        }

    }

    public void setRebackFinsh(JSONObject data) {
        System.out.println("成功回调=" + data);
        this.receiveSavedData.clear();
        this.isRunning = 3;
        if(this.SDKCallback == null) {
            System.out.println("callback失败NULL");
        } else {
            try {
                if(data.get("result").equals("success")) {
                    this.SDKCallback.onSuccess(data.toString());
                } else {
                    this.SDKCallback.onFailed(data.toString());
                }
            } catch (JSONException var3) {
                var3.printStackTrace();
                System.out.println("setRebackd的JSONObject失败");
            }

        }
    }

    private void onFail(String error) {
        this.receiveSavedData.clear();
        this.isRunning = 3;
        if(this.SDKCallback == null) {
            System.out.println("onFail:callback失败NULL");
        } else {
            this.SDKCallback.onFailed(error);
        }
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

    public void PARSED_DATA(byte[] data) {
        dunyunHandler.removeCallbacks(this.receiveTimeoutRunnable);
        ParseData parseData = new ParseData();
        new JSONObject();
        parseData.init(this.addLockUser);
        DYLockDevice device = this.getCurrentDevice();
        JSONObject getJason = parseData.start(data, this.lockUser, this.consumTimeUtil.getConsumtime(), device, mContext);
        System.out.println("数据解析结果:" + getJason.toString());
        String LockTime = null;
        int type = parseData.getReSendType();
        if(type == 1) {
            bleBluetooth.sendData(BluetoothCode.getLockTime());
        } else if(type != 2 && type != 4) {
            if(type == 0) {
                ;
            }
        } else {
            try {
                LockTime = getJason.getString("Locktime");
                if(type == 2) {
                    bleBluetooth.sendData(BluetoothCode.sendSettingAgain(TimeStampUtil.getTimeStrToByte(LockTime)));
                } else {
                    this.updateVersionData(LockTime);
                }
            } catch (JSONException var8) {
                var8.printStackTrace();
            }
        }

        if(parseData.getIsFinish().booleanValue()) {
            this.setRebackFinsh(getJason);
        }

    }
}
