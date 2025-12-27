import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class HexGameGUI extends Application {
    private final int ROWS = 10;
    private final int COLS = 11;
    private final double HEX_SIZE = 30;

    private Canvas canvas;
    private GraphicsContext gc;
    private GameController controller;
    private VBox p1Stats, p2Stats;
    private Button endTurnBtn;
    private Button restartBtn;

    @Override
    public void start(Stage stage) {
        Faction[] choice = showSelectionDialog();
        Game game = new Game(choice[0], choice[1]);

        double gridWidth = (COLS * 1.5 + 0.5) * HEX_SIZE;
        double gridHeight = ROWS * Math.sqrt(3) * HEX_SIZE + (Math.sqrt(3) * HEX_SIZE / 2);

        canvas = new Canvas(gridWidth + 60, gridHeight + 60);
        gc = canvas.getGraphicsContext2D();

        BorderPane root = new BorderPane();
        root.setCenter(new StackPane(canvas));

        HBox hud = new HBox(15);
        hud.setAlignment(Pos.CENTER);
        hud.setStyle("-fx-background-color: #ddd; -fx-padding: 10;");

        p1Stats = createPlayerPanel(game.getPlayer1().getFaction().getDisplayName());
        p2Stats = createPlayerPanel(game.getPlayer2().getFaction().getDisplayName());

        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);

        endTurnBtn = new Button("Завершить ход");
        endTurnBtn.setPrefSize(150, 40);

        restartBtn = new Button("Рестарт");
        restartBtn.setPrefSize(100, 40);
        restartBtn.setStyle("-fx-base: #ff9999;");

        hud.getChildren().addAll(p1Stats, s1, restartBtn, endTurnBtn, s2, p2Stats);
        root.setTop(hud);

        controller = new GameController(game, this, p1Stats, p2Stats, endTurnBtn, canvas);

        restartBtn.setOnAction(e -> controller.confirmRestart());

        canvas.setOnMouseClicked(e -> {
            HexCell cell = getClosestHex(e.getX(), e.getY());
            controller.handleMouseClick(e, cell, e.getScreenX(), e.getScreenY());
        });

        stage.setScene(new Scene(root, gridWidth + 150, gridHeight + 150));
        stage.setTitle("Гексагональные Танки: Битва Баз");
        stage.show();

        controller.updateHUD();
        drawGame(game);
    }

    public void showWinDialog(Player winner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Конец игры");
        alert.setHeaderText("База противника уничтожена!");
        alert.setContentText("Победитель: " + winner.getFaction().getDisplayName() + "\nХотите сыграть еще раз?");

        ButtonType restartType = new ButtonType("Новая игра");
        ButtonType exitType = new ButtonType("Выход", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(restartType, exitType);

        alert.showAndWait().ifPresent(type -> {
            if (type == restartType) {
                triggerNewGameSequence();
            } else {
                System.exit(0);
            }
        });
    }

    public void triggerNewGameSequence() {
        Faction[] f = showSelectionDialog();
        controller.resetGame(f[0], f[1]);
    }

    private Faction[] showSelectionDialog() {
        Dialog<Faction[]> dialog = new Dialog<>();
        dialog.setTitle("Выбор сторон");

        ChoiceBox<Faction> cb1 = new ChoiceBox<>(); cb1.getItems().addAll(Faction.values()); cb1.setValue(Faction.USSR);
        ChoiceBox<Faction> cb2 = new ChoiceBox<>(); cb2.getItems().addAll(Faction.values()); cb2.setValue(Faction.GERMANY);

        Label errorLabel = new Label("Выберите разные страны!");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        VBox layout = new VBox(10, new Label("Игрок 1 (Сверху):"), cb1, new Label("Игрок 2 (Снизу):"), cb2, errorLabel);
        layout.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(layout);

        ButtonType okType = new ButtonType("Начать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType);
        Node okButton = dialog.getDialogPane().lookupButton(okType);

        cb1.valueProperty().addListener((o, old, nw) -> {
            boolean invalid = (nw == cb2.getValue());
            okButton.setDisable(invalid);
            errorLabel.setVisible(invalid);
        });
        cb2.valueProperty().addListener((o, old, nw) -> {
            boolean invalid = (nw == cb1.getValue());
            okButton.setDisable(invalid);
            errorLabel.setVisible(invalid);
        });

        dialog.setResultConverter(b -> b == okType ? new Faction[]{cb1.getValue(), cb2.getValue()} : null);
        return dialog.showAndWait().orElse(new Faction[]{Faction.USSR, Faction.GERMANY});
    }

    public void drawGame(Game game) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double x = c * HEX_SIZE * 1.5 + 30;
                double y = r * HEX_SIZE * Math.sqrt(3) + ((c % 2) * HEX_SIZE * Math.sqrt(3) / 2) + 30;
                HexCell cell = game.getGrid()[r][c];

                if (controller.getReachableCells().contains(cell)) {
                    gc.setFill(Color.rgb(255, 255, 0, 0.25));
                    fillHex(x, y);
                }
                if (controller.getAttackableCells().contains(cell)) {
                    gc.setFill(Color.rgb(255, 0, 0, 0.4));
                    fillHex(x, y);
                }

                drawHex(x, y);
                Unit u = cell.getUnit();
                if (u != null) drawUnit(x, y, u);
            }
        }
    }

    private void drawHex(double x, double y) {
        double r = HEX_SIZE; double h = Math.sqrt(3) * r / 2;
        double[] xs = {x, x+r, x+r+(r/2), x+r, x, x-(r/2)};
        double[] ys = {y, y, y+h, y+2*h, y+2*h, y+h};
        gc.setStroke(Color.GRAY);
        gc.strokePolygon(xs, ys, 6);
    }

    private void fillHex(double x, double y) {
        double r = HEX_SIZE; double h = Math.sqrt(3) * r / 2;
        double[] xs = {x, x+r, x+r+(r/2), x+r, x, x-(r/2)};
        double[] ys = {y, y, y+h, y+2*h, y+2*h, y+h};
        gc.fillPolygon(xs, ys, 6);
    }

    private void drawUnit(double x, double y, Unit unit) {
        double cx = x + HEX_SIZE / 2;
        double cy = y + (Math.sqrt(3) * HEX_SIZE / 2);
        Color uColor = unit.getOwner().getFaction().getColor();
        if (unit.hasMoved() && unit.hasAttacked()) uColor = uColor.darker();

        gc.setFill(uColor);
        if (unit instanceof Base) {
            gc.fillRect(cx - 15, cy - 15, 30, 30);
            gc.setStroke(Color.WHITE);
            gc.strokeRect(cx - 15, cy - 15, 30, 30);
        } else {
            gc.fillOval(cx - 12, cy - 12, 24, 24);
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(unit.getName() + " [" + unit.getHp() + "]", x - 5, y + 5);
    }

    private HexCell getClosestHex(double mx, double my) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double x = c * HEX_SIZE * 1.5 + 30 + HEX_SIZE/2;
                double y = r * HEX_SIZE * Math.sqrt(3) + ((c % 2) * HEX_SIZE * Math.sqrt(3) / 2) + 30 + (Math.sqrt(3)*HEX_SIZE/2);
                if (Math.hypot(mx - x, my - y) < HEX_SIZE) return controller.getGame().getGrid()[r][c];
            }
        }
        return null;
    }

    private VBox createPlayerPanel(String factionName) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10)); box.setMinWidth(150);
        Label n = new Label(factionName); n.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label m = new Label("Баланс: 0");
        box.getChildren().addAll(n, m);
        return box;
    }

    public static void main(String[] args) { launch(args); }
}