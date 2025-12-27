import java.util.*;

class Player {
    private final String name;
    private Faction faction;
    private int balance = 130;
    private final List<Unit> units = new ArrayList<>();

    public Player(String name, Faction faction) {
        this.name = name;
        this.faction = faction;
    }

    public void setFaction(Faction faction) { this.faction = faction; }
    public String getName() { return name; }
    public Faction getFaction() { return faction; }
    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
    public List<Unit> getUnits() { return units; }
}