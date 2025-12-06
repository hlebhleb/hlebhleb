import java.util.*;

public class Player {
    String name;
    int balance = 130;
    List<Unit> units = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }
}