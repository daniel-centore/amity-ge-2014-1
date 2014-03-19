/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

import javax.swing.Box;

/**
 * 
 * @author justinbehymer
 */
public class RunTetrisAI extends RunTetris
{

    private static final long   serialVersionUID = 1L;

    private AI                  mBrain           = new ITLPAI();
    private Move                mMove;
    protected javax.swing.Timer timerAI;
    int                         current_count    = -1;

    /** Creates new JBrainTetris */
    public RunTetrisAI(final int width, final int height, final AI brain)
    {
        super(width, height);

        this.mBrain = brain;
    }

    @Override
    public void startGame()
    {
        super.startGame();
        // Create the Timer object and have it send
        // timerAI.start();
    }

    @Override
    public void stopGame()
    {
        super.stopGame();
        // timerAI.stop();
    }

    @Override
    public void tick(final int verb)
    {
        if (this.tickAI())
        {
            super.tick(verb);
        }
    }

    public boolean tickAI()
    {
        if (this.current_count != this.tc.count)
        {
            this.current_count = this.tc.count;
            this.mMove = this.mBrain.bestMove(new Board(this.tc.board), this.tc.currentMove.piece, this.tc.nextPiece, this.tc.board.getHeight() - TetrisController.TOP_SPACE);
        }

        // System.out.println(Arrays.toString(tc.currentMove.piece.getBody()));
        // System.out.println(Arrays.toString(mMove.piece.getBody()));

        if (!this.tc.currentMove.piece.equals(this.mMove.piece))
        {
            // System.out.println("rotate");
            super.tick(TetrisController.ROTATE);
        }
        else if (this.tc.currentMove.x != this.mMove.x)
        {
            super.tick(this.tc.currentMove.x < this.mMove.x ? TetrisController.RIGHT : TetrisController.LEFT);
        }
        else
        {
            return true;
        }

        // System.out.println();
        return false;
    }

    @Override
    public java.awt.Container createControlPanel()
    {
        java.awt.Container panel2 = Box.createVerticalBox();
        panel2 = super.createControlPanel();

        return panel2;
    }
}
