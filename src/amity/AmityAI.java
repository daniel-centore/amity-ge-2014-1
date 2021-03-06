package amity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import tetris.AI;
import tetris.Board;
import tetris.Move;
import tetris.Piece;
import tetris.PiecePanel;
import tetris.TetrisController;

/**
 * This is the main Tetris AI class for Amity High School.
 * 
 * Our algorithm is based on the ITLP one, and uses a number of weighted heuristics to measure all possible theoretical board situations for the
 * 
 * current and preview pieces. It then returns the first move for the theoretical situation we think is the best.
 * 
 * @author Daniel Centore
 * 
 */
public class AmityAI implements AI
{
    private final FinalRater rater;            // Rates different boards based on their difficulty to clear

    /**
     * Loads the Amity Tetris AI solver
     * 
     * @param coefficients The coefficients to use for the rater
     */
    public AmityAI(final double[] coefficients)
    {
        this.rater = new FinalRater(coefficients);
    }

    /**
     * Loads the Amity Tetris AI solver. Uses the default rater coefficients.
     */
    public AmityAI()
    {
        this.rater = new FinalRater();
    }

    @Override
    public Move bestMove(final Board board, final Piece piece, final Piece nextPiece, final int limitHeight)
    {
        double bestScore = Double.MAX_VALUE;
        int bestX = -1;
        int bestY = -1;
        Piece bestPiece = null;

        Piece current = piece;
        Piece next = nextPiece;

        // Loop through all possible rotations of the current piece
        do
        {
            final int yBound = limitHeight - current.getHeight() + 1;
            final int xBound = board.getWidth() - current.getWidth() + 1;

            // For the current rotation, try dropping from all the possible columns
            for (int x = 0; x < xBound; x++)
            {
                int y = board.dropHeight(current, x);
                if ((y < yBound) && board.canPlace(current, x, y))
                {
                    Board testBoard = new Board(board);
                    testBoard.place(current, x, y);
                    testBoard.clearRows();

                    // Loop through all possible rotations of the preview piece
                    do
                    {
                        final int jBound = limitHeight - next.getHeight() + 1;
                        final int iBound = testBoard.getWidth() - next.getWidth() + 1;

                        // For the current rotation, try dropping from all the possible columns
                        for (int i = 0; i < iBound; i++)
                        {
                            int j = testBoard.dropHeight(next, i);
                            if (j < jBound && testBoard.canPlace(next, i, j))
                            {
                                Board temp = new Board(testBoard);
                                temp.place(next, i, j);
                                temp.clearRows();

                                // Rate the difficulty of solving the board with this 2-piece combination
                                double nextScore = rater.rateBoard(temp);

                                // If this board is perceived as simpler to solve than the last (and thus, better), then label the 1st move as the new best
                                if (nextScore < bestScore)
                                {
                                    bestScore = nextScore;
                                    bestX = x;
                                    bestY = y;
                                    bestPiece = current;
                                }
                            }

                        }

                        next = next.nextRotation();
                    } while (next != nextPiece);
                }
            }
            current = current.nextRotation();
        } while (current != piece);

        Move move = new Move();

        if (bestPiece == null)          // There is no possible way to stay alive; just set the goal as the middle and wait for death
        {
            move.x = (board.getWidth() - piece.getWidth()) / 2;
            move.y = -1;
            move.piece = piece;

            return move;
        }

        // Send the best known move
        move.x = bestX;
        move.y = bestY;
        move.piece = bestPiece;

        return move;
    }

    @Override
    public void setRater(AIHelper.BoardRater r)
    {
        // Unused function that the AI interface requires us to implement
    }
}

/**
 * Handles the rating of the board. The higher the score, the more difficult it is to solve.
 * 
 * Works by taking a bunch of heuristics on the board and then multiplying them by pre-determined constants, adding the individual scores together
 * 
 * @author Mike Zuo
 * @author Daniel Centore
 * 
 */
class FinalRater extends BoardRater
{
    /**
     * The list of heuristics we will use to measure the status of the board
     */
    public static BoardRater raters[]     = { new ConsecHorzHoles(), new HeightAvg(), new HeightMax(), new HeightMinMax(), new HeightVar(), new HeightStdDev(), new SimpleHoles(), new ThreeVariance(), new NotTrough(), new WeightedHoles(), new RowsWithHolesInMostHoledColumn(), new AverageSquaredTroughHeight(), new BlocksAboveHoles() };

    /**
     * This is a parallel array with the raters[] one. We multiply the result from a rater by its corresponding coefficient.
     * 
     * This allows us to weigh the different heuristics based on their importance.
     * 
     * These coefficients were generated by the Genetic Algorithm at the bottom of this program
     */
    public double[]          coefficients = { 0.5122618836597426, 0.17516377132237726, 0.5809266976532828, 0.5166052958097598, 0.19427865718632464, -0.054743459627535324, 0.9739233692440133, 0.3108976644216631, 0.5945106189776568, 0.4509450257992734, 0.8887034350255546, 0.8036332723722333, 0.03162311457583533 };

    /**
     * Loads a {@link FinalRater} with the default coefficients
     */
    public FinalRater()
    {
    }

    /**
     * Loads a {@link FinalRater} using a different set of coefficients. This is for use by the Genetic Algorithm.
     * 
     * @param c The coefficients as a parallel array with raters[]
     */
    public FinalRater(final double[] c)
    {
        if (c.length != FinalRater.raters.length)
        {
            System.out.println("Make sure that the array passed into the FinalRater has the correct number of coefficients! Using DEFAULT COEFFICIENTS instead!");
            return;
        }
        this.coefficients = c;
    }

    @Override
    public double rate(final Board board)
    {
        double score = 0, temp;
        for (int x = 0; x < FinalRater.raters.length; x++)
        {
            score += (temp = this.coefficients[x]) == 0 ? 0 : temp * FinalRater.raters[x].rate(board);
        }
        return score;
    }
}

/**
 * ============================================================== Heuristics ==============================================================
 * 
 * The following are the original heuristics from the ITLP project, but with AverageSquaredTroughHeight replaced with the original from
 * 
 * tetris-ai ( https://code.google.com/p/tetris-ai/ ) because the one included in the GE ITLP Project was incorrect. Commenting and some
 * 
 * refactoring was also performed but, with the exception of AverageSquaredTroughHeight, functionality was not changed from the included ones.
 * 
 * ========================================================================================================================================
 */

/**
 * An abstract class representing a Board Rater, a single heuristic for evaluating the difficulty of solving the current board
 */
abstract class BoardRater
{
    /**
     * Rates the difficulty of solving the current board based on a single heuristic
     * 
     * @param board The board used for rating
     * @return The rating
     */
    abstract double rate(Board board);

    /**
     * Rates the difficulty of solving the current board based on a single heuristic, but enables board caching first to speed things up
     * 
     * @param board The board used for rating
     * @return The rating
     */
    public double rateBoard(final Board board)
    {
        board.enableCaching();
        final double ret = this.rate(board);
        board.disableCaching();
        return ret;
    }
}

/**
 * Works by calculating the depth of troughs on the board, squaring them all, and then finding the average
 * 
 * Squaring the height causes a bias where adding a single row causes more than just a linear increase in difficulty
 */
class AverageSquaredTroughHeight extends BoardRater
{
    @Override
    public double rate(Board board)
    {
        int w = board.getWidth();
        int[] troughs = new int[w];
        int x = 0, temp, temp2, temp3;

        // Measure the depth of the trough against the left wall
        troughs[0] = ((temp = board.getColumnHeight(1) - board.getColumnHeight(0)) > 0) ? temp : 0;

        // Measure the depth of the troughs between 2 walls
        for (x = 1; x < w - 1; x++)
        {
            troughs[x] = (temp = (((temp2 = (board.getColumnHeight(x + 1) - board.getColumnHeight(x))) > (temp3 = (board.getColumnHeight(x - 1) - board.getColumnHeight(x)))) ? temp3 : temp2)) > 0 ? temp : 0;
        }

        // Measure the depth of the trough against the right wall
        troughs[w - 1] = ((temp = board.getColumnHeight(w - 2) - board.getColumnHeight(w - 1)) > 0) ? temp : 0;

        // Add up the squares of all the heights
        double average = 0.0;
        for (x = 0; x < w; x++)
            average += troughs[x] * troughs[x];

        // Return the average of those heights
        return average / w;
    }

}

/**
 * Counts the number of blocks above holes
 */
class BlocksAboveHoles extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        final int w = board.getWidth();
        int blocksAboveHoles = 0;

        // Iterate over all the columns
        for (int x = 0; x < w; x++)
        {
            int blocksAboveHoleThisColumn = 0;
            boolean hitHoleYet = false;

            // Goes up the column until it hits the first hole
            // From then on, all occupied squares are added to the column total
            for (int i = board.getColumnHeight(x) - 1; i >= 0; i--)
            {
                if (!board.getGrid(x, i))
                    hitHoleYet = true;

                blocksAboveHoleThisColumn += hitHoleYet ? 0 : 1;
            }

            if (!hitHoleYet)
                blocksAboveHoleThisColumn = 0;

            // Add the column total to the grand total
            blocksAboveHoles += blocksAboveHoleThisColumn;
        }

        return blocksAboveHoles;
    }
}

/**
 * Counts the number of holes which come immediately after a block in each column.
 * 
 * In other words, two (or more) empty vertical spaces beneath a block are only counted as one hole
 */
class ConsecHorzHoles extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        final int width = board.getWidth();

        int holes = 0;

        // Iterate across the board width-wise
        for (int x = 0; x < width; x++)
        {
            final int colHeight = board.getColumnHeight(x);
            int y = colHeight - 2;      // address of first possible hole

            // Iterate down the column
            boolean consecutiveHole = false;
            while (y >= 0)
            {
                if (!board.getGrid(x, y))       // If we find an empty space
                {
                    if (!consecutiveHole)       // ...and it doesn't come after another empty space
                    {
                        holes++;                // ...Count it
                        consecutiveHole = true;
                    }
                }
                else
                {
                    // If we find a filled space
                    consecutiveHole = false;    // ...prepare for the next hole
                }
                y--;
            }
        }

        return holes;
    }

}

/**
 * Finds the mean column height
 */
class HeightAvg extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        // Find the sum of the individual column heights
        int sumHeight = 0;
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;
        }

        // Return the mean
        return (double) sumHeight / board.getWidth();
    }

}

/**
 * Finds the maximum column height
 */
class HeightMax extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        return board.getMaxHeight();
    }
}

/**
 * Finds the difference in height between the tallest and shortest columns
 * 
 * This is used because it is generally worse to have a really tall and really short column than to just have a flat, tall board.
 */
class HeightMinMax extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        int maxHeight = 0;
        int minHeight = board.getHeight();

        // Iterate across the board
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int height = board.getColumnHeight(x);

            // Record the shortest and tallest columns
            if (height > maxHeight)
                maxHeight = height;

            if (height < minHeight)
                minHeight = height;
        }

        // Return the difference
        return maxHeight - minHeight;
    }

}

/**
 * Finds the standard deviation of the column heights, which gives us a measure of how much the columns vary, or how "rough" the board is
 */
class HeightStdDev extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        // Just takes the square root of the variance
        return Math.sqrt(new HeightVar().rate(board));
    }
}

/**
 * Finds the variance in column heights, which gives us a measure of how much the columns vary, or how "rough" the board is
 */
class HeightVar extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        // Find the total height of all columns
        int sumHeight = 0;
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;
        }

        // ...and from that, the average column height
        final double avgHeight = (double) sumHeight / board.getWidth();

        // Now add up the square of how much each column varies from that average height
        int varisum = 0;
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);
            varisum += Math.pow(colHeight - avgHeight, 2);
        }

        // And return the average of all those
        return varisum / board.getWidth();

    }
}

/**
 * Return the number of holes in the column with the most holes
 */
class RowsWithHolesInMostHoledColumn extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        // Iterate across the board width-wise
        int mostHolesInAnyColumn = 0;
        for (int x = 0; x < board.getWidth(); x++)
        {
            // Count up the number of holes in the column
            final int colHeight = board.getColumnHeight(x);
            int y = colHeight - 2;      // The first place there could be a hole
            int holes = 0;

            // Iterate down the board, counting holes
            while (y >= 0)
            {
                if (!board.getGrid(x, y))
                    holes++;

                y--;
            }

            // Record if it is the new max
            if (mostHolesInAnyColumn < holes)
                mostHolesInAnyColumn = holes;
        }

        return mostHolesInAnyColumn;
    }

}

/**
 * Simply counts up the number of holes in the board
 */
class SimpleHoles extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        int holes = 0;

        // Iterate across the board
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);

            int y = colHeight - 2; // address of the first possible hole

            // Add up all the empty spaces in the column
            while (y >= 0)
            {
                if (!board.getGrid(x, y))
                    holes++;

                y--;
            }
        }
        return holes;
    }

}

/**
 * Finds the average variance between each set of 3 contiguous columns.
 * 
 * In other words, it finds the variance in each set of 3 contiguous columns (including overlaps) and finds their mean.
 */
class ThreeVariance extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        final int w = board.getWidth();
        double runningVarianceSum = 0.0;

        // Iterate across the board
        for (int i = 0; i < w - 2; i++)
        {
            // Find the column height of the current column, and the two immediately following it
            double h0 = board.getColumnHeight(i);
            double h1 = board.getColumnHeight(i + 1);
            double h2 = board.getColumnHeight(i + 2);

            // Find the mean of those heights
            final double m = (h0 + h1 + h2) / 3.0;

            // Find the difference between the mean and the actual for each of the three columns
            h0 -= m;
            h1 -= m;
            h2 -= m;

            // Square them
            h0 *= h0;
            h1 *= h1;
            h2 *= h2;

            // Find the variance, and add it to the running sum of variances
            runningVarianceSum += (h0 + h1 + h2) / 3.0;
        }

        // Return the mean of those variances
        return runningVarianceSum / (w - 3);
    }
}

/**
 * Counts the number of columns which have something in them
 */
class NotTrough extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        int colCount = 0;

        // Iterate across the board
        for (int x = 0; x < board.getWidth(); x++)
        {
            // Add a column if it has anything in it
            if (board.getColumnHeight(x) > 0)
                colCount++;
        }

        return colCount;
    }
}

/**
 * Comes up with a measure of the amount of holes, with a negative bias on lower holes, such that holes that are deeply buried are worse than holes near the surface
 * 
 * Each hole is represented by the difference in height between the tallest column and the hole height in order to add this negative bias.
 * 
 * The number is then divided by the height of the tallest column which puts it on a scale between 0 and 1.
 * 
 * These numbers are then added up.
 */
class WeightedHoles extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        // Find the column with the maximum height
        int maxHeight = 0;

        for (int x = 0; x < board.getWidth(); x++)
        {
            final int height = board.getColumnHeight(x);
            if (height > maxHeight)
                maxHeight = height;
        }

        double weightedHoleCount = 0.0;
        final int[] heights = new int[board.getWidth()];

        // Iterate over the board
        for (int x = 0; x < board.getWidth(); x++)
        {
            heights[x] = board.getColumnHeight(x);
            int y = heights[x] - 2;           // The first possible position for a hole

            while (y >= 0)      // Iterate down the column
            {
                if (!board.getGrid(x, y))       // For each hole,
                {
                    // Add the difference in height between the tallest column and the hole, put on a 0-1 scale based on the tallest column being 1
                    weightedHoleCount += (double) (maxHeight - y) / (double) maxHeight;
                }

                y--;
            }
        }
        return weightedHoleCount;
    }
}

/**
 * ======================================================== Coefficient Calculator ========================================================
 * 
 * The following is part of a genetic algorithm used for approximating the ideal coefficients for use in the Tetris AI
 * 
 * This code is not actually being used by the AI; it was just run for a long time to calculate the current coefficients used in FinalRater
 * 
 * ========================================================================================================================================
 */

/**
 * The main class for generating good coefficients for the raters using a Genetic Algorithm
 * 
 * @author Daniel Centore
 * 
 */
class GeneticCoefficientFinder
{
    static final int        POPULATION_SIZE      = 30;          // The size of the population of individuals
    static final int        AVG_CALCULATING_N    = 4;           // The number of trials to average on each set of coefficients
    static final double     ELITIST              = .10;         // What percent of the top individuals to retain from generation to generation
    static final int        STD_DEVIATIONS       = 3;           // Number of standard deviations to use from the positive normal model
                                                                 // while selecting mates for the individuals
    static final double     MUTATION_PROBABILITY = .50;         // The probability of an individual coefficient mutating
    static final double     MUTATION_RANGE       = .15;         // How much (+ or -) to permit a mutation to occur
    static final double     RANDOM               = .10;         // What percent of new populations to fill with completely random coefficients
    static final int        COEFFICIENTS         = 13;          // Number of coefficients we are generating

    static TetrisController controller;                         // The Tetris game controller

    static Random           rand                 = new Random();        // For generating random numbers

    public static void main(String args[])
    {
        runCoefficientFinder();
    }

    /**
     * Runs the genetic algorithm. Runs until manually stopped, constantly trying to produce better and better coefficients
     */
    public static void runCoefficientFinder()
    {
        controller = new TetrisController();

        // Load the GUI view of the running game
        // This can be safely commented out to disable the GUI
        RunTetrisGeneticView.load(controller);

        List<Individual> population = new ArrayList<>();

        // Create the initial population of completely random coefficients
        for (int i = 0; i < POPULATION_SIZE; i++)
        {
            Individual indiv = new Individual();

            indiv.coefficients = generateRandomCoefficients(COEFFICIENTS);      // Generate a random individual
            indiv.moves = avgMoves(indiv.coefficients);         // Determine the average number of moves this individual's coefficients make

            System.out.println(i + " " + indiv.moves);

            population.add(indiv);
        }

        Collections.sort(population); // Sort the population (puts better individuals earlier)

        System.out.println("Avg moves: " + populationAvgMoves(population));
        System.out.println(population);

        // Keep iterating and creating new generations
        while (true)
        {
            List<Individual> newPopulation = new ArrayList<>();

            // Add elitists in to the new generation
            // These consist of the very best individuals from the previous population
            System.out.println("Elitists");
            int number = (int) (ELITIST * POPULATION_SIZE);             // Number of elitists to add in
            for (int i = 0; i < number; i++)
            {
                // Just pull them off the top of the old population
                Individual indiv = population.get(i);
                System.out.println(i + " " + indiv.moves);
                newPopulation.add(indiv);
            }

            // Throw a couple new random individuals into the new population
            // This keeps genetic variability high so we prevent approaching arbitrary coefficients very quickly
            System.out.println("Randoms");
            number = (int) (RANDOM * POPULATION_SIZE);          // Number of random individuals to add in
            for (int i = 0; i < number; i++)
            {
                Individual indiv = new Individual();

                indiv.coefficients = generateRandomCoefficients(COEFFICIENTS);   // Generate a random individual
                indiv.moves = avgMoves(indiv.coefficients);                      // Determine the average number of moves this individual's coefficients make

                System.out.println(newPopulation.size() + " " + indiv.moves);

                newPopulation.add(indiv);
            }

            // Create new individuals by mimicking intercourse between the top individuals from the previous generation
            System.out.println("Sex");
            for (int i = newPopulation.size(); i < POPULATION_SIZE; i++)    // Iterate from the current newPoplation size through the goal size
            {
                // Choose 2 parents. Uses a normal model to randomly select individuals, giving preference to the better ones
                int element = (int) Math.abs(rand.nextGaussian() * (POPULATION_SIZE / STD_DEVIATIONS));
                if (element >= POPULATION_SIZE)
                    element = POPULATION_SIZE - 1;

                Individual parent1 = population.get(element);

                element = (int) Math.abs(rand.nextGaussian() * (POPULATION_SIZE / STD_DEVIATIONS));
                if (element >= POPULATION_SIZE)
                    element = POPULATION_SIZE - 1;

                Individual parent2 = population.get(element);

                // Imitate reproduction by randomly selecting whether each of the child's coefficients comes from parent 1 or 2
                double[] newCoefs = new double[COEFFICIENTS];

                for (int k = 0; k < COEFFICIENTS; k++)          // Iterate over the coefficients
                {
                    // Randomly choose which parent to choose it from
                    if (rand.nextBoolean())
                        newCoefs[k] = parent1.coefficients[k];
                    else
                        newCoefs[k] = parent2.coefficients[k];
                }

                // Mutate the coefficients
                for (int k = 0; k < COEFFICIENTS; k++)
                {
                    // Decide if we should mutate this one based on the constant MUTATION_PROBABILITY
                    boolean mutate = rand.nextInt((int) (1 / MUTATION_PROBABILITY)) == 0;

                    if (mutate)         // If we want to mutate it, then mutate it by a random number within +-MUTATION_RANGE
                        newCoefs[k] += (rand.nextDouble() * MUTATION_RANGE * 2 - MUTATION_RANGE);
                }

                // Create the new individual using the new coefficients and evaluate its quality
                Individual newIndiv = new Individual();
                newIndiv.coefficients = newCoefs;
                newIndiv.moves = avgMoves(newIndiv.coefficients);       // Determine the average number of moves this individual's coefficients make

                System.out.println(newPopulation.size() + " " + newIndiv.moves);
                newPopulation.add(newIndiv);
            }

            // Sort the new generation and bump it into the its parents' spot
            population = newPopulation;
            Collections.sort(population);

            System.out.println("Avg moves: " + populationAvgMoves(population));
            System.out.println(population);
        }
    }

    /**
     * Calculates the average number of moves a population achieved
     * 
     * @param pop The population to look at
     * @return The average number of moves
     */
    public static int populationAvgMoves(List<Individual> pop)
    {
        int moves = 0;
        for (int i = 0; i < pop.size(); i++)
            moves += pop.get(i).moves;

        return moves / pop.size();
    }

    /**
     * Generates an array of random coefficients between 0-1
     * 
     * @param count The size of the array
     * @return The array of random coefficients
     */
    public static double[] generateRandomCoefficients(int count)
    {
        double[] d = new double[count];

        for (int i = 0; i < d.length; i++)
            d[i] = rand.nextDouble();

        return d;
    }

    /**
     * Estimates the average number of moves a set of coefficients allows by running a few games and averaging the number of moves together
     * 
     * @param coefficients The array of coefficients to use
     * @return The average number of moves
     */
    public static int avgMoves(double[] coefficients)
    {
        System.out.println(Arrays.toString(coefficients));
        System.out.print("{");
        
        int sum = 0;
        
        for (int i = 0; i < AVG_CALCULATING_N; i++)      // For each game that we're supposed to run
        {
            int game = quickGame(coefficients);          // Run a game
            System.out.print(game + " ");

            sum += game;                // Add it to the sum
        }
        System.out.print("} ");

        // Return the mean
        return sum / AVG_CALCULATING_N;
    }

    /**
     * Runs a single game with a set of coefficients and returns the number of moves the AI survived
     * 
     * @param coef The array of coefficients to use
     * @return The number of moves the AI made it
     */
    private static int quickGame(double[] coef)
    {
        // Initialize the game
        TetrisController tc = controller;
        tc.startGame();

        // Load the AI
        AI ai = new AmityAI(coef);

        // Run the game until it is over
        while (tc.gameOn)
        {
            // Figure out what our AI thinks the best next move is based on the current coefficients 
            Move move = ai.bestMove(new Board(tc.board), tc.currentMove.piece, tc.nextPiece, tc.board.getHeight() - TetrisController.TOP_SPACE);

            // Rotate the piece until it is at the correct rotation
            while (!tc.currentMove.piece.equals(move.piece))
            {
                tc.tick(TetrisController.ROTATE);
            }

            // Move the piece horizontally until it is at the correct position
            while (tc.currentMove.x != move.x)
            {
                tc.tick(((tc.currentMove.x < move.x) ? TetrisController.RIGHT : TetrisController.LEFT));
            }

            int current_count = tc.count;

            // Drop the piece until it either hits the bottom or we lose
            while ((current_count == tc.count) && tc.gameOn)
                tc.tick(TetrisController.DOWN);
        }

        // Return the number of moves
        return tc.count;
    }
}

/**
 * Represents a single individual in the population for the Genetic Algorithm
 * 
 * Contains a set of coefficients ("genes") and the average number of move those coefficients were recorded as performing
 * 
 * @author Daniel Centore
 * 
 */
class Individual implements Comparable<Individual>
{
    double[] coefficients;   // Array of coefficients (originally chosen to be between 0 and 1, but mutations can pull them out of this range)
    int      moves;          // The average number of moves those coefficients resulted in

    @Override
    public String toString()
    {
        return "Individual [moves=" + moves + ", coefficients=" + Arrays.toString(coefficients) + "]";
    }

    @Override
    public int compareTo(Individual o)
    {
        return -(moves - o.moves); // Puts better individuals earlier
    }
}

/**
 * A modified version of RunTetris which separates the GUI logic from the algorithm and runs it in a separate thread so our algorithm
 * 
 * doesn't get slowed down by the repaint functions
 * 
 * @author Daniel Centore
 * 
 */
class RunTetrisGeneticView extends JComponent
{
    private static final long   serialVersionUID = 1L;

    protected PiecePanel        nextPiecePanel;

    // Controls
    protected JLabel            countLabel;
    protected JLabel            timeLabel;
    protected JButton           startButton;
    protected JButton           stopButton;
    protected javax.swing.Timer timer;
    protected JSlider           speed;
    protected JLabel            rowsClearedLabel;
    protected JSlider           Diffcult;
    protected JLabel            difficulty;

    // milliseconds per tick
    public final int            DELAY            = 0;

    // used to measure elapsed time
    protected long              startTime;

    TetrisController            tc;

    public RunTetrisGeneticView(final int width, final int height, TetrisController tc)
    {
        super();

        this.setPreferredSize(new Dimension(width, height));

        this.tc = tc;
        
        // Create the Timer object which repaints the current status of the game
        this.timer = new javax.swing.Timer(this.DELAY, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                RunTetrisGeneticView.this.countLabel.setText(Integer.toString(RunTetrisGeneticView.this.tc.count) + " Moves");
                RunTetrisGeneticView.this.rowsClearedLabel.setText(RunTetrisGeneticView.this.tc.rowsCleared + " Rows Cleared");
                RunTetrisGeneticView.this.nextPiecePanel.setPiece(RunTetrisGeneticView.this.tc.nextPiece);
                RunTetrisGeneticView.this.repaint();
            }
        });
    }

    // width in pixels of a block
    private final float dX()
    {
        return (float) (this.getWidth() - 2) / this.tc.board.getWidth();
    }

    // height in pixels of a block
    private final float dY()
    {
        return (float) (this.getHeight() - 2) / this.tc.board.getHeight();
    }

    // the x pixel coord of the left side of a block
    private final int xPixel(final int x)
    {
        return Math.round(1 + x * this.dX());
    }

    // the y pixel coord of the top of a block
    private final int yPixel(final int y)
    {
        return Math.round(this.getHeight() - 1 - (y + 1) * this.dY());
    }

    /**
     * Draws the current board with a 1 pixel border around the whole thing.
     */
    @Override
    public void paintComponent(final Graphics g)
    {
        Color[][] colorGrid = null;
        Field field;
        try
        {
            field = this.tc.displayBoard.getClass().getDeclaredField("colorGrid");
            field.setAccessible(true);
            colorGrid = (Color[][]) field.get(this.tc.displayBoard);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        // Draw a rect around the whole thing
        g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

        // Draw the line separating the top
        final int spacerY = this.yPixel(this.tc.displayBoard.getHeight() - TetrisController.TOP_SPACE - 1);
        g.setColor(Color.WHITE);
        g.drawLine(0, spacerY, this.getWidth() - 1, spacerY);

        // Factor a few things out to help the optimizer
        final int dx = Math.round(this.dX() - 2);
        final int dy = Math.round(this.dY() - 2);
        final int bWidth = this.tc.displayBoard.getWidth();

        int x, y;
        // Loop through and draw all the blocks
        // left-right, bottom-top
        for (x = 0; x < bWidth; x++)
        {
            final int left = this.xPixel(x); // the left pixel

            // draw from 0 up to the col height
            final int yHeight = this.tc.displayBoard.getColumnHeight(x);
            for (y = 0; y < yHeight; y++)
            {
                if (this.tc.displayBoard.getGrid(x, y))
                {
                    g.setColor(colorGrid[x][y]);// this.tc.displayBoard.colorGrid[x][y]);
                    g.fillRect(left + 1, this.yPixel(y) + 1, dx, dy);

                }
            }
        }
    }

    /**
     * Creates the panel of UI controls.
     */
    public java.awt.Container createControlPanel()
    {
        final java.awt.Container panel = Box.createVerticalBox();

        this.nextPiecePanel = new PiecePanel();
        panel.add(this.nextPiecePanel);

        // COUNT
        this.countLabel = new JLabel("0" + " Moves");
        panel.add(this.countLabel);

        // ROWS Cleared
        this.rowsClearedLabel = new JLabel("0" + " Rows Cleared");
        panel.add(this.rowsClearedLabel);

        this.difficulty = new JLabel();
        panel.add(this.difficulty);

        panel.add(Box.createVerticalStrut(12));

        final JPanel row = new JPanel();

        panel.add(row);

        return panel;
    }

    /**
     * Loads a new copy of the GUI
     * 
     * @param tc The TetrisController to monitor
     */
    public static void load(TetrisController tc)
    {
        final JFrame frame = new JFrame("TETRIS CSC");
        final JComponent container = (JComponent) frame.getContentPane();
        container.setLayout(new BorderLayout());

        // Set the metal look and feel
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (final Exception ignored)
        {
        }

        final int pixels = 20;
        RunTetrisGeneticView tetris = null;
        tetris = new RunTetrisGeneticView(TetrisController.WIDTH * pixels + 2, (TetrisController.HEIGHT + TetrisController.TOP_SPACE) * pixels + 2, tc);

        container.add(tetris, BorderLayout.CENTER);

        final Container panel = tetris.createControlPanel();

        panel.add(new JTextArea());

        container.add(panel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);

        // Quit on window close
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e)
            {
                System.exit(0);
            }
        });

        tetris.timer.start();
    }
}
