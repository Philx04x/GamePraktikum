import java.awt.image.BufferedImage;

public class Tile {
    private BoundingBox boundingBox;
    private int tileIndex; // Index des Bildes in der tileImages Liste
    private int x, y; // Position in der Kachelgrid
    private int tileSize;

    public Tile(int x, int y, int tileIndex, int tileSize) {
        this.x = x;
        this.y = y;
        this.tileIndex = tileIndex;
        this.tileSize = tileSize;

        // BoundingBox basierend auf Position und Größe erstellen
        float pixelX = x * tileSize;
        float pixelY = y * tileSize;
        this.boundingBox = new BoundingBox(pixelX, pixelY,
                pixelX + tileSize, pixelY + tileSize);
    }

    // Getter für die BoundingBox
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    // Getter für den Tile-Index (für die Darstellung)
    public int getTileIndex() {
        return tileIndex;
    }

    // Getter für Position
    public int getX() { return x; }
    public int getY() { return y; }

    // Prüft ob diese Kachel eine Kollision hat (z.B. nur solide Kacheln)
    public boolean isSolid() {
        // Index 0 = grassMid (solide), Index 1 = liquidWaterTop_mid (nicht solide)
        return tileIndex == 0;
    }

    // Hilfsmethode für Debug-Ausgabe
    @Override
    public String toString() {
        return "Tile[x=" + x + ", y=" + y + ", index=" + tileIndex +
                ", bbox=(" + boundingBox.minX + "," + boundingBox.minY +
                "," + boundingBox.maxX + "," + boundingBox.maxY + ")]";
    }
}