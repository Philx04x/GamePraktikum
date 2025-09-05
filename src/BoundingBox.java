public class BoundingBox {
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;

    public BoundingBox(float minX, float minY, float maxX, float maxY) {
        this.minX = minX - 10;
        this.minY = minY - 50;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // Prüft ob sich Tiles überschneiden
    public boolean intersect(BoundingBox b) {
        return (minX <= b.maxX) && (maxX >= b.minX) && (minY <= b.maxY) && (maxY >= b.minY);
    }

    // Berechnet wie stark sich zwei Boxen überlappen
    public Vec2 overlapSize(BoundingBox b) {
        Vec2 result = new Vec2(0, 0);

        // x Richtung
        if (minX < b.minX) {
            result.x = maxX - b.minX;
        } else {
            result.x = b.maxX - minX;
        }

        // y Richtung
        if (minY < b.minY) {
            result.y = maxY - b.minY;
        } else {
            result.y = b.maxY - minY;
        }

        return result;
    }

    public void updatePosition(float x, float y, float width, float height) {
        this.minX = x;
        this.minY = y;
        this.maxX = x + width;
        this.maxY = y + height;
    }
}