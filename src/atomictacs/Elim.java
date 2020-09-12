package atomictacs;

public class Elim implements AtomicTactic {

	public final int var;
	
	public Elim(int var) {
		this.var = var;
	}
	
	public int numNewVars() {
		return 0;
	}

	public int numOldVars() {
		return 1;
	}

	public String toCode() {
		return "elim v" + Integer.toString(var);
	}

}
