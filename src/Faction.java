import javafx.scene.paint.Color;

enum Faction {
    USSR("СССР", Color.web("#90EE90")),
    GERMANY("Германия", Color.web("#444444")),
    USA("США", Color.WHEAT),
    UK("Великобритания", Color.DARKGREEN);

    private final String displayName;
    private final Color color;

    Faction(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public Color getColor() { return color; }
    @Override
    public String toString() { return displayName; }
}