import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Player {
    // Position in X- und Y-Richtung
    public Point.Float pos;

    // Animation
    private List<BufferedImage> walkFrames;
    private BufferedImage currentImage;
    private int animationFrame = 0;
    private int animationCounter = 0;
    private final int animationSpeed = 6; // Frames zwischen Updates

    // Bewegungsgeschwindigkeit
    private final float moveSpeed = 4f;

    // Zustand
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean facingRight = true;

    // Level-Referenz für Kamera
    private Level level;

    // Spielfigur-Größe
    private final int width = 32;
    private final int height = 42;

    public Player(float startX, float startY, Level level) {
        this.pos = new Point.Float(startX, startY);
        this.level = level;

        loadWalkAnimation();
        if (!walkFrames.isEmpty()) {
            currentImage = walkFrames.get(0);
        }
    }

    private void loadWalkAnimation() {
        walkFrames = new ArrayList<>();

        // Lade alle p2_walk*.png Dateien
        for (int i = 1; i <= 11; i++) {
            try {
                String filename = String.format("p2_walk%02d.png", i);

                // Versuche verschiedene Pfade
                String[] paths = {
                        "./assets/Player/p2_walk/PNG/",
                        "/Users/philipp/Downloads/Step2/assets/Player/p2_walk/PNG/",
                        "./",
                        "./assets/",
                        "./Player/"
                };

                BufferedImage frame = null;

                for (String path : paths) {
                    try {
                        File file = new File(path + filename);
                        if (file.exists()) {
                            frame = ImageIO.read(file);
                            System.out.println("Loaded: " + path + filename);
                            break;
                        }
                    } catch (IOException e) {
                        // Nächsten Pfad versuchen
                    }
                }

                if (frame != null) {
                    walkFrames.add(frame);
                } else {
                    System.out.println("Could not find: " + filename);
                }

            } catch (Exception e) {
                System.err.println("Error loading walk frame " + i + ": " + e.getMessage());
            }
        }

        // Falls keine Bilder gefunden wurden, erstelle Fallback
        if (walkFrames.isEmpty()) {
            System.out.println("No walk images found, creating fallback graphics");
            createFallbackFrames();
        } else {
            System.out.println("Loaded " + walkFrames.size() + " walk animation frames");
        }
    }

    private void createFallbackFrames() {
        // Erstelle 8 einfache animierte Frames als Fallback
        for (int i = 0; i < 8; i++) {
            BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = frame.createGraphics();

            // Körper
            g.setColor(Color.RED);
            g.fillRect(6, 15, 20, 25);

            // Kopf
            g.setColor(new Color(255, 200, 180));
            g.fillOval(8, 2, 16, 16);

            // Beine - Animation durch Verschiebung
            int legOffset = (i % 4) - 2;
            g.setColor(Color.BLUE);
            g.fillRect(8 + legOffset, 35, 6, 7);
            g.fillRect(18 - legOffset, 35, 6, 7);

            // Augen
            g.setColor(Color.BLACK);
            g.fillOval(11, 6, 2, 2);
            g.fillOval(19, 6, 2, 2);

            g.dispose();
            walkFrames.add(frame);
        }
    }

    public void update() {
        // Bewegung in alle Richtungen
        if (movingLeft) {
            pos.x -= moveSpeed;
            facingRight = false;
        }
        if (movingRight) {
            pos.x += moveSpeed;
            facingRight = true;
        }
        if (movingUp) {
            pos.y -= moveSpeed;
        }
        if (movingDown) {
            pos.y += moveSpeed;
        }

        // Grenzen des Levels prüfen
        BufferedImage levelImg = (BufferedImage) level.getResultingImage();
        if (pos.x < 0) pos.x = 0;
        if (pos.x > levelImg.getWidth() - width) pos.x = levelImg.getWidth() - width;
        if (pos.y < 0) pos.y = 0;
        if (pos.y > levelImg.getHeight() - height) pos.y = levelImg.getHeight() - height;

        // Animation aktualisieren
        updateAnimation();

        // Kamera folgt dem Spieler
        updateCamera();
    }

    private void updateAnimation() {
        // Animation nur wenn sich bewegt
        if (movingLeft || movingRight || movingUp || movingDown) {
            // Laufanimation
            animationCounter++;
            if (animationCounter >= animationSpeed) {
                animationFrame = (animationFrame + 1) % walkFrames.size();
                animationCounter = 0;
            }
        } else {
            // Stillstehen - erstes Frame
            animationFrame = 0;
            animationCounter = 0;
        }

        currentImage = walkFrames.get(animationFrame);
    }

    private void updateCamera() {
        // Kamera folgt dem Spieler - Spieler bleibt in der Mitte des Bildschirms
        float screenWidth = 1000f; // Fensterbreite aus Platformer
        float targetCameraX = pos.x - (screenWidth / 2f);

        // Sanfte Kamera-Bewegung
        float cameraSpeed = 0.15f;
        level.offsetX += (targetCameraX - level.offsetX) * cameraSpeed;

        // Grenzen der Kamera beachten
        if (level.offsetX < 0) {
            level.offsetX = 0;
        }

        BufferedImage levelImg = (BufferedImage) level.getResultingImage();
        if (level.offsetX > levelImg.getWidth() - screenWidth) {
            level.offsetX = levelImg.getWidth() - screenWidth;
        }
    }

    public void move() {
        // Diese Methode kann leer bleiben - alles passiert in update()
    }

    public BufferedImage getImage() {
        if (facingRight || currentImage == null) {
            return currentImage;
        } else {
            // Horizontal spiegeln für Links-Bewegung
            return flipHorizontally(currentImage);
        }
    }

    private BufferedImage flipHorizontally(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, original.getType());
        java.awt.Graphics2D g = flipped.createGraphics();
        g.drawImage(original, w, 0, 0, h, 0, 0, w, h, null);
        g.dispose();
        return original;
    }

    // Input-Methoden
    public void setMovingLeft(boolean moving) {
        this.movingLeft = moving;
    }

    public void setMovingRight(boolean moving) {
        this.movingRight = moving;
    }

    public void setMovingUp(boolean moving) {
        this.movingUp = moving;
    }

    public void setMovingDown(boolean moving) {
        this.movingDown = moving;
    }

    // Getter
    public Point.Float getPos() { return pos; }
}