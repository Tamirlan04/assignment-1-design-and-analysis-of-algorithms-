public class QuickSort {
    private static final int CUTOFF = 24;

    public static void sort(int[] a, Metrics m) {
        sort(a, 0, a.length - 1, 0, m);
    }

    private static void sort(int[] a, int l, int r, int depth, Metrics m) {
        while (l < r) {
            if (m != null) m.touchDepth(depth);

            if (r - l + 1 <= CUTOFF) {
                Util.insertionSort(a, l, r, m);
                return;
            }

            int p = l + Util.RNG.nextInt(r - l + 1);
            Util.swap(a, l, p, m);
            int pivot = a[l];

            int i = l - 1, j = r + 1;
            while (true) {
                do { i++; } while (Util.cmp(a[i], pivot, m) < 0);
                do { j--; } while (Util.cmp(a[j], pivot, m) > 0);
                if (i >= j) break;
                Util.swap(a, i, j, m);
            }
            int mid = j; // [l..mid] <= pivot, [mid+1..r] >= pivot

            if (mid - l < r - (mid + 1)) {
                sort(a, l, mid, depth + 1, m); // меньшая часть — рекурсией
                l = mid + 1;                   // большая — хвостом (итеративно)
            } else {
                sort(a, mid + 1, r, depth + 1, m);
                r = mid;
            }
        }
        if (m != null) m.touchDepth(depth);
    }
}
