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
    private final FinalRater rater;
    private Movement         bestMovement;

    public AmityAI(final double[] coefficients)
    {
        this.rater = new FinalRater(coefficients);
    }

    public AmityAI()
    {
        this.rater = new FinalRater();
    }

    @Override
    public Move bestMove(final Board board, final Piece piece, final Piece nextPiece, final int limitHeight)
    {
        double bestScore = Double.MAX_VALUE;

        // Find all possible end positions, INCLUDING NEXT PIECE, including how
        // to get there
        final List<Movement> level1 = PossibleMoveGenerator.possibleBoards(board, piece, null, limitHeight);
        final List<Movement> level2 = new ArrayList<>();
        for (final Movement m : level1)
        {
            final List<Movement> sub1 = PossibleMoveGenerator.possibleBoards(m.board, nextPiece, m, limitHeight);
            level2.addAll(sub1);
        }

        // Weigh all boards using algorithm

        for (final Movement m : level2)
        {
            final double score = this.rater.rateBoard(m.board);

            if (score < bestScore)
            {
                bestScore = score;
                this.bestMovement = m;
            }
        }

        if (this.bestMovement == null)		// no possible option; give up :(
        {
            final Move m = new Move();
            m.piece = piece;
            m.x = 0;

            return m;

        }

        // Use the lowest score
        final Move m = new Move();
        final Movement actual = this.bestMovement.upper;
        m.piece = actual.piece;
        m.x = actual.dropX;

        if (actual.moveX == 0)
        {
            this.bestMovement = null;
        }

        return m;
    }

    @Override
    public void setRater(final BoardRater r)
    {

    }

}
