package genetic;

public class Tuple<X, Y> { 
	public final X first; 
	public final Y second;
	public Tuple(X x, Y y) { 
		this.first = x; 
		this.second = y; 
	}
	
	public String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}

}
