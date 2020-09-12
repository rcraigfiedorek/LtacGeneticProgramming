package atomictacs;

import java.util.Map;

public interface AtomicTactic {

	public int numNewVars();
	public int numOldVars();
	public String toCode();
	
}
