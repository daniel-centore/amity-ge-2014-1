package amity;

import amity.ai.AmityAI;
import amity.genetic.TetrisGameRunner;
import tetris.ITLPAI;
import tetris.RunTetris;
import tetris.RunTetrisAI;
import tetris.TetrisController;

/**
 * 
 * @author drdanielfc
 *
 */
public class Main
{
	public static void main(String args[])
	{
		// RunTetris.load(null);

		// RunTetris.load(new ITLPAI());
		// RunTetris.load(new AmityAI());

		TetrisGameRunner run = new TetrisGameRunner(new ITLPAI());

		int total = 0;

		for (int i = 0; i < 30; i++)
		{
			run.startGame();

			while (run.getTc().gameOn)
			{
				try
				{
					Thread.sleep(1);
				} catch (InterruptedException e)
				{
				}
			}

			total += run.getCount();

			System.out.println((double) total / (i + 1));

			run.stopGame();
		}

	}

}