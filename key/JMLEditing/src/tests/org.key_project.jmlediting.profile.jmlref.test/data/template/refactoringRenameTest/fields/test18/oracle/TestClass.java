package test;

public class TestClass {
    public int newName;
    
    /*@ normal_behavior
      @ ensures Integer.toString(newName).equals("5") ==> \result == 0;
      @ assignable \nothing;
      @*/
    public int accessBalanceFromOtherClass() {
        
        if (Integer.toString(newName).equals("5"))
            return 0;
        else
            return 1;
    }
    
    /*@ normal_behavior
      @ ensures \result == this.getClass().getDeclaredField("balance").equals(newName);
      @*/
    public boolean doSomething() throws NoSuchFieldException, SecurityException{
        
        return (this.getClass().getDeclaredField("balance").equals(newName));
    }
}
