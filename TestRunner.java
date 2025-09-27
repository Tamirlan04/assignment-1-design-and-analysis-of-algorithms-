import java.util.*;
import java.util.stream.IntStream;
import java.util.Locale;
import java.util.Arrays;

public class TestRunner {

    // --- Параметры по умолчанию ---
    private static final String DEF_ALGO = "all"; // mergesort|quicksort|select|closest|all
    private static final int DEF_N = 50_000;
    private static final int DEF_TRIALS = 1;
    private static final long DEF_SEED = 1L;
    private static final boolean DEF_VERIFY = true;
    private static final Integer DEF_K = null; // если null, возьмём n/2 для select

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        // Параметры
        String algo = DEF_ALGO;
        Integer n = DEF_N;           // для массивов/closest
        Integer trials = DEF_TRIALS;
        Long seed = DEF_SEED;
        Boolean verify = DEF_VERIFY; // сверка с эталоном
        Integer kOpt = DEF_K;        // индекс для select (если null -> n/2)

        // --- Парсер флагов ---
        Map<String,String> m = parseArgs(args);
        if (!m.isEmpty()) {
            algo   = m.getOrDefault("--algo", algo);
            n      = parseInt(m.get("--n"), n, 1, Integer.MAX_VALUE, "--n");
            trials = parseInt(m.get("--trials"), trials, 1, 1_000_000, "--trials");
            seed   = parseLong(m.get("--seed"), seed, Long.MIN_VALUE, Long.MAX_VALUE, "--seed");
            verify = parseBool(m.get("--verify"), verify);
            if (m.containsKey("--k")) {
                kOpt = parseInt(m.get("--k"), null, 0, Integer.MAX_VALUE, "--k");
            }
        }

        // --- Если аргументы не заданы, интерактив ---
        if (args.length == 0) {
            Scanner sc = new Scanner(System.in);
            System.out.println("=== TestRunner (interactive) ===");
            algo = ask(sc, "Algorithm [mergesort|quicksort|select|closest|all]", algo);
            n    = askInt(sc, "n (array size / number of points)", n, 1, Integer.MAX_VALUE);
            trials = askInt(sc, "trials (repetitions, take best time)", trials, 1, 1_000_000);
            seed = askLong(sc, "seed (Random)", seed, Long.MIN_VALUE, Long.MAX_VALUE);
            verify = askBool(sc, "verify results? [true|false]", verify);
            if ("select".equalsIgnoreCase(algo)) {
                String prompt = "k index for Select (0..n-1, empty = n/2)";
                Integer tmp = askOptionalInt(sc, prompt, null, 0, n - 1);
                kOpt = (tmp != null) ? tmp : null;
            }
            System.out.println();
        }

        // --- Валидация логики ---
        if ("select".equalsIgnoreCase(algo) && kOpt != null && (kOpt < 0 || kOpt >= n)) {
            throw new IllegalArgumentException("k must be in [0..n-1]");
        }

        // --- Запуск ---
        System.out.printf("Run: algo=%s, n=%d, trials=%d, seed=%d, verify=%s%s%n",
                algo, n, trials, seed, verify,
                ("select".equalsIgnoreCase(algo) ? (", k=" + (kOpt == null ? "n/2" : kOpt)) : ""));
        System.out.println("-----------------------------------------------------------");

        boolean ranSomething = false;
        if (algo.equalsIgnoreCase("mergesort") || algo.equalsIgnoreCase("all")) {
            ranSomething = true;
            runMergeSort(n, trials, seed, verify);
            System.out.println();
        }
        if (algo.equalsIgnoreCase("quicksort") || algo.equalsIgnoreCase("all")) {
            ranSomething = true;
            runQuickSort(n, trials, seed, verify);
            System.out.println();
        }
        if (algo.equalsIgnoreCase("select") || algo.equalsIgnoreCase("all")) {
            ranSomething = true;
            int k = (kOpt == null) ? n / 2 : kOpt;
            runSelect(n, trials, seed, verify, k);
            System.out.println();
        }
        if (algo.equalsIgnoreCase("closest") || algo.equalsIgnoreCase("all")) {
            ranSomething = true;
            runClosest(n, trials, seed, verify);
            System.out.println();
        }

        if (!ranSomething) {
            System.err.println("Unknown --algo: " + algo);
            System.err.println("Use: mergesort|quicksort|select|closest|all");
        } else {
            System.out.println("Done.");
        }
    }

    // ========================= Алгоритмы =========================

    private static void runMergeSort(int n, int trials, long seed, boolean verify) {
        System.out.println("[MergeSort]");
        Random rnd = new Random(seed);
        double bestMs = Double.MAX_VALUE;
        Metrics bestMetrics = null;

        for (int t = 1; t <= trials; t++) {
            int[] a = rnd.ints(n).toArray();
            int[] expect = null;
            if (verify) { expect = a.clone(); Arrays.sort(expect); }

            Metrics m = new Metrics();
            long t1 = System.nanoTime();
            MergeSort.sort(a, m);
            long t2 = System.nanoTime();
            double ms = (t2 - t1) / 1e6;

            if (verify && !Arrays.equals(a, expect)) {
                throw new AssertionError("MergeSort incorrect on trial " + t);
            }

            System.out.printf("  trial %d: time=%.3f ms, depth=%d, comps=%d, moves=%d%n",
                    t, ms, m.maxDepth, m.comparisons, m.moves);
            if (ms < bestMs) { bestMs = ms; bestMetrics = m; }
        }
        System.out.printf("  BEST: time=%.3f ms, depth=%d, comps=%d, moves=%d%n",
                bestMs, bestMetrics.maxDepth, bestMetrics.comparisons, bestMetrics.moves);
    }

    private static void runQuickSort(int n, int trials, long seed, boolean verify) {
        System.out.println("[QuickSort]");
        Random rnd = new Random(seed);
        double bestMs = Double.MAX_VALUE;
        Metrics bestMetrics = null;

        for (int t = 1; t <= trials; t++) {
            int[] a = rnd.ints(n).toArray();
            int[] expect = null;
            if (verify) { expect = a.clone(); Arrays.sort(expect); }

            Metrics m = new Metrics();
            long t1 = System.nanoTime();
            QuickSort.sort(a, m);
            long t2 = System.nanoTime();
            double ms = (t2 - t1) / 1e6;

            if (verify && !Arrays.equals(a, expect)) {
                throw new AssertionError("QuickSort incorrect on trial " + t);
            }

            int depthBound = 2 * (int)Math.floor(Math.log(Math.max(n,2)) / Math.log(2)) + 64;
            if (m.maxDepth > depthBound) {
                System.out.printf("  (note) depth is high: %d (bound≈%d)%n", m.maxDepth, depthBound);
            }

            System.out.printf("  trial %d: time=%.3f ms, depth=%d, comps=%d, moves=%d%n",
                    t, ms, m.maxDepth, m.comparisons, m.moves);
            if (ms < bestMs) { bestMs = ms; bestMetrics = m; }
        }
        System.out.printf("  BEST: time=%.3f ms, depth=%d, comps=%d, moves=%d%n",
                bestMs, bestMetrics.maxDepth, bestMetrics.comparisons, bestMetrics.moves);
    }

    private static void runSelect(int n, int trials, long seed, boolean verify, int k) {
        System.out.println("[SelectMoM]");
        Random rnd = new Random(seed);
        double bestMs = Double.MAX_VALUE;
        Metrics bestMetrics = null;

        for (int t = 1; t <= trials; t++) {
            int[] a = rnd.ints(n).toArray();
            Integer expect = null;
            if (verify) {
                int[] s = a.clone();
                Arrays.sort(s);
                expect = s[k];
            }

            Metrics m = new Metrics();
            long t1 = System.nanoTime();
            int v = SelectMoM.select(a, k, m);
            long t2 = System.nanoTime();
            double ms = (t2 - t1) / 1e6;

            if (verify && !Objects.equals(v, expect)) {
                throw new AssertionError("SelectMoM incorrect on trial " + t + " (got " + v + ", expect " + expect + ")");
            }

            System.out.printf("  trial %d: k=%d, value=%d, time=%.3f ms, depth=%d, comps=%d, moves=%d%n",
                    t, k, v, ms, m.maxDepth, m.comparisons, m.moves);
            if (ms < bestMs) { bestMs = ms; bestMetrics = m; }
        }
        System.out.printf("  BEST: time=%.3f ms, depth=%d, comps=%d, moves=%d%n",
                bestMs, bestMetrics.maxDepth, bestMetrics.comparisons, bestMetrics.moves);
    }

    private static void runClosest(int n, int trials, long seed, boolean verify) {
        System.out.println("[ClosestPair2D]");
        Random rnd = new Random(seed);
        double bestMs = Double.MAX_VALUE;
        Metrics bestMetrics = null;

        for (int t = 1; t <= trials; t++) {
            ClosestPair2D.Point[] pts = new ClosestPair2D.Point[n];
            for (int i = 0; i < n; i++) {
                pts[i] = new ClosestPair2D.Point(rnd.nextDouble(), rnd.nextDouble());
            }

            Metrics m = new Metrics();
            long t1 = System.nanoTime();
            ClosestPair2D.Result fast = ClosestPair2D.closest(pts, m);
            long t2 = System.nanoTime();
            double ms = (t2 - t1) / 1e6;

            if (verify && n <= 2000) {
                // Брутфорс-проверка на малых n
                ClosestPair2D.Result slow = bruteClosest(pts);
                if (Math.abs(fast.dist - slow.dist) > 1e-12) {
                    throw new AssertionError(String.format(
                            Locale.US, "ClosestPair2D incorrect on trial %d (fast=%.15f, slow=%.15f)",
                            t, fast.dist, slow.dist));
                }
            }

            System.out.printf("  trial %d: d=%.6f, time=%.3f ms, depth=%d, comps=%d%n",
                    t, fast.dist, ms, m.maxDepth, m.comparisons);
            if (ms < bestMs) { bestMs = ms; bestMetrics = m; }
        }
        System.out.printf("  BEST: time=%.3f ms, depth=%d, comps=%d%n",
                bestMs, bestMetrics.maxDepth, bestMetrics.comparisons);
    }

    // ========================= Утилиты =========================

    private static ClosestPair2D.Result bruteClosest(ClosestPair2D.Point[] pts) {
        double best = Double.POSITIVE_INFINITY;
        ClosestPair2D.Point A = null, B = null;
        for (int i = 0; i < pts.length; i++) {
            for (int j = i + 1; j < pts.length; j++) {
                double dx = pts[i].x - pts[j].x;
                double dy = pts[i].y - pts[j].y;
                double d = Math.hypot(dx, dy);
                if (d < best) { best = d; A = pts[i]; B = pts[j]; }
            }
        }
        return new ClosestPair2D.Result(best, A, B);
    }

    private static Map<String,String> parseArgs(String[] args) {
        Map<String,String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String k = args[i];
            if (k.startsWith("--")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    m.put(k, args[++i]);
                } else {
                    m.put(k, "true"); // флаг без значения (напр., --verify)
                }
            }
        }
        return m;
    }

    private static Integer parseInt(String s, Integer def, int min, int max, String name) {
        if (s == null) return def;
        try {
            int v = Integer.parseInt(s);
            if (v < min || v > max) throw new IllegalArgumentException(name + " out of range");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad int for " + name + ": " + s);
        }
    }
    private static Long parseLong(String s, Long def, long min, long max, String name) {
        if (s == null) return def;
        try {
            long v = Long.parseLong(s);
            if (v < min || v > max) throw new IllegalArgumentException(name + " out of range");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad long for " + name + ": " + s);
        }
    }
    private static Boolean parseBool(String s, Boolean def) {
        if (s == null) return def;
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) return Boolean.parseBoolean(s);
        return def;
    }

    // ---- Interactive helpers ----
    private static String ask(Scanner sc, String prompt, String def) {
        System.out.printf("%s [%s]: ", prompt, def);
        String line = sc.nextLine().trim();
        return line.isEmpty() ? def : line;
    }
    private static Integer askInt(Scanner sc, String prompt, Integer def, int min, int max) {
        while (true) {
            System.out.printf("%s [%d]: ", prompt, def);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return def;
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  Enter integer in [" + min + ".." + max + "]");
            }
        }
    }
    private static Long askLong(Scanner sc, String prompt, Long def, long min, long max) {
        while (true) {
            System.out.printf("%s [%d]: ", prompt, def);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return def;
            try {
                long v = Long.parseLong(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  Enter long in [" + min + ".." + max + "]");
            }
        }
    }
    private static Boolean askBool(Scanner sc, String prompt, Boolean def) {
        while (true) {
            System.out.printf("%s [%s]: ", prompt, def);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return def;
            if (line.equalsIgnoreCase("true")) return true;
            if (line.equalsIgnoreCase("false")) return false;
            System.out.println("  Enter true or false");
        }
    }
    private static Integer askOptionalInt(Scanner sc, String prompt, Integer def, int min, int max) {
        while (true) {
            System.out.printf("%s [%s]: ", prompt, def == null ? "" : def);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return def;
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  Enter integer in [" + min + ".." + max + "] or empty");
            }
        }
    }
}
