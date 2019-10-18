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
import java.util.Iterator;
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
    public static final String TAG = "BleBluetooth";
    private static final int connectTimeout = 15000;
    private static final int discoverServiceTimeout = 7000;
    private static final int sendDataTimeout = 500;
    private static final int getServicesDelay = 20;
    private static final int receiveDataDelay = 250;
    private static boolean connected = false;
    private boolean all = false;
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
    private DYLockDevice dyLockDevice = new DYLockDevice();
    private BluetoothDevice currentDevice;
    private String DYLockMac;
    private Callback<List<DYLockDevice>> searchDevicesCallback;
    private ConnectCallback connectCallback;
    private Callback<byte[]> receiveDataCallback;
    private Callback<Integer> getRssiCallback;
    public static Handler mCommHandler = null;
    private byte[] nextPacket;
    BleBluetooth.SearchCallBackThread searchCallBackThread;
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.d(BleBluetooth.this.context, "onConnectionStateChange----status:" + status + "---newState:" + newState + "--connectingDevice-" + BleBluetooth.this.connectingDevice + "--connectNum-" + BleBluetooth.this.connectNum);
            if(BleBluetooth.this.connectingDevice && newState != 2 && BleBluetooth.this.connectNum < 5) {
                if(gatt != null) {
                    gatt.close();
                }

                BleBluetooth.this.reConnectDevice();
            } else if(newState == 2) {
                BleBluetooth.this.currentDevice = gatt.getDevice();
                BleBluetooth.this.dyLockDevice.setBluetoothDevice(BleBluetooth.this.currentDevice);
                BleBluetooth.this.dyLockDevice.setName(BleBluetooth.this.currentDevice.getName());
                BleBluetooth.this.dyLockDevice.setMac(BleBluetooth.this.currentDevice.getAddress());
                BleBluetooth.this.dyLockDevice.setRssi(0);
                LogUtil.d(BleBluetooth.this.context, "设备连接成功");
                BleBluetooth.mCommHandler.removeCallbacks(BleBluetooth.this.connectTimeoutRunnable);
                BleBluetooth.mCommHandler.post(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException var2) {
                            var2.printStackTrace();
                        }

                        BleBluetooth.this.discoverServices();
                    }
                });
                BleBluetooth.this.startDiscoverServiceTimer();
            } else if(newState == 0) {
                LogUtil.d(BleBluetooth.this.context, "设备连接断开");
                if(gatt != null) {
                    gatt.close();
                }

                BleBluetooth.this.close();
                BleBluetooth.mCommHandler.removeCallbacks(BleBluetooth.this.connectTimeoutRunnable);
                if(BleBluetooth.this.connectCallback != null) {
                    if(BleBluetooth.this.connectingDevice) {
                        BleBluetooth.this.connectCallback.onFailed(BleBluetooth.this.dyLockDevice, "连接失败" + BleBluetooth.this.DYLockMac);
                    } else {
                        BleBluetooth.this.connectDestroy();
                        BleBluetooth.this.connectCallback.onDisconnected(BleBluetooth.this.dyLockDevice);
                    }
                }

                BleBluetooth.this.refreshDeviceCache();
            }

        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtil.d(BleBluetooth.this.context, "onServicesDiscovered----status:" + status);
            if(status == 0) {
                LogUtil.d(BleBluetooth.this.context, "服务发现成功");
                BleBluetooth.mCommHandler.postDelayed(new Runnable() {
                    public void run() {
                        boolean isSuccess = BleBluetooth.this.getServices();
                        if(isSuccess) {
                            BleBluetooth.this.stopDiscoverServiceTimer();
                            BleBluetooth.this.setCharacteristicNotification(BleBluetooth.this.receiverBluetoothGattCharacteristic, true);
                            BleBluetooth.this.startNotificationTimer();
                        }

                    }
                }, 20L);
            } else {
                BleBluetooth.this.connectingDevice = true;
                LogUtil.d(BleBluetooth.this.context, "服务发现失败...");
                BleBluetooth.this.stopDiscoverServiceTimer();
            }

        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.d(BleBluetooth.this.context, "onCharacteristicRead----status:" + status);
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.d(BleBluetooth.this.context, "onCharacteristicWrite----status:" + status);
            if(BleBluetooth.this.nextPacket != null) {
                BleBluetooth.this.sendData(BleBluetooth.this.nextPacket);
            }

        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            LogUtil.d(BleBluetooth.this.context, "onCharacteristicChanged----");
            if(characteristic.getValue() != null) {
                byte[] data = characteristic.getValue();
                LogUtil.d(BleBluetooth.this.context, "Receive data:-----" + StrUtil.bytesToString(data));
                BleBluetooth.this.tempData.add(data);
                BleBluetooth.mCommHandler.removeCallbacks(BleBluetooth.this.receiveDataRunnable);
                BleBluetooth.mCommHandler.postDelayed(BleBluetooth.this.receiveDataRunnable, 250L);
            }

        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            LogUtil.d(BleBluetooth.this.context, "onDescriptorRead----status:" + status);
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            LogUtil.d(BleBluetooth.this.context, "onDescriptorWrite----status:" + status + "-descriptor-" + descriptor.getUuid().toString());
            if(BleBluetooth.CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid())) {
                BleBluetooth.this.onNotifSuccess(gatt);
            }

        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            LogUtil.d(BleBluetooth.this.context, "onReadRemoteRssi----rssi:" + rssi + "----status:" + status);
            BleBluetooth.this.dyLockDevice.setRssi(rssi);
            if(BleBluetooth.this.getRssiCallback != null) {
                BleBluetooth.this.getRssiCallback.onSuccess(Integer.valueOf(rssi));
            }

        }
    };
    private boolean connectingDevice = false;
    private int connectNum = 0;
    public static final int OPT_DISCOVERSERVICE = 4;
    public static final int OPT_NOTIFICATION = 5;
    Timer discoverServiceTimer = null;
    TimerTask discoverServiceTimerTask = null;
    private long discoverServiceTimerTaskTime = 5000L;
    private boolean discoverServiceCallbackSuccess = false;
    private int discoverServiceNum = 0;
    Timer notificationTimer = null;
    TimerTask notificationTimerTask = null;
    private long notificationTimerTaskTime = 1000L;
    private boolean notificationCallbackSuccess = false;
    private int notificationNum = 0;
    private ArrayList<byte[]> tempData = new ArrayList();
    private ArrayList<Byte> tempByteData = new ArrayList();
    Runnable receiveDataRunnable = new Runnable() {
        public void run() {
            if(BleBluetooth.this.tempData != null && BleBluetooth.this.tempData.size() > 0) {
                int j;
                for(int i = 0; i < BleBluetooth.this.tempData.size(); ++i) {
                    for(j = 0; j < ((byte[])BleBluetooth.this.tempData.get(i)).length; ++j) {
                        BleBluetooth.this.tempByteData.add(Byte.valueOf(((byte[])BleBluetooth.this.tempData.get(i))[j]));
                    }
                }

                byte[] data = new byte[BleBluetooth.this.tempByteData.size()];

                for(j = 0; j < data.length; ++j) {
                    data[j] = ((Byte)BleBluetooth.this.tempByteData.get(j)).byteValue();
                }

                BleBluetooth.this.receiveDataCallback.onSuccess(data);
                BleBluetooth.this.tempData.clear();
                BleBluetooth.this.tempByteData.clear();
            }

        }
    };
    Runnable connectTimeoutRunnable = new Runnable() {
        public void run() {
            LogUtil.d(BleBluetooth.this.context, "设备连接超时1");
            if(BleBluetooth.this.connectCallback != null) {
                BleBluetooth.this.connectCallback.onFailed(BleBluetooth.this.dyLockDevice, "设备连接失败");
                LogUtil.d(BleBluetooth.this.context, "设备连接超时2");
            }

            BleBluetooth.this.stopNotificationTimer();
            BleBluetooth.this.stopDiscoverServiceTimer();
            BleBluetooth.this.connectNum = 0;
            BleBluetooth.this.connectingDevice = false;
            BleBluetooth.this.discoverServiceNum = 0;
            BleBluetooth.this.notificationNum = 0;
            BleBluetooth.this.connectDestroy();
        }
    };
    private Handler searchCallBackHandler;

    public DYLockDevice getCurrentDevice() {
        return this.dyLockDevice;
    }

    public BleBluetooth(Context context) {
        this.initialize(context);
    }

    public boolean initialize(Context context) {
        mCommHandler = new Handler(context.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case 1:
                    case 2:
                    case 3:
                    default:
                        break;
                    case 4:
                        BleBluetooth.this.discoverServices();
                        break;
                    case 5:
                        BleBluetooth.this.setCharacteristicNotification(BleBluetooth.this.receiverBluetoothGattCharacteristic, true);
                }

            }
        };
        this.context = context;
        this.initScan();
        if(this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            if(this.mBluetoothManager == null) {
                LogUtil.e(context, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        if(this.mBluetoothAdapter == null) {
            LogUtil.e(context, "Unable to obtain a BluetoothAdapter.");
            return false;
        } else if(!this.mBluetoothAdapter.isEnabled()) {
            return false;
        } else {
            if(Build.VERSION.SDK_INT >= 21) {
                this.mBluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
            }

            LogUtil.d(context, "------Build.VERSION.SDK_INT-------" + Build.VERSION.SDK_INT);
            return true;
        }
    }

    @TargetApi(18)
    public void initScan() {
        this.searchedDevices = new ArrayList();
        if(Build.VERSION.SDK_INT >= 21) {
            if(this.scanCallback == null) {
                this.scanCallback = new ScanCallback() {
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        System.out.println("---SDK_INT21-find out device---");
                        if(Build.VERSION.SDK_INT >= 21) {
                            LogUtil.d(BleBluetooth.this.context, "rssi------" + result.getRssi() + "---name--" + result.getDevice().getName() + "----device--" + result.getDevice().getAddress());
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("scanResult", result);
                            Message msg = new Message();
                            msg.what = 1;
                            msg.setData(bundle);
                            if(BleBluetooth.this.searchCallBackHandler != null) {
                                BleBluetooth.this.searchCallBackHandler.sendMessage(msg);
                            }
                        }

                    }
                };
            }
        } else if(mLeScanCallback == null) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    System.out.println("----find out device---");
                    LogUtil.d(BleBluetooth.this.context, "rssi------" + rssi + "---name--" + device.getName() + "----device--" + device.getAddress());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("device", device);
                    bundle.putInt("rssi", rssi);
                    bundle.putByteArray("scanRecord", scanRecord);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.setData(bundle);
                    if(BleBluetooth.this.searchCallBackHandler != null) {
                        BleBluetooth.this.searchCallBackHandler.sendMessage(msg);
                    }

                }
            };
        }

    }

    private void addDevice(DYLockDevice lockDevice) {
        for(int i = 0; i < this.searchedDevices.size(); ++i) {
            if(((DYLockDevice)this.searchedDevices.get(i)).getName().equals(lockDevice.getName())) {
                this.searchedDevices.remove(i);
                break;
            }
        }

        this.searchedDevices.add(lockDevice);
        this.searchDevicesCallback.onSuccess(this.searchedDevices);
    }

    @TargetApi(18)
    public void startScan(Callback<List<DYLockDevice>> searchDevicesCallback, boolean all) {
        this.searchDevicesCallback = searchDevicesCallback;
        this.all = all;
        if(this.searchedDevices == null) {
            this.searchedDevices = new ArrayList();
        } else {
            this.searchedDevices.clear();
        }

        this.searchCallBackThread = new BleBluetooth.SearchCallBackThread();
        this.searchCallBackThread.start();
        if(Build.VERSION.SDK_INT >= 21) {
            this.mBluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
            if(this.scanCallback != null) {
                this.mBluetoothLeScanner.startScan(this.scanCallback);
                System.out.println("搜索设置的锁start----SDK_INT=21");
            } else {
                System.out.println("搜索蓝牙开启失败");
            }
        } else {
            this.mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

    }

    @TargetApi(18)
    public void stopScan() {
        LogUtil.d(this.context, "stopScanstopScanstopScan");
        if(Build.VERSION.SDK_INT >= 21) {
            if(this.mBluetoothLeScanner != null && this.scanCallback != null) {
                this.mBluetoothLeScanner.stopScan(this.scanCallback);
            }
        } else {
            this.mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        if(this.searchCallBackThread != null) {
            this.searchCallBackHandler.getLooper().quit();
            this.searchCallBackThread = null;
        } else {
            this.searchCallBackHandler = null;
        }

    }

    private void onNotifSuccess(final BluetoothGatt gatt) {
        LogUtil.d(this.context, "onNotifSuccess");
        connected = true;
        this.notificationCallbackSuccess = true;
        mCommHandler.post(new Runnable() {
            public void run() {
                BleBluetooth.this.stopNotificationTimer();
                BleBluetooth.this.stopDiscoverServiceTimer();
                BleBluetooth.this.connectNum = 0;
                BleBluetooth.this.connectingDevice = false;
                BleBluetooth.this.discoverServiceNum = 0;
                BleBluetooth.this.notificationNum = 0;
            }
        });
        mCommHandler.postDelayed(new Runnable() {
            public void run() {
                if(BleBluetooth.this.connectCallback != null) {
                    String name = gatt.getDevice().getName();
                    gatt.readRemoteRssi();
                    LogUtil.d(BleBluetooth.this.context, "--gatt-name-" + name + "--currentDevice=" + BleBluetooth.this.currentDevice.getName());
                    BleBluetooth.this.connectCallback.onSuccess(BleBluetooth.this.dyLockDevice);
                } else {
                    LogUtil.d(BleBluetooth.this.context, "调用的connectCallback已经被清空");
                    BleBluetooth.this.destroy();
                }

            }
        }, 10L);
    }

    public void discoverServices() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.discoverServices();
        } else {
            LogUtil.d(this.context, "  ");
        }

    }

    public void connectBLe(final String bleMac, ConnectCallback connectCallback, Callback<byte[]> receiveDataCallback) {
        this.destroy();
        this.stopScan();
        this.connectCallback = connectCallback;
        this.receiveDataCallback = receiveDataCallback;
        this.stopNotificationTimer();
        this.stopDiscoverServiceTimer();
        this.discoverServiceNum = 0;
        this.notificationNum = 0;
        this.connectNum = 0;
        this.connectingDevice = true;
        mCommHandler.post(new Runnable() {
            public void run() {
                BluetoothDevice device = BleBluetooth.this.mBluetoothAdapter.getRemoteDevice(bleMac);
                BleBluetooth.mBluetoothGatt = device.connectGatt(BleBluetooth.this.context, false, BleBluetooth.this.mGattCallback);
            }
        });
        mCommHandler.postDelayed(this.connectTimeoutRunnable, 15000L);
    }

    public void connectDevice(final DYLockDevice dyLockDevice, ConnectCallback connectCallback, Callback<byte[]> receiveDataCallback) {
        this.destroy();
        this.stopScan();
        this.dyLockDevice = dyLockDevice;
        this.connectCallback = connectCallback;
        this.receiveDataCallback = receiveDataCallback;
        this.DYLockMac = dyLockDevice.getBluetoothDevice().getAddress();
        this.stopNotificationTimer();
        this.stopDiscoverServiceTimer();
        this.discoverServiceNum = 0;
        this.notificationNum = 0;
        this.connectNum = 0;
        this.connectingDevice = true;
        mCommHandler.post(new Runnable() {
            public void run() {
                BluetoothDevice device = BleBluetooth.this.mBluetoothAdapter.getRemoteDevice(dyLockDevice.getBluetoothDevice().getAddress());
                BleBluetooth.mBluetoothGatt = device.connectGatt(BleBluetooth.this.context, false, BleBluetooth.this.mGattCallback);
            }
        });
        mCommHandler.postDelayed(this.connectTimeoutRunnable, 15000L);
    }

    private void reConnectDevice() {
        ++this.connectNum;
        mCommHandler.postDelayed(new Runnable() {
            public void run() {
                LogUtil.d("-----重连次数:" + BleBluetooth.this.connectNum + "----" + BleBluetooth.this.DYLockMac);
                if(BleBluetooth.this.connectingDevice && BleBluetooth.this.DYLockMac != null) {
                    BluetoothDevice device = BleBluetooth.this.mBluetoothAdapter.getRemoteDevice(BleBluetooth.this.DYLockMac);
                    BleBluetooth.mBluetoothGatt = device.connectGatt(BleBluetooth.this.context, false, BleBluetooth.this.mGattCallback);
                }

            }
        }, 1000L);
    }

    private void startDiscoverServiceTimer() {
        this.stopDiscoverServiceTimer();
        this.discoverServiceTimer = new Timer();
        this.discoverServiceTimerTask = new TimerTask() {
            public void run() {
                LogUtil.d("-----重新发现服务：-----------" + BleBluetooth.this.discoverServiceNum);
                if(BleBluetooth.this.discoverServiceNum > 3) {
                    BleBluetooth.this.stopDiscoverServiceTimer();
                    if(BleBluetooth.this.connectCallback != null) {
                        BleBluetooth.this.connectCallback.onFailed((DYLockDevice)null, "发现服务失败");
                    }

                    LogUtil.d(BleBluetooth.this.context, "未找到服务");
                    BleBluetooth.this.destroy();
                } else if(BleBluetooth.this.discoverServiceCallbackSuccess) {
                    BleBluetooth.this.discoverServiceNum = 0;
                    BleBluetooth.this.stopDiscoverServiceTimer();
                } else {
                    BleBluetooth.this.discoverServiceNum++;
                    BleBluetooth.mCommHandler.sendEmptyMessage(4);
                }

            }
        };
        this.discoverServiceTimer.schedule(this.discoverServiceTimerTask, this.discoverServiceTimerTaskTime, this.discoverServiceTimerTaskTime);
    }

    private void stopDiscoverServiceTimer() {
        if(this.discoverServiceTimerTask != null) {
            this.discoverServiceTimerTask.cancel();
        }

        if(this.discoverServiceTimer != null) {
            this.discoverServiceTimer.cancel();
        }

    }

    private void startNotificationTimer() {
        this.stopNotificationTimer();
        this.notificationTimer = new Timer();
        this.notificationTimerTask = new TimerTask() {
            public void run() {
                LogUtil.d("-----重新读取特征值：-----------" + BleBluetooth.this.notificationNum);
                if(BleBluetooth.this.notificationNum > 3) {
                    BleBluetooth.this.stopNotificationTimer();
                    if(BleBluetooth.this.connectCallback != null && BleBluetooth.this.DYLockMac != null) {
                        BleBluetooth.this.connectCallback.onFailed(BleBluetooth.this.dyLockDevice, "读取特征值失败");
                    }

                    BleBluetooth.this.destroy();
                } else if(BleBluetooth.this.notificationCallbackSuccess) {
                    BleBluetooth.this.stopNotificationTimer();
                } else {
                    BleBluetooth.this.notificationNum++;
                    BleBluetooth.mCommHandler.sendEmptyMessage(5);
                }

            }
        };
        this.notificationTimer.schedule(this.notificationTimerTask, this.notificationTimerTaskTime, this.notificationTimerTaskTime);
    }

    private void stopNotificationTimer() {
        if(this.notificationTimerTask != null) {
            this.notificationTimerTask.cancel();
        }

        if(this.notificationTimer != null) {
            this.notificationTimer.cancel();
        }

    }

    public boolean getServices() {
        if(mBluetoothGatt == null) {
            return false;
        } else {
            List<BluetoothGattService> bluetoothGattService = mBluetoothGatt.getServices();
            boolean isSuccess = false;
            Iterator var3 = bluetoothGattService.iterator();

            while(var3.hasNext()) {
                BluetoothGattService gattService = (BluetoothGattService)var3.next();
                if(serviceUuid.equals(gattService.getUuid())) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    Iterator var6 = gattCharacteristics.iterator();

                    while(var6.hasNext()) {
                        BluetoothGattCharacteristic gattCharacteristic = (BluetoothGattCharacteristic)var6.next();
                        if(characteristicUuid.equals(gattCharacteristic.getUuid())) {
                            this.receiverBluetoothGattCharacteristic = gattCharacteristic;
                        }

                        if(characteristicSendUuid.equals(gattCharacteristic.getUuid())) {
                            this.sendBluetoothGattCharacteristic = gattCharacteristic;
                        }
                    }

                    isSuccess = true;
                    break;
                }
            }

            return isSuccess;
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if(this.mBluetoothAdapter != null && mBluetoothGatt != null) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            if(descriptor != null) {
                LogUtil.d(this.context, "write descriptor----");
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            } else {
                LogUtil.d(this.context, "setCharacteristicNotification----service uuid未打开");
                this.connectCallback.onFailed((DYLockDevice)null, "service uuid未打开");
            }

        } else {
            LogUtil.d(this.context, "BluetoothAdapter not initialized");
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private void disconnect() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }

    }

    private void close() {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

    }

    public boolean refreshDeviceCache() {
        LogUtil.d(this.context, "清除蓝牙缓存...");
        if(mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if(localMethod != null) {
                    boolean bool = ((Boolean)localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception var4) {
                LogUtil.d(this.context, "清除蓝牙缓存出错...");
            }
        }

        return false;
    }

    public void destroy() {
        LogUtil.d(this.context, "BleBluetooth destroy");
        this.disconnect();
        this.DYLockMac = null;
        connected = false;
    }

    public void connectDestroy() {
        LogUtil.d(this.context, "BleBluetooth destroy");
        this.DYLockMac = null;
        connected = false;
    }

    public void readRemoteRssi(Callback<Integer> getRssiCallback) {
        this.getRssiCallback = getRssiCallback;
        if(mBluetoothGatt == null) {
            LogUtil.d(this.context, "mBluetoothGatt is null");
        } else {
            mBluetoothGatt.readRemoteRssi();
        }

    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(this.mBluetoothAdapter != null && mBluetoothGatt != null) {
            mBluetoothGatt.writeCharacteristic(characteristic);
        } else {
            LogUtil.d(this.context, "BluetoothAdapter not initialized");
        }
    }

    public void sendData(byte[] data) {
        byte[] sendData;
        if(data.length > 20) {
            sendData = new byte[20];
            this.nextPacket = new byte[data.length - 20];
            System.arraycopy(data, 0, sendData, 0, 20);
            System.arraycopy(data, 20, this.nextPacket, 0, data.length - 20);
        } else {
            sendData = data;
            this.nextPacket = null;
        }

        LogUtil.d(this.context, "sendData--" + StrUtil.bytesToString(sendData));
        this.sendBluetoothGattCharacteristic.setValue(sendData);
        this.sendBluetoothGattCharacteristic.setWriteType(1);
        this.writeCharacteristic(this.sendBluetoothGattCharacteristic);
    }

    public void stopAll() {
        this.stopNotificationTimer();
        this.stopDiscoverServiceTimer();
        this.connectNum = 0;
        this.connectingDevice = false;
        this.discoverServiceNum = 0;
        this.notificationNum = 0;
    }

    public boolean isWorking() {
        return this.connectingDevice;
    }

    class SearchCallBackThread extends Thread {
        SearchCallBackThread() {
        }

        public void run() {
            Looper.prepare();
            BleBluetooth.this.searchCallBackHandler = new Handler() {
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    switch(msg.what) {
                        case 0:
                            BluetoothDevice device = (BluetoothDevice)bundle.getParcelable("device");
                            int rssi = bundle.getInt("rssi");
                            byte[] scanRecord = bundle.getByteArray("scanRecord");
                            byte[] byteName = new byte[16];
                            System.arraycopy(scanRecord, 33, byteName, 0, 16);
                            String recordName = new String(byteName);
                            String deviceMacx;
                            DYLockDevice lockDevice;
                            String deviceNamex;
                            if(BleBluetooth.this.all) {
                                if(device.getName() != null) {
                                    deviceNamex = device.getName();
                                    deviceMacx = device.getAddress();
                                    if(recordName.startsWith("dy")) {
                                        deviceNamex = recordName.trim();
                                    }

                                    lockDevice = new DYLockDevice();
                                    lockDevice.setName(deviceNamex);
                                    if(deviceNamex != null && deviceNamex.length() > 10) {
                                        lockDevice.setMac(deviceMacx);
                                    }

                                    if(deviceNamex.substring(deviceNamex.length() - 2, deviceNamex.length() - 1).equals("B")) {
                                        lockDevice.setRssi(rssi);
                                        lockDevice.setBluetoothDevice(device);
                                        BleBluetooth.this.addDevice(lockDevice);
                                    }
                                }
                            } else if(device.getName() != null && device.getName().startsWith("dy")) {
                                deviceNamex = device.getName();
                                deviceMacx = device.getAddress();
                                if(recordName.startsWith("dy")) {
                                    deviceNamex = recordName.trim();
                                    lockDevice = new DYLockDevice();
                                    lockDevice.setName(deviceNamex);
                                    lockDevice.setMac(deviceMacx);
                                    lockDevice.setRssi(rssi);
                                    lockDevice.setBluetoothDevice(device);
                                    BleBluetooth.this.addDevice(lockDevice);
                                }
                            }
                            break;
                        case 1:
                            ScanResult result = (ScanResult)bundle.getParcelable("scanResult");
                            if(Build.VERSION.SDK_INT >= 21) {
                                ScanRecord record = result.getScanRecord();
                                String deviceName;
                                String deviceMac;
                                int deviceRssi;
                                BluetoothDevice devicex;
                                DYLockDevice lockDevicex;
                                if(BleBluetooth.this.all) {
                                    if(result.getDevice().getName() != null) {
                                        deviceName = result.getDevice().getName();
                                        deviceMac = result.getDevice().getAddress();
                                        deviceRssi = result.getRssi();
                                        devicex = result.getDevice();
                                        if(record != null && record.getDeviceName() != null && record.getDeviceName().startsWith("dy")) {
                                            deviceName = record.getDeviceName().trim();
                                        }

                                        lockDevicex = new DYLockDevice();
                                        lockDevicex.setName(deviceName);
                                        if(deviceName != null && deviceName.length() > 10) {
                                            lockDevicex.setMac(deviceMac);
                                        }

                                        if(deviceName.substring(deviceName.length() - 2, deviceName.length() - 1).equals("B")) {
                                            lockDevicex.setRssi(deviceRssi);
                                            lockDevicex.setBluetoothDevice(devicex);
                                            BleBluetooth.this.addDevice(lockDevicex);
                                        }
                                    }
                                } else if(result.getDevice().getName() != null && result.getDevice().getName().startsWith("dy")) {
                                    deviceName = result.getDevice().getName();
                                    deviceMac = result.getDevice().getAddress();
                                    deviceRssi = result.getRssi();
                                    devicex = result.getDevice();
                                    if(record != null && record.getDeviceName() != null && record.getDeviceName().startsWith("dy")) {
                                        deviceName = record.getDeviceName().trim();
                                    }

                                    lockDevicex = new DYLockDevice();
                                    lockDevicex.setName(deviceName);
                                    lockDevicex.setRssi(deviceRssi);
                                    lockDevicex.setMac(deviceMac);
                                    lockDevicex.setBluetoothDevice(devicex);
                                    BleBluetooth.this.addDevice(lockDevicex);
                                }
                            }
                    }

                }
            };
            Looper.loop();
        }
    }
}
