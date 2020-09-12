package second_try;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import atomictacs.*;

public class ProofTactic extends ArrayList<AtomicTactic> {
	
	public final static Random rand = new Random();
	public CoqSession coq;
	public Theorem theorem;
	public int lemnum;
	
	public ProofTactic(CoqSession coq, Theorem theorem) {
		super();
		this.coq = coq;
		this.theorem = theorem;
		lemnum = 0;
		for (String lemma : theorem.lemmata) {
			lemnum++;
			add(new LemmaIntro(lemma));
		}
	}
	
	public int numVarsDefined() {
		int out = 0;
		for (AtomicTactic atom : this)
			out += atom.numNewVars();
		return out;
	}
	
	public String toCode() {
		int varnum = 0;
		String out = "";
		for (int i = 0; i < size(); i++) {
			if (i != 0)
				out += "; ";
			out += "try ";
			out += this.get(i).toCode();
			for (int j = 0; j < get(i).numNewVars(); j++) {
				out += " v" + Integer.toString(varnum);
				varnum++;
			}
		}
		out += ".";
		return out;
	}
	
	public void addRandAtom() throws Exception {
		List<String> availtacs = theorem.availtacs;
		while (true) {
			String choice = availtacs.get(rand.nextInt(availtacs.size()));
			if (choice.equals("intro")) {
				add(new Intro());
				break;
			}
			else if (choice.equals("simpl")) {
				add(new StdAtom("simpl", new ArrayList<>()));
				break;
			}
			else if (choice.equals("reflexivity")) {
				add(new StdAtom("reflexivity", new ArrayList<>()));
				break;
			}
			else if (choice.equals("f_equal")) {
				add(new StdAtom("f_equal", new ArrayList<>()));
				break;
			}
			else if (choice.equals("split")) {
				add(new StdAtom("split", new ArrayList<>()));
				break;
			}
			else if (choice.equals("left")) {
				add(new StdAtom("left", new ArrayList<>()));
				break;
			}
			else if (choice.equals("right")) {
				add(new StdAtom("right", new ArrayList<>()));
				break;
			}
			else if (choice.equals("subst")) {
				add(new StdAtom("subst", new ArrayList<>()));
				break;
			}
			else if (choice.equals("elim")) {
				if (numVarsDefined() == 0)
					continue;
				add(new StdAtom("elim", Collections.singletonList(rand.nextInt(numVarsDefined()))));
			}
			else if (choice.equals("exists")) {
				if (numVarsDefined() == 0)
					continue;
				add(new StdAtom("exists", Collections.singletonList(rand.nextInt(numVarsDefined()))));
			}
			else if (choice.equals("generalize dependent")) {
				if (numVarsDefined() == 0)
					continue;
				add(new StdAtom("generalize dependent", Collections.singletonList(rand.nextInt(numVarsDefined()))));
			}
			else if (choice.equals("injection")) {
				if (numVarsDefined() == 0)
					continue;
				add(new StdAtom("injection", Collections.singletonList(rand.nextInt(numVarsDefined()))));
			}
			else if (choice.equals("discriminate")) {
				if (numVarsDefined() == 0)
					continue;
				add(new StdAtom("discriminate", Collections.singletonList(rand.nextInt(numVarsDefined()))));
			}
			else if (choice.equals("case_eq")) {
				int appnum = rand.nextInt(theorem.maxApp) + 1;
				if (numVarsDefined() < appnum)
					continue;
				List<Integer> args = new ArrayList<>();
				for (int i = 0; i < appnum; i++) {
					Integer var = rand.nextInt(numVarsDefined());
					if (args.contains(var)) {
						i--;
						continue;
					}
					args.add(var);
				}
				add(new StdAtom("case_eq",args));
			}
			else if (choice.equals("apply")) {
				int appnum = rand.nextInt(theorem.maxApp) + 1;
				if (numVarsDefined() < appnum)
					continue;
				List<Integer> args = new ArrayList<>();
				for (int i = 0; i < appnum; i++) {
					Integer var = rand.nextInt(numVarsDefined());
					if (args.contains(var)) {
						i--;
						continue;
					}
					args.add(var);
				}
				add(new StdAtom("apply",args));
			}
			else if (choice.equals("rewrite ->")) {
				int appnum = rand.nextInt(theorem.maxApp) + 1;
				if (numVarsDefined() < appnum)
					continue;
				List<Integer> args = new ArrayList<>();
				for (int i = 0; i < appnum; i++) {
					Integer var = rand.nextInt(numVarsDefined());
					if (args.contains(var)) {
						i--;
						continue;
					}
					args.add(var);
				}
				add(new StdAtom("rewrite ->",args));
			}
			else if (choice.equals("rewrite <-")) {
				int appnum = rand.nextInt(theorem.maxApp) + 1;
				if (numVarsDefined() < appnum)
					continue;
				List<Integer> args = new ArrayList<>();
				for (int i = 0; i < appnum; i++) {
					Integer var = rand.nextInt(numVarsDefined());
					if (args.contains(var)) {
						i--;
						continue;
					}
					args.add(var);
				}
				add(new StdAtom("rewrite <-",args));
			}
			else {
				throw new Exception("can't find tactic : \"" + choice + "\"");
			}
			break;
		}
	}
	
	public static ProofTactic genTac(CoqSession coq, Theorem theorem) throws Exception {
		ProofTactic tac = new ProofTactic(coq, theorem);
		int buildlength = rand.nextInt(30) + 2 + theorem.lemmata.size();
		while (tac.size() < buildlength) {
			tac.addRandAtom();
		}
		return tac;
	}
	
	public ProofTactic subTac(int fromindex, int toindex) {
		ProofTactic out = new ProofTactic(coq,theorem);
		out.clear();
		for (int i = fromindex; i < toindex; i++)
			out.add(get(i));
		return out;
	}
	
	public ProofTactic mutate(double probchange) throws Exception {
		return mutate(probchange, lemnum);
	}
	
	public ProofTactic mutate(double probchange, int index) throws Exception {
		//System.out.println(this.toCode());
		//System.out.println("    " + index);
		double roll = rand.nextDouble();
		if (roll < probchange) {
			int choice = rand.nextInt(2);
			if (choice == 0) {
				//deletion
				ProofTactic mutated = deletionmutation(index);
				if (index + 1 == size())
					return mutated;
				else
					return mutated.mutate(probchange, index);
			}
			else {
				//insertion
				ProofTactic mutated = insertionmutation(index); //will insert right before the object at index
				if (index + 1 == size())
					return mutated;
				else
					return mutated.mutate(probchange, index + 2);
			}
		}
		else {
			if (index + 1 == size())
				return this;
			else
				return mutate(probchange, index + 1);
		}
	}
	
	public ProofTactic insertionmutation(int index) throws Exception {
		ProofTactic out = subTac(0,index);
		out.addRandAtom();
		for (int i = index; i < size(); i++) {
			out.add(get(i));
		}
		return out;
	}
	
	public ProofTactic deletionmutation(int index) {
		ProofTactic out = new ProofTactic(coq,theorem);
		out.clear();
		for (int i = 0; i < size(); i++) {
			if (i != index) {
				out.add(get(i));
			}
		}
		return out;
	}
	
	public ProofTactic crossOver(ProofTactic other) throws Exception {
		int firstcut = rand.nextInt(size() - lemnum) + lemnum;
		int secondcut = rand.nextInt(size() - firstcut) + firstcut + 1;
		
		int otherfirstcut = rand.nextInt(other.size() - other.lemnum) + other.lemnum;
		int othersecondcut = rand.nextInt(other.size() - otherfirstcut) + otherfirstcut + 1;
		
		ProofTactic out = subTac(0, firstcut);
		
		out.addAll(other.subTac(otherfirstcut, othersecondcut));
		
		out.addAll(subTac(secondcut, size()));
		
		
		
		return out;
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
