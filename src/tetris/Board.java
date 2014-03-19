package tetris;

// Board.java
// package Hw2;
import java.awt.Point;

/**
 * Represents a Tetris board -- essentially a 2-d grid of booleans. Supports
 * tetris pieces and row clearning. Has an "undo" feature that allows clients to
 * add and remove pieces efficiently. Does not do any drawing or have any idea
 * of pixels. Intead, just represents the abtsract 2-d board. See
 * Tetris-Architecture.html for an overview.
 * 
 * This is the starter file version -- a few simple things are filled in already
 * 
 * @author Nick Parlante
 * @version 1.0, Mar 1, 2001
 */
public class Board {
    protected int         width;          // width of the board
    protected int         height;
    private boolean       caching = false;
    protected boolean[][] grid;

    /**
     * Creates an empty board of the given width and height measured in blocks.
     */
    public Board(final int aWidth, final int aHeight) {
        this.width = aWidth;
        this.height = aHeight;

        this.grid = new boolean[this.width][this.height];

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.grid[x][y] = false;
            }
        }
    }

    public Board(final Board o) {
        this(o.width, o.height);

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.grid[x][y] = o.grid[x][y];
            }
        }
    }

    @Override
    public Board clone() {
        final Board cloned = new Board(this.width, this.height);

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                cloned.grid[x][y] = this.grid[x][y];
            }
        }

        return cloned;
    }

    public void makeDirty() {
        this.maxHeightDirty = true;
        for (int i = 0; i < 10; i++) {
            this.columnHeightDirties[i] = true;
        }
    }

    // CACHING CAN ONLY BE ENABLED WHILE A BOARDRATER IS READING THE BOARD. I
    // couldn't figure out where to mark the caches as outdated, so I just make
    // sure caching is only enabled in read-only situations.
    public void enableCaching() {
        this.caching = true;
        this.makeDirty();
    }

    public void disableCaching() {
        this.caching = false;
    }

    /**
     * Returns the width of the board in blocks.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height of the board in blocks.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the max column height present in the board. For an empty board
     * this is 0.
     */
    private boolean maxHeightDirty = true;
    private int     _maxHeight     = -1;

    public int getMaxHeight() {
        if (this.caching && !this.maxHeightDirty) {
            return this._maxHeight;
        }
        this.maxHeightDirty = false;
        int max = 0;
        for (int i = 0; i < this.width; i++) {
            if (this.getColumnHeight(i) > max) {
                max = this.getColumnHeight(i);
            }
        }
        return this._maxHeight = max;

    }

    /**
     * Given a piece and an x, returns the y value where the piece would come to
     * rest if it were dropped straight down at that x.
     * 
     * <p>
     * Implementation: use the skirt and the col heights to compute this fast --
     * O(skirt length).
     */
    public int dropHeight(final Piece piece, final int x) {
        int high = 10000;
        int result = 0;
        int temp;
        int i;
        final int[] skirt = piece.getSkirt();
        for (i = x; i < x + piece.getWidth(); i++) {
            temp = skirt[i - x] - this.getColumnHeight(i);
            if (temp < high) {
                high = temp;
                result = i;
            }
        }
        return this.getColumnHeight(result) - skirt[result - x];
    }

    /**
     * Returns the height of the given column -- i.e. the y value of the highest
     * block + 1. The height is 0 if the column contains no blocks.
     */
    private final boolean[] columnHeightDirties = { true, true, true, true,
            true, true, true, true, true, true };
    private final int[]     _columnHeights      = { -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1                         };

    public int getColumnHeight(final int x) {
        if (this.caching && !this.columnHeightDirties[x]) {
            return this._columnHeights[x];
        }
        this.columnHeightDirties[x] = false;
        for (int j = this.height - 1; j >= 0; j--) {
            if (this.grid[x][j]) {
                return this._columnHeights[x] = j + 1;
            }
        }
        return this._columnHeights[x] = 0;
    }

    /**
     * Returns the number of filled blocks in the given row.
     */
    public int getRowCount(final int y) {
        int count = 0;

        for (int x = 0; x < this.width; x++) {
            if (this.grid[x][y]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns true if the given block is filled in the board. Blocks outside of
     * the valid width/height area always return true.
     * 
     */
    public final boolean getGrid(final int x, final int y) {
        if (x < 0 || x > this.width) {
            return true;
        }
        if (y < 0 || y > this.height) {
            return true;
        }
        return this.grid[x][y];
    }

    public boolean canPlace(final Piece piece, final int x, final int y) {
        // might as well do all the error checking at once, since we must leave
        // the board unchanged if we've got
        // an out of bounds error - this has the added bonus of not altering the
        // board on a bad place
        for (final Point block : piece.getBody()) {
            final int putx = x + block.x;
            final int puty = y + block.y;

            if (putx < 0 || putx >= this.width || puty < 0
                    || puty < this.height && this.grid[putx][puty]) {
                return false;
            }
        }
        return true;
    }

    public boolean canPlace(final Move move) {
        return this.canPlace(move.piece, move.x, move.y);
    }

    public void place(final Piece piece, final int x, final int y) {
        if (!this.canPlace(piece, x, y)) {
            return;
        }
        for (final Point block : piece.getBody()) {
            this.grid[x + block.x][y + block.y] = true;
        }
    }

    public void place(final Move move) {
        this.place(move.piece, move.x, move.y);
    }

    /**
     * Deletes rows that are filled all the way across, moving things above
     * down. Returns true if any row clearing happened.
     * 
     * <p>
     * Implementation: This is complicated. Ideally, you want to copy each row
     * down to its correct location in one pass. Note that more than one row may
     * be filled.
     */
    public int clearRows() {
        int cleared = 0;

        for (int i = 0; i < this.height; i++) {
            if (this.getRowCount(i) >= this.width) {
                cleared++;

                for (int j = 0; j < this.width; j++) {
                    System.arraycopy(this.grid[j], i + 1, this.grid[j], i,
                            this.height - 1 - i);
                    this.grid[j][this.height - 1] = false;
                }

                i--;
            }
        }
        return cleared;

    }
}
