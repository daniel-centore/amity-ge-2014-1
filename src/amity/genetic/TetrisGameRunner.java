/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
	private TetrisController tc;
	

	private javax.swing.Timer timer;
	private AI brain;
	int current_count = -1;
	private Move mMove;

	public TetrisGameRunner(AI brain)
	{
		this.brain = brain;
		tc = new TetrisController();

		timer = new javax.swing.Timer(0, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tick(TetrisController.DOWN);
			}
		});

	}
	
	public boolean running()
	{
	    return tc.gameOn;
	}
	
	public int getResult()
	{
		startGame();
		
		while (tc.gameOn)
		{
			try
			{
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
			}
		}
		
		stopGame();
		
		return tc.count;
	}
	
	public TetrisController getTc()
	{
		return tc;
	}

	protected void tick(int verb)
	{
		if (tickAI())
		{
			superTick(verb);
		}
	}
	
	private void superTick(int verb)
	{
		tc.tick(verb);

		if (!tc.gameOn)
			stopGame();
	}
	
	public int getCount()
	{
		return tc.count;
	}
	
	public boolean tickAI()
	{
		if (current_count != tc.count)
		{
			current_count = tc.count;
			mMove = brain.bestMove(new Board(tc.board), tc.currentMove.piece, tc.nextPiece, tc.board.getHeight() - TetrisController.TOP_SPACE);
		}

		if (!tc.currentMove.piece.equals(mMove.piece))
		{
			superTick(TetrisController.ROTATE);
		}
		else if (tc.currentMove.x != mMove.x)
		{
			superTick(((tc.currentMove.x < mMove.x) ? TetrisController.RIGHT : TetrisController.LEFT));
		}
		else
		{
			return true;
		}
		return false;
	}


	/**
	 Sets the internal state and starts the timer
	 so the game is happening.
	*/
	public void startGame()
	{
		tc.startGame();

		timer.start();
	}

	/**
	 Stops the game.
	*/
	public void stopGame()
	{
        tc.gameOn = false;
        timer.stop();
    }

}
