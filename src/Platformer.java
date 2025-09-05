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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
                if (gameTimer != null) {
                    gameTimer.cancel();
                }

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

            // Game Timer mit TimerTask - alle 10ms
            gameTimer = new Timer();
            gameTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateGameStateAndRepaint();
                }
            }, 0, 10); // 0ms Verzögerung, dann alle 10ms

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateGameStateAndRepaint() {
        if (l != null) {
            l.update();
        }
        if (player != null) {
            player.update();
            checkCollision();
        }

        repaint();
    }

    private void checkCollision() {
        if (player != null) {
            player.checkCollision();
        }
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

        @Override
        public void keyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();

            if (keyCode == KeyEvent.VK_ESCAPE) {
                if (gameTimer != null) {
                    gameTimer.cancel();
                }
                dispose();
            }

            // Player-Steuerung - neue Zustände
            if (player != null) {
                // Links
                if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                    player.setWalkingLeft(true);
                }
                // Rechts
                if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                    player.setWalkingRight(true);
                }
                // Springen
                if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_SPACE) {
                    player.setJumping(true);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent event) {
            int keyCode = event.getKeyCode();

            if (player != null) {
                // Links
                if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                    player.setWalkingLeft(false);
                }
                // Rechts
                if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                    player.setWalkingRight(false);
                }
                // Springen
                if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_SPACE) {
                    player.setJumping(false);
                }
            }
        }
    }
}