package atomictacs;

public class Reflexivity implements AtomicTactic {

	public Reflexivity() {
		;
	}
	
	public int numNewVars() {
		return 0;
	}

	public int numOldVars() {
		return 0;
	}

	public String toCode() {
		return "reflexivity";
	}
	/*
	public AtomicTactic shiftUp(int i) {
		return new Reflexivity();
	}
	
	public AtomicTactic shiftDown(int i) {
		return new Reflexivity();
	}
	*/

}
