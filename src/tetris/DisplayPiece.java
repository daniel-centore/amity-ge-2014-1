/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tetris;

import java.awt.Color;
import java.awt.Point;

/**
 *
 * @author justinbehymer
 */
public class DisplayPiece extends Piece {

    protected static DisplayPiece[] pieces; //singleton array of first rotations
    String label = "";
    Color color;
    protected DisplayPiece next; //"next" rotation
    
    
    public DisplayPiece(Point[] points) {
        super(points);
    }
    
    public DisplayPiece(String label, Color color, Point[] points)
    {
        this(points);
        this.label = label;
        this.color = color;
    }
    
    
    
    
    
    
    /**
     * Returns a piece that is 90 degrees counter-clockwise rotation from the reciever
     * 
     * 
     * This class  precomputes all the rotations at once. This method just hops from one precomupted
     * rotation to the next rotation in constant time
     * @return 
     */
    public DisplayPiece nextRotation() {
        return next;
        
    }
    
    
    /**
     * Returns an array containing the first rotation of each of the 7 standard tetris pieces
     * The next counterclock rotation can be obtained from each piece with NextRotation message
     * @return 
     */
    public static DisplayPiece[] getPieces() 
    {
		if (pieces==null) 
                {	
			// use pieceRow() to compute all the rotations for each piece
			pieces = new DisplayPiece[] 
                        {
				pieceRow(new DisplayPiece("i", Color.cyan, parsePoints("0 0	0 1	0 2	0 3"))),	// 0
				pieceRow(new DisplayPiece("j", Color.blue, parsePoints("0 0	0 1	0 2	1 0"))),	// 1
				pieceRow(new DisplayPiece("l", Color.pink, parsePoints("0 0	1 0	1 1	1 2"))),	// 2
				pieceRow(new DisplayPiece("z", Color.red, parsePoints("0 0	1 0	1 1	2 1"))),	// 3
				pieceRow(new DisplayPiece("s", Color.green, parsePoints("0 1	1 1	1 0	2 0"))),	// 4
				pieceRow(new DisplayPiece("o", Color.yellow, parsePoints("0 0	0 1	1 0	1 1"))),	// 5
				pieceRow(new DisplayPiece("t", Color.magenta, parsePoints("0 0	1 0	1 1	2 0"))),	// 6
			};
		}
		
		return(pieces);
	}
    
    
    protected static DisplayPiece pieceRow(DisplayPiece root)
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

            
            if(!temp.equals(root))
            {
                prev.next = temp;
            } else {
                
                prev.next = root;
                break;
                
            }
        }
        
        return root;
    }
    
    
    
    protected DisplayPiece rotatePiece()
    {
        DisplayPiece piece = null;
        Point[] temp = new Point[body.length];
        
        //switch x,y y,x
        for (int i = 0; i < body.length; i++)
        {
            temp[i] = new Point();
            temp[i].x = body[i].y;
            temp[i].y = body[i].x;           
            
        }
        
        piece = new DisplayPiece(label, color, temp);
        piece.setPieceDims();
        
        for (int i = 0; i < piece.body.length; i++)
        {
            temp[i].x = (piece.width - 1) - piece.body[i].x;
            temp[i].y = piece.body[i].y;
            
        }
        
        piece = new DisplayPiece(label, color, temp);
        return(piece);
    }
    
    
    
}
