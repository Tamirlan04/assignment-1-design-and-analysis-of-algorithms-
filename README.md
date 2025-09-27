# Assignment 1 — Design & Analysis of Algorithms (Divide & Conquer)

## Learning goals
- Implement classic divide-and-conquer algorithms with safe recursion patterns.
- Analyze running-time recurrences (Master Theorem cases; Akra–Bazzi intuition) and validate with measurements.
- Collect metrics (time, recursion depth, comparisons/moves) and communicate results via a short report and clean Git history.

## Implemented algorithms
**1) MergeSort (D&C, Master Case 2)**  
Implementation details: linear merge, a single reusable buffer, small-n cut-off to insertion sort, early-exit if boundary is already in order.  
**2) QuickSort (robust)**  
Randomized pivot; recurse **only into the smaller partition**, iterate over the larger (bounded stack ~O(log n) typical).  
**3) Deterministic Select (Median-of-Medians)**  
Groups of 5; in-place medians gathering; pivot = median of medians; in-place partition; continue only into the needed side.  
**4) Closest Pair of Points in 2D (O(n log n))**  
Sort by x; recursive split; build a y-sorted strip with classic 7–8 neighbor scan.

---

## Architecture notes (depth & allocations control)
- **MergeSort** uses a single `int[] buf` allocated once and reused at all recursion levels; cut-off (`n ≤ 24`) to insertion sort reduces overhead; stable merge uses `<=` to preserve order; early-exit if `a[mid] ≤ a[mid+1]`.  
- **QuickSort** uses Hoare partition + *smaller-first recursion* (larger side handled by loop) to keep recursion depth bounded in practice; randomized pivot mitigates adversarial inputs.  
- **Select (MoM5)** is fully **in-place**: medians of 5 are gathered at the front, then we select the median among them **within that subrange**; the search always continues to the side that contains `k` (no unnecessary recursion).  
- **Closest Pair**: maintain arrays sorted by x and y exactly once; in recursion, split `py` by **membership to left/right half of `px`** (no off-by-one from `x == midx`); strip scan checks ≤ 7–8 forward neighbors per point.

---

## Recurrence analyses (short)
- **MergeSort**: `T(n)=2T(n/2)+Θ(n)` (merge). Master Theorem (case 2) ⇒ `Θ(n log n)`. Stack depth `Θ(log n)`.  
- **QuickSort (avg.)**: randomized pivot ⇒ expected split near half; `E[T(n)]=Θ(n log n)`; with smaller-first recursion the **observed** stack is typically `O(log n)` even without TCO.  
- **Select (MoM5)**: groups of 5 give recurrence `T(n)=T(n/5)+T(7n/10)+Θ(n)` ⇒ `Θ(n)`; we recurse only where `k` lies; depth is `O(log n)` in practice due to rapid shrinking.  
- **Closest Pair (2D)**: sort once `Θ(n log n)` + divide & conquer with linear strip work: `T(n)=2T(n/2)+Θ(n)` ⇒ `Θ(n log n)`.
