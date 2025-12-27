class HexCell {
    private final int r, c;
    private Unit unit;

    public HexCell(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
    public int getR() { return r; }
    public int getC() { return c; }
}