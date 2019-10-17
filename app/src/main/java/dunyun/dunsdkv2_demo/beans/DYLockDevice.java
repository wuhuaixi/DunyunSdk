package dunyun.dunsdkv2_demo.beans;

import android.bluetooth.BluetoothDevice;

/**
 * <DL>
 * <DD>蓝牙设备列表</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/21
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public class DYLockDevice {

    private String name;
    private String mac;
    private int rssi;
    private BluetoothDevice bluetoothDevice;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DYLockDevice that = (DYLockDevice) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
