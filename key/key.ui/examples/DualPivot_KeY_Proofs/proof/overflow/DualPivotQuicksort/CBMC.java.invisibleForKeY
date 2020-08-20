class CBMC {

    static int e1,e2,e3,e4,e5,seventh;

    static void calcE(int left, int right) {
        int length = right - left + 1;
        seventh = (length >> 3) + (length >> 6);
        seventh++;
        e3 = (left + right) >>> 1; // The midpoint
        e2 = e3 - seventh;
        e1 = e2 - seventh;
        e4 = e3 + seventh;
        e5 = e4 + seventh;
    }

    public static void main(int left, int right, int[] a) {
        if( 0 <= left && left < right && right - left >= 46 && right < Integer.MAX_VALUE) {
            calcE(left, right);
            assert left < e1 && e1 < e2 && e2 < e3 && e3 < e4 && e4 < e5 && e5 < right;
        }
    }

    // public static void main(String[] arg) {
    //     calcE(0, Integer.MAX_VALUE);
    //     System.out.println(java.util.Arrays.asList(e1, e2, e3, e4, e5, seventh));
    // }
}

