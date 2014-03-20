/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in the editor.
 */

package AIHelper;

import tetris.Board;

/**
 * 
 * @author justinbehymer
 */
public class FinalRater extends BoardRater
{
    public static class RaterPair
    {
        public double     coeff;
        public BoardRater rater;

        public RaterPair(final double coeff_, final BoardRater rater_)
        {
            this.coeff = coeff_;
            this.rater = rater_;
        }
    }

    public static RaterPair[] raters = {
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

    public FinalRater()
    {
    }

    public FinalRater(final double[] c)
    {
        if (c.length != FinalRater.raters.length)
        {
            System.err.println("Make sure that the array passed into the FinalRater has the correct number of coefficients! Using DEFAULT COEFFICIENTS instead!");
            return;
        }

        for (int i = 0; i < c.length; i++)
        {
            FinalRater.raters[i].coeff = c[i];
        }
    }

    @Override
    double rate(final Board board)
    {
        double score = 0;

        for (final RaterPair rater : FinalRater.raters)
        {
            score += rater.coeff * rater.rater.rate(board);
        }

        return score;
    }
}
