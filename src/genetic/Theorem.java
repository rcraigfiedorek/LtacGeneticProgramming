package genetic;

import java.util.List;

public class Theorem {

	public String type;
	public List<String> lemmata;
	public List<String> availtacs;
	public int maxApp;
	
	public Theorem(String type, List<String> lemmata, List<String> availtacs) {
		this.type = type;
		this.lemmata = lemmata;
		this.availtacs = availtacs;
		maxApp = 1;
	}
	
	public Theorem(String type, List<String> lemmata, List<String> availtacs, int maxApp) {
		this.type = type;
		this.lemmata = lemmata;
		this.availtacs = availtacs;
		this.maxApp = maxApp;
	}
	
	public String toString() {
		return type;
	}
	
}
