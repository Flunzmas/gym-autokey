package mainpack;
public class Main {
    
    /*@
      @ public invariant test1p2.Settings.x<100;
      @*/
    int x=0;
    
    /*@ 
      @ normal_behavior
      @ ensures test1p2.Settings.x == 42;
      @*/
    public static void bla() {
        test1p2.Settings.x = 42;
    }
}