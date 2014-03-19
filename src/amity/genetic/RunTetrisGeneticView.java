/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package amity.genetic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tetris.AI;
import tetris.PiecePanel;
import tetris.RunTetrisAI;
import tetris.TetrisController;

/**
 * 
 * @author justinbehymer
 */

public class RunTetrisGeneticView extends JComponent
{

    private static final long   serialVersionUID = 1L;

    protected PiecePanel        nextPiecePanel;

    // Controls
    protected JLabel            countLabel;
    protected JLabel            timeLabel;
    protected JButton           startButton;
    protected JButton           stopButton;
    protected javax.swing.Timer timer;
    protected JSlider           speed;
    protected JLabel            rowsClearedLabel;
    protected JSlider           Diffcult;
    protected JLabel            difficulty;

    // milliseconds per tick
    public final int            DELAY            = 0;

    // used to measure elapsed time
    protected long              startTime;

    TetrisController            tc;

    public RunTetrisGeneticView(final int width, final int height, TetrisController tc)
    {
        super();

        this.setPreferredSize(new Dimension(width, height));

        this.tc = tc;//new TetrisController();

        // Create the Timer object and have it send
        this.timer = new javax.swing.Timer(this.DELAY, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                RunTetrisGeneticView.this.countLabel.setText(Integer.toString(RunTetrisGeneticView.this.tc.count) + " Moves");
                RunTetrisGeneticView.this.rowsClearedLabel.setText(RunTetrisGeneticView.this.tc.rowsCleared + " Rows Cleared");
                RunTetrisGeneticView.this.nextPiecePanel.setPiece(RunTetrisGeneticView.this.tc.nextPiece);
                RunTetrisGeneticView.this.repaint();
            }
        });
        
        this.timer.start();
    }


    // width in pixels of a block
    private final float dX()
    {
        return (float) (this.getWidth() - 2) / this.tc.board.getWidth();
    }

    // height in pixels of a block
    private final float dY()
    {
        return (float) (this.getHeight() - 2) / this.tc.board.getHeight();
    }

    // the x pixel coord of the left side of a block
    private final int xPixel(final int x)
    {
        return Math.round(1 + x * this.dX());
    }

    // the y pixel coord of the top of a block
    private final int yPixel(final int y)
    {
        return Math.round(this.getHeight() - 1 - (y + 1) * this.dY());
    }

    /**
     * Draws the current board with a 1 pixel border around the whole thing.
     */
    @Override
    public void paintComponent(final Graphics g)
    {

        // Draw a rect around the whole thing
        g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

        // Draw the line separating the top
        final int spacerY = this.yPixel(this.tc.displayBoard.getHeight() - TetrisController.TOP_SPACE - 1);
        g.setColor(Color.WHITE);
        g.drawLine(0, spacerY, this.getWidth() - 1, spacerY);

        // Factor a few things out to help the optimizer
        final int dx = Math.round(this.dX() - 2);
        final int dy = Math.round(this.dY() - 2);
        final int bWidth = this.tc.displayBoard.getWidth();

        int x, y;
        // Loop through and draw all the blocks
        // left-right, bottom-top
        for (x = 0; x < bWidth; x++)
        {
            final int left = this.xPixel(x); // the left pixel

            // draw from 0 up to the col height
            final int yHeight = this.tc.displayBoard.getColumnHeight(x);
            for (y = 0; y < yHeight; y++)
            {
                if (this.tc.displayBoard.getGrid(x, y))
                {
                    g.setColor(this.tc.displayBoard.colorGrid[x][y]);
                    g.fillRect(left + 1, this.yPixel(y) + 1, dx, dy);

                }
            }
        }
    }

    /**
     * Updates the timer to reflect the current setting of the speed slider.
     */
    public void updateTimer()
    {
        final double value = (double) this.speed.getValue() / this.speed.getMaximum();
        this.timer.setDelay((int) (this.DELAY - value * this.DELAY));
    }

    /**
     * Creates the panel of UI controls.
     */
    public java.awt.Container createControlPanel()
    {
        final java.awt.Container panel = Box.createVerticalBox();

        this.nextPiecePanel = new PiecePanel();
        panel.add(this.nextPiecePanel);

        // COUNT
        this.countLabel = new JLabel("0" + " Moves");
        panel.add(this.countLabel);

        // ROWS Cleared
        this.rowsClearedLabel = new JLabel("0" + " Rows CLeared");
        panel.add(this.rowsClearedLabel);

        this.difficulty = new JLabel();
        panel.add(this.difficulty);

        // TIME
        this.timeLabel = new JLabel(" ");
        panel.add(this.timeLabel);

        panel.add(Box.createVerticalStrut(12));

        final JPanel row = new JPanel();

        panel.add(row);

        return panel;
    }

    /**
     * Creates a Window
     */
    public static void load(TetrisController tc)
    {
        final JFrame frame = new JFrame("TETRIS CSC");
        final JComponent container = (JComponent) frame.getContentPane();
        container.setLayout(new BorderLayout());

        // Set the metal look and feel
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (final Exception ignored)
        {
        }

        final int pixels = 20;
        RunTetrisGeneticView tetris = null;
        tetris = new RunTetrisGeneticView(TetrisController.WIDTH * pixels + 2, (TetrisController.HEIGHT + TetrisController.TOP_SPACE) * pixels + 2, tc);
        
        container.add(tetris, BorderLayout.CENTER);

        final Container panel = tetris.createControlPanel();
        
        panel.add(new JTextArea());

        container.add(panel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);

        // Quit on window close
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e)
            {
                System.exit(0);
            }
        });

    }

}
