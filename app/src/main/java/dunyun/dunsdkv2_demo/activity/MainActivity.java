package dunyun.dunsdkv2_demo.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.psoft.bluetooth.bluetooth.DunyunSDKv2;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dunyun.dunsdkv2_demo.R;
import dunyun.dunsdkv2_demo.beans.DYLockDevice;
import dunyun.dunsdkv2_demo.beans.LockInfo;
import dunyun.dunsdkv2_demo.beans.LockUser;
import dunyun.dunsdkv2_demo.callback.Callback;
import dunyun.dunsdkv2_demo.datebase.SharedPreference;
import dunyun.dunsdkv2_demo.sdk.DunyunSDK;
import dunyun.dunsdkv2_demo.utils.CrcUtil;
import dunyun.dunsdkv2_demo.utils.DialogUtil;
import dunyun.dunsdkv2_demo.utils.LogUtil;
import dunyun.dunsdkv2_demo.utils.TimeStampUtil;

public class MainActivity extends MPermissionsActivity {
    private String TAG="MainActivity";
    private DunyunSDK dunyunSDKv2;//初始化一个SDK的对象
    private DunyunSDKv2 ddddddddd;

    private boolean isSearching = false;//判断蓝牙是否在搜索中
    private LockUser testLockUser = new LockUser();//初始化一个用户信息，
    private boolean isRunning = false;//蓝牙在运行中
    private TextView verificationCode;

    List<DYLockDevice> listData;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Context mcontext;
    private String companyID;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);
        mcontext = MainActivity.this;
        //读取保存下来的添加好了的钥匙和用户信息，如果未保存由开发者传入
        SharedPreference sharedPreference = new SharedPreference();
        //testLockUser = sharedPreference.getKeyMessage(mcontext);
        //初始化蓝牙，很重要
        dunyunSDKv2 = DunyunSDK.getInstance(this);
        initView();//listview的初始化
        bluetoothIsOpen();//判断蓝牙是否开启，如果在关闭蓝牙状态下使用蓝牙，程序会崩溃
        byte[] dddd={0x55,0x73,0x56,0x22,(byte)0x96,0x34};

        testLockUser.setOpenLockPwd("000000");
        testLockUser.setUserId("13594888143");
        testLockUser.setOpenPwdKey("0000000000000000000000000000000000000");
        testLockUser.setUserIndex(15);


    }


    private void showDialog(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtil.showDialog(mcontext, data);
            }
        });
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return
     */
    private boolean bluetoothIsOpen() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) this
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                // LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            //LogUtil.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            // 请求打开 Bluetooth
            Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 请求开启 Bluetooth
            this.startActivityForResult(requestBluetoothOn, 1);
            return false;
        }
        return true;
    }

    private Button btn_record;

    private void initView() {
        /**
         * 获取蓝牙权限和位置权限，注：android6.0及其以上，没有位置权限无法添加钥匙
         */
        boolean b = checkPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
        if (!b) {
            requestPermission(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        companyID="2becde9154824a3aa7c226393c916298";

        listData = new ArrayList<DYLockDevice>();
        ListView listBlue=(ListView) findViewById(R.id.listBlue);
        verificationCode = (TextView) findViewById(R.id.VerificationCode);
    }

    public void getCurrentDevice() {
        //供开发者使用获取当前连接的device的信息
       //DYLockDevice bluetoothDevice = dunyunSDKv2.getCurrentDevice();
        //bluetoothDevice.getName();//当前的地址
       // bluetoothDevice.getName();//当前的名字
    }

    //
    public void toastMsg(final String message) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mcontext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 搜索设备
     */
    private void stop() {
        dunyunSDKv2.stopSearchDevices();
        isRunning = false;
        isSearching = false;
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
        LogUtil.e("关闭搜索");

    }

    private void start() {
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    //displayDevice();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void displayDevice() {
//        MainActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                 = new DeviceAdapter(mcontext, listData, R.layout.item_device);
//                deviceAdapter.setData(listData);
//                listView.setAdapter(deviceAdapter);
//            }
//        });
    }

    public void search(View view) {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
        if (!isSearching) {
            isSearching = true;
            start();
            dunyunSDKv2.startSearchDevices(10*1000,new Callback<List<DYLockDevice>>() {
                @Override
                public void onSuccess(List<DYLockDevice> data) {
                    for(int i=0;i<data.size();i++)
                    {
                        Log.e(TAG,"name ="+data.get(i).getName());
                        Log.e(TAG,"name ="+data.get(i).getMac());
                    }
                    listData = data;
                }

                @Override
                public void onFailed(String error) {
                    stop();
                    toastMsg(error);
                }
            });
        } else {
            stop();
        }
    }


    /***
     * 蓝牙开始操作的检查
     *
     * @return
     */
    private Boolean BleBeginCheck() {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return false;
        } else {
            //testLockUser.setCurrentTime(TimeStampUtil.getCurrentTimeBytes());
        }
        return true;
    }

    /**
     * 固件刷新       注：固件刷新由于没有更新一个版本的固件，所有暂时只有测试功能，未有实际的编译功能
     */
    public void updateVersion(View view) {
        byte[] bytes = getBytes();//获取更新的数据流
        String version = "3.25.09";//中间必须是用小数点隔开，否则将不执行，用来辨别是否更新成功
        version = "0." + version;//如果只有两个小数点，需要在前面加一个小数点
        if (BleBeginCheck()) {
//            dunyunSDKv2.updateVersion(testLockUser, bytes, version, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    LogUtil.d(data);
//
//                    DialogUtil.showDialog(mcontext, data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg(error);
//                }
//            });

        }
    }

    /***
     * 获取固件刷新的数据流
     */
    public byte[] getBytes() {
        byte[] buffer = null;
        try {
            //得到资源中的Raw数据流
            InputStream in = getResources().openRawResource(R.raw.t0655);
            //得到数据的大小
            int length = in.available();
            buffer = new byte[length];
            //读取数据
            in.read(buffer);
            //关闭
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /***
     * 固件刷新  获取固件刷新的更新结果
     *
     * @param view
     */
    public void updateVersionResult(View view) {
        //此处的testLockUser  只要MAC地址不为空就可以的
        if (BleBeginCheck()) {
//            dunyunSDKv2.updateVersionGetResult(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg(error);
//                }
//            });
        }
    }

    /***
     * 获取序列号
     */
    public void serialnum(View view) {
        if (BleBeginCheck()) {
//            dunyunSDKv2.GetSerialNum(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, "序列号:" + data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg(error);
//                }
//            });
        }
    }

    /***
     * 断开连接
     * 注：可以不再使用，直接destr
     *
     * @param view
     */
    public void disconnt(View view) {

    }


    /**
     * 获取锁内用户
     */
    public void getusers(View view) {
        if (BleBeginCheck()) {
//            dunyunSDKv2.getLockUsers(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    //获取用户信息成功
//                    ArrayList<String> keyList = getKeyList(data);
//
//                    String allList = "";
//                    if (keyList.size() > 0) {
//                        for (int i = 0; i < keyList.size(); i++) {
//                            String[] recordTypes = keyList.get(i).split(",");
//                            allList += keyList.get(i) + "\n";
//                        }
//                    }
//                    DialogUtil.showDialog(mcontext, "成功\n" + allList);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    DialogUtil.showDialog(mcontext, "" + error);
//                }
//            });
        }
    }

    /***
     * 添加钥匙
     *
     * @param view
     */
    public void adduser(View view) {
        // mo  co   mac   时间写在SDK里面
        //只需要传入  mobile 就可以添加钥匙了  mac地址可以传可以不传
        dunyunSDKv2.addLockUser(testLockUser, new Callback<String>() {
            @Override
            public void onSuccess(String data) {

            }

            @Override
            public void onFailed(String error) {

            }
        });
    }

    /***
     * 读取锁内时间
     *
     * @param view
     */
    public void GetLockTime(View view) {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
        testLockUser.setCurrentTime(TimeStampUtil.getCurrentTimeBytes());
        dunyunSDKv2.GetLockTime(testLockUser, new Callback<String>() {

            @Override
            public void onSuccess(String data) {

            }

            @Override
            public void onFailed(String error) {

            }
        });

    }

    /***
     * 更新锁内时间
     *
     * @param view
     */
    public void updateTime(View view) {

        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
        //这一句的作用设置需要将锁内时间更新到什么时候
        //testLockUser.setCurrentTime(TimeStampUtil.getTimeStrToByte("19700101000000"));//这里使用的系统时间，最好以服务器时间为准
        //testLockUser.setCurrentTime(TimeStampUtil.getCurrentTimeBytes());
        String date = "20170101010101";
        //时间格式为空会采用sdk内部逻辑更新时间，不为null由传入的日期为准
        dunyunSDKv2.updateTime(testLockUser, "null", new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                DialogUtil.showDialog(mcontext, "更改成功" + data);
            }

            @Override
            public void onFailed(String error) {
                DialogUtil.showDialog(mcontext, "更改失败," + error);
            }
        });
    }

    /***
     * 开锁
     * 开锁后会得到锁内时间和锁电量
     *
     * @param view
     */
    public void openlock(View view) {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
        //封装好的sdk调用
        dunyunSDKv2.openLock(testLockUser,new Callback<String>(){
            @Override
            public void onSuccess(String data) {

            }

            @Override
            public void onFailed(String error) {

            }
        });
    }

    /***
     * 删除用户
     *
     * @param view
     */
    public void deluser(View view) {


    }

    /***
     * 获取电量
     *
     * @param view
     */
    public void getbatterypower(View view) {
    }

    /***
     * 修改密码
     *
     * @param view
     */
    public void updatepasswd(View view) {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
        testLockUser.setCurrentTime(TimeStampUtil.getCurrentTimeBytes());
        dunyunSDKv2.updateOpenLockPwd(testLockUser,"041806",new Callback<String>() {

            @Override
            public void onSuccess(String data) {

            }

            @Override
            public void onFailed(String error) {

            }
        });
    }
    /***
     * 读取锁状态
     *
     * @param view
     */
    public void readLockStatus(View view) {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }

    }

    /***
     * 读取使能状态
     *
     * @param view
     */
    public void readStatus(View view) {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
//        testLockUser.setCurrentTime(TimeStampUtil.getCurrentTimeBytes());
//        dunyunSDKv2.ReadEnable(testLockUser, new Callback<String>() {
//            @Override
//            public void onSuccess(String data) {
//                DialogUtil.showDialog(mcontext, "成功:" + data);
//                //从左到右依次为：1、密码开锁使能；2、添加钥匙使能；3、删除钥匙使能；4、回复出厂设置使能；5、电量获取使能；6、状态获取使能；7-16为预留的位置展示不用
//                //ll_status.setVisibility(View.VISIBLE);
//                //displayCheckbox(data);
//            }
//
//            @Override
//            public void onFailed(String error) {
//                DialogUtil.showDialog(mcontext, "失败:" + error);
//            }
//        });
    }

    /***
     * 开启使能
     *
     * @param view
     */
    public void open_enable(View view) {
        if (dunyunSDKv2 != null && !dunyunSDKv2.bluetoothIsOpen()) {
            toastMsg("蓝牙未开启");
            return;
        }
//        testLockUser.setCurrentTime(TimeStampUtil.getCurrentTimeBytes());
//        String enable = "1000010000000000";
//        //从左到右依次为：1、密码开锁使能；2、添加钥匙使能；3、删除钥匙使能；4、回复出厂设置使能；5、电量获取使能；6、状态获取使能；7-16为预留的位置展示不用
//        dunyunSDKv2.openEnable(testLockUser, enable, new Callback<String>() {
//            @Override
//            public void onSuccess(String data) {
//                DialogUtil.showDialog(mcontext, "成功:" + data.toString());
//            }
//
//            @Override
//            public void onFailed(String error) {
//                DialogUtil.showDialog(mcontext, "失败" + error);
//            }
//        });

    }

    /***
     * 关闭使能
     *
     * @param view
     */
    public void closeEnable(View view) {

        String enable = "1000010000000000";
        //从左到右依次为：1、密码开锁使能；2、添加钥匙使能；3、删除钥匙使能；4、回复出厂设置使能；5、电量获取使能；6、状态获取使能；7-16为预留的位置展示不用
//        if (BleBeginCheck()) {
//            dunyunSDKv2.closeEnable(testLockUser, enable, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, "成功:" + data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    DialogUtil.showDialog(mcontext, "失败" + error);
//                }
//            });
//        }
    }

    /***
     * 开关门记录
     *
     * @param
     */
    public void records(View view) {
//        if (BleBeginCheck()) {
//            dunyunSDKv2.readRecords(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
////                    DBrecords dBhelper=new DBrecords(getApplicationContext());
////                    dBhelper.open();
//                    //dBhelper.updateContact(2,"time","type","operator","by_operator");
//                    ArrayList<String> keyList = getKeyList(data);
//                    String allList = "时间：类型：电话：内容：操作码：索引号\n";
//                    if (keyList.size() > 0) {
//                        for (int i = 0; i < keyList.size(); i++) {
//                            String[] recordTypes = keyList.get(i).split(",");
//                            //String recordType = OpcodeUtil.getRecordOperation(Integer.parseInt(recordTypes[1]));
//                            allList += keyList.get(i) + "\n";
//                            //dBhelper.insertContact(recordTypes[0],recordType,recordTypes[2],recordTypes[3]);
//                            //dBhelper.insertContact("dddd","ddd","无语","悲伤");
//                        }
//                    }
//                    // dBhelper.close();
//                    DialogUtil.showDialog(mcontext, "成功\n" + allList);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    DialogUtil.showDialog(mcontext, "失败", error);
//                }
//            });
//        }

    }

    private ArrayList<String> getKeyList(String json) {
        ArrayList<String> keyList = new ArrayList<String>();
        String iteratorkey = null;
        //System.out.println("门锁记录："+json);
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator iterator = jsonObject.keys();
            int i = 0;
            while (iterator.hasNext()) {
                iteratorkey = iterator.next().toString();
                if (i++ > 2) {
                    if (iteratorkey.equals("isFinish")) {
                        System.out.println("门锁记录是否获取完全" + jsonObject.get(iteratorkey).toString());
                    } else {
                        System.out.println(jsonObject.get(iteratorkey));
                        keyList.add(jsonObject.get(iteratorkey).toString());
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return keyList;
    }

    /***
     * 蓝牙断开
     *
     * @param view
     */
    public void disconnect(View view) {
        if (dunyunSDKv2 != null) {
            dunyunSDKv2.destroy();
        }
    }

    /***
     * 待定
     *
     * @param view
     */
    public void getAdminID(View view) {
//        if(BleBeginCheck())
//        {
//            dunyunSDKv2.getAdminID(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, "成功", data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg("断开失败，" + error);
//                }
//            });
//        }
    }


    /***
     * 0
     * 待定
     *
     * @param view
     */
    public void settingAdmin(View view) {
//        if(BleBeginCheck())
//        {
//            dunyunSDKv2.settingAdminID(testLockUser, 0, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, "成功", data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg("断开失败，" + error);
//                }
//            });
//        }
    }


    /***
     * 读取锁的硬件版本号
     *
     * @param view
     */
    public void getLockVersion(View view) {
//        if (BleBeginCheck()) {
//            dunyunSDKv2.GetHardwareVersion(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg("失败" + error);
//                }
//            });
//        }
    }

    /***
     * 读取锁的硬件版本号
     *
     * @param view
     */
    public void readVersion(View view) {
//        if (BleBeginCheck()) {
//            dunyunSDKv2.GetSoftwareVersion(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg("失败" + error);
//                }
//            });
//        }
    }

    /***
     * 读取生产日期
     *
     * @param view
     */
    public void GetProductionDate(View view) {
//        if (BleBeginCheck()) {
//            dunyunSDKv2.GetProductionDate(testLockUser, new Callback<String>() {
//                @Override
//                public void onSuccess(String data) {
//                    DialogUtil.showDialog(mcontext, "成功", data);
//                }
//
//                @Override
//                public void onFailed(String error) {
//                    toastMsg("断开失败，" + error);
//                }
//            });
//        }
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dunyunSDKv2.destroy();
        isRunning = false;
    }
}
