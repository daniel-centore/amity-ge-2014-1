package amity.ai;

import tetris.Board;
import tetris.Piece;

public class Movement
{

	public Movement(int dropX, Board board, Piece piece, Movement upper)
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
		result = prime * result + ((board == null) ? 0 : board.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Movement other = (Movement) obj;
		if (board == null)
		{
			if (other.board != null)
				return false;
		}
		else if (!board.equals(other.board))
			return false;
		return true;
	}

	int dropX; // The x-value at which we drop until we hit the ground
	int moveX; // The x-value to go to once at the bottom
	Board board; // The final position
	Piece piece;
	Movement upper;	// The earlier movement in a chain
}
