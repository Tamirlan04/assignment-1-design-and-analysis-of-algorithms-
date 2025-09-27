import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.stream.IntStream;

public class Main {
    private static void writeCsvHeader(PrintWriter out) {
        out.println("algo,n,time_ms,depth,comparisons,moves");
    }
    private static void writeCsv(PrintWriter out, String algo, int n, double ms, Metrics m) {
        out.printf(Locale.US, "%s,%d,%.3f,%d,%d,%d%n",
                algo, n, ms, m.maxDepth, m.comparisons, m.moves);
    }

    public static void main(String[] args) throws Exception {
        // Параметры по умолчанию
        String algo = "all"; // mergesort|quicksort|select|closest|all
        int minN = 10_000, maxN = 50_000, step = 10_000, trials = 3;
        String outFile = "metrics.csv";

        // Парсинг аргументов
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--algo":    algo   = args[++i]; break;
                case "--minN":    minN   = Integer.parseInt(args[++i]); break;
                case "--maxN":    maxN   = Integer.parseInt(args[++i]); break;
                case "--step":    step   = Integer.parseInt(args[++i]); break;
                case "--trials":  trials = Integer.parseInt(args[++i]); break;
                case "--out":     outFile= args[++i]; break;
                default: System.err.println("Unknown arg: " + args[i]);
            }
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
            writeCsvHeader(out);

            if (algo.equals("closest")) {
                for (int n = minN; n <= maxN; n += step) {
                    Metrics m = new Metrics();
                    ClosestPair2D.Point[] pts = new ClosestPair2D.Point[n];
                    Random r = new Random();
                    for (int i = 0; i < n; i++) pts[i] = new ClosestPair2D.Point(r.nextDouble(), r.nextDouble());
                    double best = Double.MAX_VALUE;
                    for (int t = 0; t < trials; t++) {
                        m.reset();
                        long t1 = System.nanoTime();
                        ClosestPair2D.Result res = ClosestPair2D.closest(pts, m);
                        long t2 = System.nanoTime();
                        best = Math.min(best, (t2 - t1) / 1e6);
                    }
                    writeCsv(out, "closest", n, best, m);
                }
                System.out.println("Done. CSV -> " + outFile);
                return;
            }

            for (int n = minN; n <= maxN; n += step) {
                int[] base = IntStream.generate(() -> Util.RNG.nextInt()).limit(n).toArray();

                if (algo.equals("mergesort") || algo.equals("all")) {
                    Metrics m = new Metrics();
                    double best = Double.MAX_VALUE;
                    for (int t = 0; t < trials; t++) {
                        int[] a = base.clone();
                        m.reset();
                        long t1 = System.nanoTime();
                        MergeSort.sort(a, m);
                        long t2 = System.nanoTime();
                        best = Math.min(best, (t2 - t1) / 1e6);
                    }
                    writeCsv(out, "mergesort", n, best, m);
                }

                if (algo.equals("quicksort") || algo.equals("all")) {
                    Metrics m = new Metrics();
                    double best = Double.MAX_VALUE;
                    for (int t = 0; t < trials; t++) {
                        int[] a = base.clone();
                        m.reset();
                        long t1 = System.nanoTime();
                        QuickSort.sort(a, m);
                        long t2 = System.nanoTime();
                        best = Math.min(best, (t2 - t1) / 1e6);
                    }
                    writeCsv(out, "quicksort", n, best, m);
                }

                if (algo.equals("select") || algo.equals("all")) {
                    Metrics m = new Metrics();
                    int k = n / 2;
                    double best = Double.MAX_VALUE;
                    int expected = Arrays.stream(base.clone()).sorted().toArray()[k];
                    for (int t = 0; t < trials; t++) {
                        int[] a = base.clone();
                        m.reset();
                        long t1 = System.nanoTime();
                        int kth = SelectMoM.select(a, k, m);
                        long t2 = System.nanoTime();
                        if (kth != expected) throw new AssertionError("Select incorrect");
                        best = Math.min(best, (t2 - t1) / 1e6);
                    }
                    writeCsv(out, "select", n, best, m);
                }
            }
        }
        System.out.println("Done. CSV -> metrics.csv");
    }
}
