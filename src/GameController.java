import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class GameController {

    private final Game game;
    private final HexGameGUI gui;
    private final Node anchorNode;

    private ContextMenu activeMenu;
    private final VBox p1Stats;
    private final VBox p2Stats;
    private final Button endTurnBtn;

    public GameController(Game game, HexGameGUI gui, VBox p1Stats, VBox p2Stats, Button endTurnBtn, Node anchorNode) {
        this.game = game;
        this.gui = gui;
        this.p1Stats = p1Stats;
        this.p2Stats = p2Stats;
        this.endTurnBtn = endTurnBtn;
        this.anchorNode = anchorNode;

        this.endTurnBtn.setOnAction(e -> endTurn());
    }


    public void endTurn() {
        closeMenuIfOpen();

        if (game.current == game.player1) {
            game.current = game.player2;
        } else {
            game.current = game.player1;
        }

        game.current.balance += 10;

        updateHUD();
    }

    public void handleMouseClick(MouseEvent e, HexCell clickedCell, double screenX, double screenY) {
        closeMenuIfOpen();

        if (clickedCell != null) {
            if (e.getButton() == MouseButton.SECONDARY) {
                if (clickedCell.unit == null) {
                    showSpawnMenu(clickedCell, screenX, screenY);
                }
            } else if (e.getButton() == MouseButton.PRIMARY) {
                System.out.println("Выбрано: " + clickedCell.r + ", " + clickedCell.c);
            }
        }
    }

    private void showSpawnMenu(HexCell cell, double screenX, double screenY) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem tank1 = new MenuItem("Легкий танк (20)");
        tank1.setOnAction(ev -> buyUnit(1, cell));

        MenuItem tank2 = new MenuItem("Средний танк (40)");
        tank2.setOnAction(ev -> buyUnit(2, cell));

        MenuItem tank3 = new MenuItem("Тяжелый танк (70)");
        tank3.setOnAction(ev -> buyUnit(3, cell));

        contextMenu.getItems().addAll(tank1, tank2, tank3);

        activeMenu = contextMenu;
        contextMenu.show(anchorNode, screenX, screenY);
    }

    public void buyUnit(int type, HexCell cell) {
        int cost = switch(type) { case 1 -> 20; case 2 -> 40; case 3 -> 70; default -> 0; };

        if (game.current.balance >= cost) {
            Unit newUnit = UnitFactory.create(type, game.current);
            cell.unit = newUnit;
            game.current.balance -= cost;

            updateHUD();
            gui.drawGame(game);
        } else {
            showWarning("Недостаточно средств",
                    "Не хватает средств для покупки юнита.\n" +
                            "Ваш баланс: " + game.current.balance +
                            "\nЦена: " + cost);
        }
    }


    public void closeMenuIfOpen() {
        if (activeMenu != null) {
            activeMenu.hide();
            activeMenu = null;
        }
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void updateHUD() {
        Label p1MoneyLabel = (Label) p1Stats.getChildren().get(1);
        Label p2MoneyLabel = (Label) p2Stats.getChildren().get(1);

        p1MoneyLabel.setText("Баланс: " + game.player1.balance);
        p2MoneyLabel.setText("Баланс: " + game.player2.balance);

        String activeStyle = "-fx-border-color: red; -fx-border-width: 3; -fx-background-color: white; -fx-border-radius: 5;";
        String inactiveStyle = "-fx-border-color: grey; -fx-border-width: 1; -fx-background-color: #f4f4f4; -fx-border-radius: 5;";

        if (game.current == game.player1) {
            p1Stats.setStyle(activeStyle);
            p2Stats.setStyle(inactiveStyle);
            endTurnBtn.setText("Ход: " + game.player1.name);
        } else {
            p1Stats.setStyle(inactiveStyle);
            p2Stats.setStyle(activeStyle);
            endTurnBtn.setText("Ход: " + game.player2.name);
        }
    }

    public Game getGame() {
        return game;
    }
}