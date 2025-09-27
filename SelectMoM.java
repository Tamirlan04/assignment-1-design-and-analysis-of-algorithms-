public class SelectMoM {
    // Возвращает k-й по возрастанию элемент (0..n-1)
    public static int select(int[] a, int k, Metrics m) {
        if (k < 0 || k >= a.length) throw new IllegalArgumentException("k out of range");
        return selectRange(a, 0, a.length - 1, k, 0, m);
    }

    private static int selectRange(int[] a, int l, int r, int k, int depth, Metrics m) {
        if (m != null) m.touchDepth(depth);

        while (true) {
            int n = r - l + 1;
            if (n <= 24) {
                Util.insertionSort(a, l, r, m);
                return a[k];
            }

            int pivotIdx = medianOfMediansIndex(a, l, r, depth, m);
            int pos = partitionAround(a, l, r, pivotIdx, m);

            if (k == pos) return a[pos];
            else if (k < pos) r = pos - 1;
            else l = pos + 1;

            if (m != null) m.touchDepth(depth);
        }
    }

    // Возвращает ИНДЕКС pivot (медианы медиан) в диапазоне [l..r]
    private static int medianOfMediansIndex(int[] a, int l, int r, int depth, Metrics m) {
        int n = r - l + 1;
        int groups = (n + 4) / 5; // потолок

        // Сортируем группы по 5 и собираем их медианы в начало [l..l+groups-1]
        for (int g = 0; g < groups; g++) {
            int gs = l + 5 * g;
            int ge = Math.min(gs + 4, r);
            Util.insertionSort(a, gs, ge, m);
            int median = (gs + ge) >>> 1;
            Util.swap(a, l + g, median, m);
        }

        int medStart = l;
        int medEnd   = l + groups - 1;
        int medK     = medStart + (groups - 1) / 2;

        // Находим значение медианы среди медиан (внутри их поддиапазона)
        int medValue = selectRange(a, medStart, medEnd, medK, depth + 1, m);

        // Возвращаем индекс этого значения среди собранных медиан
        for (int i = medStart; i <= medEnd; i++) {
            if (Util.cmp(a[i], medValue, m) == 0) return i;
        }
        return medStart; // теоретически не нужен
    }

    private static int partitionAround(int[] a, int l, int r, int pivotIdx, Metrics m) {
        int pivot = a[pivotIdx];
        Util.swap(a, pivotIdx, r, m);
        int store = l;
        for (int i = l; i < r; i++) {
            if (Util.cmp(a[i], pivot, m) < 0) Util.swap(a, i, store++, m);
        }
        Util.swap(a, store, r, m);
        return store;
    }
}

