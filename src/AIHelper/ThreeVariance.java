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
public class ThreeVariance extends BoardRater
{
    @Override
    double rate(final Board board)
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
