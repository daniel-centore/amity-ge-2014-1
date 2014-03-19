package amity.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import amity.ai.AmityAI;

public class GeneticCoefficientFinder {
    static final int    POPULATION_SIZE      = 50;
    static final int    AVG_CALCULATING_N    = 7;
    static final double ELITIST              = .10;
    static final int    STD_DEVIATIONS       = 3;
    static final double MUTATION_PROBABILITY = .15;
    static final double RANDOM               = .15;
    static final int    COEFFICIENTS         = 13;

    public static void main(String args[]) {
        List<Individual> population = new ArrayList<>();

        // Create the initial population
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual indiv = new Individual();

            indiv.coefficients = generateRandomCoefficients(COEFFICIENTS);
            indiv.moves = avgMoves(indiv.coefficients);

            System.out.println(i + " " + indiv.moves); //

            population.add(indiv);
        }

        Collections.sort(population); // sort

        System.out.println("Avg moves: " + calculateAvgMoves(population));
        System.out.println(population);

        // Iterate forever
        while (true) {
            List<Individual> newPopulation = new ArrayList<>();

            // Add elitists in
            int number = (int) (ELITIST * POPULATION_SIZE);
            for (int i = 0; i < number; i++)
                newPopulation.add(population.get(number));

            // Throw a couple random ones in
            number = (int) (RANDOM * POPULATION_SIZE);
            for (int i = 0; i < number; i++)
            {
                Individual indiv = new Individual();

                indiv.coefficients = generateRandomCoefficients(COEFFICIENTS);
                indiv.moves = avgMoves(indiv.coefficients);

                System.out.println(newPopulation.size() + " " + indiv.moves);

                newPopulation.add(indiv);
            }

            // Have sex
            for (int i = newPopulation.size(); i < POPULATION_SIZE; i++)
            {
                // Choose fit parents
                int element = (int) Math.abs(rand.nextGaussian() * (POPULATION_SIZE / STD_DEVIATIONS));
                if (element >= POPULATION_SIZE)
                    element = POPULATION_SIZE - 1;

                Individual parent1 = population.get(element);
                
                element = (int) Math.abs(rand.nextGaussian() * (POPULATION_SIZE / STD_DEVIATIONS));
                if (element >= POPULATION_SIZE)
                    element = POPULATION_SIZE - 1;

                Individual parent2 = population.get(element);

                // Mix the parents
                double[] newCoefs = new double[COEFFICIENTS];
               
                for (int k = 0; k < COEFFICIENTS; k++)
                {
                    if (rand.nextBoolean())
                        newCoefs[k] = parent1.coefficients[k];
                    else
                        newCoefs[k] = parent2.coefficients[k];
                }

                // Mutation
                for (int k = 0; k < COEFFICIENTS; k++) {
                    boolean mutate = rand.nextInt((int) (1 / MUTATION_PROBABILITY)) == 0;

                    if (mutate)
                        newCoefs[k] = rand.nextDouble();
                }

                Individual newIndiv = new Individual();
                newIndiv.coefficients = newCoefs;
                newIndiv.moves = avgMoves(newIndiv.coefficients);

                System.out.println(newPopulation.size() + " " + newIndiv.moves);
                newPopulation.add(newIndiv);
            }

            // Prepare for next iteration
            population = newPopulation;
            Collections.sort(population);

            System.out.println("Avg moves: " + calculateAvgMoves(population));
            System.out.println(population);
        }
    }

    public static int calculateAvgMoves(List<Individual> pop)
    {
        int moves = 0;
        for (int i = 0; i < pop.size(); i++)
            moves += pop.get(i).moves;

        return moves / pop.size();
    }

    static Random rand = new Random();

    public static double[] generateRandomCoefficients(int count) {
        double[] d = new double[count];

        for (int i = 0; i < d.length; i++)
            d[i] = rand.nextDouble();

        return d;
    }

    public static int avgMoves(double[] coefficients)
    {
        List<TetrisGameRunner> runners = new ArrayList<>();
        int sum = 0;
        for (int i = 0; i < AVG_CALCULATING_N; i++)
        {
            TetrisGameRunner runner = new TetrisGameRunner(new AmityAI(coefficients));
            runners.add(runner);
            runner.startGame();
        }
        
        while (!runners.isEmpty())
        {
            Iterator<TetrisGameRunner> itr = runners.iterator();
            
            while (itr.hasNext())
            {
                TetrisGameRunner runner = itr.next();
                
                if (!runner.running())
                {
                    sum += runner.getCount();
                    runner.stopGame();
                    itr.remove();
                }
            }
            
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e) {
            }
        }
            //sum += new TetrisGameRunner(new AmityAI(coefficients)).getResult();

        return sum / AVG_CALCULATING_N;
    }

}

class Individual implements Comparable<Individual> {
    double[] coefficients; // 13
    int      moves;

    @Override
    public String toString() {
        return "Individual [moves=" + moves + ", coefficients="
                + Arrays.toString(coefficients) + "]";
    }

    @Override
    public int compareTo(Individual o) {
        return -(moves - o.moves); // Puts bigger numbers earlier
    }
}
