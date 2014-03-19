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
public class HeightVar extends BoardRater
{
    @Override
    double rate(final Board board)
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
