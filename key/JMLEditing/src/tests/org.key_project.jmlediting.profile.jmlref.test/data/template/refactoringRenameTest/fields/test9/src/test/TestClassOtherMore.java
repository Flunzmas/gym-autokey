package test;

public class TestClassOtherMore {
    
    public TestClass mainClass = new TestClass();
    
    /*@ normal_behavior
    @ ensures \result == mainClass.balance;
    @ assignable \nothing;
    @*/
    private int accessBalanceFromMainClass() {
        return mainClass.getBalance();
    }

}
