abstract class Unit {
    private final String name;
    private int hp;
    private final int damage, speed;
    private final Player owner;
    private boolean hasMoved = false, hasAttacked = false;
    Unit(String n, int c, int hp, int d, int s, Player o) { name = n; this.hp = hp; damage = d; speed = s; owner = o; }
    public void resetActions() { hasMoved = false; hasAttacked = false; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public void setHp(int v) { hp = v; }
    public int getDamage() { return damage; }
    public int getSpeed() { return speed; }
    public Player getOwner() { return owner; }
    public boolean hasMoved() { return hasMoved; }
    public void setHasMoved(boolean v) { hasMoved = v; }
    public boolean hasAttacked() { return hasAttacked; }
    public void setHasAttacked(boolean v) { hasAttacked = v; }
}