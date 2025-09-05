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

    // BoundingBox für Kollisionserkennung
    private BoundingBox boundingBox;

    // Animation
    private List<BufferedImage> walkFrames;
    private BufferedImage currentImage;
    private int animationFrame = 0;
    private int animationCounter = 0;
    private final int animationSpeed = 6; // Frames zwischen Updates

    // Physik
    private float velocityX = 0f;
    private float velocityY = 0f;
    private final float gravity = 0.5f;
    private final float airResistance = 0.98f;
    private final float walkSpeed = 3f;
    private final float jumpPower = 12f;

    // Neue Zustände
    private boolean jumping = false;
    private boolean walkingLeft = false;
    private boolean walkingRight = false;
    private boolean facingRight = true;
    private boolean onGround = false;

    // Level-Referenz
    private Level level;

    // Spielfigur-Größe
    private final int width = 32;
    private final int height = 42;

    public Player(float startX, float startY, Level level) {
        this.pos = new Point.Float(startX, startY);
        this.level = level;

        // BoundingBox initialisieren
        this.boundingBox = new BoundingBox(startX, startY, startX + width, startY + height);

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
        // Bewegung entsprechend der Zustände
        if (walkingLeft) {
            velocityX = -walkSpeed;
            facingRight = false;
        } else if (walkingRight) {
            velocityX = walkSpeed;
            facingRight = true;
        } else {
            velocityX = 0;
        }

        // Sprung nur wenn am Boden
        if (jumping && onGround) {
            velocityY = -jumpPower;
            onGround = false;
        }

        // Schwerkraft anwenden
        velocityY += gravity;

        // Luftreibung anwenden
        velocityX *= airResistance;
        velocityY *= airResistance;

        // Position aktualisieren
        pos.x += velocityX;
        pos.y += velocityY;

        // Sicherstellen, dass Spielfigur das Level nicht verlässt
        BufferedImage levelImg = (BufferedImage) level.getResultingImage();
        if (pos.x < 0) {
            pos.x = 0;
            velocityX = 0;
        }
        if (pos.x > levelImg.getWidth() - width) {
            pos.x = levelImg.getWidth() - width;
            velocityX = 0;
        }
        if (pos.y < 0) {
            pos.y = 0;
            velocityY = 0;
        }
        if (pos.y > levelImg.getHeight() - height) {
            pos.y = levelImg.getHeight() - height;
            velocityY = 0;
            onGround = true;
        }

        // BoundingBox aktualisieren
        updateBoundingBox();

        // Animation aktualisieren
        updateAnimation();

        // Kamera folgt dem Spieler
        updateCamera();
    }

    private void updateBoundingBox() {
        boundingBox.updatePosition(pos.x, pos.y, width, height);
    }

    private void updateAnimation() {
        // Animation nur wenn sich bewegt
        if (walkingLeft || walkingRight) {
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

    public void checkCollision() {
        ArrayList<Tile> tiles = level.getTiles();
        onGround = false; // Reset onGround status

        for (Tile tile : tiles) {
            if (this.boundingBox.intersect(tile.getBoundingBox())) {
                // Kollision erkannt - bestimme die Richtung und reagiere
                Vec2 overlap = this.boundingBox.overlapSize(tile.getBoundingBox());
                BoundingBox tileBB = tile.getBoundingBox();

                // Bestimme Kollisionsrichtung basierend auf kleinster Überlappung
                if (overlap.x < overlap.y) {
                    // Horizontale Kollision
                    if (pos.x < tileBB.minX) {
                        // Player links von Tile - schiebe nach links
                        pos.x = tileBB.minX - width;
                        velocityX = 0;
                    } else {
                        // Player rechts von Tile - schiebe nach rechts
                        pos.x = tileBB.maxX;
                        velocityX = 0;
                    }
                } else {
                    // Vertikale Kollision
                    if (pos.y < tileBB.minY) {
                        // Player über Tile - schiebe nach oben
                        pos.y = tileBB.minY - height;
                        velocityY = 0;
                        onGround = true;
                    } else {
                        // Player unter Tile - schiebe nach unten
                        pos.y = tileBB.maxY;
                        velocityY = 0;
                    }
                }

                // BoundingBox nach Positionskorrektur aktualisieren
                updateBoundingBox();
            }
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

    // BoundingBox getter
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    // Zustand-Steuerung
    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void setWalkingLeft(boolean walkingLeft) {
        this.walkingLeft = walkingLeft;
    }

    public void setWalkingRight(boolean walkingRight) {
        this.walkingRight = walkingRight;
    }

    // Getter
    public Point.Float getPos() { return pos; }
}