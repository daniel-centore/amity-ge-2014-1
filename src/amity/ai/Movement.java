package amity.ai;

import tetris.Board;
import tetris.Piece;

public class Movement
{

    public Movement(final int dropX, final Board board, final Piece piece, final Movement upper)
    {
        this.dropX = dropX;
        this.board = board;
        this.piece = piece;
        this.upper = upper;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.board == null ? 0 : this.board.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        final Movement other = (Movement) obj;
        if (this.board == null)
        {
            if (other.board != null)
            {
                return false;
            }
        }
        else if (!this.board.equals(other.board))
        {
            return false;
        }
        return true;
    }

    int      dropX; // The x-value at which we drop until we hit the ground
    int      moveX; // The x-value to go to once at the bottom
    Board    board; // The final position
    Piece    piece;
    Movement upper;	// The earlier movement in a chain
}
