package genetic;

public class ThreeTuple<X, Y, Z> { 
	public final X first; 
	public final Y second;
	public final Z third;
	public ThreeTuple(X x, Y y, Z z) { 
		this.first = x; 
		this.second = y; 
		this.third = z;
	}
	
	public String toString() {
		return "(" + first.toString() + ", " + second.toString() + ", " + third.toString() + ")";
	}

}

