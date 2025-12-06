public abstract class Unit {
    String name;
    int cost, hp, damage, speed;
    Player owner;

    Unit(String n, int cost, int hp, int dmg, int spd, Player o) {
        this.name = n;
        this.cost = cost;
        this.hp = hp;
        this.damage = dmg;
        this.speed = spd;
        this.owner = o;
    }
}
