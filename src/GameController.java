import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.util.*;

class GameController {
    private final Game game;
    private final HexGameGUI gui;
    private final Node anchorNode;
    private ContextMenu activeMenu;
    private final VBox p1Stats, p2Stats;
    private final Button endTurnBtn;

    private HexCell selectedCell = null;
    private Set<HexCell> reachableCells = new HashSet<>();
    private Set<HexCell> attackableCells = new HashSet<>();

    public GameController(Game game, HexGameGUI gui, VBox p1, VBox p2, Button btn, Node anchor) {
        this.game = game; this.gui = gui; this.p1Stats = p1; this.p2Stats = p2;
        this.endTurnBtn = btn; this.anchorNode = anchor;
        this.endTurnBtn.setOnAction(e -> endTurn());
    }

    public void confirmRestart() {
        closeMenuIfOpen();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Вы хотите начать игру заново?");
        alert.setContentText("Текущий прогресс будет потерян.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            gui.triggerNewGameSequence();
        }
    }

    public void resetGame(Faction f1, Faction f2) {
        game.reset(f1, f2);
        selectedCell = null;
        clearZones();
        updateHUD();
        ((Label) p1Stats.getChildren().get(0)).setText(f1.getDisplayName());
        ((Label) p2Stats.getChildren().get(0)).setText(f2.getDisplayName());
        gui.drawGame(game);
    }

    public void handleMouseClick(MouseEvent e, HexCell clickedCell, double sx, double sy) {
        closeMenuIfOpen();
        if (clickedCell == null) {
            selectedCell = null; clearZones(); gui.drawGame(game);
            return;
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            if (clickedCell.getUnit() != null && clickedCell.getUnit().getOwner() == game.getCurrent()) {
                if (!(clickedCell.getUnit() instanceof Base)) {
                    selectedCell = clickedCell; updateActionZones(clickedCell);
                }
            } else if (clickedCell.getUnit() == null && canSpawnAt(clickedCell)) {
                showSpawnMenu(clickedCell, sx, sy);
            }
        } else if (e.getButton() == MouseButton.PRIMARY && selectedCell != null) {
            if (selectedCell.getUnit() == null) {
                selectedCell = null; clearZones();
            } else {
                processAction(clickedCell);
            }
        }
        gui.drawGame(game);
    }

    private void processAction(HexCell clickedCell) {
        Unit actor = selectedCell.getUnit();
        if (clickedCell.getUnit() != null && clickedCell.getUnit().getOwner() != game.getCurrent()) {
            if (!actor.hasAttacked() && attackableCells.contains(clickedCell)) {
                performAttack(actor, clickedCell);
                updateActionZones(selectedCell);
            }
        } else if (clickedCell.getUnit() == null && !actor.hasMoved() && reachableCells.contains(clickedCell)) {
            clickedCell.setUnit(actor);
            selectedCell.setUnit(null);
            actor.setHasMoved(true);
            selectedCell = clickedCell;
            updateActionZones(selectedCell);
        }
    }

    private void performAttack(Unit attacker, HexCell targetCell) {
        Unit victim = targetCell.getUnit();
        if (victim == null) return;
        victim.setHp(victim.getHp() - attacker.getDamage());
        attacker.setHasAttacked(true);
        if (victim.getHp() <= 0) {
            targetCell.setUnit(null);
            if (victim instanceof Base) gui.showWinDialog(attacker.getOwner());
        }
    }

    private boolean canSpawnAt(HexCell target) {
        for (HexCell[] row : game.getGrid()) {
            for (HexCell cell : row) {
                Unit u = cell.getUnit();
                if (u != null && u.getOwner() == game.getCurrent() && getDistance(target, cell) <= 1) return true;
            }
        }
        return false;
    }

    private int getDistance(HexCell a, HexCell b) {
        int q1 = a.getC(), r1 = a.getR() - (a.getC() - (a.getC() & 1)) / 2;
        int q2 = b.getC(), r2 = b.getR() - (b.getC() - (b.getC() & 1)) / 2;
        return (Math.abs(q1 - q2) + Math.abs(q1 + r1 - q2 - r2) + Math.abs(r1 - r2)) / 2;
    }

    private void updateActionZones(HexCell start) {
        clearZones();
        if (start == null || start.getUnit() == null) return;
        Unit u = start.getUnit();
        for (HexCell[] row : game.getGrid()) {
            for (HexCell cell : row) {
                int dist = getDistance(start, cell);
                if (!u.hasMoved() && cell.getUnit() == null && dist <= u.getSpeed()) reachableCells.add(cell);
                if (!u.hasAttacked() && cell.getUnit() != null && cell.getUnit().getOwner() != game.getCurrent() && dist <= 2) attackableCells.add(cell);
            }
        }
    }

    private void clearZones() { reachableCells.clear(); attackableCells.clear(); }

    public void endTurn() {
        closeMenuIfOpen();
        for (HexCell[] row : game.getGrid()) {
            for (HexCell cell : row) if (cell.getUnit() != null) cell.getUnit().resetActions();
        }
        selectedCell = null; clearZones();
        game.setCurrent(game.getCurrent() == game.getPlayer1() ? game.getPlayer2() : game.getPlayer1());
        game.getCurrent().setBalance(game.getCurrent().getBalance() + 15);
        updateHUD();
        gui.drawGame(game);
    }

    public void updateHUD() {
        ((Label) p1Stats.getChildren().get(1)).setText("Баланс: " + game.getPlayer1().getBalance());
        ((Label) p2Stats.getChildren().get(1)).setText("Баланс: " + game.getPlayer2().getBalance());
        String active = "-fx-border-color: red; -fx-border-width: 3; -fx-background-color: white; -fx-border-radius: 5;";
        String inactive = "-fx-border-color: grey; -fx-border-width: 1; -fx-background-color: #f4f4f4; -fx-border-radius: 5;";
        p1Stats.setStyle(game.getCurrent() == game.getPlayer1() ? active : inactive);
        p2Stats.setStyle(game.getCurrent() == game.getPlayer2() ? active : inactive);
        endTurnBtn.setText("Ход: " + game.getCurrent().getFaction().getDisplayName());
    }

    public void showSpawnMenu(HexCell cell, double sx, double sy) {
        ContextMenu menu = new ContextMenu();
        int[] costs = {20, 40, 70}; String[] names = {"Легкий", "Средний", "Тяжелый"};
        for (int i = 0; i < 3; i++) {
            int type = i + 1; int cost = costs[i];
            MenuItem item = new MenuItem(names[i] + " (" + cost + ")");
            item.setOnAction(e -> {
                if (game.getCurrent().getBalance() >= cost) {
                    cell.setUnit(UnitFactory.create(type, game.getCurrent()));
                    game.getCurrent().setBalance(game.getCurrent().getBalance() - cost);
                    updateHUD(); gui.drawGame(game);
                }
            });
            menu.getItems().add(item);
        }
        activeMenu = menu; menu.show(anchorNode, sx, sy);
    }

    public void closeMenuIfOpen() { if (activeMenu != null) activeMenu.hide(); }
    public Game getGame() { return game; }
    public Set<HexCell> getReachableCells() { return reachableCells; }
    public Set<HexCell> getAttackableCells() { return attackableCells; }
}