package tv.banko.valorantevent.tournament.rank;

public enum Rank {

    UNRATED("Unrated", "<:unrated:945318985174499378>", 0),
    UNUSED_1("-1", "", 1),
    UNUSED_2("-2", "", 2),
    IRON_1("Iron 1", "<:iron1:944225287707688990>", 3),
    IRON_2("Iron 2", "<:iron2:944225288680788028>", 4),
    IRON_3("Iron 3", "<:iron3:944225288680796221>", 5),
    BRONZE_1("Bronze 1", "<:bronze1:944225286868852786>", 6),
    BRONZE_2("Bronze 2", "<:bronze2:944225286403260458>", 7),
    BRONZE_3("Bronze 3", "<:bronze3:944225288173260822>", 8),
    SILVER_1("Silver 1", "<:silver1:944225288420728892>", 9),
    SILVER_2("Silver 2", "<:silver2:944225288806625300>", 10),
    SILVER_3("Silver 3", "<:silver3:944225288882126868>", 11),
    GOLD_1("Gold 1", "<:gold1:944225288458502144>", 12),
    GOLD_2("Gold 2", "<:gold2:944225288513024061>", 13),
    GOLD_3("Gold 3", "<:gold3:944225288131313715>", 14),
    PLATINUM_1("Platinum 1", "<:platinum1:944225287992901662>", 15),
    PLATINUM_2("Platinum 2", "<:platinum2:944225288638844929>", 16),
    PLATINUM_3("Platinum 3", "<:platinum3:944225288852750456>", 17),
    DIAMOND_1("Diamond 1", "<:diamond1:944225286747222118>", 18),
    DIAMOND_2("Diamond 2", "<:diamond2:944225288382980106>", 19),
    DIAMOND_3("Diamond 3", "<:diamond3:944225288722731078>", 20),
    IMMORTAL_1("Immortal 1", "<:immortal1:944225288638832650>", 21),
    IMMORTAL_2("Immortal 2", "<:immortal2:944225288559153254>", 22),
    IMMORTAL_3("Immortal 3", "<:immortal3:944225288265564251>", 23),
    RADIANT("Radiant", "<:radiant:944225288710144051>", 24);

    private final String name;
    private final String emoji;
    private final int id;

    Rank(String name, String emoji, int id) {
        this.name = name;
        this.emoji = emoji;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getId() {
        return id;
    }

    public static Rank byId(int id) {
        for (Rank rank : values()) {
            if (rank.id != id) {
                continue;
            }

            return rank;
        }

        new IllegalArgumentException("rank with id " + id + " does not exist").printStackTrace();

        return Rank.UNRATED;
    }
}
