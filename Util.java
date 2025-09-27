import java.util.Random;

public final class Util {
    private Util() {}
    public static final Random RNG = new Random();

    public static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) return;
        int t = a[i]; a[i] = a[j]; a[j] = t;
        if (m != null) m.moves += 3;
    }

    public static int cmp(int x, int y, Metrics m) {
        if (m != null) m.comparisons++;
        return Integer.compare(x, y);
    }

    public static void insertionSort(int[] a, int l, int r, Metrics m) {
        for (int i = l + 1; i <= r; i++) {
            int x = a[i];
            int j = i - 1;
            while (j >= l && cmp(a[j], x, m) > 0) {
                a[j + 1] = a[j]; if (m != null) m.moves++;
                j--;
            }
            a[j + 1] = x; if (m != null) m.moves++;
        }
    }
}
