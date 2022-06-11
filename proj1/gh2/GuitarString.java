package gh2;
   import deque.Deque;
   import deque.LinkedListDeque;
// TODO: maybe more imports 也许更多的导入

//Note: This file will not compile until you complete the Deque implementations
//注意：在完成Deque实现之前，不会编译此文件
public class GuitarString {
    private static final int SR = 44100;      // Sampling Rate 采样率
    private static final double DECAY = .996; // energy decay factor 能量衰减系数

    /* Buffer for storing sound data. */ //用于存储声音数据的缓冲区
    // TODO: uncomment the following line once you're ready to start this portion
     private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */ //创建给定频率的吉他弦
    public GuitarString(double frequency) {
        // TODO: Create a buffer with capacity = SR / frequency. You'll need to
        //       cast the result of this division operation into an int. For
        //       better accuracy, use the Math.round() function before casting.
        //       Your should initially fill your buffer array with zeros.
        //创建一个容量=SR/频率的缓冲区。你需要将此除法运算的结果转换为int。For更好的准确性，使用数学。强制转换前的round（）函数。您应该首先用零填充缓冲区数组。
        double capacity = Math.round(SR/frequency);
        buffer = new LinkedListDeque<>();
        for (int i = 0; i < capacity; i++) {
            buffer.addFirst((double) 0);
        }
    }

    /* Pluck the guitar string by replacing the buffer with white noise. */ //用白噪声替换缓冲器，弹拨吉他弦
    public void pluck() {
        // TODO: Dequeue everything in buffer, and replace with random numbers
        //       between -0.5 and 0.5. You can get such a number by using:
        //       double r = Math.random() - 0.5;
        //将缓冲区中的所有内容出列，并替换为随机数 介于-0.5和0.5之间。您可以使用以下方法获得此数字：双r=数学。随机（）-0.5；

        for (int i=0;i<buffer.size();i++){
            double r = Math.random() - 0.5;
            buffer.removeLast();
            buffer.addFirst(r) ;
        }
        //
        //       Make sure that your random numbers are different from each
        //       other. This does not mean that you need to check that the numbers
        //       are different from each other. It means you should repeatedly call
        //       Math.random() - 0.5 to generate new random numbers for each array index.
        //确保您的随机数不同于每个 其他。这并不意味着您需要检查数字 它们彼此不同。这意味着你应该反复打电话 数学。random（）-0.5为每个数组索引生成新的随机数。
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    //通过执行一次Karplus强算法。
    public void tic() {
        // TODO: Dequeue the front sample and enqueue a new sample that is
        //       the average of the two multiplied by the DECAY factor.
        //       **Do not call StdAudio.play().**
        //将前面的样本出列，并将一个新样本 两者的平均值乘以衰减因子。请勿致电StdAudio。播放（）**
            double next =buffer.get(1);
            double first = buffer.get(0);
            double r = (next + first)*0.5 *DECAY;
            buffer.removeFirst();
            buffer.addLast(r);
    }

    /* Return the double at the front of the buffer. */
    //返回缓冲区前部的 double
    public double sample() {
        // TODO: Return the correct thing. 返回正确的内容
        return buffer.get(0);
    }
}
