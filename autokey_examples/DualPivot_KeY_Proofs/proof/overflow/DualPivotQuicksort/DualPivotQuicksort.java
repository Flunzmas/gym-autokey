 //import java.util.Arrays;
 //import java.util.Random;

class DualPivotQuicksort {

//    public static void main(String[] args) {
//        long seed = System.currentTimeMillis();
//        System.out.println(seed);
//        Random rand = new Random(1457618756183l);
//        int[] a = new int[75];
//        for (int i = 0; i < a.length; ++i) {
//            a[i] = rand.nextInt(100);
//        }
//        int[] b = new int[a.length];
//        System.arraycopy(a,0,b,0,a.length);
//        sort(a);
//        Arrays.sort(b);
//        for (int j = 0; j < a.length-1; ++j) {
//            if (a[j] > a[j+1]) System.out.println("SORT FAILED: " + j + " - " + a[j] + " " + a[j+1]);
//            if (a[j] != b[j] || a[j+1] != b[j+1]) System.out.println("NOT A PERMUTATION OF ORIGINAL ARRAY!");
//        }
//        System.out.println(Arrays.toString(a));
//    }

    static int less, great; // , pivot1, pivot2;
    static int e1,e2,e3,e4,e5;

    /*@
      @ normal_behaviour requires true;
      @ requires (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free (\forall int i; 0 <= i && i < a.length; (\forall int j; 0 < j && j < a.length; i < j ==> a[i] <= a[j]));
      @ assignable less, great, e1,e2,e3,e4,e5,a[*];
      @*/ 
    static void sort(int[] a) {
        sort(a, 0, a.length-1,true);
    }
    
    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free 0 <= left && right < a.length;
      @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int i; left <= i && i <= right; (\forall int j; left <= j && j <= right; i < j ==> a[i] <= a[j]));
      @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable less, great, e1,e2,e3,e4,e5, a[left..right];
      @ measured_by right - left + 5;
      @*/ 
    static void sort(int[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;
        if (length > 46) {
            prepare_indices(a, left, right);
            if  (allLess(a[e1],a[e2],a[e3],a[e4],a[e5])) {// usually, ThreeWayQs will be done if two of the pivots are equal

                split(a, left, right, a[e2], a[e4]);
                
                int lless = less;
                int lgreat = great;
                // Sort left and right parts recursively, excluding known pivots
                sort(a, left, lless - 2, leftmost);
                sort(a, lgreat + 2, right, false);
                
                // SwapValues.java if central part is large
                // Sort center part recursively
                sort(a, lless, lgreat, false);
            } else {
                insertionSort(a, left, right, leftmost);
            }
        } else if (length > 1) {
            insertionSort(a, left, right, leftmost);
        }
    }

    static boolean allLess(int e1, int e2, int e3, int e4, int e5) {
        return e1<e2 && e2<e3 && e3<e4 && e4<e5;
    } 

    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free right - left + 1 > 46 && 0 <= left && right < a.length;
      @ requires_free (\exists int x; left < x && x < right; a[x] < pivot1); //z.B.a[e1]
      @ requires_free (\exists int y; left < y && y < right; a[y] > pivot2); //z.B.a[e5]
      @ requires_free a[e1] < a[e2] && a[e2] < a[e3] && a[e3] < a[e4] && a[e4] < a[e5];
      @ requires_free left < e1 && e1 < e2 && e2 < e3 && e3 < e4 && e4 < e5 && e5 < right;
      @ requires_free a[e2] == pivot1 && a[e4] == pivot2;
      @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int i; left <= i && i < less-1; a[i] < pivot1);
      @ ensures_free a[less-1] == pivot1;
      @ ensures_free (\forall int j; less <= j && j <= great; pivot1 <= a[j] && a[j] <= pivot2);
      @ ensures_free a[great+1] == pivot2;
      @ ensures_free (\forall int l; great+1 < l && l <= right; a[l] > pivot2);
      @ ensures_free left < less-1;
      @ ensures_free great < right-1;
      @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable less, great, a[left..right];
      @*/ 
    static void split(int[] a, int left, int right, int pivot1, int pivot2) {
        less  = left;  // The index of the first element of center part
        great = right; // The index before the first element of right part

        /*
         * The first and the last elements to be sorted are moved to the
         * locations formerly occupied by the pivots. When partitioning
         * is complete, the pivots are swapped back into their final
         * positions, and excluded from subsequent sorting.
         */
        a[e2] = a[left];
        a[e4] = a[right];

        move_less_right(a, left, right, pivot1);
        move_great_left(a, left, right, pivot2);
        outer:
        /*@ loop_invariant true;
          @ loop_invariant_free
          @    less-1 <= k && k <= great+1
          @ && left < less && less <= right
          @ && left <= great && great < right 
          @ && (k <= great || less <= k)
          @ && pivot1 < pivot2 
          @ && left < less && great < right
          @ && (\forall int i; left < i && i < less; a[i] < pivot1)
          @ && (\forall int j; less <= j && j < k+1 && j <= great; pivot1 <= a[j] && a[j] <= pivot2)
          @ && (\forall int l; great < l && l < right; a[l] > pivot2)
          @ && (\exists int x; left < x && x < right; a[x] < pivot1)
          @ && (\exists int y; left < y && y < right; a[y] > pivot2)
          @ && (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j]))
          @ && (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ loop_invariant_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ loop_invariant_free \dl_inInt(less) && \dl_inInt(great);
          @ assignable less, great, a[left..right];
          @ decreases great + 5 - k;
          @     
          @*/ 
        for (int k = less - 1; ++k <= great; ) {
            loop_body(a, k, left, right, pivot1, pivot2);
        }

        // Swap pivots into their final positions
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;

    }

    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free
      @    less <= k && k <= great
      @ && 0 <= k && k < a.length
      @ && 0 <= left && left < less && less <= right
      @ && left <= great && great < right && right < a.length
      @ && pivot1 < pivot2 
      @ && (\forall int i; left < i && i < less; a[i] < pivot1)
      @ && (\forall int j; less <= j && j < k && j <= great; pivot1 <= a[j] && a[j] <= pivot2)
      @ && (\forall int l; great < l && l < right; a[l] > pivot2)
      @ && (\exists int x; left < x && x < right; a[x] < pivot1)
      @ && (\exists int y; left < y && y < right; a[y] > pivot2);
      @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ 
      @ ensures_free
      @    (k <= great || less <= k)
      @ && less-1 <= k && k <= great+1
      @ && left <= less && less <= right
      @ && left <= great && great <= right 
      @ && pivot1 < pivot2 
      @ && (\forall int i; left < i && i < less; a[i] < pivot1)
      @ && (\forall int j; less <= j && j < k+1 && j <= great; pivot1 <= a[j] && a[j] <= pivot2)
      @ && (\forall int l; great < l && l < right; a[l] > pivot2)
      @ && (\exists int x; left < x && x < right; a[x] < pivot1)
      @ && (\exists int y; left < y && y < right; a[y] > pivot2)
      @ && \old(less) <= less
      @ && great <= \old(great);
      @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @
      @ assignable less, great, a[left..right];
      @*/
    static void loop_body(int[] a, int k, int left, int right, int pivot1, int pivot2) {
            int ak = a[k];
            if (ak < pivot1) { // Move a[k] to left part
                a[k] = a[less];
                a[less] = ak;
                ++less;
            } else if (ak > pivot2) { // Move a[k] to right part
                move_great_left_in_loop(a, k, left, right, pivot2);
                if (great == k) {
                    great--;
                    return;
                }
                if (a[great] < pivot1) { // a[great] <= pivot2
                    a[k] = a[less];
                    a[less] = a[great];
                    ++less;
                } else { // pivot1 <= a[great] <= pivot2
                    a[k] = a[great];
                }
                a[great] = ak;
                --great;
            }
        }
    
    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free 0 <= left && left < right && right - left >= 46 && right < a.length;
      @ requires_free a.length > 46;
      @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free a[e1] <= a[e2] && a[e2] <= a[e3] && a[e3] <= a[e4] && a[e4] <= a[e5];
      @ ensures_free left < e1 && e1 < e2 && e2 < e3 && e3 < e4 && e4 < e5 && e5 < right;
      @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ assignable e1,e2,e3,e4,e5, a[left..right];
      @*/ 
    static void prepare_indices(int[] a, int left, int right) {
        {calcE(left, right);}
        eInsertionSort(a,left,right,e1,e2,e3,e4,e5); 
    }

    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free 0 <= left && right < a.length;
      @ requires_free left == less && less < great && great < a.length;
      @ requires_free (\exists int j; less+1 <= j && j < great; a[j] >= pivot1);
      @ ensures_free less < great;
      @ ensures_free (\forall int i; left < i && i < less; a[i] < pivot1);
      @ ensures_free a[less] >= pivot1;
      @ ensures_free \old(less) < less;
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable less;
      @*/ 
    static void  move_less_right(int[] a, int left, int right, int pivot1) {
        /*@ loop_invariant true;
          @ loop_invariant_free
          @     0 <= less && less <= great && great < a.length
          @  && (\forall int i; left < i && i < less+1; a[i] < pivot1)
          @  && (\exists int j; less+1 <= j && j < great; a[j] >= pivot1)
          @  && \old(less) <= less;
      @ loop_invariant_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ loop_invariant_free \dl_inInt(less) && \dl_inInt(great);
          @  assignable less;
          @  decreases great - less;
          @*/ 
        while (a[++less] < pivot1) {
        }
    }

    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free 0 <= left && left <= less && less < great && great == right && right < a.length;
      @ requires_free (\exists int i; less <= i && i < great; a[i] <= pivot2);
      @ ensures_free less <= great;
      @ ensures_free (\forall int i; great < i && i < right; a[i] > pivot2);
      @ ensures_free a[great] <= pivot2;
      @ ensures_free great < \old(great);
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable great;
      @*/ 
    static void move_great_left(int[] a, int left, int right, int pivot2) {
      /*@ loop_invariant true;
        @ loop_invariant_free great > 0;
        @ loop_invariant_free left <= great && great <= right;
        @ loop_invariant_free less <= great;
        @ loop_invariant_free (\forall int i; great-1 < i && i < right; a[i] > pivot2);
        @ loop_invariant_free (\exists int i; less <= i && i < great; a[i] <= pivot2);
      @ loop_invariant_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ loop_invariant_free \dl_inInt(less) && \dl_inInt(great);
        @ decreases great;
        @ assignable great;
        @*/
        while (a[--great] > pivot2) {
        }
    }

    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free 0 <= k && k <= great && great < a.length;
      @ requires_free (\exists int i; left <= i && i <= great; a[i] <= pivot2);
      @ ensures_free 0 <= great;
      @ ensures_free (\forall int i; great < i && i <= \old(great); a[i] > pivot2);
      @ ensures_free a[great] <= pivot2 || great == k;
      @ ensures_free k <= great && great <= \old(great);
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable great;
      @*/ 
    static void move_great_left_in_loop(int[] a, int k, int left, int right, int pivot2) {
        /*@ loop_invariant true;
          @ loop_invariant_free
          @     k <= great && 0 <= great
          @  && (\exists int i; left <= i && i <= great; a[i] <= pivot2)
          @  && (\forall int i; great < i && i <= \old(great); a[i] > pivot2)
          @  && great <= \old(great);
      @ loop_invariant_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ loop_invariant_free \dl_inInt(less) && \dl_inInt(great);
          @ decreases great;
          @ assignable great;
          @*/ 
        while (a[great] > pivot2 && great != k) {
            --great;
        }
    }

    /*@
      @ normal_behaviour requires true;
      @ requires right < Integer.MAX_VALUE;
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free 0 <= left && left < right && right - left >= 46;
      @ ensures_free left < e1 && e1 < e2 && e2 < e3 && e3 < e4 && e4 < e5 && e5 < right;
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable e1,e2,e3,e4,e5;
      @*/ 
    static void calcE(int left, int right) {
        int length = right - left + 1;
        int seventh = (length / 8) + (length / 64) + 1;
        e3 = (left + right) / 2; // The midpoint
        e2 = e3 - seventh;
        e1 = e2 - seventh;
        e4 = e3 + seventh;
        e5 = e4 + seventh;
    }

    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free a.length > 46;
      @ requires_free 0 <= left && left < e1 && e5 < right && right < a.length;
      @ requires_free left < e1 && e1 < e2 && e2 < e3 && e3 < e4 && e4 < e5 && e5 < right;
      @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free a[e1] <= a[e2] && a[e2] <= a[e3] && a[e3] <= a[e4] && a[e4] <= a[e5];
      @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable a[left..right];
      @*/ 
    static void eInsertionSort(int[] a, int left, int right, int e1, int e2, int e3, int e4, int e5) {
        /*@
          @ ensures_free (a[e1] <= a[e2]);
          @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
          @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
          @ assignable a[e1], a[e2];
          @ signals_only \nothing;
          @*/
        {
        if (a[e2] < a[e1]) { int t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
        }

        /*@
          @ ensures_free (a[e1] <= a[e2] && a[e2] <= a[e3]);
          @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
          @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
          @ assignable a[e1], a[e2], a[e3];
          @ signals_only \nothing;
          @*/
        {
        if (a[e3] < a[e2]) { int t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }}
        
        /*@
          @ ensures_free (a[e1] <= a[e2] && a[e2] <= a[e3] && a[e3] <= a[e4]);
          @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
          @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
          @ assignable a[e1], a[e2], a[e3], a[e4];
          @ signals_only \nothing;
          @*/
        {
        if (a[e4] < a[e3]) { int t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }}

        /*@
          @ ensures_free (a[e1] <= a[e2] && a[e2] <= a[e3] && a[e3] <= a[e4] && a[e4] <= a[e5]);
          @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
          @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
          @ assignable a[e1], a[e2], a[e3], a[e4], a[e5];
          @ signals_only \nothing;
          @*/
        {
        if (a[e5] < a[e4]) { int t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }}
    }


    /*@
      @ normal_behaviour requires true;
      @ requires_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ requires_free \dl_inInt(less) && \dl_inInt(great);
      @ requires_free 0 <= left && right < a.length;
      @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int i; left <= i && i <= right; (\forall int j; left <= j && j <= right; i < j ==> a[i] <= a[j]));
      @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free (\forall int f; 0 <= f < a.length; \dl_inInt(a[f]));
      @ ensures_free \dl_inInt(less) && \dl_inInt(great);
      @ assignable a[left..right];
      @*/ 
    static void insertionSort(int[] a, int left, int right, boolean leftmost) {
        if (leftmost) {
            /*
             * Traditional (without sentinel) insertion sort,
             * optimized for server VM, is used in case of
             * the leftmost part.
             */
            for (int i = left, j = i; i < right; j = ++i) {
                int ai = a[i + 1];
                while (ai < a[j]) {
                    a[j + 1] = a[j];
                    if (j-- == left) {
                        break;
                    }
                }
                a[j + 1] = ai;
            }
        } else {
            /*
             * Skip the longest ascending sequence.
             */
            do {
                if (left >= right) {
                    return;
                }
            } while (a[++left] >= a[left - 1]);

            /*
             * Every element from adjoining part plays the role
             * of sentinel, therefore this allows us to avoid the
             * left range check on each iteration. Moreover, we use
             * the more optimized algorithm, so called pair insertion
             * sort, which is faster (in the context of Quicksort)
             * than traditional implementation of insertion sort.
             */
            for (int k = left; ++left <= right; k = ++left) {
                int a1 = a[k], a2 = a[left];

                if (a1 < a2) {
                    a2 = a1; a1 = a[left];
                }
                while (a1 < a[--k]) {
                    a[k + 2] = a[k];
                }
                a[++k + 1] = a1;

                while (a2 < a[--k]) {
                    a[k + 1] = a[k];
                }
                a[k + 1] = a2;
            }
            int last = a[right];

            while (last < a[--right]) {
                a[right + 1] = a[right];
            }
            a[right + 1] = last;
        }
        return;
    }
} 
