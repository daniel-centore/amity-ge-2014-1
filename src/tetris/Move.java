/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

/**
 * 
 * @author justinbehymer
 */
public class Move
{

    // move is used as a struct to store a single move

    @Override
    public String toString()
    {
        return "Move [x=" + this.x + ", y=" + this.y + ", piece=" + this.piece + "]";
    }
    public int   x;
    public int   y;
    public Piece piece;

}
