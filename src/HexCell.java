class HexCell {
    private final int row, col;
    private Unit unit;

    public HexCell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public int getR() { return row; }
    public int getC() { return col; }
}