class UnitFactory {
    public static Unit create(int option, Player owner) {
        Faction f = owner.getFaction();
        return switch (f) {
            case USSR -> switch (option) {
                case 1 -> new Tank("T-80", 20, 15, 5, 4, owner);
                case 2 -> new Tank("T-34", 40, 25, 10, 3, owner);
                case 3 -> new Tank("IS-1", 70, 50, 20, 2, owner);
                default -> null;
            };
            case GERMANY -> switch (option) {
                case 1 -> new Tank("PzII", 20, 15, 5, 4, owner);
                case 2 -> new Tank("PzIV", 40, 25, 10, 3, owner);
                case 3 -> new Tank("Tiger", 70, 50, 20, 2, owner);
                default -> null;
            };
            case USA -> switch (option) {
                case 1 -> new Tank("M2", 20, 15, 5, 4, owner);
                case 2 -> new Tank("M4", 40, 25, 10, 3, owner);
                case 3 -> new Tank("M6", 70, 50, 20, 2, owner);
                default -> null;
            };
            case UK -> switch (option) {
                case 1 -> new Tank("Mk4", 20, 15, 5, 4, owner);
                case 2 -> new Tank("Cromwell", 40, 25, 10, 3, owner);
                case 3 -> new Tank("Churchill", 70, 50, 20, 2, owner);
                default -> null;
            };
        };
    }
}