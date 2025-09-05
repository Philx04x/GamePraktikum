import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serial;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Platformer extends JFrame {
    @Serial
    private static final long serialVersionUID = 5736902251450559962L;

    private Level l = null;
    private Player player = null;

    private Timer gameTimer;
    private BufferStrategy bufferStrategy;

    public Platformer() {
        //exit program when window is closed
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("./"));
        fc.setDialogTitle("Select input image");
        FileFilter filter = new FileNameExtensionFilter("Level image (.bmp)", "bmp");
        fc.setFileFilter(filter);
        int result = fc.showOpenDialog(this);
        File selectedFile = new File("");
        addKeyListener(new AL(this));

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fc.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        } else {
            dispose();
            System.exit(0);
        }

        try {
            l = new Level(selectedFile.getAbsolutePath());
            player = new Player(100, 100, l);

            this.setBounds(0, 0, 1000, 5 * 70);
            this.setVisible(true);

            createBufferStrategy(2);
            bufferStrategy = this.getBufferStrategy();

            // Game Timer für Animation
            gameTimer = new Timer(16, e -> updateGameStateAndRepaint());
            gameTimer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateGameStateAndRepaint() {
        l.update();
        if (player != null) {
            player.update();
            player.move();
        }

        repaint();
    }

    public void paint(Graphics g) {
        if (bufferStrategy != null) {
            Graphics2D g2 = null;
            try {
                g2 = (Graphics2D) bufferStrategy.getDrawGraphics();
                draw(g2);
            } finally {
                if (g2 != null) {
                    g2.dispose();
                }
            }
            bufferStrategy.show();
        } else {
            Graphics2D g2 = (Graphics2D) g;
            draw(g2);
        }
    }

    private void draw(Graphics2D g2d) {
        BufferedImage level = (BufferedImage) l.getResultingImage();
        if (l.offsetX > level.getWidth() - 1000)
            l.offsetX = level.getWidth() - 1000;
        BufferedImage bi = level.getSubimage((int) l.offsetX, 0, 1000, level.getHeight());
        g2d.drawImage(bi, 0, 0, this);

        // Player zeichnen
        if (player != null) {
            g2d.drawImage(player.getImage(),
                    (int) (player.pos.x - l.offsetX),
                    (int) player.pos.y,
                    this);
        }
    }

    public class AL extends KeyAdapter {
        Platformer p;

        public AL(Platformer p) {
            super();
            this.p = p;
        }

        // Komplette Key Handler Klasse für freie Bewegung in alle Richtungen:

        @Override
        public void keyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();

            if (keyCode == KeyEvent.VK_ESCAPE) {
                dispose();
            }

            // Player-Steuerung - WASD oder Pfeiltasten
            if (player != null) {
                // Links
                if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                    player.setMovingLeft(true);
                }
                // Rechts
                if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                    player.setMovingRight(true);
                }
                // Hoch
                if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                    player.setMovingUp(true);
                }
                // Runter
                if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                    player.setMovingDown(true);
                }
            }

            updateGameStateAndRepaint();
        }

        @Override
        public void keyReleased(KeyEvent event) {
            int keyCode = event.getKeyCode();

            if (player != null) {
                // Links
                if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                    player.setMovingLeft(false);
                }
                // Rechts
                if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                    player.setMovingRight(false);
                }
                // Hoch
                if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                    player.setMovingUp(false);
                }
                // Runter
                if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                    player.setMovingDown(false);
                }
            }
        }
    }
}