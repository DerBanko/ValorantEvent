package tv.banko.valorantevent.discord.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import tv.banko.valorantevent.tournament.match.Map;
import tv.banko.valorantevent.tournament.match.MapVote;
import tv.banko.valorantevent.tournament.match.Match;
import tv.banko.valorantevent.tournament.team.Team;

public record MapVoteMessage(Match match) {

    // Actions: map_vote:button:roll_dice
    public void roleDice() {
        match.getChannel().getChannel()
                .sendMessage(new MessageBuilder()
                        .setContent("<@" + match.getTeam1().getCaptain() + "> <@" + match.getTeam2().getCaptain() + ">")
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(":game_die: | Auswahl beginnen")
                                .setDescription("> Die **Auswahl** beginnt, indem beide **Team-Leader** " +
                                        "eine zufällige Zahl ziehen.\n> **Klickt** dazu auf den :game_die: " +
                                        "unter der Nachricht!")
                                .build())
                        .build())
                .setActionRow(Button.primary("map_vote:button:roll_dice", Emoji.fromUnicode("\uD83C\uDFB2")).withLabel("Wirf den Würfel!"))
                .queue();
    }

    // Actions: map_vote:button:map_pick -> Winner picks map, map_vote:button:side_pick -> Winner picks side later
    public void winnerChooses() {
        Team team = match.getMapVote().getNumberWinner();
        match.getChannel().getChannel()
                .sendMessage(new MessageBuilder()
                        .setContent("<@" + match.getTeam1().getCaptain() + "> <@" + match.getTeam2().getCaptain() + ">")
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(":crown: | Start auswählen")
                                .setDescription("**Zufallszahlen**: " +
                                        "\n> - <@" + match.getTeam1().getCaptain() + ">: " + match.getMapVote().getTeam1Number() +
                                        "\n> - <@" + match.getTeam2().getCaptain() + ">: " + match.getMapVote().getTeam2Number() +
                                        "\n\n> <@" + team.getCaptain() + "> darf wegen der **höheren Zahl** entscheiden, " +
                                        "ob sie/er nach den Bans die **Map picken** (:map:) wollen oder später zwischen **Angreifer " +
                                        "oder Verteidiger** entscheiden (:round_pushpin:) wollen.")
                                .build())
                        .build())
                .setActionRow(Button.primary("map_vote:button:map_pick", Emoji.fromUnicode("\uD83D\uDDFA")).withLabel("Map auswählen"),
                        Button.primary("map_vote:button:side_pick", Emoji.fromUnicode("\uD83D\uDCCD")).withLabel("Seite auswählen"))
                .queue();
    }

    // Actions: map_vote:select:first_bans -> Select Menu (2 Bans allowed)
    public void firstTwoBans() {

        MapVote vote = match.getMapVote();

        SelectMenu.Builder builder = SelectMenu.create("map_vote:select:first_bans")
                .setPlaceholder("Wähle **2 Maps**, welche Du bannen möchtest")
                .setRequiredRange(2, 2);

        for (Map map : vote.getMaps()) {
            builder.addOption(map.getName(), map.name());
        }

        match.getChannel().getChannel()
                .sendMessage(new MessageBuilder()
                        .setContent("<@" + vote.getTurn().getCaptain() + ">")
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(":map: | 2 Maps bannen")
                                .setDescription("> Du kannst nun **2 Maps bannen**. " +
                                        "Danach bannt das andere Team **2 weitere Maps**.")
                                .build())
                        .build())
                .setActionRow(builder.build())
                .queue();
    }

    // Actions: map_vote:select:second_bans -> Select Menu (2 Bans allowed)
    public void secondTwoBans() {
        MapVote vote = match.getMapVote();

        SelectMenu.Builder builder = SelectMenu.create("map_vote:select:second_bans")
                .setPlaceholder("Wähle **2 Maps**, welche Du bannen möchtest")
                .setRequiredRange(2, 2);

        for (Map map : vote.getMaps()) {
            builder.addOption(map.getName(), map.name());
        }

        match.getChannel().getChannel()
                .sendMessage(new MessageBuilder()
                        .setContent("<@" + vote.getTurn().getCaptain() + ">")
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(":map: | 2 Maps bannen")
                                .setDescription("> Du kannst nun **2 Maps bannen**. Danach hat das andere Team die " +
                                        "Möglichkeit zwischen den übrigen Maps **eine Map** zu picken.")
                                .build())
                        .build())
                .setActionRow(builder.build())
                .queue();
    }

    // Actions: map_vote:select:pick_map -> Select Menu (1 Map Pick)
    public void chooseMap() {
        MapVote vote = match.getMapVote();

        SelectMenu.Builder builder = SelectMenu.create("map_vote:select:pick_map")
                .setPlaceholder("Wähle **die Map**, die gespielt werden soll")
                .setRequiredRange(1, 1);

        for (Map map : vote.getMaps()) {
            builder.addOption(map.getName(), map.name());
        }

        match.getChannel().getChannel()
                .sendMessage(new MessageBuilder()
                        .setContent("<@" + vote.getTurn().getCaptain() + ">")
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(":map: | Map auswählen")
                                .setDescription("> Du kannst nun **die Map auswählen**, welche gespielt werden soll. Danach " +
                                        "wählt das andere Team zwischen **Angreifer** oder **Verteidiger**.")
                                .build())
                        .build())
                .setActionRow(builder.build())
                .queue();
    }

    // Actions: map_vote:select:pick_side -> Select Menu (Side Pick)
    public void chooseSide() {
        MapVote vote = match.getMapVote();

        SelectMenu.Builder builder = SelectMenu.create("map_vote:select:pick_side")
                .setPlaceholder("Wähle **die Map**, die gespielt werden soll")
                .setRequiredRange(1, 1);

        builder.addOption("Verteidiger", "defender", "Die ersten 12 Runden werden auf der Verteidiger-Seite gespielt")
                .addOption("Angreifer", "attacker", "Die ersten 12 Runden werden auf der Angreifer-Seite gespielt");

        match.getChannel().getChannel()
                .sendMessage(new MessageBuilder()
                        .setContent("<@" + vote.getTurn().getCaptain() + ">")
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(":round_pushpin: | Seite auswählen")
                                .setDescription("> Map: **" + match.getMap().getName() + "** \n\n> Wähle, ob dein Team als **Angreifer** oder **Verteidiger** starten soll.")
                                .build())
                        .build())
                .setActionRow(builder.build())
                .queue();
    }

    public void result() {
        match.getChannel().getChannel()
                .sendMessage(new MessageBuilder()
                        .setContent("<@" + match.getTeam1().getCaptain() + "> <@" + match.getTeam2().getCaptain() + ">")
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(":postal_horn: | Information")
                                .setDescription("> Map: **" + match.getMap().getName() + "**" +
                                        "\n> erster Defender: **" + match.getDefender().getName() + "**" +
                                        "\n\n:warning: **Bitte folgt nun den Anweisungen des Komitees!**")
                                .build())
                        .build())
                .queue();
    }
}
