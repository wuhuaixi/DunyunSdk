package dunyun.dunsdkv2_demo.callback;

import java.util.List;

/**
 * <DL>
 * <DD>操作状态.</DD><BR>
 * </DL>
 *
 * @author psoft <Chenzp>
 * @date 2016/3/21
 * 修改记录:
 * 初始化
 * @Copyright 重庆平软科技有限公司 2015
 */
public interface ListCallback<T> {

    /**成功*/
    void onSuccess(List<T> data);

    /**失败*/
    void onFailed(String error);
}
