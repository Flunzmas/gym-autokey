//import java.util.Arrays;
//import java.util.Random;

class ThreeWayQs {

//public static void main(String[] args) {
//    Random rand = new Random();
//    int[] a = new int[50];
//    for (int i = 0; i < a.length; i++) {
//        a[i] = rand.nextInt(100);
//    }
//    sort(a);
//}

static int less;
static int great;
static int pivot;
/*@
  @ public normal_behaviour requires true;
  @ assignable less, great, pivot, a[*];
  @ ensures_free (\forall int i; 0 <= i && i < a.length; (\forall int j; 0 <= j && j < a.length; i < j ==> a[i] <= a[j]));
  @*/ 
public static void sort(int[] a) {
    if (a.length > 0) {
        sort(a, 0, a.length-1);
    }
}

/*@
  @ normal_behaviour requires true;
  @ requires_free 0 <= left && right < a.length;
  @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
  @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
  @ ensures_free (\forall int i; left <= i && i <= right; (\forall int j; left <= j && j <= right; i < j ==> a[i] <= a[j]));
  @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
  @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
  @ assignable less, great, pivot, a[left..right];
  @ measured_by right - left + 1;
  @*/ 
static void sort(int[] a, int left, int right) { 
    if (left < right) {
        split(a, left, right);
        int lless = less;
        int lgreat = great;
        int lpivot = pivot;

        if(left < less-1) sort(a, left, lless-1);

        if(lgreat+1 < right) sort(a, lgreat+1, right);
    }
}

/*@
  @ normal_behaviour requires true;
  @ requires_free 0 <= left && left < right && right < a.length;
  @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
  @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
  @ ensures_free (\forall int i; left <= i && i < less; a[i] < pivot);
  @ ensures_free (\forall int j; less <= j && j <= great; a[j] == pivot);
  @ ensures_free (\forall int l; great < l && l <= right; a[l] > pivot);
  @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
  @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
  @ ensures_free left <= great && less <= right;
  @ ensures_free left <= less && great <= right;
  @ ensures_free less <= great;
  @ assignable less, great, pivot, a[left..right];
  @*/ 
static void split(int[] a, int left, int right) {
    pivot = a[left];
    less = left;
    great = right;
    
    /*@ loop_invariant true;
      @ loop_invariant_free
      @     less < k && k <= great + 2
      @  && 0 <= left && left < right && right < a.length
      @  && left <= less && great <= right
      @  && left <= great && less <= right
      @  && less <= great
      @  && (\forall int i; left <= i && i < less; a[i] < pivot)
      @  && (\forall int j; less <= j && j < k && j <= great; a[j] == pivot)
      @  && (\forall int l; great < l && l <= right; a[l] > pivot)
      @  && (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j]))
      @  && (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ assignable a[left..right], less, great;
      @ decreases great + 100 - k;
      @*/ 
    for (int k = less + 1; k <= great; ++k) {   //changed init to k = less+1 because first step was always omitted anyway
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
        //System.out.println("left: " + left + " less: " + less + " k: " + k + " great: " + great + " right: " + right);
    }
}
    /*@ 
      @ normal_behaviour requires true;
      @ requires_free 0 <= left && left < right && right < a.length;
      @ requires_free less < k && k <= great
      @ && left <= less && left <= great &&  great <= right
      @ && (\forall int i; left <= i && i < less; a[i] < pivot)
      @ && (\forall int j; less <= j && j < k; a[j] == pivot)
      @ && (\forall int l; great < l && l <= right; a[l] > pivot);
      @ requires_free a[k] > pivot; //path condition
      @ requires_free k <= great; //loop condition
      @ requires_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ requires_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free less <= k && k <= great + 1
      @ && left <= less && great <= right
      @ && (\forall int i; left <= i && i < less; a[i] < pivot)
      @ && (\forall int j; less <= j && j < k; a[j] == pivot)
      @ && (\forall int l; great < l && l <= right; a[l] > pivot);
      @ ensures_free great < \old(great);
      @ ensures_free \old(less) <= less;
      @ ensures_free a[k] == pivot || great < k;
      @ ensures_free left <= great && less <= right;
      @ ensures_free (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j])); 
      @ ensures_free (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ ensures_free less <= great;
      @ assignable great, less, a[less..great];
      @*/
static void case_right(int[] a, int left, int right, int k, int pivot) {
    int ak = a[k];
    /*@ loop_invariant true;
      @ loop_invariant_free
      @     a[k] > pivot
      @  && a[k-1] == pivot
      @  && 0 <= left && right < a.length
      @  && left <= less && great <= right
      @  && left <= great
      @  && less < k && k <= great+1
      @  && (\forall int i; great < i && i <= right; a[i] > pivot)
      @  && (\forall int i; 0 <= i && i < left; (\forall int j; left <= j && j < a.length; a[i] <= a[j]))
      @  && (\forall int i; 0 <= i && i <= right; (\forall int j; right < j && j < a.length; a[i] <= a[j]));
      @ assignable great;
      @ decreases great;
      @*/ 
    while (a[great] > pivot) {
        --great;
    }   // if great < k here, split() is done. Changed the code to do that properly
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
}
