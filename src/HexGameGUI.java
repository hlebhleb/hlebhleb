import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HexGameGUI extends Application {

    //константы
    private final int ROWS = 10;
    private final int COLS = 11;
    private final double HEX_SIZE = 30;

    private double offsetX;
    private double offsetY;

    private double gridWidth;
    private double gridHeight;

    //поля
    private Canvas canvas;
    private GraphicsContext gc;

    private VBox p1Stats;
    private VBox p2Stats;
    private Button endTurnBtn;

    private GameController controller;

    @Override
    public void start(Stage stage) {
        Game game = new Game();

        //сетка
        gridWidth = (COLS * 1.5 + 0.5) * HEX_SIZE;
        double hexHeight = Math.sqrt(3) * HEX_SIZE;
        gridHeight = ROWS * hexHeight + hexHeight / 2;

        //размеры поля
        double initialCanvasWidth = gridWidth + HEX_SIZE * 2;
        double initialCanvasHeight = gridHeight + hexHeight;

        //создание компоновки
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();

        StackPane canvasContainer = new StackPane(canvas);

        BorderPane root = new BorderPane();
        root.setCenter(canvasContainer);

        //Настройка HUD
        HBox hud = new HBox();
        hud.setSpacing(20);
        hud.setAlignment(Pos.CENTER);
        hud.setStyle("-fx-background-color: #ddd; -fx-padding: 10;");

        p1Stats = createPlayerPanel(game.player1.name);
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        endTurnBtn = new Button("Завершить ход");
        endTurnBtn.setPrefSize(120, 40);
        endTurnBtn.setStyle("-fx-font-size: 14; -fx-base: #b6e7c9;");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        p2Stats = createPlayerPanel(game.player2.name);

        hud.getChildren().addAll(p1Stats, spacer1, endTurnBtn, spacer2, p2Stats);
        root.setTop(hud);

        controller = new GameController(game, this, p1Stats, p2Stats, endTurnBtn, canvas);

        double hudApproxHeight = 80;
        double initialSceneHeight = initialCanvasHeight + hudApproxHeight;

        Scene scene = new Scene(root, initialCanvasWidth, initialSceneHeight);
        stage.setScene(scene);
        stage.setTitle("Гексагональные Танки");
        stage.show();

        canvas.setOnMouseClicked(this::handleMouseClick);
        canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> redraw());

        controller.updateHUD();

        redraw();
    }

    private void redraw() {
        canvas.setWidth(canvas.getParent().getBoundsInLocal().getWidth());
        canvas.setHeight(canvas.getParent().getBoundsInLocal().getHeight());

        //центрирование
        this.offsetX = (canvas.getWidth() - gridWidth) / 2;
        this.offsetY = (canvas.getHeight() - gridHeight) / 2;

        this.drawGame(controller.getGame());
    }

    private VBox createPlayerPanel(String name) {
        VBox box = new VBox(5);
        box.setPadding(new javafx.geometry.Insets(10));
        box.setMinWidth(150);
        box.setAlignment(Pos.CENTER_LEFT);

        Label lblName = new Label(name);
        lblName.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label lblMoney = new Label("Баланс: 0");
        lblMoney.setFont(Font.font("Monospace", 14));

        box.getChildren().addAll(lblName, lblMoney);
        return box;
    }

    private void handleMouseClick(MouseEvent e) {
        HexCell clickedCell = getClosestHex(e.getX(), e.getY());
        controller.handleMouseClick(e, clickedCell, e.getScreenX(), e.getScreenY());
    }

    public void updateHUD() {
        controller.updateHUD();
    }

    //рендер

    public void drawGame(Game game) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double[] hexOrigin = getHexOrigin(r, c);
                drawHex(gc, hexOrigin[0], hexOrigin[1]);

                if (game.grid[r][c].unit != null) {
                    double centerX = hexOrigin[0] + HEX_SIZE * 0.5;
                    double h = Math.sqrt(3) * HEX_SIZE / 2;
                    double centerY = hexOrigin[1] + h;

                    drawUnit(gc, centerX, centerY, game.grid[r][c].unit);
                }
            }
        }
    }

    private void drawUnit(GraphicsContext gc, double x, double y, Unit unit) {
        if (unit.owner.name.equals("Германия")) {
            gc.setFill(Color.web("#444444"));
        } else {
            gc.setFill(Color.web("#228B22"));
        }

        double r = HEX_SIZE * 0.4;
        gc.fillOval(x - r, y - r, r * 2, r * 2);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 10));
        gc.fillText(unit.name, x - 10, y - 12);
    }

    private void drawHex(GraphicsContext gc, double x, double y) {
        double r = HEX_SIZE;
        double h = Math.sqrt(3) * r / 2;
        double[] xs = { x, x+r, x+r+(r/2), x+r, x, x-(r/2) };
        double[] ys = { y, y, y+h, y+2*h, y+2*h, y+h };
        gc.strokePolygon(xs, ys, 6);
    }

    public double[] getHexOrigin(int r, int c) {
        double x = c * HEX_SIZE * 1.5 + this.offsetX;
        double y = r * HEX_SIZE * Math.sqrt(3) + ((c % 2) * HEX_SIZE * Math.sqrt(3) / 2) + this.offsetY;
        return new double[]{x, y};
    }

    private HexCell getClosestHex(double mouseX, double mouseY) {
        Game game = controller.getGame();
        HexCell closest = null;
        double minDist = Double.MAX_VALUE;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double[] pos = getHexOrigin(r, c);
                double cx = pos[0] + HEX_SIZE * 0.5;
                double h = Math.sqrt(3) * HEX_SIZE / 2;
                double cy = pos[1] + h;

                double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));

                if (dist < HEX_SIZE && dist < minDist) {
                    minDist = dist;
                    closest = game.grid[r][c];
                }
            }
        }
        return closest;
    }

    public static void main(String[] args) {
        launch(args);
    }
}