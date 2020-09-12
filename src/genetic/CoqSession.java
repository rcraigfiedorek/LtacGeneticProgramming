package genetic;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.ExpectIOException;
import net.sf.expectit.matcher.Matchers;

public class CoqSession {

	private Process coqtop;
	private Expect main;
	private Expect err;
	public List<String> imports;
	private boolean proofMode;
	
	public CoqSession(List<String> imports) throws IOException {
		coqtop = Runtime.getRuntime().exec("/usr/local/bin/coqtop");
		main = new ExpectBuilder()
				.withInputs(coqtop.getInputStream())
		        .withOutput(coqtop.getOutputStream())
		        .withTimeout(1000, TimeUnit.MICROSECONDS)
		        .withExceptionOnFailure()
		        .build();
		
		err = new ExpectBuilder()
				.withInputs(coqtop.getErrorStream())
				.withTimeout(200, TimeUnit.MILLISECONDS)
				.withExceptionOnFailure()
				.build();
		proofMode = false;
		
		err.expect(Matchers.contains("Coq < "));
		main.sendLine("Set Default Timeout 2.");
		getMainOut();
		this.imports = imports;
		for (String imp : imports) {
			main.sendLine("Require Import " + imp + ".");
			getMainOut();
		}
	}
	
	public CoqSession dup(boolean killOld) throws IOException {
		if (killOld)
			kill();
		return new CoqSession(imports);
	}
	
	public void proofMode(Theorem theorem) throws IOException, Exception {
		if (proofMode)
			throw new Exception("Already in proof mode.");
		err.expect(Matchers.contains("Coq < "));
		
		main.sendLine("Theorem theorem : " + theorem + ".");
		getMainOut();
		proofMode = true;
	}
	
	public void exitProof() throws IOException, Exception {
		if (!proofMode)
			throw new Exception("Not in proof mode.");
		main.sendLine("Abort.");
		proofMode = false;
	}
	
	public List<String> getErrOut() throws IOException {
		List<String> results = new ArrayList<>();
		while (true) {
			try {
				results.add(err.expect(Matchers.contains("\n")).getBefore());
			} catch (ExpectIOException e) {
				break;
			}
		}
		return results;
	}
	
	public List<String> getMainOut() throws IOException {
		return getMainOut(0);
	}
	
	public List<String> getMainOut(int iter) throws IOException {
		List<String> results = new ArrayList<>();
		if (iter >= 100)
			return results;
		while (true) {
			try {
				results.add(main.expect(Matchers.contains("\n")).getBefore());
			} catch (ExpectIOException e) {
				if (results.isEmpty()) {
					//System.out.println("LOOP WORKS");
					return getMainOut(iter + 1);
				}
				break;
			}
		}
		return results;
	}
	
	public Tuple<Double,List<String>> fitnessFunc(ProofTactic tac, Theorem theorem) throws Exception {
		return fitnessFunc(tac.toCode(), theorem);
	}
	
	public Tuple<Double,List<String>> fitnessFunc(String tac, Theorem theorem) throws Exception {
		proofMode(theorem);

		//System.out.println(tac);
		
		main.sendLine(tac);
		
		List<String> results = getMainOut();
		//System.out.println(results);
		
		if (results.isEmpty()) {
			exitProof();
			return new Tuple<>(-1.0, new ArrayList<>());
		}
		//System.out.println("Results: " + results);
		List<String> goals = new ArrayList<>();
		boolean firstgoalfound = false;
		boolean searchingforfirst = false;
		for (int i = 0; i < results.size(); i++) {
			String result = results.get(i);
			if (result.contains("Error") || result.contains("error") || result.contains("Timeout") || result.contains("timeout")) {
				//error, return negative value
				//System.out.println("exit 1");
				exitProof();
				getMainOut();
				return new Tuple<>(-1.0, new ArrayList<>());
			}
			if (result.contains("Current goal aborted")) {
				exitProof();
				return fitnessFunc(tac, theorem);
			}
			if (result.contains("No more subgoals")) {
				//theorem proven, return obscenely high value
				//System.out.println("exit 2");
				exitProof();
				return new Tuple<>(Double.POSITIVE_INFINITY, new ArrayList<>());
			}
			if (result.contains("subgoal")) {
				if (firstgoalfound && !searchingforfirst) {
					try {
						goals.add(results.get(i+1));
					} catch (IndexOutOfBoundsException e) {
						exitProof();
						return new Tuple<>(-1.0, new ArrayList<>());
					}
					continue;
				}
				else {
					firstgoalfound = true;
					searchingforfirst = true;
					continue;
				}
			}
			if (result.contains("====") && searchingforfirst) {
				goals.add(results.get(i+1));
				searchingforfirst = false;
			}		
		}
		
		double goalsize = 0;
		goalsize += goals.size() - 1;
		for (String goal : goals) {
			goalsize += goal.split("\\s+").length;
		}
		
		main.sendLine("Show Proof.");
		
		List<String> proofterm = getMainOut();
		
		double proofsize = 0;
		for (String line : proofterm) {
			proofsize += line.split("\\s+").length;
		}
		//System.out.println("exit 3");
		//System.out.println("Goals: " + goals);
		//System.out.println("Proofterm: " + proofterm);
		exitProof();
		//System.out.println("Score: " + proofsize/goalsize);
		
		double score;
		if (goals.size() > 4)
			score = 1.0;
		else score = proofsize / goalsize;
		
		//System.out.println(score);
		
		return new Tuple<>( score , goals);
		
		//return new Tuple<>( proofsize / (goalsize * goalMod(goals.size(), 4) ) , goals ) ;
		
	}
	
	public int goalMod(int goalnum, int allowablegoals) {
		if (goalnum <= allowablegoals)
			return 1;
		else
			return goalnum * goalnum * goalnum * goalnum * goalnum;
	}
	
	public void kill() throws IOException {
		main.close();
		err.close();
		coqtop.destroy();
	}
	
	/*
	public List<Integer> casesNeeded(ProofTactic prevTac, String newTac, Theorem theorem) throws Exception {
		
		proofMode(theorem);
		
		String base = prevTac.toCode().substring(0, prevTac.toCode().length() - 1);
		
		String nocases = base + "; " + newTac + ".";
		main.sendLine(nocases);
		
		List<String> results = getMainOut();
		
		for (String result : results) {
			if (result.contains("error") || result.contains("Error")) {
				exitProof();
				return null;
			}
		}
		
		main.sendLine("Back.");
		
		getMainOut();
		
		String manybranches = base + "; " + newTac + " as [ | | | | | | | | | | | | ].";
		
		main.sendLine(manybranches);
		
		results = getMainOut();
				
		int branchNum = -1;
		for (String result : results) {
			if (result.contains("Error")) {
				String[] words;
				try {
				words = result.split("\\s+");
				}
				catch (ArrayIndexOutOfBoundsException e) {
					exitProof();
					return null;
				}
				try {
				branchNum = Integer.parseInt(words[6]);
				} catch (NumberFormatException e) {
					exitProof();
					return null;
				}
				break;
			}
		}
		if (branchNum == -1) {
			//System.out.println("Error: couldn't find number of branches");
			exitProof();
			return null;
		}
		
		String testvars = " ";
		for (int i=0; i<9; i++)
			testvars += "test" + Integer.toString(i) + " ";
		
		String bracketlist = "[";
		for (int i=0; i < branchNum; i++) {
			if (i != 0)
				bracketlist += "|";
			bracketlist += testvars;
		}
		bracketlist += "]";
		
		String manyvars = base + "; " + newTac + " as " + bracketlist + ".";
		
		main.sendLine(manyvars);
		
		String errout = String.join(" ",getErrOut());
		
		List<Integer> out = new ArrayList<>();
		
		while (errout.contains("Warning: Unused")) {
			errout = errout.substring(errout.indexOf("Warning: Unused") + 43);
			out.add(Integer.parseInt(errout.substring(0, 1)));
		}
		
		exitProof();
		return out;
	
	}
	*/

	
}
