/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

import java.util.Random;

/**
 * 
 * @author justinbehymer
 */
public class TetrisController
{

    public static final int WIDTH       = 10;
    public static final int HEIGHT      = 20;

    public int              rowsCleared = 0;

    // extra blocks at the top for the pieces to start
    // if a piece is sticking into the area, the game is over
    public static final int TOP_SPACE   = 4;

    // board data structures
    public DisplayBoard     displayBoard;
    public DisplayBoard     board;
    public DisplayPiece[]   pieces;

    // the current piece in play or null;

    // the piece which will be genereted next
    public DisplayPiece     nextPiece;
    public Move             currentMove;

    // game state
    public boolean          gameOn;          // true if game is playing
    public int              count;
    public int              difficulty;

    // random generate new pieces
    public Random           random;

    public TetrisController()
    {
        this.gameOn = false;

        this.pieces = DisplayPiece.getPieces();
        this.board = new DisplayBoard(TetrisController.WIDTH, TetrisController.HEIGHT + TetrisController.TOP_SPACE);
        this.displayBoard = new DisplayBoard(this.board);
    }

    public static final int ROTATE = 0;
    public static final int LEFT   = 1;
    public static final int RIGHT  = 2;
    public static final int DROP   = 3;
    public static final int DOWN   = 4;

    /**
     * Called to change the position of the current piece Each key press call
     * this once with the verbs LEFT RIGHT ROTATE DROP for the user moves and
     * the timer calls it with the verb DOWN to move the piece down one square
     * 
     * 
     * Before this is called, the piece is at the same location in the board.
     * this advances the piece to be at its next location
     */
    public void tick(final int verb)
    {
        if (!this.gameOn)
        {
            return;
        }

        // sets the new ivars
        final Move newMove = this.computeNewPosition(verb, this.currentMove);

        // how to detect when a piece has landed
        // if this move hits something on its down vert, and the pervious verb
        // was also down
        // then the pervious position must be the correct landed position

        if (this.board.canPlace(newMove))
        {
            this.currentMove = newMove;
            this.displayBoard = new DisplayBoard(this.board);
            this.displayBoard.place(this.currentMove);
        }
        else
        {
            // landed
            if (verb == TetrisController.DOWN)
            {
                this.board.place(this.currentMove);
                this.rowsCleared += this.board.clearRows();

                // check to see if board is too tall
                if (this.board.getMaxHeight() > this.board.getHeight() - TetrisController.TOP_SPACE)
                {
                    this.gameOn = false;
                }
                else
                {
                    // add a new piece and keep playing
                    this.addNewPiece();
                }
            }
        }
    }

    /**
     * figues a new position for the current piece based on the given verb sets
     * the ivars newX, newY, and newPiece to hold what it thinks the new piece
     * position should be
     */
    public Move computeNewPosition(final int verb, final Move currentMove)
    {
        // as a starting point, the new position is the same as the old
        Piece newPiece = currentMove.piece;
        int newX = currentMove.x;
        int newY = currentMove.y;

        // make changes based on the verbs
        switch (verb)
        {
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
                break;
            case ROTATE:
                newPiece = newPiece.nextRotation();

                // make the piece rotate at its center
                newX = newX + (currentMove.piece.getWidth() - newPiece.getWidth()) / 2;
                newY = newY + (currentMove.piece.getHeight() - newPiece.getHeight()) / 2;

                break;

            case DOWN:
                newY--;
                break;
            case DROP:
                newY = this.board.dropHeight(newPiece, newX);
                if (newY > currentMove.y)
                {
                    newY = currentMove.y;
                }
                break;
            default:
                throw new RuntimeException("Bad verb");
        }

        final Move newMove = new Move();
        newMove.piece = newPiece;
        newMove.x = newX;
        newMove.y = newY;

        return newMove;
    }

    /**
     * sets the intial state and starts the timer
     * 
     */
    public void startGame()
    {
        // different sequence each game
        this.random = new Random();

        // reset the baord state
        this.board = new DisplayBoard(TetrisController.WIDTH, TetrisController.HEIGHT + TetrisController.TOP_SPACE);

        this.count = 0;
        this.rowsCleared = 0;
        this.gameOn = true;

        this.nextPiece = this.pickNextPiece();

        this.addNewPiece();

    }

    /**
     * selects the next piece to use using the random generator
     * 
     */
    public DisplayPiece pickNextPiece()
    {
        return this.pieces[this.random.nextInt(this.pieces.length)];
    }

    /**
     * tries to add a new random pieces at the top of the board
     */
    public void addNewPiece()
    {
        this.count++;
        // random difficulty
        this.difficulty = (int) (Math.random() * 100);

        System.out.println(this.board.getMaxHeight());

        // move it up at the ceter top
        final Move newMove = new Move();
        newMove.piece = this.nextPiece;
        newMove.x = (this.board.getWidth() - newMove.piece.getWidth()) / 2;
        newMove.y = this.board.getHeight() - newMove.piece.getHeight();

        this.nextPiece = this.pickNextPiece();

        if (this.board.canPlace(newMove))
        {
            this.currentMove = newMove;
        }
        else
        {
            this.gameOn = false;
        }
    }

}
