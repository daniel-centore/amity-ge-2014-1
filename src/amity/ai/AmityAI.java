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
        double bestScore = 1e20;
        int bestX = -1;
        int bestY = -1;
        Piece bestPiece = null;

        Piece current = piece;
        Piece next = nextPiece;

        // loop through all the rotations
        do
        {
            final int yBound = limitHeight - current.getHeight() + 1;
            final int xBound = board.getWidth() - current.getWidth() + 1;

            // For current rotation, try all the possible columns
            for (int x = 0; x < xBound; x++)
            {
                int y = board.dropHeight(current, x);
                if ((y < yBound) && board.canPlace(current, x, y))
                {
                    Board testBoard = new Board(board);
                    testBoard.place(current, x, y);
                    testBoard.clearRows();

                    // Everything in this while loop evaluates possible moves
                    // with the next piece
                    do
                    {
                        final int jBound = limitHeight - next.getHeight() + 1;
                        final int iBound = testBoard.getWidth() - next.getWidth() + 1;

                        for (int i = 0; i < iBound; i++)
                        {
                            int j = testBoard.dropHeight(next, i);
                            if (j < jBound && testBoard.canPlace(next, i, j))
                            {
                                Board temp = new Board(testBoard);
                                temp.place(next, i, j);
                                temp.clearRows();

                                double nextScore = rater.rateBoard(temp);

                                if (nextScore < bestScore)
                                {
                                    bestScore = nextScore;
                                    bestX = x;
                                    bestY = y;
                                    bestPiece = current;
                                }
                            }

                        }

                        next = next.nextRotation();
                    } while (next != nextPiece);
                    // Back out to the current piece

                }
            }
            current = current.nextRotation();
        } while (current != piece);

        Move move = new Move();
        move.x = bestX;
        move.y = bestY;
        move.piece = bestPiece;
        return (move);
    }

    @Override
    public void setRater(final BoardRater r)
    {

    }

}
