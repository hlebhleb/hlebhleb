public class Game {
    public HexCell[][] grid;

    public Player player1 = new Player("СССР");
    public Player player2 = new Player("Германия");
    public Player current = player1;

    private final int ROWS = 10;
    private final int COLS = 11;

    public Game() {
        grid = new HexCell[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = new HexCell(r, c);
    }
}