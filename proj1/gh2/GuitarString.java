
package gh2;

import deque.Deque;
import deque.LinkedListDeque;

public class GuitarString {
    private static final int SR = 44100;      // Sampling Rate 采样率
    private static final double DECAY = .996; // energy decay factor 能量衰减系数

    private Deque<Double> buffer;

    public GuitarString(double frequency) {
        //创建一个容量=SR/频率的缓冲区。你需要将此除法运算的结果转换为int。For更好的准确性，使用数学。强制转换前的round（）函数。您应该首先用零填充缓冲区数组。
        double capacity = Math.round(SR / frequency);
        buffer = new LinkedListDeque<>();
        for (int i = 0; i < capacity; i++) {
            buffer.addFirst((double) 0);
        }
    }

    /* Pluck the guitar string by replacing the buffer with white noise. */ //用白噪声替换缓冲器，弹拨吉他弦
    public void pluck() {
        //将缓冲区中的所有内容出列，并替换为随机数 介于-0.5和0.5之间。您可以使用以下方法获得此数字：双r=数学。随机（）-0.5；
        for (int i = 0; i < buffer.size(); i++) {
            double r = Math.random() - 0.5;
            buffer.removeLast();
            buffer.addFirst(r);
        }
    }

    //通过执行一次Karplus强算法。
    public void tic() {
        //将前面的样本出列，并将一个新样本 两者的平均值乘以衰减因子。请勿致电StdAudio。播放（）**
        double next = buffer.get(1);
        double first = buffer.get(0);
        double r = (next + first) * 0.5 * DECAY;
        buffer.removeFirst();
        buffer.addLast(r);
    }

    //返回缓冲区前部的 double
    public double sample() {
        return buffer.get(0);
    }
}