package amity.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import tetris.AI;
import tetris.Board;
import tetris.ITLPAI;
import tetris.Move;
import tetris.TetrisController;

import amity.ai.AmityAI;

public class GeneticCoefficientFinder
{
    static final int                POPULATION_SIZE      = 30;
    static final int                AVG_CALCULATING_N    = 4;
    static final double             ELITIST              = .10;
    static final int                STD_DEVIATIONS       = 3;
    static final double             MUTATION_PROBABILITY = .50;
    static final double             RANDOM               = .10;
    static final int                COEFFICIENTS         = 13;
    private static final double     MUTATION_RANGE       = .15;
    static final int                ASSUME_AVERAGE       = 100000;                // When the number is so large you just assume it's the average to save time

    private static Random           rand                 = new Random();
    private static TetrisController controller           = new TetrisController();

    public static void main(String args[])
    {
        try
        {
            Thread.sleep(100);          // let the TetrisController wake up
        }
        catch (InterruptedException e)
        {
        }
        RunTetrisGeneticView.load(controller);

        List<Individual> population = new ArrayList<>();

        // Create the initial population
        for (int i = 0; i < POPULATION_SIZE; i++)
        {
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
        while (true)
        {
            List<Individual> newPopulation = new ArrayList<>();

            // Add elitists in
            System.out.println("Elitists");
            int number = (int) (ELITIST * POPULATION_SIZE);
            for (int i = 0; i < number; i++)
            {
                Individual indiv = population.get(i);
                System.out.println(i + " " + indiv.moves);
                newPopulation.add(indiv);
            }

            // Throw a couple random ones in
            System.out.println("Randoms");
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
            System.out.println("Sex");
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
                for (int k = 0; k < COEFFICIENTS; k++)
                {
                    boolean mutate = rand.nextInt((int) (1 / MUTATION_PROBABILITY)) == 0;

                    if (mutate)
                        newCoefs[k] += (rand.nextDouble() * MUTATION_RANGE * 2 - MUTATION_RANGE);
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

    public static double[] generateRandomCoefficients(int count)
    {
        double[] d = new double[count];

        for (int i = 0; i < d.length; i++)
            d[i] = rand.nextDouble();

        return d;
    }

    public static int avgMoves(double[] coefficients)
    {
        System.out.println(Arrays.toString(coefficients));
        System.out.print("{");
        int sum = 0;
        for (int i = 0; i < AVG_CALCULATING_N; i++)
        {
            int game = quickGame(coefficients);
            System.out.print(game + " ");

            if (game > ASSUME_AVERAGE)
                return game;

            sum += game;
        }
        System.out.print("} ");

        return sum / AVG_CALCULATING_N;
    }

    private static int quickGame(double[] coef)
    {
        TetrisController tc = controller;
        tc.startGame();

        AI ai = new AmityAI(coef);

        while (tc.gameOn)
        {
            Move move = ai.bestMove(new Board(tc.board), tc.currentMove.piece, tc.nextPiece, tc.board.getHeight() - TetrisController.TOP_SPACE);

            while (!tc.currentMove.piece.equals(move.piece))
                tc.tick(TetrisController.ROTATE);
            while (tc.currentMove.x != move.x)
                tc.tick(((tc.currentMove.x < move.x) ? TetrisController.RIGHT : TetrisController.LEFT));

            int current_count = tc.count;

            while ((current_count == tc.count) && tc.gameOn)
                tc.tick(TetrisController.DOWN);
        }
        return tc.count;
    }
}

class Individual implements Comparable<Individual>
{
    double[] coefficients; // 13
    int      moves;

    @Override
    public String toString()
    {
        return "Individual [moves=" + moves + ", coefficients=" + Arrays.toString(coefficients) + "]";
    }

    @Override
    public int compareTo(Individual o)
    {
        return -(moves - o.moves); // Puts bigger numbers earlier
    }
}
