package dunyun.dunsdkv2_demo.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import dunyun.dunsdkv2_demo.beans.DYLockDevice;
import dunyun.dunsdkv2_demo.callback.Callback;
import dunyun.dunsdkv2_demo.callback.ConnectCallback;
import dunyun.dunsdkv2_demo.sdk.DunyunSDK;
import dunyun.dunsdkv2_demo.utils.Base64Util;
import dunyun.dunsdkv2_demo.utils.LogUtil;
import dunyun.dunsdkv2_demo.utils.StrUtil;

/**
 * <DL>
 * <DD>蓝牙Ble操作.</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/22
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public class BleBluetooth {
    public static final String TAG = BleBluetooth.TAG;

    /**
     * 连接超时时间
     */
    private static final int connectTimeout = 15 * 1000;
    /**
     * 发现服务超时时间
     */
    private static final int discoverServiceTimeout = 7 * 1000;
    /**
     * 发送数据超时时间
     */
    private static final int sendDataTimeout = 500;
    /**
     * 获取服务特征值延时
     */
    private static final int getServicesDelay = 20;
    /**
     * 接收数据延时
     */
    private static final int receiveDataDelay = 250;
//    private static final int receiveDataDelay = 400;
    /**
     * 是否连接
     */
    private static boolean connected = false;

    private Context context;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback scanCallback;
    private List<DYLockDevice> searchedDevices;

    private static BluetoothGatt mBluetoothGatt;

    protected static final UUID serviceUuid = UUID.fromString("0000ffb0-0000-1000-8000-00805f9b34fb");
    protected static final UUID characteristicUuid = UUID.fromString("0000ffb2-0000-1000-8000-00805f9b34fb");
    protected static final UUID characteristicSendUuid = UUID.fromString("0000ffb2-0000-1000-8000-00805f9b34fb");
    protected static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic receiverBluetoothGattCharacteristic = null;
    private BluetoothGattCharacteristic sendBluetoothGattCharacteristic = null;

    private static BluetoothAdapter.LeScanCallback mLeScanCallback;

    private DYLockDevice dyLockDevice;

    /**
     * 设备搜索回调
     */
    private Callback<List<DYLockDevice>> searchDevicesCallback;
    /**
     * 设备连接回调
     */
    private ConnectCallback connectCallback;
    /**
     * 接收数据回调
     */
    private Callback<byte[]> receiveDataCallback;
    /**
     * 读取信息值回调
     */
    private Callback<Integer> getRssiCallback;

    public static Handler mCommHandler = null;

    private byte[] nextPacket;

    private boolean all = false;

    public DYLockDevice getDYLockDevice() {
        return dyLockDevice;
    }

    public BleBluetooth(Context context) {
        initialize(context);
    }

    /***
     * 初始化设备
     *
     * @param context
     * @return
     */
    public boolean initialize(Context context) {
        mCommHandler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        break;
                    case OPT_DISCOVERSERVICE://发现服务
                        discoverServices();
                        break;
                    case OPT_NOTIFICATION://Notif
                        setCharacteristicNotification(receiverBluetoothGattCharacteristic, true);
                        break;
                }

            }
        };

        this.context = context;

        initScan();

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogUtil.e(context, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtil.e(context, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        LogUtil.d(context, "------Build.VERSION.SDK_INT-------" + Build.VERSION.SDK_INT);



        return true;
    }

    /**
     * 初始化扫描
     */
    @TargetApi(18)
    public void initScan() {
        searchedDevices = new ArrayList<DYLockDevice>();
        if (Build.VERSION.SDK_INT >= 21) {
            if (scanCallback == null) {
                scanCallback = new ScanCallback() {
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);

                        if (Build.VERSION.SDK_INT >= 21) {
                            LogUtil.d(context, "rssi------" + result.getRssi()
                                    + "---name--" + result.getDevice().getName()
                                    + "----device--" + result.getDevice().getAddress());

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("scanResult", result);
                            Message msg = new Message();
                            msg.what = 1;
                            msg.setData(bundle);
                            if (searchCallBackHandler != null) {
                                searchCallBackHandler.sendMessage(msg);
                            }
                        }

                    }
                };
            }
        } else {
            if (mLeScanCallback == null) {
                mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                        LogUtil.d(context, "rssi------" + rssi
                                + "---name--" + device.getName()
                                + "----device--" + device.getAddress());
////////////////////////////////////////////////////////////////////////////////////////////////////////
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("device", device);
                        bundle.putInt("rssi", rssi);
                        bundle.putByteArray("scanRecord", scanRecord);
                        Message msg = new Message();
                        msg.what = 0;
                        msg.setData(bundle);
                        if (searchCallBackHandler != null) {
                            searchCallBackHandler.sendMessage(msg);
                        }
////////////////////////////////////////////////////////////////////////////////////////
                    }
                };
            }
        }
    }

    private void addDevice(DYLockDevice lockDevice) {
        boolean isFind = false;
        for (int i = 0; i < searchedDevices.size(); i++) {
            if (searchedDevices.get(i).getName().equals(lockDevice.getName())) {
                isFind = true;
                searchedDevices.remove(i);
                break;
            }
        }

        if (!isFind) {
        searchedDevices.add(lockDevice);
        searchDevicesCallback.onSuccess(searchedDevices);
        }
    }

    SearchCallBackThread searchCallBackThread;

    /***
     * 扫描设备
     */
    @TargetApi(18)
    public void startScan(Callback<List<DYLockDevice>> searchDevicesCallback, boolean all) {
        this.searchDevicesCallback = searchDevicesCallback;
        this.all = all;
        if (searchedDevices == null) {
            searchedDevices = new ArrayList<DYLockDevice>();
        } else {
            searchedDevices.clear();
        }

        searchCallBackThread = new SearchCallBackThread();
        searchCallBackThread.start();

        if (Build.VERSION.SDK_INT >= 21) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (scanCallback != null) {
                mBluetoothLeScanner.startScan(scanCallback);
            }
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    /***
     * 停止扫描
     */
    @TargetApi(18)
    public void stopScan() {
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
            if (Build.VERSION.SDK_INT >= 21) {
                if (mBluetoothLeScanner != null && scanCallback != null) {
                    mBluetoothLeScanner.stopScan(scanCallback);
                }
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }

        if (searchCallBackThread != null) {
            searchCallBackHandler.getLooper().quit();
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.d(context, "onConnectionStateChange----status:" + status + "---newState:" + newState+"--connectingDevice-"+connectingDevice+"--connectNum-"+connectNum);
            //-status:133---newState:0 异常
            ///0>>>>>2 未连接到连接
            //0>>>>>0 断开连接
            //正在连接
            if (connectingDevice && (newState != BluetoothProfile.STATE_CONNECTED) && connectNum < 5) {
                if (gatt != null) {
                    gatt.close();
                }
                reConnectDevice();
            } else {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    LogUtil.d(context, "设备连接成功");
                    //1、取消连接超时
                    mCommHandler.removeCallbacks(connectTimeoutRunnable);
                    //TODO 一直获取服务，直到成功
                    //2、发现服务
                    mCommHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            discoverServices();
                        }
                    });
                    //开启定时任务，判断是否发现服务成功
                    startDiscoverServiceTimer();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    LogUtil.d(context, "设备连接断开");
                    if (gatt != null) {
                        gatt.close();
                    }
                    close();

                    //取消连接超时
                    mCommHandler.removeCallbacks(connectTimeoutRunnable);
                    if(connectCallback != null){
                        if(connectingDevice){
                            connectCallback.onFailed(dyLockDevice, "连接失败");
                        }else{
                            connectCallback.onDisconnected(dyLockDevice);
                        }
                    }
                    refreshDeviceCache();
                }
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            LogUtil.d(context, "onServicesDiscovered----status:" + status);
            //发现服务成功
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.d(context, "服务发现成功");
                //获取对应的特征值
                mCommHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSuccess = getServices();
                        if (isSuccess) {
                            //停止检测服务发现
                            stopDiscoverServiceTimer();
                            //读写特征值
                            setCharacteristicNotification(receiverBluetoothGattCharacteristic, true);
                            //开始定时任务，检测是否可以读写特征值
                            startNotificationTimer();
                        }
                    }
                }, getServicesDelay);
            }else{
                connectingDevice = true;
                LogUtil.d(context, "服务发现失败...");
                stopDiscoverServiceTimer();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.d(context, "onCharacteristicRead----status:" + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.d(context, "onCharacteristicWrite----status:" + status);
            if (nextPacket != null) {
                sendData(nextPacket);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            LogUtil.d(context, "onCharacteristicChanged----");
            if (characteristic.getValue() != null) {
                final byte[] data = characteristic.getValue();
                LogUtil.d(context, "Receive data:-----" + StrUtil.bytesToString(data));

                tempData.add(data);
                mCommHandler.removeCallbacks(receiveDataRunnable);
                mCommHandler.postDelayed(receiveDataRunnable, receiveDataDelay);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            LogUtil.d(context, "onDescriptorRead----status:" + status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            LogUtil.d(context, "onDescriptorWrite----status:" + status+"-descriptor-"+descriptor.getUuid().toString());
            if(CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid())){
                onNotifSuccess(gatt);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            LogUtil.d(context, "onReadRemoteRssi----rssi:" + rssi + "----status:" + status);
            getRssiCallback.onSuccess(rssi);
        }
    };

    private void onNotifSuccess(final BluetoothGatt gatt){
        LogUtil.d(context, "找到特征值,开始监听数据通道");
        connected = true;
        notificationCallbackSuccess = true;
        //停止Notification
        //清理资源
        mCommHandler.post(new Runnable() {
            @Override
            public void run() {
                stopNotificationTimer();
                stopDiscoverServiceTimer();
                connectNum = 0;
                connectingDevice = false;
                discoverServiceNum = 0;
                notificationNum = 0;
            }
        });

        mCommHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connectCallback != null && dyLockDevice != null) {
                    String name = gatt.getDevice().getName();
                    LogUtil.d(context, "----gatt-name------" + name);
                    if (name != null && name.length() == 16) {
                        String versionStr1 = Base64Util.getPosition(name.charAt(name.length() - 4)) + "";
                        String versionStr2 = Base64Util.getPosition(name.charAt(name.length() - 3)) + "";

                        int verserionInt1 = Integer.parseInt(versionStr1) * 10;
                        int verserionInt2 = Integer.parseInt(versionStr2);
                        int verserionInt = verserionInt1 + verserionInt2;
                        if (verserionInt >= 10) {
                            DunyunSDK.version = 100;
                        } else {
                            DunyunSDK.version = 10;
                        }
                    } else {
                        DunyunSDK.version = 100;
                        name = dyLockDevice.getName();
                        if(name != null  && name.length() == 16){
                            String versionStr1 = Base64Util.getPosition(name.charAt(name.length() - 4)) + "";
                            String versionStr2 = Base64Util.getPosition(name.charAt(name.length() - 3)) + "";

                            int verserionInt1 = Integer.parseInt(versionStr1) * 10;
                            int verserionInt2 = Integer.parseInt(versionStr2);
                            int verserionInt = verserionInt1 + verserionInt2;
                            if (verserionInt >= 10) {
                                DunyunSDK.version = 100;
                            } else {
                                DunyunSDK.version = 10;
                            }
                        }else{
                            DunyunSDK.version = 100;
                        }
                    }
                    connectCallback.onSuccess(dyLockDevice);
                }
            }
        }, 10);
    }
    /***
     * 发现服务
     */
    public void discoverServices() {
        if (mBluetoothGatt != null)
            mBluetoothGatt.discoverServices();
    }

    /***
     * 连接设备
     *
     * @param dyLockDevice
     * @param connectCallback
     */
    public void connectDevice(final DYLockDevice dyLockDevice, ConnectCallback connectCallback, Callback<byte[]> receiveDataCallback) {
        //1、连接前先断开之前的连接，
        //TODO 如果连接和之前的连接一样，直接返回连接成功
        destroy();
        //2、结束扫描操作
        //TODO 如果在扫描过程中，停止扫描
        stopScan();
        //3、开始连接设备
        this.dyLockDevice = dyLockDevice;
        this.connectCallback = connectCallback;
        this.receiveDataCallback = receiveDataCallback;

        stopNotificationTimer();
        stopDiscoverServiceTimer();
        discoverServiceNum = 0;
        notificationNum = 0;

        connectNum = 0;
        connectingDevice = true;

        mCommHandler.post(new Runnable() {
            @Override
            public void run() {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(dyLockDevice.getBluetoothDevice().getAddress());
                mBluetoothGatt = device.connectGatt(context, false, mGattCallback);//TODO port
            }
        });

        //5、设置连接超时，超时则返回失败
        mCommHandler.postDelayed(connectTimeoutRunnable, connectTimeout);
    }

    /**
     * 正在连接
     */
    private boolean connectingDevice = false;
    /**
     * 连接次数
     */
    private int connectNum = 0;

    private void reConnectDevice() {
        connectNum++;
        mCommHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("-----重连次数：-----------" + connectNum);
                if (connectingDevice) {
                    if(dyLockDevice != null && dyLockDevice.getBluetoothDevice() != null){
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(dyLockDevice.getBluetoothDevice().getAddress());
                        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);//TODO port
                    }
                }
            }
        }, 1000);
    }

    /**
     * 发现服务
     */
    public static final int OPT_DISCOVERSERVICE = 4;
    /**
     * notification服务
     */
    public static final int OPT_NOTIFICATION = 5;

    Timer discoverServiceTimer = null;
    TimerTask discoverServiceTimerTask = null;

    private long discoverServiceTimerTaskTime = 5 * 1000;
    /**
     * 发现服务回调
     */
    private boolean discoverServiceCallbackSuccess = false;
    /**
     * 发现服务次数
     */
    private int discoverServiceNum = 0;

    /**
     * 发现服务
     */
    private void startDiscoverServiceTimer() {
        stopDiscoverServiceTimer();
        discoverServiceTimer = new Timer();
        discoverServiceTimerTask = new TimerTask() {
            @Override
            public void run() {
                LogUtil.d("-----重新发现服务：-----------" + discoverServiceNum);
                if (discoverServiceNum > 3) {//超时次数
                    stopDiscoverServiceTimer();
                    if (connectCallback != null && dyLockDevice != null) {
                        connectCallback.onFailed(dyLockDevice, "发现服务失败");
                    }
                    LogUtil.d(context, "未找到服务");
                    //清理资源
                    destroy();
                } else {
                    if (discoverServiceCallbackSuccess) {
                        discoverServiceNum = 0;
                        stopDiscoverServiceTimer();
                    } else {
                        discoverServiceNum++;
                        mCommHandler.sendEmptyMessage(OPT_DISCOVERSERVICE);
                    }
                }
            }
        };

        discoverServiceTimer.schedule(discoverServiceTimerTask, discoverServiceTimerTaskTime, discoverServiceTimerTaskTime);
    }

    /**
     * 停止发现服务
     */
    private void stopDiscoverServiceTimer() {
        if (discoverServiceTimerTask != null) {
            discoverServiceTimerTask.cancel();
        }
        if (discoverServiceTimer != null) {
            discoverServiceTimer.cancel();
        }
    }

    Timer notificationTimer = null;
    TimerTask notificationTimerTask = null;
    private long notificationTimerTaskTime = 1 * 1000;
    /**
     * 发现服务回调
     */
    private boolean notificationCallbackSuccess = false;

    /**
     * notif次数
     */
    private int notificationNum = 0;
    /**
     * notifi
     */
    private void startNotificationTimer() {
        stopNotificationTimer();
        notificationTimer = new Timer();
        notificationTimerTask = new TimerTask() {
            @Override
            public void run() {
                LogUtil.d("-----重新读取特征值：-----------" + notificationNum);
                if(notificationNum > 3){
                    stopNotificationTimer();
                    if (connectCallback != null && dyLockDevice != null) {
                        connectCallback.onFailed(dyLockDevice,"读取特征值失败");
                    }
                    //清理资源
                    destroy();
                }else{
                    if (notificationCallbackSuccess) {
                        stopNotificationTimer();
                    } else {
                        notificationNum++;
                        mCommHandler.sendEmptyMessage(OPT_NOTIFICATION);
                    }
                }
            }
        };
        notificationTimer.schedule(notificationTimerTask, notificationTimerTaskTime, notificationTimerTaskTime);
    }

    /**
     * 停止发现notif
     */
    private void stopNotificationTimer() {
        if (notificationTimerTask != null) {
            notificationTimerTask.cancel();
        }
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
    }

    /***
     * 获取服务特征值
     *
     * @return
     */
    public boolean getServices() {
        if (mBluetoothGatt == null)
            return false;
        List<BluetoothGattService> bluetoothGattService = mBluetoothGatt.getServices();
        boolean isSuccess = false;

        for (BluetoothGattService gattService : bluetoothGattService) {
            LogUtil.d(context, "-->service uuid:" + gattService.getUuid());
            if (serviceUuid.equals(gattService.getUuid())) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    LogUtil.d(context, "---->char uuid:" + gattCharacteristic.getUuid());
                    if (characteristicUuid.equals(gattCharacteristic.getUuid())) {
                        receiverBluetoothGattCharacteristic = gattCharacteristic;
                    }
                    if (characteristicSendUuid.equals(gattCharacteristic.getUuid())) {
                        sendBluetoothGattCharacteristic = gattCharacteristic;
                    }
                }
                isSuccess = true;
                break;
            }
        }
        return isSuccess;
    }

    /***
     * 设置Notif
     *
     * @param characteristic
     * @param enabled
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.d(context, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic
                .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            LogUtil.d(context, "write descriptor----");
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * 设备是否连接
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 断开连接
     */
    private void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    /***
     * 关闭连接
     */
    private void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    public boolean refreshDeviceCache() {
        LogUtil.d(context, "清除蓝牙缓存...");
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod(
                        "refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(
                            localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                LogUtil.d(context, "清除蓝牙缓存出错...");
            }
        }
        return false;
    }

    /**
     * 清理资源
     */
    public void destroy() {
        LogUtil.d(context, "BleBluetooth destroy");
        disconnect();
        dyLockDevice = null;
        connected = false;
    }

    /**
     * 清理资源
     */
    public void connectDestroy() {
        LogUtil.d(context, "BleBluetooth destroy");
        dyLockDevice = null;
        connected = false;
    }

    /***
     * 读取信号值
     */
    public void readRemoteRssi(Callback<Integer> getRssiCallback) {
        this.getRssiCallback = getRssiCallback;
        if (mBluetoothGatt == null) {
            LogUtil.d(context, "mBluetoothGatt is null");
        } else {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.d(context, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /***
     * 发送数据
     *
     * @param data
     */
    public void sendData(byte[] data) {
        byte[] sendData = null;
        if (data.length > 20) {
            sendData = new byte[20];
            nextPacket = new byte[data.length - 20];
            System.arraycopy(data, 0, sendData, 0, 20);
            System.arraycopy(data, 20, nextPacket, 0, data.length - 20);
        } else {
            sendData = data;
            nextPacket = null;
        }

        LogUtil.d(context, "sendData--" + StrUtil.bytesToString(sendData));
        sendBluetoothGattCharacteristic.setValue(sendData);
        sendBluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        writeCharacteristic(sendBluetoothGattCharacteristic);
        //TODO 超时设置
    }

    private ArrayList<byte[]> tempData = new ArrayList<byte[]>();
    private ArrayList<Byte> tempByteData = new ArrayList<Byte>();

    /***
     * 接收数据
     */
    Runnable receiveDataRunnable = new Runnable() {

        public void run() {
            if (tempData != null && tempData.size() > 0) {
                for (int i = 0; i < tempData.size(); i++) {
                    for (int j = 0; j < tempData.get(i).length; j++) {
                        tempByteData.add(tempData.get(i)[j]);
                    }
                }

                byte[] data = new byte[tempByteData.size()];
                for (int j = 0; j < data.length; j++) {
                    data[j] = tempByteData.get(j);
                }
                receiveDataCallback.onSuccess(data);

                tempData.clear();
                tempByteData.clear();
            }
        }
    };

    // 连接超时线程
    Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(context, "设备连接超时");
            //回调方法
            if (connectCallback != null && dyLockDevice != null) {
                connectCallback.onFailed(dyLockDevice, "设备连接失败");
            }
            stopNotificationTimer();
            stopDiscoverServiceTimer();
            connectNum = 0;
            connectingDevice = false;
            discoverServiceNum = 0;
            notificationNum = 0;
            //清理资源
            //TODO 没有断开gatt
            connectDestroy();
        }
    };

    public void stopAll(){
        stopNotificationTimer();
        stopDiscoverServiceTimer();
        connectNum = 0;
        connectingDevice = false;
        discoverServiceNum = 0;
        notificationNum = 0;
    }

    private Handler searchCallBackHandler;

    class SearchCallBackThread extends Thread {

        @Override
        public void run() {

            Looper.prepare();
            searchCallBackHandler = new Handler() {
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();

                    switch (msg.what) {
                        case 1:
                            ScanResult result = bundle.getParcelable("scanResult");
                            if (Build.VERSION.SDK_INT >= 21) {
                                ScanRecord record = result.getScanRecord();
                                if (all) {
                                    if (result.getDevice().getName() != null) {

                                        String deviceName = result.getDevice().getName();
                                        String deviceMac = result.getDevice().getAddress();
                                        int deviceRssi = result.getRssi();
                                        BluetoothDevice device = result.getDevice();
                                        if (record != null && record.getDeviceName() != null && record.getDeviceName().startsWith("dy")) {
                                            deviceName = record.getDeviceName().trim();
                                        }

                                        DYLockDevice lockDevice = new DYLockDevice();
                                        lockDevice.setName(deviceName);
                                        if (deviceName != null && deviceName.length() > 10) {
                                            lockDevice.setMac(deviceName.substring(2, 10));
                                        }
                                        lockDevice.setRssi(deviceRssi);
                                        lockDevice.setBluetoothDevice(device);

                                        addDevice(lockDevice);
                                    }
                                } else {
                                    if (result.getDevice().getName() != null && result.getDevice().getName().startsWith("dy")) {

                                        String deviceName = result.getDevice().getName();
                                        String deviceMac = result.getDevice().getAddress();
                                        int deviceRssi = result.getRssi();
                                        BluetoothDevice device = result.getDevice();

                                        if (record != null && record.getDeviceName() != null && record.getDeviceName().startsWith("dy")) {
                                            deviceName = record.getDeviceName().trim();
                                        }

                                        DYLockDevice lockDevice = new DYLockDevice();
                                        lockDevice.setName(deviceName);
                                        if (deviceName != null && deviceName.length() > 10) {
                                            lockDevice.setMac(deviceName.substring(2, 10));
                                        }
                                        lockDevice.setRssi(deviceRssi);
                                        lockDevice.setBluetoothDevice(device);

                                        addDevice(lockDevice);
                                    }
                                }
                            }

                            break;
                        case 0:
                            BluetoothDevice device = bundle.getParcelable("device");
                            int rssi = bundle.getInt("rssi");
                            byte[] scanRecord = bundle.getByteArray("scanRecord");

                            byte[] byteName = new byte[16];
                            System.arraycopy(scanRecord, 33, byteName, 0, 16);
                            String recordName = new String(byteName);
//                        LogUtil.d("scanCallback", "scanRecord------"+ new String(byteName));
                            if (all) {
                                if (device.getName() != null) {
                                    String deviceName = device.getName();
                                    String deviceMac = device.getAddress();
                                    int deviceRssi = rssi;
                                    if (recordName.startsWith("dy")) {
                                        deviceName = recordName.trim();
                                    }

                                    DYLockDevice lockDevice = new DYLockDevice();
                                    lockDevice.setName(deviceName);
                                    if (deviceName != null && deviceName.length() > 10) {
                                        lockDevice.setMac(deviceName.substring(2, 10));
                                    }
                                    lockDevice.setRssi(deviceRssi);
                                    lockDevice.setBluetoothDevice(device);

                                    addDevice(lockDevice);
                                }
                            } else {
                                if (device.getName() != null && device.getName().startsWith("dy")) {
                                    String deviceName = device.getName();
                                    String deviceMac = device.getAddress();
                                    int deviceRssi = rssi;
                                    if (recordName.startsWith("dy")) {
                                        deviceName = recordName.trim();
                                    }

                                    DYLockDevice lockDevice = new DYLockDevice();
                                    lockDevice.setName(deviceName);
                                    if (deviceName != null && deviceName.length() > 10) {
                                        lockDevice.setMac(deviceName.substring(2, 10));
                                    }
                                    lockDevice.setRssi(deviceRssi);
                                    lockDevice.setBluetoothDevice(device);

                                    addDevice(lockDevice);
                                }
                            }

                            break;
                    }
                }
            };
            Looper.loop();//4、启动消息循环
        }
    }

    public boolean isWorking(){
        return connectingDevice;
    }
}
