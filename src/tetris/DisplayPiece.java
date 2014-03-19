/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

import java.awt.Color;
import java.awt.Point;

/**
 * 
 * @author justinbehymer
 */
public class DisplayPiece extends Piece
{

    protected static DisplayPiece[] pieces;    // singleton array of first
                                                // rotations
    String                          label = "";
    Color                           color;
    protected DisplayPiece          next;      // "next" rotation

    public DisplayPiece(final Point[] points)
    {
        super(points);
    }

    public DisplayPiece(final String label, final Color color, final Point[] points)
    {
        this(points);
        this.label = label;
        this.color = color;
    }

    /**
     * Returns a piece that is 90 degrees counter-clockwise rotation from the
     * reciever
     * 
     * 
     * This class precomputes all the rotations at once. This method just hops
     * from one precomupted rotation to the next rotation in constant time
     * 
     * @return
     */
    @Override
    public DisplayPiece nextRotation()
    {
        return this.next;

    }

    /**
     * Returns an array containing the first rotation of each of the 7 standard
     * tetris pieces The next counterclock rotation can be obtained from each
     * piece with NextRotation message
     * 
     * @return
     */
    public static DisplayPiece[] getPieces()
    {
        if (DisplayPiece.pieces == null)
        {
            // use pieceRow() to compute all the rotations for each piece
            DisplayPiece.pieces = new DisplayPiece[] { DisplayPiece.pieceRow(new DisplayPiece("i", Color.cyan, Piece.parsePoints("0 0	0 1	0 2	0 3"))), // 0
                    DisplayPiece.pieceRow(new DisplayPiece("j", Color.blue, Piece.parsePoints("0 0	0 1	0 2	1 0"))), // 1
                    DisplayPiece.pieceRow(new DisplayPiece("l", Color.pink, Piece.parsePoints("0 0	1 0	1 1	1 2"))), // 2
                    DisplayPiece.pieceRow(new DisplayPiece("z", Color.red, Piece.parsePoints("0 0	1 0	1 1	2 1"))), // 3
                    DisplayPiece.pieceRow(new DisplayPiece("s", Color.green, Piece.parsePoints("0 1	1 1	1 0	2 0"))), // 4
                    DisplayPiece.pieceRow(new DisplayPiece("o", Color.yellow, Piece.parsePoints("0 0	0 1	1 0	1 1"))), // 5
                    DisplayPiece.pieceRow(new DisplayPiece("t", Color.magenta, Piece.parsePoints("0 0	1 0	1 1	2 0"))), // 6
            };
        }

        return DisplayPiece.pieces;
    }

    protected static DisplayPiece pieceRow(final DisplayPiece root)
    {
        DisplayPiece temp = root;
        DisplayPiece prev = root;

        for (;;)
        {
            prev = temp;
            prev.setPieceDims();
            prev.setPieceSkirt();
            temp = new DisplayPiece(prev.label, prev.color, prev.body);
            temp = temp.rotatePiece();

            if (!temp.equals(root))
            {
                prev.next = temp;
            }
            else
            {

                prev.next = root;
                break;

            }
        }

        return root;
    }

    @Override
    protected DisplayPiece rotatePiece()
    {
        DisplayPiece piece = null;
        final Point[] temp = new Point[this.body.length];

        // switch x,y y,x
        for (int i = 0; i < this.body.length; i++)
        {
            temp[i] = new Point();
            temp[i].x = this.body[i].y;
            temp[i].y = this.body[i].x;

        }

        piece = new DisplayPiece(this.label, this.color, temp);
        piece.setPieceDims();

        for (int i = 0; i < piece.body.length; i++)
        {
            temp[i].x = piece.width - 1 - piece.body[i].x;
            temp[i].y = piece.body[i].y;

        }

        piece = new DisplayPiece(this.label, this.color, temp);
        return piece;
    }

}
