package dunyun.dunsdkv2_demo.utils;

/**
 * Created by GuoWen on 2019/10/18.
 * description
 */

public class ConsumTimeUtil {

    long timeBegin = 0L;
    long timeEnd = 0L;

    public ConsumTimeUtil() {
    }

    public void startConsumtime() {
        this.timeBegin = System.currentTimeMillis();
    }

    public int getConsumtime() {
        this.timeEnd = System.currentTimeMillis() - this.timeBegin;
        return (int)this.timeEnd;
    }
}
