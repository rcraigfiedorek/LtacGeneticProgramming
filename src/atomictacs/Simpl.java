package atomictacs;

public class Simpl implements AtomicTactic {

	public Simpl() {
		;
	}
	
	public int numNewVars() {
		return 0;
	}

	public int numOldVars() {
		return 0;
	}

	public String toCode() {
		return "simpl";
	}
	/*
	public AtomicTactic shiftUp(int i) {
		return new Simpl();
	}
	
	public AtomicTactic shiftDown(int i) {
		return new Simpl();
	}
	*/

}
