/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

/**
 * 
 * @author justinbehymer
 */
public class PiecePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private DisplayPiece      piece;
    public int                blockSize        = 10;

    public void setPiece(final DisplayPiece piece) {
        this.piece = piece;
        this.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.blockSize * 6, this.blockSize * 5);
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (this.piece != null) {
            g.setColor(this.piece.color);
            for (final Point block : this.piece.body) {
                g.fillRect(block.x * (this.blockSize + 2) + this.blockSize,
                        this.blockSize * 3 - block.y * (this.blockSize + 2)
                                + this.blockSize, this.blockSize,
                        this.blockSize);
            }
        }
    }
}
