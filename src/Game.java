class Game {
    private final HexCell[][] grid;
    private final Player player1, player2;
    private Player current;
    public Game(Faction f1, Faction f2) {
        player1 = new Player("Игрок 1", f1); player2 = new Player("Игрок 2", f2);
        grid = new HexCell[10][11]; reset(f1, f2);
    }
    public void reset(Faction f1, Faction f2) {
        player1.setFaction(f1); player1.setBalance(130);
        player2.setFaction(f2); player2.setBalance(130);
        current = player1;
        for (int r = 0; r < 10; r++) for (int c = 0; c < 11; c++) grid[r][c] = new HexCell(r, c);
        grid[0][0].setUnit(new Base(player1));
        grid[9][10].setUnit(new Base(player2));
    }
    public HexCell[][] getGrid() { return grid; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Player getCurrent() { return current; }
    public void setCurrent(Player p) { current = p; }
}