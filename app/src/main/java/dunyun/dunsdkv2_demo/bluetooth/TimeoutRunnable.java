package dunyun.dunsdkv2_demo.bluetooth;

import dunyun.dunsdkv2_demo.callback.Callback;
import dunyun.dunsdkv2_demo.callback.ListCallback;

/**
 * Created by chenzp on 2016/4/29.
 */
public class TimeoutRunnable implements Runnable{
    private Callback callback = null;
    private ListCallback listCallback = null;

    public TimeoutRunnable(Callback callback) {
        this.callback = callback;
    }

    public TimeoutRunnable(ListCallback listCallback) {
        this.listCallback = listCallback;
    }

    @Override
    public void run() {
        if(callback != null){
            callback.onFailed(com.psoft.bluetooth.bluetooth.BleConstant.TIMEOUT);
        }else if(listCallback != null){
            listCallback.onFailed(BleConstant.TIMEOUT);
        }
    }
}
