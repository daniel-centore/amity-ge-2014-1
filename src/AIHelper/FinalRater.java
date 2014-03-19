/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package AIHelper;

import tetris.Board;

/**
 * 
 * @author justinbehymer
 */
public class FinalRater extends BoardRater
{
    public static BoardRater raters[]     = { new ConsecHorzHoles(), new HeightAvg(), new HeightMax(), new HeightMinMax(), new HeightVar(), new HeightStdDev(), new SimpleHoles(), new ThreeVariance(), new Through(), new WeightedHoles(), new RowsWithHolesInMostHoledColumn(), new AverageSquaredTroughHeight(), new BlocksAboveHoles() };

    
//    public double[]          coefficients = {
//                                          /* new ConsecHorzHoles(), */0,
//                                          /* new HeightAvg(), */10,
//                                          /* new HeightMax(), */1,
//                                          /* new HeightMinMax(), */1,
//                                          /* new HeightVar(), */0,
//                                          /* new HeightStdDev(), */5,
//                                          /* new SimpleHoles(), */40,
//                                          /* new ThreeVariance(), */10,
//                                          /* new Trough(), */1,
//                                          /* new WeightedHoles(), */4,
//                                          /*
//                                           * new
//                                           * RowsWithHolesInMostHoledColumn()
//                                           */100,
//                                          /* new AverageSquaredTroughHeight() */15,
//                                          /* new BlocksAboveHoles() */2 };
    
    public double[] coefficients = {0.41430724103382527, 0.04413383739389207, 0.1420172532064692, -0.13881428312611474, 0.06887679285238696, -0.052368130931930074, 0.33235754477242435, 0.2851778629665227, -0.03011693088344261, -0.02534983335709433, 0.21155050264421074, 0.8292064267563932, 0.0038145282373974604};

    public FinalRater()
    {
        // System.out.println("new final rater:");
        // String temp;`
        // for(int i=0; i<raters.length; i++) {
        // System.out.println((temp=""+coefficients[i]).substring(0,temp.length()>=4?temp.length():3)+"\t\t"+raters[i]);
        // }
    }

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
    double rate(final Board board)
    {
        double score = 0, temp;
        for (int x = 0; x < FinalRater.raters.length; x++)
        {
            score += (temp = this.coefficients[x]) == 0 ? 0 : temp * FinalRater.raters[x].rate(board);
            // System.out.print(this.coefficients[x]);
        }
        return score;
    }

    double rate(final Board board, final double[] coefficients)
    {
        final double[] temp = this.coefficients;
        this.coefficients = coefficients;
        final double ret = this.rate(board);
        this.coefficients = temp;
        return ret;
    }
}
