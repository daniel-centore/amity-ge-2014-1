/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * An immutable representation of a tetris piece in a particular rotation. Each
 * piece is defined by the blocks that make up the body
 * 
 */
/**
 * 
 * @author justinbehymer
 */
public class Piece
{

    protected Point[]        body;
    private int[]            skirt;
    @Override
    public String toString()
    {
        return "Piece [body=" + Arrays.toString(this.body) + "]";
    }

    int                      width;
    private int              height;
    protected Piece          next;  // "next" rotation
    protected static Piece[] pieces; // singleton array of first rotations

    /**
     * Defines a new piece given the Points make up its body. Make its own copy
     * of the array and the Points inside it. Does not set up the rotations
     * 
     * 
     * As a private constructor, if the client wants a piece they must use
     * Piece.getPieces()
     * 
     * 
     */
    protected Piece(final Point[] points)
    {
        this.body = new Point[points.length];

        for (int i = 0; i < points.length; i++)
        {
            this.body[i] = new Point();
            this.body[i] = points[i];
        }
    }

    /**
     * Returns the width of the piece measured in blocks
     * 
     * @return
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Returns the height of the piece measured in blocks
     * 
     * @return
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Returns the pointer to the pieces body. The caller should not modify this
     * array
     * 
     * @return
     */
    public Point[] getBody()
    {
        return this.body;
    }

    /**
     * Returns the pointer to the pieces skirt. For each x value across the
     * piece, the skirt gives the lowest y value in the body. This is usually
     * for computing where the piece will land. The caller should not modify
     * this array
     * 
     * @return
     */
    public int[] getSkirt()
    {
        return this.skirt;
    }

    /**
     * Returns a piece that is 90 degrees counter-clockwise rotated from the
     * receiver
     * 
     * @return
     */
    public Piece nextRotation()
    {
        return this.next;
    }

    /**
     * Returns true if two pieces are the same, their bodies contain the same
     * points Used internally to detect if two rotations are effectively the
     * same
     * 
     * @param obj
     * @return
     */
    @Override
    public boolean equals(final Object obj)
    {
        // standard equals()
        if (((Piece) obj).body.length != this.body.length)
        {
            return false;
        }

        final Collection<Point> setA = new HashSet<Point>();
        final Collection<Point> setB = new HashSet<Point>();

        for (int i = 0; i < this.body.length; i++)
        {
            setA.add(((Piece) obj).body[i]);
            setB.add(this.body[i]);

        }
        return setA.equals(setB);

    }

    /**
     * Returns the array containing the first rotation of each of the 7 standard
     * tetris pieces. The next counter clock wise rotation can be obtained from
     * each pieces with the nextRotation().
     * 
     * @return
     */
    public static Piece[] getPieces()
    {
        // lazy evaluation -- create array if needed
        if (Piece.pieces == null)
        {

            // use pieceRow() to compute all the rotations for each piece
            Piece.pieces = new Piece[] { Piece.pieceRow(new Piece(Piece.parsePoints("0 0	0 1	0 2	0 3"))), // 0
                    Piece.pieceRow(new Piece(Piece.parsePoints("0 0	0 1	0 2	1 0"))), // 1
                    Piece.pieceRow(new Piece(Piece.parsePoints("0 0	1 0	1 1	1 2"))), // 2
                    Piece.pieceRow(new Piece(Piece.parsePoints("0 0	1 0	1 1	2 1"))), // 3
                    Piece.pieceRow(new Piece(Piece.parsePoints("0 1	1 1	1 0	2 0"))), // 4
                    Piece.pieceRow(new Piece(Piece.parsePoints("0 0	0 1	1 0	1 1"))), // 5
                    Piece.pieceRow(new Piece(Piece.parsePoints("0 0	1 0	1 1	2 0"))), // 6
            };
        }

        return Piece.pieces;
    }

    /**
     * given a string of x,y pairs. parse the points into a Point[] array
     * 
     * @param string
     * @return
     */
    protected static Point[] parsePoints(final String string)
    {

        final Vector<Point> points = new Vector<Point>();
        final StringTokenizer tok = new StringTokenizer(string);

        try
        {
            while (tok.hasMoreTokens())
            {
                final int x = Integer.parseInt(tok.nextToken());
                final int y = Integer.parseInt(tok.nextToken());

                points.addElement(new Point(x, y));
            }

        }
        catch (final NumberFormatException e)
        {
            throw new RuntimeException("Could not parse x,y string:" + string);
        }

        // make an array out of the vector
        final Point[] array = new Point[points.size()];
        points.copyInto(array);
        return array;

    }

    /**
     * given the first rotation of a piece. this computes all the other
     * rotations and links them all together by their next pointer
     * NextRotation() relies on the next pointers to get from one rotation to
     * the next Internally, uses Piece.equals() to detect when the rotations
     * have gotten us back to the first place
     * 
     * @param root
     * @return
     */
    protected static Piece pieceRow(final Piece root)
    {
        Piece temp = root;
        Piece prev = root;

        for (;;)
        {
            prev = temp;
            prev.setPieceDims();
            prev.setPieceSkirt();
            temp = new Piece(prev.body);

            if (!temp.equals(root))
            {
                prev.next = root;
            }
            else
            {

                prev.next = root;
                break;
            }
        }

        return root;
    }

    protected Piece rotatePiece()
    {
        Piece piece = null;
        final Point[] temp = new Point[this.body.length];

        // switch x,y to y,x
        for (int i = 0; i < this.body.length; i++)
        {
            temp[i] = new Point();
            temp[i].x = this.body[i].y;
            temp[i].y = this.body[i].x;
        }

        piece = new Piece(temp);
        piece.setPieceDims();

        for (int i = 0; i < piece.body.length; i++)
        {
            temp[i].x = piece.width - 1 - piece.body[i].x;
            temp[i].y = piece.body[i].y;

        }

        piece = new Piece(temp);
        return piece;
    }

    protected void setPieceDims()
    {
        int wmax = -1;
        int hmax = -1;

        for (int i = 0; i < this.body.length; i++)
        {
            if (this.body[i].x > wmax)
            {
                wmax = this.body[i].x;
            }
            if (this.body[i].y > hmax)
            {
                hmax = this.body[i].y;
            }
        }

        this.width = wmax + 1;
        this.height = hmax + 1;

    }

    protected void setPieceSkirt()
    {
        final int wmax = this.width;
        int hmax;

        this.skirt = new int[wmax];

        for (int i = 0; i < wmax; i++)
        {
            Point temp = null;
            hmax = 10000;

            for (int j = 0; j < this.body.length; j++)
            {
                if (this.body[j].x == i)
                {
                    if (this.body[j].y < hmax)
                    {
                        hmax = this.body[j].y;
                        temp = this.body[j];
                    }
                }
            }

            this.skirt[i] = temp.y;
        }
    }

}
