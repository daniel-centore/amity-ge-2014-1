package amity.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import amity.ai.AmityAI;

public class GeneticCoefficientFinder
{
	static final int POPULATION_SIZE = 50;
	static final int COEFFICIENTS = 13;
	static final int AVG_CALCULATING_N = 10;
	static final double ELITIST = .05;
	static final int PARENTS = 3;
	static final int STD_DEVIATIONS = 3;
	//static final double MUTATION_PROBABILITY = .05;

	public static void main(String args[])
	{
		List<Individual> population = new ArrayList<>();
		
		// Create the initial population
		for (int i = 0; i < POPULATION_SIZE; i++)
		{
			Individual indiv = new Individual();
			
			indiv.coefficients = generateRandomCoefficients(COEFFICIENTS);
			indiv.moves = avgMoves(indiv.coefficients);
			
			System.out.println(i + " " + indiv.moves);		//
			
			population.add(indiv);
		}
		
		Collections.sort(population);		// sort
		
		System.out.println(population);
		
		// Iterate forever
		while (true)
		{
			List<Individual> newPopulation = new ArrayList<>();
			
			// Add elitists in
			int number = (int) (ELITIST * POPULATION_SIZE);
			for (int i = 0; i < number; i++)
				newPopulation.add(population.get(number));
			
			// Have sex
			for (int i = newPopulation.size(); i < POPULATION_SIZE; i++)
			{
				List<Individual> parents = new ArrayList<>();
				for (int j = 0; j < PARENTS; j++)
				{
					int element = (int) Math.abs(rand.nextGaussian() * (POPULATION_SIZE / STD_DEVIATIONS));
					if (element >= POPULATION_SIZE)
						element = POPULATION_SIZE - 1;
					
					parents.add(population.get(element));
				}
				
				
			}
			
			// Prepare for next iteration
			population = newPopulation;
			Collections.sort(population);
		}
	}

	static Random rand = new Random();

	public static double[] generateRandomCoefficients(int count)
	{
		double[] d = new double[count];

		for (int i = 0; i < d.length; i++)
			d[i] = rand.nextDouble();

		return d;
	}

	public static int avgMoves(double[] coefficients)
	{
		int sum = 0;
		for (int i = 0; i < AVG_CALCULATING_N; i++)
			sum += new TetrisGameRunner(new AmityAI(coefficients)).getResult();

		return sum / AVG_CALCULATING_N;
	}

}

class Individual implements Comparable<Individual>
{
	double[] coefficients; // 13
	int moves;

	@Override
	public String toString()
	{
		return "Individual [coefficients=" + Arrays.toString(coefficients) + ", moves=" + moves + "]";
	}

	@Override
	public int compareTo(Individual o)
	{
		return moves - o.moves;
	}
}
