/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

package tetris;

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
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * @author justinbehymer
 */

public class RunTetris extends JComponent {

    private static final long   serialVersionUID = 1L;

    protected PiecePanel        nextPiecePanel;        // Displays the
                                                        // nextPiece for the
                                                        // player to see

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
    public final int            DELAY            = 400;

    // used to measure elapsed time
    protected long              startTime;

    TetrisController            tc;

	public RunTetris(int width, int height)
	{
		super();

        this.setPreferredSize(new Dimension(width, height));

        this.tc = new TetrisController();

        // Create the Timer object and have it send
        this.timer = new javax.swing.Timer(this.DELAY, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.DOWN);
            }
        });
    }

    void tick(final int verb) {
        this.tc.tick(verb);

        if (!this.tc.gameOn) {
            this.stopGame();
        }

        this.countLabel.setText(Integer.toString(this.tc.count) + " Moves");
        this.rowsClearedLabel.setText(this.tc.rowsCleared + " Rows Cleared");
        // difficulty.setText(Integer.toString(tc.difficulty));
        this.nextPiecePanel.setPiece(this.tc.nextPiece);

        this.repaint();
    }

    /**
     * Sets the internal state and starts the timer so the game is happening.
     */
    public void startGame() {
        this.tc.startGame();

        // draw the new board state once
        this.repaint();

        this.enableButtons();
        this.timeLabel.setText(" ");

        this.timer.start();
        this.startTime = System.currentTimeMillis();

        // LEFT
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.LEFT);
            }
        }, "left", KeyStroke.getKeyStroke('4'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.LEFT);
            }
        }, "left", KeyStroke.getKeyStroke('a'), WHEN_IN_FOCUSED_WINDOW);
        // RIGHT
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.RIGHT);
            }
        }, "right", KeyStroke.getKeyStroke('6'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.RIGHT);
            }
        }, "right", KeyStroke.getKeyStroke('d'), WHEN_IN_FOCUSED_WINDOW);
        // ROTATE
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.ROTATE);
            }
        }, "rotate", KeyStroke.getKeyStroke('5'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.ROTATE);
            }
        }, "rotate", KeyStroke.getKeyStroke('w'), WHEN_IN_FOCUSED_WINDOW);
        // DROP
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.DROP);
            }
        }, "drop", KeyStroke.getKeyStroke('0'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.tick(TetrisController.DROP);
            }
        }, "drop", KeyStroke.getKeyStroke('s'), WHEN_IN_FOCUSED_WINDOW);

    }

    /**
     * Sets the enabling of the start/stop buttons based on the gameOn state.
     */
    private void enableButtons() {
        this.startButton.setEnabled(!this.tc.gameOn);
        this.stopButton.setEnabled(this.tc.gameOn);
    }

    /**
     * Stops the game.
     */
    public void stopGame() {
        this.tc.gameOn = false;
        this.enableButtons();
        this.timer.stop();

        final long delta = (System.currentTimeMillis() - this.startTime) / 10;
        this.timeLabel.setText(Double.toString(delta / 100.0) + " seconds");
        // LEFT
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "left", KeyStroke.getKeyStroke('4'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "left", KeyStroke.getKeyStroke('a'), WHEN_IN_FOCUSED_WINDOW);
        // RIGHT
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "right", KeyStroke.getKeyStroke('6'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "right", KeyStroke.getKeyStroke('d'), WHEN_IN_FOCUSED_WINDOW);
        // ROTATE
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "rotate", KeyStroke.getKeyStroke('5'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "rotate", KeyStroke.getKeyStroke('w'), WHEN_IN_FOCUSED_WINDOW);
        // DROP
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "drop", KeyStroke.getKeyStroke('0'), WHEN_IN_FOCUSED_WINDOW);
        this.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            }
        }, "drop", KeyStroke.getKeyStroke('s'), WHEN_IN_FOCUSED_WINDOW);
    }

    // width in pixels of a block
    private final float dX() {
        return (float) (this.getWidth() - 2) / this.tc.board.getWidth();
    }

    // height in pixels of a block
    private final float dY() {
        return (float) (this.getHeight() - 2) / this.tc.board.getHeight();
    }

    // the x pixel coord of the left side of a block
    private final int xPixel(final int x) {
        return Math.round(1 + x * this.dX());
    }

    // the y pixel coord of the top of a block
    private final int yPixel(final int y) {
        return Math.round(this.getHeight() - 1 - (y + 1) * this.dY());
    }

    /**
     * Draws the current board with a 1 pixel border around the whole thing.
     */
    @Override
    public void paintComponent(final Graphics g) {

        // Draw a rect around the whole thing
        g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

        // Draw the line separating the top
        final int spacerY = this.yPixel(this.tc.displayBoard.getHeight()
                - TetrisController.TOP_SPACE - 1);
        g.setColor(Color.WHITE);
        g.drawLine(0, spacerY, this.getWidth() - 1, spacerY);

        // Factor a few things out to help the optimizer
        final int dx = Math.round(this.dX() - 2);
        final int dy = Math.round(this.dY() - 2);
        final int bWidth = this.tc.displayBoard.getWidth();

        int x, y;
        // Loop through and draw all the blocks
        // left-right, bottom-top
        for (x = 0; x < bWidth; x++) {
            final int left = this.xPixel(x); // the left pixel

            // draw from 0 up to the col height
            final int yHeight = this.tc.displayBoard.getColumnHeight(x);
            for (y = 0; y < yHeight; y++) {
                if (this.tc.displayBoard.getGrid(x, y)) {
                    g.setColor(this.tc.displayBoard.colorGrid[x][y]);
                    g.fillRect(left + 1, this.yPixel(y) + 1, dx, dy);

                }
            }
        }
    }

    /**
     * Updates the timer to reflect the current setting of the speed slider.
     */
    public void updateTimer() {
        final double value = (double) this.speed.getValue()
                / this.speed.getMaximum();
        this.timer.setDelay((int) (this.DELAY - value * this.DELAY));
    }

    /**
     * Creates the panel of UI controls.
     */
    public java.awt.Container createControlPanel() {
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

        // START button
        this.startButton = new JButton("Start");
        panel.add(this.startButton);
        this.startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.startGame();
            }
        });

        // STOP button
        this.stopButton = new JButton("Stop");
        panel.add(this.stopButton);
        this.stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                RunTetris.this.stopGame();
            }
        });

        this.enableButtons();

        final JPanel row = new JPanel();

        // SPEED slider
        panel.add(Box.createVerticalStrut(12));
        row.add(new JLabel("Speed:"));
        this.speed = new JSlider(0, this.DELAY, this.DELAY); // min, max, current
        this.speed.setPreferredSize(new Dimension(100, 15));

        this.updateTimer();
        row.add(this.speed);

        panel.add(row);
        this.speed.addChangeListener(new ChangeListener() {
            // when the slider changes, sync the timer to its value
            @Override
            public void stateChanged(final ChangeEvent e) {
                RunTetris.this.updateTimer();
            }

        });

        return panel;
    }

    /**
     * Creates a Window
     */
    public static void load(final AI brain)
    {
        final JFrame frame = new JFrame("TETRIS CSC");
        final JComponent container = (JComponent) frame.getContentPane();
        container.setLayout(new BorderLayout());

        // Set the metal look and feel
        try {
            UIManager.setLookAndFeel(UIManager
                    .getCrossPlatformLookAndFeelClassName());
        }
        catch (final Exception ignored) {
        }

		final int pixels = 20;
		RunTetris tetris = null;
		if (brain == null)
		{
			tetris = new RunTetris(TetrisController.WIDTH * pixels + 2, (TetrisController.HEIGHT + TetrisController.TOP_SPACE) * pixels + 2);
		}
		else
		{
			tetris = new RunTetrisAI(TetrisController.WIDTH * pixels + 2, (TetrisController.HEIGHT + TetrisController.TOP_SPACE) * pixels + 2, brain);
		}

        container.add(tetris, BorderLayout.CENTER);

        final Container panel = tetris.createControlPanel();

        // Add the quit button last so it's at the bottom
        panel.add(Box.createVerticalStrut(12));
        final JButton quit = new JButton("Quit");
        panel.add(quit);
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.exit(0);
            }
        });

        container.add(panel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);

        // Quit on window close
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });

    }

}
