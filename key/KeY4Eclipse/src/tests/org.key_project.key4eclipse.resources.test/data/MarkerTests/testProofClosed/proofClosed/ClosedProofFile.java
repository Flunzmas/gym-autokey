package proofClosed;

public class ClosedProofFile {
	/*@
	  @ ensures \result == x + y;
	  @*/
	public static int add(int x, int y) {
		return x + y;
	}
}
