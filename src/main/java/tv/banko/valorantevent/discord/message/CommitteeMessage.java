package tv.banko.valorantevent.discord.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import tv.banko.valorantevent.tournament.match.Match;

public class CommitteeMessage {

    private final Match match;
    private String url;

    public CommitteeMessage(Match match) {
        this.match = match;
        this.url = "https://discord.com/channels/" + match.getTournament().getDiscord().getGuildId() + "/" + match.getChannel().getChannelId();
    }

    // Actions: committee:button:delete_match
    public void deleteMatch() {
        match.getCommittee().getChannel()
                .sendMessage(new MessageBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setAuthor("Match " + match.getId(), url)
                                .setTitle(":wastebasket: | Match beendet")
                                .setDescription("> Klicke auf den unten stehenden Knopf, um das Match zu löschen!")
                                .build())
                        .build())
                .setActionRow(Button.primary("committee:button:delete_match", Emoji.fromUnicode("\uD83D\uDDD1"))
                        .withLabel("Match löschen"))
                .queue();
    }

    // Actions: committee:button:start_map_vote
    public void startMapVote() {
        match.getCommittee().getChannel()
                .sendMessage(new MessageBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setAuthor("Match " + match.getId(), url)
                                .setTitle(":ballot_box: | Map-Auswahl beginnen")
                                .setDescription("> Klicke auf den unten stehenden Knopf, um die Map-Auswahl des Matches **" +
                                        match.getId() + "** zu beginnen!")
                                .build())
                        .build())
                .setActionRow(Button.primary("committee:button:start_map_vote", Emoji.fromUnicode("\uD83D\uDDF3"))
                        .withLabel("Starte die Map-Auswahl"))
                .queue();
    }

    // Actions: committee:button:start_match
    public void startMatch() {
        match.getCommittee().getChannel()
                .sendMessage(new MessageBuilder()
                        .setEmbeds(new EmbedBuilder()
                                .setAuthor("Match " + match.getId(), url)
                                .setTitle(":tada: | Match starten")
                                .setDescription("> Klicke auf den unten stehenden Knopf, um das Match zu starten!")
                                .build())
                        .build())
                .setActionRow(Button.primary("committee:button:start_match", Emoji.fromUnicode("\uD83C\uDF89"))
                        .withLabel("Starte das Match"))
                .queue();
    }

    // Actions: committee:button:team2, committee:button:team1
    public void setPoints() {
        try {
            int team1Points = match.getTeam1Points().getPoints();
            int team2Points = match.getTeam2Points().getPoints();

            match.getCommittee().getChannel()
                    .sendMessage(new MessageBuilder()
                            .setEmbeds(new EmbedBuilder()
                                    .setAuthor("Match " + match.getId(), url)
                                    .setTitle(":coin: | Punkte verteilen")
                                    .setDescription("> Runde **" + match.getRounds() + "**: " +
                                            "\n\n> :one: " + match.getTeam1().getName() + ": **" + team1Points + "**" +
                                            "\n> :two: " + match.getTeam2().getName() + ": **" + team2Points + "**" +
                                            "\n\n> Klicke auf einen der **unten stehenden Knöpfe**, um die **Punkte** " +
                                            "für die Runde zu **verteilen**.")
                                    .setFooter(match.getRounds() + "")
                                    .build())
                            .build())
                    .setActionRow(Button.primary("committee:button:team1", Emoji.fromUnicode("1️⃣")).withLabel(match.getTeam1().getName()),
                            Button.primary("committee:button:team2", Emoji.fromUnicode("2️⃣")).withLabel(match.getTeam2().getName()))
                    .queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Actions: committee:button:team1_11, committee:button:team1_10, committee:button:team1_01, committee:button:team1_00
    public void setTeam1(ButtonInteractionEvent event, int rounds) {
        if (rounds <= 0) {
            throw new IllegalArgumentException("rounds <= 0 (" + rounds + ")");
        }

        event.reply(new MessageBuilder()
                        .setContent(event.getUser().getAsMention())
                        .setEmbeds(new EmbedBuilder()
                                .setAuthor("Match " + match.getId(), url)
                                .setTitle(":one: | Team 1: Runde " + match.getRounds())
                                .setDescription("""
                                        > :crown: **Challenge abgeschlossen, Runde gewonnen**: +2 Punkte
                                        > :military_medal: **Challenge abgeschlossen, Runde verloren**: +1 Punkt
                                        > :skull: **Challenge abgebrochen, Runde gewonnen**: -1 Punkt
                                        > :clown: **Challenge abgebrochen, Runde verloren**: -1 Punkt""")
                                .setFooter(rounds + "")
                                .build())
                        .build())
                .addActionRow(Button.success("committee:button:team1_11", Emoji.fromUnicode("\uD83D\uDC51")),
                        Button.primary("committee:button:team1_10", Emoji.fromUnicode("\uD83C\uDF96")),
                        Button.danger("committee:button:team1_01", Emoji.fromUnicode("\uD83D\uDC80")),
                        Button.danger("committee:button:team1_00", Emoji.fromUnicode("\uD83E\uDD21")))
                .queue();
    }

    // Actions: committee:button:team2_11, committee:button:team2_10, committee:button:team2_01, committee:button:team2_00
    public void setTeam2(ButtonInteractionEvent event, int rounds) {
        if (rounds <= 0) {
            throw new IllegalArgumentException("rounds <= 0 (" + rounds + ")");
        }

        event.reply(new MessageBuilder()
                        .setContent(event.getUser().getAsMention())
                        .setEmbeds(new EmbedBuilder()
                                .setAuthor("Match " + match.getId(), url)
                                .setTitle(":two: | Team 2: Runde " + match.getRounds())
                                .setDescription("""
                                        > :crown: **Challenge abgeschlossen, Runde gewonnen**: +2 Punkte
                                        > :military_medal: **Challenge abgeschlossen, Runde verloren**: +1 Punkt
                                        > :skull: **Challenge abgebrochen, Runde gewonnen**: -1 Punkt
                                        > :clown: **Challenge abgebrochen, Runde verloren**: -1 Punkt""")
                                .setFooter(rounds + "")
                                .build())
                        .build())
                .addActionRow(Button.success("committee:button:team2_11", Emoji.fromUnicode("\uD83D\uDC51")),
                        Button.primary("committee:button:team2_10", Emoji.fromUnicode("\uD83C\uDF96")),
                        Button.danger("committee:button:team2_01", Emoji.fromUnicode("\uD83D\uDC80")),
                        Button.danger("committee:button:team2_00", Emoji.fromUnicode("\uD83E\uDD21")))
                .queue();
    }

    public void updateURL() {
        this.url = "https://discord.com/channels/" + match.getTournament().getDiscord().getGuildId() + "/" + match.getChannel().getChannelId();
    }
}
