package tv.banko.valorantevent.tournament.match;

public enum Map {

    FRACTURE("Fracture", "A", "B"),
    BREEZE("Breeze", "A", "B"),
    ICEBOX("Icebox", "A", "B"),
    BIND("Bind", "A", "B"),
    HAVEN("Haven", "A", "B", "C"),
    SPLIT("Split", "A", "B"),
    ASCENT("Ascent", "A", "B");

    private final String name;
    private final String[] spots;

    Map(String name, String... spots) {
        this.name = name;
        this.spots = spots;
    }

    public String getName() {
        return name;
    }

    public String[] getSpots() {
        return spots;
    }

    public int spotCount() {
        return spots.length;
    }
}
