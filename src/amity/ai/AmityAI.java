package amity.ai;

import java.util.ArrayList;
import java.util.List;

import tetris.AI;
import tetris.Board;
import tetris.Move;
import tetris.Piece;
import AIHelper.BoardRater;
import AIHelper.FinalRater;

public class AmityAI implements AI
{
	private FinalRater rater;
	private Movement bestMovement;

	public AmityAI(double[] coefficients)
	{
		rater = new FinalRater(coefficients);
	}

	public AmityAI()
	{
		rater = new FinalRater();
	}

	@Override
	public Move bestMove(Board board, Piece piece, Piece nextPiece, int limitHeight)
	{
		double bestScore = Double.MAX_VALUE;

		// Find all possible end positions, INCLUDING NEXT PIECE, including how to get there
		List<Movement> level1 = PossibleMoveGenerator.possibleBoards(board, piece, null, limitHeight);
		List<Movement> level2 = new ArrayList<>();
		for (Movement m : level1)
		{
			List<Movement> sub1 = PossibleMoveGenerator.possibleBoards(m.board, nextPiece, m, limitHeight);
			level2.addAll(sub1);
		}

		// Weigh all boards using algorithm

		for (Movement m : level2)
		{
			double score = rater.rateBoard(m.board);

			if (score < bestScore)
			{
				bestScore = score;
				bestMovement = m;
			}
		}

		if (bestMovement == null)		// no possible option; give up :(
		{
			Move m = new Move();
			m.piece = piece;
			m.x = 0;
			
			return m;
				
		}
		
		// Use the lowest score
		Move m = new Move();
		Movement actual = bestMovement.upper;
		m.piece = actual.piece;
		m.x = actual.dropX;
		
		if (actual.moveX == 0)	// if there's no second step, then don't use one
			bestMovement = null;
		
		return m;
	}

	@Override
	public void setRater(BoardRater r)
	{

	}

}
