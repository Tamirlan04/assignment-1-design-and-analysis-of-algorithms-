import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ClosestPair2D {
    public static class Point {
        public final double x, y;
        public Point(double x, double y) { this.x = x; this.y = y; }
    }
    public static class Result {
        public final double dist; public final Point a, b;
        public Result(double d, Point a, Point b) { this.dist = d; this.a = a; this.b = b; }
    }

    public static Result closest(Point[] pts, Metrics m) {
        if (pts.length < 2) return new Result(Double.POSITIVE_INFINITY, null, null);
        Point[] px = pts.clone();
        Arrays.sort(px, Comparator.comparingDouble(p -> p.x));
        Point[] py = px.clone();
        Arrays.sort(py, Comparator.comparingDouble(p -> p.y));
        return rec(px, py, 0, px.length - 1, 0, m);
    }

    private static Result rec(Point[] px, Point[] py, int l, int r, int depth, Metrics m) {
        if (m != null) m.touchDepth(depth);
        int n = r - l + 1;
        if (n <= 3) return brute(px, l, r, m);

        int mid = (l + r) >>> 1;
        double midx = px[mid].x;

        // Точное разбиение py: принадлежность определяется по px, а не по <= midx
        Set<Point> leftSet = new HashSet<>(mid - l + 1);
        for (int i = l; i <= mid; i++) leftSet.add(px[i]);

        Point[] pyl = new Point[mid - l + 1];
        Point[] pyr = new Point[r - mid];
        int il = 0, ir = 0;
        for (Point p : py) {
            if (leftSet.contains(p)) pyl[il++] = p; else pyr[ir++] = p;
        }

        Result left = rec(px, pyl, l, mid, depth + 1, m);
        Result right = rec(px, pyr, mid + 1, r, depth + 1, m);
        Result best = left.dist < right.dist ? left : right;
        double d = best.dist;

        // «Полоса» шириной 2d, py уже отсортирован по y
        Point[] strip = new Point[n];
        int s = 0;
        for (Point p : py) if (Math.abs(p.x - midx) < d) strip[s++] = p;

        for (int i = 0; i < s; i++) {
            for (int j = i + 1; j < s && (strip[j].y - strip[i].y) < d; j++) {
                double dist = dist(strip[i], strip[j], m);
                if (dist < d) { d = dist; best = new Result(d, strip[i], strip[j]); }
            }
        }
        return best;
    }

    private static Result brute(Point[] px, int l, int r, Metrics m) {
        double best = Double.POSITIVE_INFINITY;
        Point A = null, B = null;
        for (int i = l; i <= r; i++) {
            for (int j = i + 1; j <= r; j++) {
                double d = dist(px[i], px[j], m);
                if (d < best) { best = d; A = px[i]; B = px[j]; }
            }
        }
        return new Result(best, A, B);
    }

    private static double dist(Point a, Point b, Metrics m) {
        if (m != null) m.comparisons += 2; // условный счётчик «операций»
        double dx = a.x - b.x, dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }
}
