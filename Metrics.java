public class Metrics {
    public long comparisons = 0;
    public long moves = 0;
    public int maxDepth = 0;

    public void touchDepth(int d) { if (d > maxDepth) maxDepth = d; }
    public void reset() { comparisons = 0; moves = 0; maxDepth = 0; }
}
