class SwapValues {

    static int less, great, pivot1, pivot2;
    /*@
      @ normal_behaviour
      @ requires 0 < l && l < g && g < a.length-1;
      @ requires p1 < p2;
      @ requires (\forall int i; l <= i && i <= g; p1 <= a[i] && a[i] <= p2);
      @ requires (\exists int j; l <= j && j <= g; a[j] == p1);
      @ requires (\exists int m; l <= m && m <= g; a[m] == p2);
      @ ensures (\forall int i; l <= i && i < less; a[i] == pivot1);
      @ ensures (\forall int j; less <= j && j <= great; pivot1 < a[j] && a[j] < pivot2);
      @ ensures (\forall int m; great < m && m < g; a[m] == pivot2);
      @ assignable less, great, pivot1, pivot2, a[l..g];
      @*/ 
    static void swapValues(int[] a, int l, int g, int p1, int p2) {
        // for easier specification
        less = l;
        great = g;
        pivot1 = p1;
        pivot2 = p2;

        move_less_right(a,l,g);
        move_great_left(a,l,g,less);    //last parameter is meaningless here
    
        outer:

        /*@
          @ loop_invariant
          @     0 < less && l <= less && 0 <= great && great <= g && less <= g && pivot1 < pivot2
          @  && (\exists int m; l <= m && m <= g; a[m] == pivot2)
          @  && less-1 <= k && k <= great+1 && k < a.length
          @  && (\forall int i; l <= i && i < less; a[i] == pivot1)
          @  && (\forall int j; less <= j && j < k+1; pivot1 < a[j] && a[j] < pivot2)
          @  && (\forall int t; k+1 <= t && t <= great; pivot1 <= a[t] && a[t] <= pivot2)
          @  && (\forall int m; great < m && m <= g; a[m] == pivot2);
          @ decreases great - k;
          @ assignable less, great, a[l..g];
          @*/ 
        for (int k = less - 1; ++k <= great; ) {
            int ak = a[k];
            if (ak == pivot1) { // Move a[k] to left part
                case_left(a,k,l,g);
            } else if (ak == pivot2) { // Move a[k] to right part
                move_great_left(a,l,g,k);
                if (great == k) {
                    great--;
                    break outer; // We're done here
                }
                case_right(a,k,l,g);
            }

        }
    }

   
    static void case_right(int[] a, int k, int l, int g) {
        int ak = a[k];
        if (a[great] == pivot1) { // a[great] < pivot2
            a[k] = a[less];
            a[less] = pivot1;
            ++less;
        } else { // pivot1 < a[great] < pivot2
            a[k] = a[great];
        } 
        a[great] = ak;
        --great;
    }

    static void case_left(int[] a, int k, int l, int g) {
        int ak = a[k];
        a[k] = a[less];
        a[less] = ak;
        ++less;
    }

    /*@
      @ normal_behaviour
      @ requires 0 <= less && less < great && great <= g && great < a.length;
      @ requires pivot1 < pivot2;
      @ requires (\forall int i; less <= i && i <= great; pivot1 <= a[i] && a[i] <= pivot2);
      @ requires (\exists int j; less <= j && j <= great; a[j] == pivot1);
      @ requires (\exists int m; less <= m && m <= great; a[m] == pivot2);
      @ ensures 0 <= less && less <= great && great < a.length;
      @ ensures (\forall int i; \old(less) <= i && i < less; a[i] == pivot1);
      @ ensures (\exists int m; less <= m && m <= great; a[m] == pivot2);
      @ ensures \old(less) <= less;
      @ assignable less;
      @*/ 
    static void move_less_right(int[] a, int l, int g) {
        /*@
          @ loop_invariant
          @     0 <= less && less <= great && great < a.length
          @  && (\forall int i; \old(less) <= i && i < less; a[i] == pivot1)
          @  && (\exists int j; less <= j && j <= great; a[j] == pivot2)
          @  && \old(less) <= less;
          @ assignable less;
          @ decreases great - less;
          @*/ 
        while (a[less] == pivot1) {
            ++less;
        }
    }

    /*@ normal_behaviour
      @ requires great < a.length;
      @ requires k <= great;
      @ requires k >= 0;
      @ ensures  great < a.length;
      @ ensures (\forall int i; great < i && i <= \old(great); a[i] == pivot2);
      @ ensures great == k || a[great] !=  pivot2;
      @ ensures k <= great && great <= \old(great);
      @ assignable great;
      @*/
    static void move_great_left(int[] a, int l, int g, int k) {
        /*@
          @ loop_invariant
          @     great < a.length 
          @  && (\forall int i; great < i && i <= \old(great); a[i] == pivot2)
          @  && k <= great && great <= \old(great);
          @ assignable great;
          @ decreases great + 1;
          @*/ 
        while (a[great] == pivot2 && great != k) {
            great--;
        }
    }
}
