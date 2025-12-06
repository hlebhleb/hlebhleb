public class UnitFactory {

    public static Unit create(int option, Player owner) {

        boolean isGerman = owner.name.equals("Германия");

        return switch (option) {
            case 1 -> isGerman
                    ? new Tank("PzII",20,15,5,4,owner)
                    : new Tank("T80",20,15,5,4,owner);

            case 2 -> isGerman
                    ? new Tank("PzIV",40,25,10,3,owner)
                    : new Tank("T34",40,25,10,3,owner);

            case 3 -> isGerman
                    ? new Tank("Tiger",70,50,20,2,owner)
                    : new Tank("IS1",70,50,20,2,owner);

            default -> null;
        };
    }
}