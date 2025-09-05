public class Tile {
    private BoundingBox boundingBox;
    private int tileIndex;

    public Tile(float x, float y, int tileSize, int tileIndex) {
        this.tileIndex = tileIndex;
        this.boundingBox = new BoundingBox(x, y, x + tileSize, y + tileSize);
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
    }
}