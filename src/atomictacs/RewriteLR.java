package atomictacs;

import java.util.Collections;
import java.util.List;

public class RewriteLR implements AtomicTactic {

	public final List<Integer> vars;
	
	public RewriteLR(List<Integer> vars) {
		this.vars = vars;
	}
	
	public RewriteLR(int var) {
		vars = Collections.singletonList(var);
	}
	
	public int numNewVars() {
		return 0;
	}

	public int numOldVars() {
		return vars.size();
	}

	public String toCode() {
		String out = "rewrite -> (";
		for (Integer var : vars)
			out += " v" + Integer.toString(var);
		out += " )";
		return out;
	}

}
