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
 * Our algorithm is based heavily on the ITLP one, and uses a number of weighted heuristics to measure all possible theoretical board situations for the
 * 
 * current and next placed pieces.
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

            // For the current rotation, try all the possible columns
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

                        // For current rotation, try all the possible columns
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

        if (bestPiece == null)          // No possible way to stay alive; just send a move to give up
        {
            move.x = 2;
            move.y = 1;
            move.piece = piece;

            return move;
        }

        // Send the best known move onward
        move.x = bestX;
        move.y = bestY;
        move.piece = bestPiece;

        return move;
    }

    @Override
    public void setRater(AIHelper.BoardRater r)
    {
        // We handle this on our own
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
     * Holds a coefficient-rater pair
     * 
     * @author Mike Zuo
     * 
     */
    private static class RaterPair
    {
        private double     coeff;
        private BoardRater rater;

        private RaterPair(final double coeff, final BoardRater rater)
        {
            this.coeff = coeff;
            this.rater = rater;
        }
    }

    /**
     * These are the default raters and coefficients. The coefficients were generated using {@link GeneticCoefficientFinder}.
     */
    // @formatter:off
    private static RaterPair[] DEFAULT_RATERS = {
        new RaterPair(0.41430724103382527, new ConsecHorzHoles()),
        new RaterPair(0.04413383739389207, new HeightAvg()),
        new RaterPair(0.1420172532064692, new HeightMax()),
        new RaterPair(-0.13881428312611474, new HeightMinMax()),
        new RaterPair(0.06887679285238696, new HeightVar()),
        new RaterPair(-0.052368130931930074, new HeightStdDev()),
        new RaterPair(0.33235754477242435, new SimpleHoles()),
        new RaterPair(0.2851778629665227, new ThreeVariance()),
        new RaterPair(-0.03011693088344261, new Through()),
        new RaterPair(-0.02534983335709433, new WeightedHoles()),
        new RaterPair(0.21155050264421074, new RowsWithHolesInMostHoledColumn()),
        new RaterPair(0.8292064267563932, new AverageSquaredTroughHeight()),
        new RaterPair(0.0038145282373974604, new BlocksAboveHoles()) };
     // @formatter:on

    private RaterPair[]        raters;

    /**
     * Initializes the {@link FinalRater} using the default rater coefficients
     */
    public FinalRater()
    {
        this.raters = FinalRater.DEFAULT_RATERS;
    }

    /**
     * Initializes the {@link FinalRater}
     * 
     * @param c Array of coefficients which map up to raters in DEFAULT_RATERS as a parallel array
     */
    public FinalRater(final double[] c)
    {
        this();

        if (c.length != FinalRater.DEFAULT_RATERS.length)
        {
            System.err.println("Make sure that the array passed into the FinalRater has the correct number of coefficients! Using DEFAULT COEFFICIENTS instead!");
            return;
        }

        this.raters = new RaterPair[FinalRater.DEFAULT_RATERS.length];

        for (int i = 0; i < c.length; i++)
        {
            this.raters[i] = new RaterPair(c[i], FinalRater.DEFAULT_RATERS[i].rater);
        }
    }

    @Override
    public double rate(final Board board)
    {
        double score = 0;

        for (final RaterPair rater : this.raters)
            score += rater.coeff * rater.rater.rate(board);

        return score;
    }
}

/**
 * ============================================================== Heuristics ==============================================================
 * 
 * The following are the original heuristics from the project, but with AverageSquaredTroughHeight replaced with the original from tetris-ai
 * 
 * because the one included in the GE Tetris Project was incorrect.
 * 
 * ========================================================================================================================================
 */

abstract class BoardRater
{
    int callCount = 0;
    int runTime   = 0;

    public abstract double rate(Board board);

    public double rateBoard(final Board board)
    {
        this.callCount++;
        board.enableCaching();
        final long start = System.nanoTime();
        final double ret = this.rate(board);
        this.runTime += System.nanoTime() - start;
        board.disableCaching();
        return ret;
    }
}

class AverageSquaredTroughHeight extends BoardRater
{
    public double rate(Board board)
    {
        int w = board.getWidth();
        int[] troughs = new int[w];
        int x = 0, temp, temp2, temp3;
        troughs[0] = ((temp = board.getColumnHeight(1) - board.getColumnHeight(0)) > 0) ? temp : 0;
        for (x = 1; x < w - 1; x++)
        {
            troughs[x] = (temp = (((temp2 = (board.getColumnHeight(x + 1) - board.getColumnHeight(x))) > (temp3 = (board.getColumnHeight(x - 1) - board.getColumnHeight(x)))) ? temp3 : temp2)) > 0 ? temp : 0;
        }
        troughs[w - 1] = ((temp = board.getColumnHeight(w - 2) - board.getColumnHeight(w - 1)) > 0) ? temp : 0;
        double average = 0.0;
        for (x = 0; x < w; x++)
            average += troughs[x] * troughs[x];
        return average / w;
    }

}

class BlocksAboveHoles extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        final int w = board.getWidth();
        int blocksAboveHoles = 0;
        for (int x = 0; x < w; x++)
        {
            int blocksAboveHoleThisColumn = 0;
            boolean hitHoleYet = false;
            for (int i = board.getColumnHeight(x) - 1; i >= 0; i--)
            {
                if (!board.getGrid(x, i))
                {
                    hitHoleYet = true;
                }
                blocksAboveHoleThisColumn += hitHoleYet ? 0 : 1;
            }

            if (!hitHoleYet)
            {
                blocksAboveHoleThisColumn = 0;
            }
            blocksAboveHoles += blocksAboveHoleThisColumn;
        }
        return blocksAboveHoles;
    }
}

class ConsecHorzHoles extends BoardRater
{

    @Override
    public double rate(final Board board)
    {
        final int width = board.getWidth();
        // final int maxHeight = board.getMaxHeight();

        int holes = 0;

        // Count the holes, and sum up the heights
        for (int x = 0; x < width; x++)
        {
            final int colHeight = board.getColumnHeight(x);
            int y = colHeight - 2; // addr of first possible hole

            boolean consecutiveHole = false;
            while (y >= 0)
            {
                if (!board.getGrid(x, y))
                {
                    if (!consecutiveHole)
                    {
                        holes++;
                        consecutiveHole = true;
                    }
                }
                else
                {
                    consecutiveHole = false;
                }
                y--;
            }
        }

        return holes;
    }

}

class HeightAvg extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        int sumHeight = 0;
        // count the holes and sum up the heights
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;
        }

        return (double) sumHeight / board.getWidth();
    }

}

class HeightMax extends BoardRater
{

    @Override
    public double rate(final Board board)
    {
        return board.getMaxHeight();
    }
}

class HeightMinMax extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        int maxHeight = 0;
        int minHeight = board.getHeight();

        for (int x = 0; x < board.getWidth(); x++)
        {
            final int height = board.getColumnHeight(x);

            if (height > maxHeight)
            {
                // record the height of highest coloumn
                maxHeight = height;
            }
            if (height < minHeight)
            {
                // record height of lowest coloumn
                minHeight = height;
            }

        }

        return maxHeight - minHeight;
    }

}

class HeightStdDev extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        return Math.sqrt(new HeightVar().rate(board));
    }
}

class HeightVar extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        int sumHeight = 0;

        // count the holes and sum up the height
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;
        }

        final double avgHeight = (double) sumHeight / board.getWidth();

        // first variance
        int varisum = 0;
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);
            varisum += Math.pow(colHeight - avgHeight, 2);
        }

        return varisum / board.getWidth();

    }
}

class RowsWithHolesInMostHoledColumn extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        // count the holes and sum up the heights
        int mostHolesInAnyColumn = 0;
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);
            int y = colHeight - 2;
            int holes = 0;

            while (y >= 0)
            {
                if (!board.getGrid(x, y))
                {
                    holes++;
                }
                y--;
            }

            if (mostHolesInAnyColumn < holes)
            {
                mostHolesInAnyColumn = holes;
            }
        }

        return mostHolesInAnyColumn;
    }

}

class SimpleHoles extends BoardRater
{

    @Override
    public double rate(final Board board)
    {
        int holes = 0;
        // Count the holes, and sum up the heights
        for (int x = 0; x < board.getWidth(); x++)
        {
            final int colHeight = board.getColumnHeight(x);

            int y = colHeight - 2; // addr of first possible hole

            while (y >= 0)
            {
                if (!board.getGrid(x, y))
                {
                    holes++;
                }
                y--;
            }
        }
        return holes;
    }

}

class ThreeVariance extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        final int w = board.getWidth();
        double runningVarianceSum = 0.0;
        for (int i = 0; i < w - 2; i++)
        {
            double h0 = board.getColumnHeight(i), h1 = board.getColumnHeight(i + 1), h2 = board.getColumnHeight(i + 2);
            final double m = (h0 + h1 + h2) / 3.0;
            h0 -= m;
            h1 -= m;
            h2 -= m;
            h0 *= h0;
            h1 *= h1;
            h2 *= h2;
            runningVarianceSum += (h0 + h1 + h2) / 3.0;
        }
        return runningVarianceSum / (w - 3);
    }

}

class Through extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        final int[] through = new int[board.getWidth()];
        int troughCount = 0;

        for (int x = 0; x < board.getWidth(); x++)
        {
            final int height = board.getColumnHeight(x);
            // store the hieght for each coloumn
            if (height > 0 && board.getGrid(x, height - 1))
            {
                through[x]++;
                troughCount++;
            }
        }
        return troughCount;
    }
}

class WeightedHoles extends BoardRater
{
    @Override
    public double rate(final Board board)
    {
        int maxHeight = 0;
        int minHeight = board.getHeight();

        for (int x = 0; x < board.getWidth(); x++)
        {
            final int height = board.getColumnHeight(x);
            if (height > maxHeight)
            {
                maxHeight = height;
            }
            if (height < minHeight)
            {
                minHeight = height;
            }
        }

        double weightedHoleCount = 0.0;
        final int[] heights = new int[board.getWidth()];

        for (int x = 0; x < board.getWidth(); x++)
        {
            heights[x] = board.getColumnHeight(x);
            int y = heights[x] - 2;
            while (y >= 0)
            {
                if (!board.getGrid(x, y))
                {
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
    static final int                POPULATION_SIZE      = 30;          // The size of the population of individuals
    static final int                AVG_CALCULATING_N    = 4;           // The number of trials to average on each set of coefficients
    static final double             ELITIST              = .10;         // What percent of the top individuals to retain from generation to generation
    static final int                STD_DEVIATIONS       = 3;           // Number of standard deviations to use from the positive normal model
                                                                         // while selecting mates for the individuals
    static final double             MUTATION_PROBABILITY = .50;         // The probability of an individual coefficient mutating
    private static final double     MUTATION_RANGE       = .15;         // How much (+ or -) to permit a mutation to occur
    static final double             RANDOM               = .10;         // What percent of new populations to fill with completely random coefficients
    static final int                COEFFICIENTS         = 13;          // Number of coefficients we are generating

    private static Random           rand                 = new Random();        // For generating random stuff
    private static TetrisController controller;                         // The Tetris game controller

    public static void main(String args[])
    {
        runCoefficientFinder();
    }

    /**
     * Runs the genetic algorithm
     */
    public static void runCoefficientFinder()
    {
        controller = new TetrisController();

        // Load the GUI
        // This can be safely commented out to disable the GUI
        RunTetrisGeneticView.load(controller);

        List<Individual> population = new ArrayList<>();

        // Create the initial population of completely random coefficients
        for (int i = 0; i < POPULATION_SIZE; i++)
        {
            Individual indiv = new Individual();

            indiv.coefficients = generateRandomCoefficients(COEFFICIENTS);
            indiv.moves = avgMoves(indiv.coefficients);         // Determine the average number of moves these coefficients create

            System.out.println(i + " " + indiv.moves);

            population.add(indiv);
        }

        Collections.sort(population); // Sort the population (better individuals earlier)

        System.out.println("Avg moves: " + populationAvgMoves(population));
        System.out.println(population);

        // Keep iterating and creating new generations
        while (true)
        {
            List<Individual> newPopulation = new ArrayList<>();

            // Add elitists in to the new generation
            // These consist of the very best individuals from the previous population
            System.out.println("Elitists");
            int number = (int) (ELITIST * POPULATION_SIZE);
            for (int i = 0; i < number; i++)
            {
                Individual indiv = population.get(i);
                System.out.println(i + " " + indiv.moves);
                newPopulation.add(indiv);
            }

            // Throw a couple new random individuals into the new population
            // This keeps genetic variability high so we prevent approaching coefficients that aren't the ideal
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

            // Create new individuals by mimicking intercourse between the top individuals from the previous generation
            System.out.println("Sex");
            for (int i = newPopulation.size(); i < POPULATION_SIZE; i++)
            {
                // Choose 2 fit parents. Uses a normal model to randomly select individuals, giving preference to the better ones
                int element = (int) Math.abs(rand.nextGaussian() * (POPULATION_SIZE / STD_DEVIATIONS));
                if (element >= POPULATION_SIZE)
                    element = POPULATION_SIZE - 1;

                Individual parent1 = population.get(element);

                element = (int) Math.abs(rand.nextGaussian() * (POPULATION_SIZE / STD_DEVIATIONS));
                if (element >= POPULATION_SIZE)
                    element = POPULATION_SIZE - 1;

                Individual parent2 = population.get(element);

                // Mix the parents by randomly selecting whether each coefficient comes from parent 1 or 2
                double[] newCoefs = new double[COEFFICIENTS];

                for (int k = 0; k < COEFFICIENTS; k++)
                {
                    if (rand.nextBoolean())
                        newCoefs[k] = parent1.coefficients[k];
                    else
                        newCoefs[k] = parent2.coefficients[k];
                }

                // Mutate the coefficients
                for (int k = 0; k < COEFFICIENTS; k++)
                {
                    // Randomly decide if we should mutate this one
                    boolean mutate = rand.nextInt((int) (1 / MUTATION_PROBABILITY)) == 0;

                    if (mutate)
                        newCoefs[k] += (rand.nextDouble() * MUTATION_RANGE * 2 - MUTATION_RANGE);       // Mutate it by +-MUTATION_RANGE
                }

                // Create the new individual using the new coefficients and evaluate its quality
                Individual newIndiv = new Individual();
                newIndiv.coefficients = newCoefs;
                newIndiv.moves = avgMoves(newIndiv.coefficients);

                System.out.println(newPopulation.size() + " " + newIndiv.moves);
                newPopulation.add(newIndiv);
            }

            // Prepare for the next generation by sorting and bumping the new one into the old spot
            population = newPopulation;
            Collections.sort(population);

            System.out.println("Avg moves: " + populationAvgMoves(population));
            System.out.println(population);
        }
    }

    /**
     * Calculates the average number of moves a population accomplished
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
        for (int i = 0; i < AVG_CALCULATING_N; i++)
        {
            int game = quickGame(coefficients);
            System.out.print(game + " ");

            sum += game;
        }
        System.out.print("} ");

        return sum / AVG_CALCULATING_N;
    }

    /**
     * Runs a single game with a set of coefficients and returns the number of moves the AI made it
     * 
     * @param coef The array of coefficients to use
     * @return The number of moves the AI made it
     */
    private static int quickGame(double[] coef)
    {
        // Start a game
        TetrisController tc = controller;
        tc.startGame();

        // Load the AI
        AI ai = new AmityAI(coef);

        // Run the game
        while (tc.gameOn)
        {
            Move move = ai.bestMove(new Board(tc.board), tc.currentMove.piece, tc.nextPiece, tc.board.getHeight() - TetrisController.TOP_SPACE);

            while (!tc.currentMove.piece.equals(move.piece))
            {
                tc.tick(TetrisController.ROTATE);
            }

            while (tc.currentMove.x != move.x)
            {
                tc.tick(((tc.currentMove.x < move.x) ? TetrisController.RIGHT : TetrisController.LEFT));
            }

            int current_count = tc.count;

            while ((current_count == tc.count) && tc.gameOn)
                tc.tick(TetrisController.DOWN);
        }

        return tc.count;
    }
}

/**
 * A set of coefficients and the avg number of move those coefficients were recorded as performing
 * 
 * @author Daniel Centore
 * 
 */
class Individual implements Comparable<Individual>
{
    double[] coefficients;
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

        // Create the Timer object and have it send
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

        // this.timer.start();
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
            // TODO Auto-generated catch block
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
     * Updates the timer to reflect the current setting of the speed slider.
     */
    public void updateTimer()
    {
        final double value = (double) this.speed.getValue() / this.speed.getMaximum();
        this.timer.setDelay((int) (this.DELAY - value * this.DELAY));
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
        this.rowsClearedLabel = new JLabel("0" + " Rows CLeared");
        panel.add(this.rowsClearedLabel);

        this.difficulty = new JLabel();
        panel.add(this.difficulty);

        // TIME
        this.timeLabel = new JLabel(" ");
        panel.add(this.timeLabel);

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
