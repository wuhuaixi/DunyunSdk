package dunyun.dunsdkv2_demo.beans;

/**
 * Created by GuoWen on 2019/10/17.
 * description
 */

public class LockUpdate {
        byte[] getBytes;
        int length = 0;
        int datelength = 70;
        int currentLength = 0;
        int pianyiliang = 0;

        public LockUpdate(byte[] getBytes, int datelength) {
            this.getBytes = getBytes;
            this.datelength = datelength;
            this.length = getBytes.length;
        }

        public byte[] getNextBytes() {
            byte[] newBytes;
            if(this.currentLength + this.datelength <= this.length) {
                newBytes = new byte[this.datelength];
                System.arraycopy(this.getBytes, this.currentLength, newBytes, 0, this.datelength);
            } else {
                newBytes = new byte[this.length - this.currentLength];
                System.arraycopy(this.getBytes, this.currentLength, newBytes, 0, newBytes.length);
            }

            this.pianyiliang = this.currentLength;
            this.currentLength += newBytes.length;
            return newBytes;
        }

        public int getCurrentLength() {
            return this.pianyiliang;
        }

        public Boolean isNext() {
            Boolean isnext = Boolean.valueOf(true);
            if(this.currentLength >= this.length) {
                isnext = Boolean.valueOf(false);
            }

            return isnext;
        }

}
