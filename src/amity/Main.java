package amity;

import tetris.RunTetris;

/**
 * 
 * @author drdanielfc
 * 
 */
public class Main {
    public static void main(final String args[]) {
        RunTetris.load(new AIRunner());
    }

}
