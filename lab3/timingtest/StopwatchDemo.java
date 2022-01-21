package timingtest;

import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class StopwatchDemo {
    /** Computes the nth Fibonacci number using a slow naive recursive strategy.*/
    private static int fib(int n) {
        if (n < 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) {
        Stopwatch sw = new Stopwatch();
        int fib41 = fib(41);
        double timeInSeconds = sw.elapsedTime();
        //第50个斐波那契数是
        System.out.println("The 50th fibonacci number is " + fib41);
        //计算第41斐波那契数所用的时间     秒
        System.out.println("Time taken to compute 41st fibonacci number: " + timeInSeconds + " seconds.");
    }
}
