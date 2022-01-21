package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(SLList<Integer> Ns, SLList<Double> times, SLList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.getLast();
            double time = times.getLast();
            int opCount = opCounts.getLast();
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    private static int fib(int n) {
        if (n < 0) {
            return 0;
        }
        return n;
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {

        SLList<Integer>  L = new SLList<>();
        SLList<Integer>  opCounts = new SLList<>();
        SLList<Double> values = new SLList<>();
        for(int n=1000; n <=128000 ; n*=2){
            L.addLast(fib(n));
            opCounts.addLast(10000);
            Stopwatch sw = new Stopwatch();

            double timeInSeconds = sw.elapsedTime();
            values.addLast(timeInSeconds);
        }
        System.out.println(L.size());
        System.out.println(L.size());
        printTimingTable(L,values,opCounts);
    }

}
