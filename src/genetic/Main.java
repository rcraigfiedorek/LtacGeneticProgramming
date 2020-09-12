package genetic;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.ExpectIOException;
import net.sf.expectit.matcher.Matchers;

import atomictacs.*;

public class Main {
	
	public static final Random rand = new Random();
	
	public static void main(String[] Args) throws Exception {
		
		/*
		 * Theorems to show:
		 * 
		 * plus_comm
		 * mult_comm
		 * mult_assoc
		 * mult_distr
		 * rev_unit
		 * rev_involutive
		 * 
		 * 
		 */
		
		
		Theorem theorem = new Theorem(
				"forall n :nat, n + 0 = n",
				//"forall (X:Type)(a b c : X), a = b -> b = c -> a = c",
				Arrays.asList(), //lemmas
				Arrays.asList( //tactics
						"intro",
						"simpl",
						"reflexivity",
						"rewrite ->",
						"rewrite <-",
						"elim"/*,
						
						"discriminate",
						"injection",
						"apply",
						
						"case_eq",
						"subst",
						
						"generalize dependent",
						
						"f_equal",

						"left",
						"right",
						"exists"*/
						),
				1); //depth
		CoqSession coq = new CoqSession(Arrays.asList());
		
		
		/*
		 * Average time for "forall n m : nat, n + m = m + n" is 96
		 * 
		 * Average time for "forall n m : nat, n * S m = n * m + n" is 6
		 * 		plus_n_Sm
		 * 		plus_assoc
		 * Average time for "forall n m : nat, n * m = m * n" is 17
		 * 		plus_comm
		 * 		mult_n_Sm
		 * 
		 * 		
		 */
		
		long begin = System.currentTimeMillis();
		
		System.out.println(Main.evolve(coq, theorem, 100, 500, 0.2, 0.95, 0.15, 5).toCode());
		
		long end = System.currentTimeMillis();
		long diff = end - begin;
		long totalseconds = diff / 1000;
		long minutes = totalseconds / 60;
		long seconds = totalseconds % 60;
		
		System.out.println("Process took " + minutes + " minutes and " + seconds + " seconds.");
		
		//testCrossover(coq,theorem, 50);
		
		coq.kill();
	}
	
	public static long timeEvolution(CoqSession coq, Theorem theorem, int popsize, int maxgen, double mutationrate, double pexp, double pnew, int numpreserve) throws Exception {
		
		long begin = System.currentTimeMillis();
		
		Main.evolve(coq, theorem, 100, 500, 0.2, 0.95, 0.15, 5).toCode();
		
		long end = System.currentTimeMillis();
		long diff = end - begin;
		long totalseconds = diff / 1000;
		return totalseconds;
	}
	
	public static void testCrossover(CoqSession coq, Theorem theorem, int testnum) throws IOException, Exception {
		int childrenoffset = 0;
		int improve = 0;
		int worsen = 0;
		for (int i = 0; i < testnum; i++) {
			System.out.println("first:");
			ProofTactic first = ProofTactic.genTac(coq, theorem);
			System.out.println(first.toCode());
			double firstresult = coq.fitnessFunc(first, theorem).first;
			System.out.println(firstresult);
			System.out.println();
			
			System.out.println("second:");
			ProofTactic second = ProofTactic.genTac(coq, theorem);
			System.out.println(second.toCode());
			double secondresult = coq.fitnessFunc(second, theorem).first;
			System.out.println(secondresult);
			System.out.println();
			
			System.out.println("Child:");
			ProofTactic child = first.crossOver(second);
			System.out.println(child.toCode());
			double childresult = coq.fitnessFunc(child, theorem).first;
			System.out.println(childresult);
			for (int j = 2; j < child.size(); j++) {
				if (child.get(j) instanceof LemmaIntro)
					System.out.println("\n\n\n\n\n\n\n\n HERERKJSDHFKSDJFHSKLDJFH \n\n\n\n\n\n\n\n\n");
			}
			System.out.println();
			
			childrenoffset += childresult - (firstresult + secondresult) / 2;
			if (childresult > (firstresult + secondresult) / 2)
				improve++;
			else if (childresult < (firstresult + secondresult) / 2)
				worsen++;
		}
		System.out.println("Offset: " + childrenoffset);
		System.out.println("Number improved " + improve);
		System.out.println("Number worsened " + worsen);
	}
	
	public static void testMutation(CoqSession coq, Theorem theorem, int testnum) throws IOException, Exception {
		double mutoffset = 0;
		for (int i = 0; i < testnum; i++) {
			System.out.println("original:");
			ProofTactic tac = ProofTactic.genTac(coq, theorem);
			System.out.println(tac.toCode());
			double result = coq.fitnessFunc(tac, theorem).first;
			System.out.println(result);
			System.out.println();
			
			System.out.println("mutated:");
			ProofTactic mutated = tac.mutate(0.05);
			System.out.println(mutated.toCode());
			double mutresult = coq.fitnessFunc(mutated, theorem).first;
			System.out.println(mutresult);
			System.out.println();
			
			mutoffset += mutresult - result;
		}
		System.out.println("mutoffset: " + Double.toString(mutoffset));
	}
	
	public static int expselect(int max, double pexp) {
		int out = max;
		while (out >= max || out < 0) {
			out = (int) (Math.log(rand.nextDouble()) / Math.log(pexp));
		}
		return out;
	}
	
	public static ProofTactic evolve(CoqSession coqs, Theorem theorem, int popsize, int maxgen, double mutationrate, double pexp, double pnew, int numpreserve) throws Exception {
		
		CoqSession coq = coqs;
		List<ThreeTuple<ProofTactic,Double,List<String>>> population = new ArrayList<>();
		while (population.size() < popsize) {
			System.out.print(".");
			ProofTactic tac = ProofTactic.genTac(coq, theorem);
			Tuple<Double,List<String>> fitness;
			try {
				fitness = coq.fitnessFunc(tac, theorem);
			}
			catch (ExpectIOException e) {
				coq = coq.dup(coq != coqs);
				continue;
			}
			population.add(new ThreeTuple<>(tac,fitness.first,fitness.second));
		}
		
		for (int gen = 0; gen < maxgen; gen++) {
			Collections.sort(population, (t1,t2) -> t2.second.compareTo(t1.second));
			
			System.out.println("\n\nGeneration " + gen + ". Best tactics:");
			
			for (int i = 0; i < 5; i++) {
				ThreeTuple<ProofTactic,Double,List<String>> ind = population.get(i);
				System.out.println(ind.first.toCode());
				System.out.println(ind.second);
			}
			
			if (Double.isInfinite(population.get(0).second)) {
				if (coq != coqs) coq.kill();
				return population.get(0).first;
			}
			
			List<ThreeTuple<ProofTactic,Double,List<String>>> newpop = new ArrayList<>();
			
			for (int i = 0; i < numpreserve; i++) {
				ProofTactic tac = population.get(i).first;
				Tuple<Double,List<String>> fitness;
				try {
					fitness = coq.fitnessFunc(tac, theorem);
				}
				catch (ExpectIOException e) {
					coq = coq.dup(coq != coqs);
					i--;
					continue;
				}
				newpop.add(new ThreeTuple<>(tac,fitness.first,fitness.second));
			}
			
			while (population.get(population.size() - 1).second <= 0)
				population.remove(population.size() - 1);
			
			addInd: while (newpop.size() < popsize) {
				System.out.print(".");
				if (rand.nextDouble() > pnew) {
					ProofTactic parent1 = population.get(expselect(population.size(),pexp)).first;
					ProofTactic parent2 = population.get(expselect(population.size(),pexp)).first;
					ProofTactic child = parent1.crossOver(parent2).mutate(mutationrate);
					if (child.size() > 200)
						continue;
					Tuple<Double,List<String>> fitness;
					try {
						fitness = coq.fitnessFunc(child, theorem);
					}
					catch (ExpectIOException e) {
						coq = coq.dup(coq != coqs);
						continue;
					}
					/*
					for (ThreeTuple<ProofTactic,Double,List<String>> ind : newpop)
						if (ind.second.equals(fitness.first))
							continue addInd;*/
					newpop.add(new ThreeTuple<>(child,fitness.first,fitness.second));
				}
				else {
					ProofTactic tac = ProofTactic.genTac(coq, theorem);
					Tuple<Double,List<String>> fitness;
					try {
						fitness = coq.fitnessFunc(tac, theorem);
					}
					catch (ExpectIOException e) {
						coq = coq.dup(coq != coqs);
						continue;
					}
					newpop.add(new ThreeTuple<>(tac,fitness.first,fitness.second));
				}
			}
			population = newpop;
		}
		Collections.sort(population, (t1,t2) -> t2.second.compareTo(t1.second));
		if (coq != coqs) coq.kill();
		return population.get(0).first;
		
	}
	
	
}


