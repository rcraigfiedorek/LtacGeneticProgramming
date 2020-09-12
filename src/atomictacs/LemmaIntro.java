package atomictacs;

public class LemmaIntro implements AtomicTactic {

	public String lemma;
	
	public LemmaIntro(String lemma) {
		this.lemma = lemma;
	}
	
	
	public int numNewVars() {
		return 1;
	}

	public int numOldVars() {
		return 0;
	}

	public String toCode() {
		return "pose proof " + lemma + " as";
	}

}
