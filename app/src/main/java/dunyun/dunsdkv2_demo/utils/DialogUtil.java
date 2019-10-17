package dunyun.dunsdkv2_demo.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/***
 ** 名称：DialogUtil.java
 * 描述：对话框工具类.
 *
 * @author chenzp
 * @date 2015/9/13
 * @copyright:重庆平软科技有限公司
 */
public class DialogUtil {

    private static final String TITLE= "提示";
    private static final String BUTTON_OK= "确认";
    private static final String BUTTON_CANCEL= "取消";

    /***
     * 默认提示框
     * @param context
     * @param message 提示内容
     */
    public static void showDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(TITLE);
        builder.setPositiveButton(BUTTON_OK, new DialogInterface.OnClickListener() {
            @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                  }
             });
         builder.create().show();
    }

    /***
     * 可设置title的对话框
     * @param context
     * @param message 提示内容
     * @param title 提示标题
     */
    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton(BUTTON_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    /***
     * 带事件回调的对话框
     *
     * @param context
     * @param message 提示内容
     * @param listener 点击事件
     */
    public static void showDialog(Context context, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(TITLE);
        builder.setPositiveButton(BUTTON_OK, listener);
        builder.create().show();
    }

    /***
     * 带事件回调的对话框
     *
     * @param context
     * @param message 提示内容
     * @param listener 点击事件
     */
    public static void showDialog(Context context, String message,
                                  DialogInterface.OnClickListener listener,
                                  DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(TITLE);
        builder.setPositiveButton(BUTTON_OK, listener);
        builder.setNegativeButton(BUTTON_CANCEL, cancelListener);
        builder.create().show();
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String content){
        return ProgressDialog.show(context, title, content);
    }

    public static void cancelProgressDialog(ProgressDialog progressDialog){
        if(progressDialog != null)
            progressDialog.cancel();
    }
}
