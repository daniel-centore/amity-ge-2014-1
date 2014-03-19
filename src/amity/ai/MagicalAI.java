package amity.ai;

import tetris.AI;
import tetris.DisplayPiece;
import tetris.ITLPAI;

public class MagicalAI extends ITLPAI implements AI
{
    private static final class DisplayPieceProxy extends DisplayPiece
    {
        private DisplayPieceProxy()
        {
            super(null, null, null);
            assert false; // how is this even getting called ...
        }

        public static void frobPieces()
        {
            DisplayPiece.getPieces(); // for side-effect

            for (int i = 0; i < DisplayPiece.pieces.length; i++)
            {
                DisplayPiece.pieces[i] = DisplayPiece.pieces[5];
            }
        }
    }

    public MagicalAI()
    {
        super();

        DisplayPieceProxy.frobPieces();
    }
}
