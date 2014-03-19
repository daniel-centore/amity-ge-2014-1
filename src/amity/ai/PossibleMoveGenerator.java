package amity.ai;

import java.util.ArrayList;
import java.util.List;

import tetris.Board;
import tetris.Piece;

public class PossibleMoveGenerator
{
    public static List<Movement> possibleBoards(final Board board, final Piece pieceAdd, final Movement upper, final int limitHeight)
    {
        final List<Movement> allMovements = new ArrayList<>();

        Piece current = pieceAdd;

        // loop through all the rotations
        do
        {
            final List<Movement> movements = new ArrayList<>();

            // Find all possible drop-down positions
            final int yBound = limitHeight - current.getHeight() + 1;
            final int xBound = board.getWidth() - current.getWidth() + 1;

            for (int x = 0; x < xBound; x++)
            {
                final int y = board.dropHeight(current, x);

                if (y < yBound && board.canPlace(current, x, y))
                {
                    final Board testBoard = new Board(board);
                    testBoard.place(current, x, y);

                    final Movement m = new Movement(x, testBoard, current, upper);

                    movements.add(m);
                }
            }

            allMovements.addAll(movements);

            // // Find all possible left+right movements once at bottom
            // for (Movement m : movements)
            // {
            // int x = m.dropX;
            // int y = board.dropHeight(current, x);
            //
            // // See how far right we can go
            // int curX = x;
            // while (true)
            // {
            // curX++;
            //
            // if (board.canPlace(current, curX, y))
            // {
            // Board testBoard = new Board(board);
            // testBoard.place(current, curX, y);
            //
            // Movement mR = new Movement(x, curX, testBoard, current, upper);
            //
            // allMovements.add(mR);
            // }
            // else
            // break;
            // }
            //
            // // See how far left we can go
            // curX = x;
            // while (true)
            // {
            // curX--;
            //
            // if (board.canPlace(current, curX, y))
            // {
            // Board testBoard = new Board(board);
            // testBoard.place(current, curX, y);
            //
            // Movement mR = new Movement(x, curX, testBoard, current, upper);
            //
            // // checks if allMovements already has a movement with a final
            // board equal to this one
            // // skip if so b/c not necessary to have multiple ways to get
            // there - waste of time
            // if (!allMovements.contains(new Movement(0, 0, testBoard, current,
            // upper)))
            // {
            // allMovements.add(mR);
            // }
            // }
            // else
            // break;
            // }
            //
            // }

            current = current.nextRotation();
        } while (current != pieceAdd);

        return allMovements;
    }

}
