/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package amity.heuristic;

import tetris.Board;

/**
 * 
 * @author justinbehymer
 */
public class Through extends BoardRater
{
    @Override
    double rate(final Board board)
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
