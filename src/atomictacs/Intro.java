package atomictacs;

public class Intro implements AtomicTactic {

	
	public Intro() {
		;
	}
	
	public int numNewVars() {
		return 1;
	}

	public int numOldVars() {
		return 0;
	}

	public String toCode() {
		return "intro";
	}
	/*
	public AtomicTactic shiftUp(int insertedVar) {
		if (var < insertedVar) {
			//System.out.println("Error: inserting var greater than future introvar");
			return new Intro(var);
		}
		else
			return new Intro(var + 1);
	}
	
	public AtomicTactic shiftDown(int deletedVar) {
		if (var <= deletedVar) {
			//System.out.println("Error: deleting var greater than or equal to future introvar");
			return new Intro(var);
		}
		else
			return new Intro(var - 1);
	}
	*/

}
