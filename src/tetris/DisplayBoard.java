/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

import java.awt.Color;
import java.awt.Point;

/**
 * 
 * @author justinbehymer
 */
public class DisplayBoard extends Board
{

    Color[][] colorGrid;

    public DisplayBoard(final int width, final int height)
    {
        super(width, height);
        this.colorGrid = new Color[width][height];
    }

    public DisplayBoard(final DisplayBoard o)
    {
        this(o.width, o.height);

        for (int x = 0; x < this.width; x++)
        {
            for (int y = 0; y < this.height; y++)
            {
                this.grid[x][y] = o.grid[x][y];
                this.colorGrid[x][y] = o.colorGrid[x][y];
            }

        }
    }

    @Override
    public DisplayBoard clone()
    {
        final DisplayBoard cloned = new DisplayBoard(this.width, this.height);

        for (int x = 0; x < this.width; x++)
        {
            for (int y = 0; y < this.height; y++)
            {
                cloned.grid[x][y] = this.grid[x][y];
                cloned.colorGrid[x][y] = this.colorGrid[x][y];

            }
        }

        return cloned;
    }

    public void place(final DisplayPiece piece, final int x, final int y)
    {
        if (!this.canPlace(piece, x, y))
        {
            return;
        }

        for (final Point block : piece.getBody())
        {
            // do not allow to create blocks above the screen
            if (y + block.y < this.height)
            {
                this.grid[x + block.x][y + block.y] = true;
                this.colorGrid[x + block.x][y + block.y] = piece.color;

            }
        }
    }

    @Override
    public void place(final Move move)
    {
        this.place((DisplayPiece) move.piece, move.x, move.y);
    }

    @Override
    public int clearRows()
    {
        int cleared = 0;

        for (int i = 0; i < this.height; i++)
        {
            if (this.getRowCount(i) >= this.width)
            {
                cleared++;

                for (int j = 0; j < this.width; j++)
                {
                    System.arraycopy(this.grid[j], i + 1, this.grid[j], i, this.height - 1 - i);
                    this.grid[j][this.height - 1] = false;
                    this.colorGrid[j][i] = this.colorGrid[j][i + 1];
                }

                i--;
            }
        }

        return cleared;
    }

}
