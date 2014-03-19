/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package amity.genetic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import tetris.AI;
import tetris.Board;
import tetris.Move;
import tetris.TetrisController;

/**
 * 
 * @author justinbehymer
 */
public class TetrisGameRunner
{
    private final TetrisController  tc;

    private final javax.swing.Timer timer;
    private final AI                brain;
    int                             current_count = -1;
    private Move                    mMove;

    public TetrisGameRunner(final AI brain)
    {
        this.brain = brain;
        this.tc = new TetrisController();

        this.timer = new javax.swing.Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                TetrisGameRunner.this.tick(TetrisController.DOWN);
            }
        });

    }

    public int getResult()
    {
        this.startGame();

        while (this.tc.gameOn)
        {
            try
            {
                Thread.sleep(1);
            }
            catch (final InterruptedException e)
            {
            }
        }

        this.stopGame();

        return this.tc.count;
    }

    public TetrisController getTc()
    {
        return this.tc;
    }

    protected void tick(final int verb)
    {
        if (this.tickAI())
        {
            this.superTick(verb);
        }
    }

    private void superTick(final int verb)
    {
        this.tc.tick(verb);

        if (!this.tc.gameOn)
        {
            this.stopGame();
        }
    }

    public int getCount()
    {
        return this.tc.count;
    }

    public boolean tickAI()
    {
        if (this.current_count != this.tc.count)
        {
            this.current_count = this.tc.count;
            this.mMove = this.brain.bestMove(new Board(this.tc.board), this.tc.currentMove.piece, this.tc.nextPiece, this.tc.board.getHeight() - TetrisController.TOP_SPACE);
        }

        if (!this.tc.currentMove.piece.equals(this.mMove.piece))
        {
            this.superTick(TetrisController.ROTATE);
        }
        else if (this.tc.currentMove.x != this.mMove.x)
        {
            this.superTick(this.tc.currentMove.x < this.mMove.x ? TetrisController.RIGHT : TetrisController.LEFT);
        }
        else
        {
            return true;
        }
        return false;
    }

    /**
     * Sets the internal state and starts the timer so the game is happening.
     */
    public void startGame()
    {
        this.tc.startGame();

        this.timer.start();
    }

    /**
     * Stops the game.
     */
    public void stopGame()
    {
        this.tc.gameOn = false;
        this.timer.stop();
    }

}
