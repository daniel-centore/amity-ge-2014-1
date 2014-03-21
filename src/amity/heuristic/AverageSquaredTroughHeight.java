/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package amity.heuristic;

import tetris.Board;

public class AverageSquaredTroughHeight extends BoardRater
{
    double rate(Board board)
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
