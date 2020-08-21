class SinglePivotPartition {

    static int less;
    static int great;
    static int pivot;
    /*@
      @ public normal_behaviour
      @ assignable less, great, pivot, a[*];
      @ ensures (\forall int i; 0 <= i && i < a.length; (\forall int j; 0 <= j && j < a.length; i < j ==> a[i] <= a[j]));
      @*/ 
    public static void sort(int[] a) {
        if (a.length > 0) {
            sort(a, 0, a.length-1);
        }
    }

    /*@
      @ normal_behaviour
      @ requires 0 <= left && right < a.length;
      @ requires (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures (\forall int i; left <= i && i <= right; (\forall int j; left <= j && j <= right; i < j ==> a[i] <= a[j]));
      @ ensures (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ assignable less, great, pivot, a[left..right];
      @ measured_by right - left + 1;
      @*/ 
    static void sort(int[] a, int left, int right) {
        if (left < right) {
            if (right - left + 1 > 46) {
                split(a, left, right);
                int lless = less;
                int lgreat = great;
                int lpivot = pivot;

                if(left < less-1) sort(a, left, lless-1);

                if(lgreat+1 < right) sort(a, lgreat+1, right);
            } else {
                insertionSort(a, left, right, false);
            }
        }
    }

    /*@
      @ normal_behaviour
      @ requires right-left+1 > 46;
      @ requires 0 <= left && left < right && right < a.length;
      @ requires (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures (\forall int i; left <= i && i < less; a[i] < pivot);
      @ ensures (\forall int j; less <= j && j <= great; a[j] == pivot);
      @ ensures (\forall int l; great < l && l <= right; a[l] > pivot);
      @ ensures (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures left <= great && less <= right;
      @ ensures left <= less && great <= right;
      @ //ensures less <= great;
      @ assignable less, great, pivot, a[left..right];
      @*/ 
    static void split(int[] a, int left, int right) {
        int mid = (left+right) / 2;
        pivot = a[mid];
        less = left;
        great = right;
        int test = -1;
        
        /*@
          @ loop_invariant
          @     less <= k && k <= great + 2
          @  && 0 <= left && left < right && right < a.length
          @  && left <= less && great <= right
          @  && left <= great && less <= right
          @  //&& less <= great
          @  && (\forall int i; left <= i && i < less; a[i] < pivot)
          @  && (\forall int j; less <= j && j < k-1 && j <= great; a[j] == pivot) // k? k-1?
          @  && (\forall int l; great < l && l <= right; a[l] > pivot)
          @  && (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j]))
          @  && (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
          @ loop_invariant (\exists int p; left <= p && p <= right; a[p] == pivot);
          @ //loop_invariant a[k-1] == pivot || a[mid] == pivot;
          @ assignable a[left..right], less, great;
          @ decreases great + 100 - k;
          @*/ 
        for (int k = less; k <= great; ++k) {
            if (a[k] == pivot) {
                continue;
            }
            int ak = a[k];
            if (ak < pivot) { // Move a[k] to left part
                a[k] = a[less];
                a[less] = ak;
                ++less;
            } else { // a[k] > pivot - Move a[k] to right part
                case_right(a, left, right, k, pivot);
            }
        }
    }
        /*@ 
          @ normal_behaviour
          @ requires 0 <= left && left < right && right < a.length;
          @ requires less <= k && k <= great
          @ && left <= less && left <= great &&  great <= right
          @ && (\forall int i; left <= i && i < less; a[i] < pivot)
          @ && (\forall int j; less <= j && j < k; a[j] == pivot)
          @ && (\forall int l; great < l && l <= right; a[l] > pivot);
          @ //requires k == less || a[k-1] == pivot;
          @ requires a[k] > pivot; //path condition
          @ requires k <= great; //loop condition
          @ requires (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
          @ requires (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
          @ requires (\exists int p; left <= p && p <= right; a[p] == pivot);
          @
          @ ensures less - 1 <= k && k <= great + 1
          @ && left <= less && great <= right
          @ && (\forall int i; left <= i && i < less; a[i] < pivot)
          @ && (\forall int j; less <= j && j < k; a[j] == pivot)
          @ && (\forall int l; great < l && l <= right; a[l] > pivot);
          @ ensures great < \old(great);
          @ ensures \old(less) <= less;
          @ //ensures a[k] == pivot || great < k;
          @ ensures left <= great && less <= right;
          @ ensures (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
          @ ensures (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
          @ ensures (\exists int p; left <= p && p <= right; a[p] == pivot);
          @ ensures less <= great;
          @ assignable great, less, a[less..great];
          @*/
    static void case_right(int[] a, int left, int right, int k, int pivot) {
        int ak = a[k];
        /*@
          @ loop_invariant
          @     a[k] > pivot
          @  && 0 <= left && right < a.length
          @  && left <= less && great <= right
          @  && left <= great
          @  && less <= k 
          @  //&& (k == less || a[k-1] == pivot)
          @  && k <= great+1
          @  && (\forall int i; great < i && i <= right; a[i] > pivot)
          @  && (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j]))
          @  && (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]))
          @  && (\exists int p; left <= p && p <= right; a[p] == pivot);
          @ assignable great;
          @ decreases great;
          @*/ 
        while (a[great] > pivot) {
            --great;
        }  
        
        // if great < k here, split() is done. Changed the code to do that properly
        if (great < k) {}
        else if (a[great] < pivot) { // a[great] <= pivot
            a[k] = a[less];
            a[less] = a[great];
            ++less;
            a[great] = ak;
            --great;
        } else { // a[great] == pivot
            a[k] = pivot;
            a[great] = ak;
            --great;
        }
    }
    /*@
      @ normal_behaviour
      @ requires 0 <= left && right < a.length;
      @ requires (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures (\forall int i; left <= i && i <= right; (\forall int j; left <= j && j <= right; i < j ==> a[i] <= a[j]));
      @ ensures (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
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

