package tv.banko.valorantevent.discord.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.tournament.match.Map;
import tv.banko.valorantevent.tournament.match.MapVote;
import tv.banko.valorantevent.tournament.match.Match;
import tv.banko.valorantevent.tournament.team.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record InteractionListeners(ValorantEvent event) implements EventListener {

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {

        if (genericEvent instanceof SelectMenuInteractionEvent event) {

            if (event.getComponent().getId() == null) {
                return;
            }

            switch (event.getComponent().getId().split(":")[0]) {
                case "map_vote" -> selectMapVote(event);
            }

            return;
        }

        if (genericEvent instanceof ButtonInteractionEvent event) {

            if (event.getComponent().getId() == null) {
                return;
            }

            switch (event.getComponent().getId().split(":")[0]) {
                case "map_vote" -> buttonMapVote(event);
            }

            return;
        }

    }

    private void selectMapVote(SelectMenuInteractionEvent event) {

        if (event.getComponent().getId() == null) {
            return;
        }

        Match match = this.event.getTournament().getMatch().getMatchByChannelId(event.getChannel().getId());

        if (match == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#noMatch`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();

        if (member == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#noMember`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        Team team = this.event.getTournament().getTeam().getTeamByPlayer(member);

        if (team == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#noTeam`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        MapVote vote = match.getMapVote();

        if (vote.getTurn() != null && !vote.getTurn().equals(team)) {
            if (team.getCaptain().equalsIgnoreCase(member.getId())) {
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(":warning: | Hinweis")
                        .setDescription("> Gerade ist das **andere Team** am Zug.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":warning: | Hinweis")
                    .setDescription("> Nur als **Team-Captain** kannst du abstimmen.")
                    .build()).setEphemeral(true).queue();
            return;
        }

        String id = event.getComponent().getId().split(":")[2].toLowerCase();

        switch (id) {
            case "first_bans", "second_bans" -> {
                MapVote.Phase phase = id.equals("first_bans") ? MapVote.Phase.TEAM_1_BANS : MapVote.Phase.TEAM_2_BANS;

                if (!Objects.equals(vote.getPhase(), phase)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                List<SelectOption> list = event.getInteraction().getSelectedOptions();

                if (list.size() != 2) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                     > Bitte versuche es erneut.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#not2`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                List<Map> maps = new ArrayList<>();
                StringBuilder mapsString = new StringBuilder();

                for (SelectOption option : list) {
                    Map map = Map.valueOf(option.getValue());
                    maps.add(map);

                    if (!mapsString.isEmpty()) {
                        mapsString.append(", ");
                    }

                    mapsString.append("**").append(map.getName()).append("**");
                }

                vote.banMaps(maps.toArray(new Map[0]));
                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.changeTurn();
                    switch (phase) {
                        case TEAM_1_BANS -> vote.getMessage().secondTwoBans();
                        case TEAM_2_BANS -> vote.getMessage().chooseMap();
                    }
                });

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(":white_check_mark: | Maps gebannt")
                        .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                        .setDescription("> Die Maps " + mapsString + " wurden gebannt.")
                        .build()).queue();
            }
            case "pick_map" -> {
                if (!Objects.equals(vote.getPhase(), MapVote.Phase.TEAM_1_CHOOSES_MAP)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                List<SelectOption> list = event.getInteraction().getSelectedOptions();

                if (list.size() != 1) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                     > Bitte versuche es erneut.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#not1`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                Map map = Map.valueOf(list.get(0).getValue());

                match.setMap(map);
                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.changeTurn();
                    vote.getMessage().chooseSide();
                });

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(":white_check_mark: | Map ausgewählt")
                        .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                        .setDescription("> Die Map **" + map.getName() + "** wird gespielt.")
                        .build()).queue();
            }
            case "pick_side" -> {
                if (!Objects.equals(vote.getPhase(), MapVote.Phase.TEAM_2_CHOOSES_STARTING_SIDE)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                List<SelectOption> list = event.getInteraction().getSelectedOptions();

                if (list.size() != 1) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                     > Bitte versuche es erneut.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#not1`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                vote.setSide(vote.getTurn(), list.get(0).getValue()
                        .equalsIgnoreCase("defender"));

                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.getMessage().result();
                });

                event.replyEmbeds(new EmbedBuilder()
                                .setTitle(":white_check_mark: | Seite ausgewählt")
                                .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                                .setDescription("> Das Team von <@" + match.getDefender().getCaptain() +
                                        "> spielt anfangs als **Verteidiger**." +
                                        "\n> Das Team von <@" + match.getAttacker().getCaptain() +
                                        "> spielt anfangs als **Angreifer**.").build())
                        .queue();
            }
        }
    }

    private void buttonMapVote(ButtonInteractionEvent event) {

        if (event.getComponent().getId() == null) {
            return;
        }

        Match match = this.event.getTournament().getMatch().getMatchByChannelId(event.getChannel().getId());

        if (match == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#noMatch`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();

        if (member == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#noMember`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        Team team = this.event.getTournament().getTeam().getTeamByPlayer(member);

        if (team == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#noTeam`""")
                    .build()).setEphemeral(true).queue();
            return;
        }
        
        MapVote vote = match.getMapVote();

        if (vote.getTurn() != null && !vote.getTurn().equals(team)) {
            if (team.getCaptain().equalsIgnoreCase(member.getId())) {
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(":warning: | Hinweis")
                        .setDescription("> Gerade ist das **andere Team** am Zug.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":warning: | Hinweis")
                    .setDescription("> Nur als **Team-Captain** kannst du abstimmen.")
                    .build()).setEphemeral(true).queue();
            return;
        }

        String id = event.getComponent().getId().split(":")[2].toLowerCase();

        switch (id) {
            case "roll_dice" -> {
                if (!Objects.equals(vote.getPhase(), MapVote.Phase.HIGHER_RANDOM_NUMBER_WINS)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (team.equals(match.getTeam1())) {
                    if (vote.hasTeam1Number()) {
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle(":warning: | Hinweis")
                                .setDescription("> **Du hast bereits eine Nummer generiert**." +
                                        "\n> Du musst auf das andere Team warten.")
                                .build()).setEphemeral(true).queue();
                        return;
                    }

                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":white_check_mark: | Zahl generiert")
                            .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                            .setDescription("> Zufällige Zahl von " + member.getAsMention() + ": **" + vote.getTeam1Number() + "**")
                            .build()).queue();

                    if (!vote.hasTeam2Number()) {
                        return;
                    }
                } else {
                    if (vote.hasTeam2Number()) {
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle(":warning: | Hinweis")
                                .setDescription("> **Du hast bereits eine Nummer generiert**." +
                                        "\n> Du musst auf das andere Team warten.")
                                .build()).setEphemeral(true).queue();
                        return;
                    }

                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":white_check_mark: | Zahl generiert")
                            .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                            .setDescription("> Zufällige Zahl von " + member.getAsMention() + ": **" + vote.getTeam2Number() + "**")
                            .build()).queue();

                    if (!vote.hasTeam1Number()) {
                        return;
                    }
                }

                if (vote.getTeam1Number() > vote.getTeam2Number()) {
                    vote.setTurn(match.getTeam1());
                } else {
                    vote.setTurn(match.getTeam2());
                }

                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.getMessage().winnerChooses();
                });
            }
            case "map_pick" -> {
                if (!Objects.equals(vote.getPhase(), MapVote.Phase.WINNER_CHOOSES)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.getMessage().firstTwoBans();
                });

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(":white_check_mark: | Map auswählen")
                        .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                        .setDescription("> " + member.getAsMention() + " wählt die **Map** aus.")
                        .build()).queue();
            }
            case "side_pick" -> {
                if (!Objects.equals(vote.getPhase(), MapVote.Phase.WINNER_CHOOSES)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#manageMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.changeTurn();
                    vote.getMessage().firstTwoBans();
                });

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(":white_check_mark: | Map auswählen")
                        .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                        .setDescription("> " + member.getAsMention() + " wählt die **Seite** aus.")
                        .build()).queue();
            }
        }
    }

}
