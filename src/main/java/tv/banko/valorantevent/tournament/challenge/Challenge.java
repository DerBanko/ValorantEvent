package tv.banko.valorantevent.tournament.challenge;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tv.banko.valorantevent.tournament.match.Match;

public class Challenge {

    private final String name;
    private final String allowedWeapons;
    private final String allowedAbilities;
    private final boolean shield;
    private final String limitation;

    private final int[] rounds;

    private Challenge(String name, String allowedWeapons, String allowedAbilities, boolean shield, String limitation, int[] rounds) {
        this.name = name;
        this.allowedWeapons = allowedWeapons;
        this.allowedAbilities = allowedAbilities;
        this.shield = shield;
        this.limitation = limitation;
        this.rounds = rounds;
    }

    public String getName() {
        return name;
    }

    public String getAllowedWeapons() {
        return allowedWeapons;
    }

    public String getAllowedAbilities() {
        return allowedAbilities;
    }

    public boolean isShield() {
        return shield;
    }

    public String getLimitation() {
        return limitation;
    }

    public int[] getRounds() {
        return rounds;
    }

    public MessageEmbed getEmbedBuilder(int round, Match match) {

        StringBuilder builder = new StringBuilder();

        builder.append("Name: **").append(name).append("**\n\n");

        if(allowedWeapons != null) {
            builder.append("Erlaubte **Waffen**: ").append(allowedWeapons).append("\n");
        }

        if(allowedAbilities != null) {
            builder.append("Erlaubte **Fähigkeiten**: ").append(allowedAbilities).append("\n");
        }

        builder.append("Schild: ").append(shield ? "erlaubt" : "verboten").append("\n");

        if(limitation != null) {
            builder.append("Einschränkungen: ").append(limitation).append("\n");
        }

        String additional = match.getRoundChallenge(round).getAdditionalInformation(match);

        if(additional != null) {
            builder.append("\nZusätzlich: ").append(additional);
        }

        return new EmbedBuilder()
                .setTitle(":no_pedestrians: | Challenge der " + round + ". Runde")
                .setDescription(builder)
                .build();
    }

    public static Challenge from(@NotNull String name,
                                 @Nullable String allowedWeapons,
                                 @Nullable String allowedAbilities,
                                 @Nullable Boolean shield,
                                 @Nullable String limitation,
                                 int... rounds) {
        if(allowedAbilities == null && allowedWeapons == null && shield == null && limitation == null) {
            throw new IllegalArgumentException("there cannot be every challenge value null");
        }

        String a = allowedWeapons == null ? "alle" : allowedWeapons;
        String b = allowedAbilities == null ? "alle" : allowedAbilities;
        boolean c = shield == null || shield;

        return new Challenge(name, a, b, c, limitation, rounds);
    }
}
