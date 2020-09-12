package atomictacs;

public class Fequal implements AtomicTactic {

	public Fequal() {
		;
	}

	public int numNewVars() {
		return 0;
	}

	public int numOldVars() {
		return 0;
	}

	public String toCode() {
		return "f_equal";
	}

}
