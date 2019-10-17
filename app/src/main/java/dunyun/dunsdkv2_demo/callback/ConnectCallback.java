package dunyun.dunsdkv2_demo.callback;


import dunyun.dunsdkv2_demo.beans.DYLockDevice;

/**
 * <DL>
 * <DD>连接回调.</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/21
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public interface ConnectCallback {

    /**连接成功*/
    void onSuccess(DYLockDevice device);

    /**连接失败*/
    void onFailed(DYLockDevice device, String reason);

    /**发现服务失败*/
    void onDescoverServiceFailed(DYLockDevice device);

    /**接收数据*/
    void onDataReceive(byte[] data);

    /**连接断开*/
    void onDisconnected(DYLockDevice device);
}
