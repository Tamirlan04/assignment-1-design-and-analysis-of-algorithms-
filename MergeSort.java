public class MergeSort {
    private static final int CUTOFF = 24;

    public static void sort(int[] a, Metrics m) {
        if (a.length <= 1) return;
        int[] buf = new int[a.length];
        sort(a, 0, a.length - 1, buf, 0, m);
    }

    private static void sort(int[] a, int l, int r, int[] buf, int depth, Metrics m) {
        if (m != null) m.touchDepth(depth);
        if (r - l + 1 <= CUTOFF) {
            Util.insertionSort(a, l, r, m);
            return;
        }
        int mid = (l + r) >>> 1;
        sort(a, l, mid, buf, depth + 1, m);
        sort(a, mid + 1, r, buf, depth + 1, m);

        if (Util.cmp(a[mid], a[mid + 1], m) <= 0) return; // уже склеено

        int i = l, j = mid + 1, k = l;
        while (i <= mid && j <= r) {
            if (Util.cmp(a[i], a[j], m) <= 0) buf[k++] = a[i++];
            else                              buf[k++] = a[j++];
            if (m != null) m.moves++;
        }
        while (i <= mid) { buf[k++] = a[i++]; if (m != null) m.moves++; }
        while (j <= r)   { buf[k++] = a[j++]; if (m != null) m.moves++; }

        for (int t = l; t <= r; t++) { a[t] = buf[t]; if (m != null) m.moves++; }
    }
}
