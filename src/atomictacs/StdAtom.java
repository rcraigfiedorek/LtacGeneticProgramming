package atomictacs;

import java.util.List;

public class StdAtom implements AtomicTactic {

	public String code;
	public List<Integer> vars;
	
	public StdAtom(String code, List<Integer> vars) {
		this.code = code;
		this.vars = vars;
	}
	
	public int numNewVars() {
		return 0;
	}

	public int numOldVars() {
		return vars.size();
	}

	public String toCode() {
		String out = code;
		if (vars.size() > 1)
			out += " (";
		for (Integer var : vars)
			out += " v" + Integer.toString(var);
		if (vars.size() > 1)
			out += " )";
		return out;
	}

}
