package amity.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import amity.ai.AmityAI;

public class GeneticCoefficientFinder
{
    static final int    POPULATION_SIZE      = 50;
    static final int    AVG_CALCULATING_N    = 7;
    static final double ELITIST              = .10;
    static final int    PARENTS              = 3;
    static final int    STD_DEVIATIONS       = 3;
    static final double MUTATION_PROBABILITY = .15;
    static final double RANDOM               = .15;
    static final int    COEFFICIENTS         = 13;

    public static void main(final String args[])
    {
        List<Individual> population = new ArrayList<>();

        // Create the initial population
        for (int i = 0; i < GeneticCoefficientFinder.POPULATION_SIZE; i++)
        {
            final Individual indiv = new Individual();

            indiv.coefficients = GeneticCoefficientFinder.generateRandomCoefficients(GeneticCoefficientFinder.COEFFICIENTS);
            indiv.moves = GeneticCoefficientFinder.avgMoves(indiv.coefficients);

            System.out.println(i + " " + indiv.moves); //

            population.add(indiv);
        }

        Collections.sort(population); // sort

        System.out.println("Avg moves: " + GeneticCoefficientFinder.calculateAvgMoves(population));
        System.out.println(population);

        // Iterate forever
        while (true)
        {
            final List<Individual> newPopulation = new ArrayList<>();

            // Add elitists in
            int number = (int) (GeneticCoefficientFinder.ELITIST * GeneticCoefficientFinder.POPULATION_SIZE);
            for (int i = 0; i < number; i++)
            {
                newPopulation.add(population.get(number));
            }

            // Throw a couple random ones in
            number = (int) (GeneticCoefficientFinder.RANDOM * GeneticCoefficientFinder.POPULATION_SIZE);
            for (int i = 0; i < number; i++)
            {
                final Individual indiv = new Individual();

                indiv.coefficients = GeneticCoefficientFinder.generateRandomCoefficients(GeneticCoefficientFinder.COEFFICIENTS);
                indiv.moves = GeneticCoefficientFinder.avgMoves(indiv.coefficients);

                System.out.println(newPopulation.size() + " " + indiv.moves);

                newPopulation.add(indiv);
            }

            // Have sex
            for (int i = newPopulation.size(); i < GeneticCoefficientFinder.POPULATION_SIZE; i++)
            {
                // Choose fit parents
                final List<Individual> parents = new ArrayList<>();
                for (int j = 0; j < GeneticCoefficientFinder.PARENTS; j++)
                {
                    int element = (int) Math.abs(GeneticCoefficientFinder.rand.nextGaussian() * (GeneticCoefficientFinder.POPULATION_SIZE / GeneticCoefficientFinder.STD_DEVIATIONS));
                    if (element >= GeneticCoefficientFinder.POPULATION_SIZE)
                    {
                        element = GeneticCoefficientFinder.POPULATION_SIZE - 1;
                    }

                    parents.add(population.get(element));
                }

                // Mix those parents
                final double[] newCoefs = new double[GeneticCoefficientFinder.COEFFICIENTS];
                for (final Individual ind : parents)
                {
                    for (int k = 0; k < GeneticCoefficientFinder.COEFFICIENTS; k++)
                    {
                        newCoefs[k] += ind.coefficients[k];
                    }
                }
                for (int k = 0; k < GeneticCoefficientFinder.COEFFICIENTS; k++)
                {
                    newCoefs[k] /= GeneticCoefficientFinder.PARENTS;
                }

                // Mutation
                for (int k = 0; k < GeneticCoefficientFinder.COEFFICIENTS; k++)
                {
                    final boolean mutate = GeneticCoefficientFinder.rand.nextInt((int) (1 / GeneticCoefficientFinder.MUTATION_PROBABILITY)) == 0;

                    if (mutate)
                    {
                        newCoefs[k] = GeneticCoefficientFinder.rand.nextDouble();
                    }
                }

                final Individual newIndiv = new Individual();
                newIndiv.coefficients = newCoefs;
                newIndiv.moves = GeneticCoefficientFinder.avgMoves(newIndiv.coefficients);

                System.out.println(newPopulation.size() + " " + newIndiv.moves);
                newPopulation.add(newIndiv);
            }

            // Prepare for next iteration
            population = newPopulation;
            Collections.sort(population);

            System.out.println("Avg moves: " + GeneticCoefficientFinder.calculateAvgMoves(population));
            System.out.println(population);
        }
    }

    public static int calculateAvgMoves(final List<Individual> pop)
    {
        int moves = 0;
        for (int i = 0; i < pop.size(); i++)
        {
            moves += pop.get(i).moves;
        }

        return moves / pop.size();
    }

    static Random rand = new Random();

    public static double[] generateRandomCoefficients(final int count)
    {
        final double[] d = new double[count];

        for (int i = 0; i < d.length; i++)
        {
            d[i] = GeneticCoefficientFinder.rand.nextDouble();
        }

        return d;
    }

    public static int avgMoves(final double[] coefficients)
    {
        int sum = 0;
        for (int i = 0; i < GeneticCoefficientFinder.AVG_CALCULATING_N; i++)
        {
            sum += new TetrisGameRunner(new AmityAI(coefficients)).getResult();
        }

        return sum / GeneticCoefficientFinder.AVG_CALCULATING_N;
    }

}

class Individual implements Comparable<Individual>
{
    double[] coefficients; // 13
    int      moves;

    @Override
    public String toString()
    {
        return "Individual [moves=" + this.moves + ", coefficients=" + Arrays.toString(this.coefficients) + "]";
    }

    @Override
    public int compareTo(final Individual o)
    {
        return -(this.moves - o.moves); // Puts bigger numbers earlier
    }
}
