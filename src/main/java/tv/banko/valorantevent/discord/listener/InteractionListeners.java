package tv.banko.valorantevent.discord.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.discord.guild.GuildHelper;
import tv.banko.valorantevent.tournament.match.GameMap;
import tv.banko.valorantevent.tournament.match.MapVote;
import tv.banko.valorantevent.tournament.match.Match;
import tv.banko.valorantevent.tournament.match.MatchPoints;
import tv.banko.valorantevent.tournament.rank.Rank;
import tv.banko.valorantevent.tournament.team.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                case "template" -> buttonTemplate(event);
                case "committee" -> buttonCommittee(event);
                case "match" -> buttonMatch(event);
            }
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#noMatch`""")
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#noMember`""")
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#noTeam`""")
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

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#phaseDone`""")
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

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#not2`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                List<GameMap> gameMaps = new ArrayList<>();
                StringBuilder mapsString = new StringBuilder();

                for (SelectOption option : list) {
                    GameMap gameMap = GameMap.valueOf(option.getValue());
                    gameMaps.add(gameMap);

                    if (!mapsString.isEmpty()) {
                        mapsString.append(", ");
                    }

                    mapsString.append("**").append(gameMap.getName()).append("**");
                }

                vote.banMaps(gameMaps.toArray(new GameMap[0]));
                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.changeTurn();
                    switch (phase) {
                        case TEAM_1_BANS -> vote.getMessage().secondTwoBans();
                        case TEAM_2_BANS -> vote.getMessage().chooseMap();
                    }
                });

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Maps gebannt")
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

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#phaseDone`""")
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

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#not1`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                GameMap gameMap = GameMap.valueOf(list.get(0).getValue());

                match.setMap(gameMap);
                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.changeTurn();
                    vote.getMessage().chooseSide();
                });

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Map ausgewählt")
                        .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                        .setDescription("> Die Map **" + gameMap.getName() + "** wird gespielt.")
                        .build()).queue();
            }
            case "pick_side" -> {
                if (!Objects.equals(vote.getPhase(), MapVote.Phase.TEAM_2_CHOOSES_STARTING_SIDE)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#phaseDone`""")
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

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#selectMapVote#not1`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                vote.setSide(vote.getTurn(), list.get(0).getValue()
                        .equalsIgnoreCase("team1"));

                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.getMessage().result();

                    match.getChannel().sendChallenge(1);
                    match.getCommittee().getMessage().startMatch();
                });

                event.replyEmbeds(new EmbedBuilder()
                                .setTitle("<:check:950493473436487760> | Seite ausgewählt")
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMapVote#noMatch`""")
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMapVote#noMember`""")
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMapVote#noTeam`""")
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

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMapVote#phaseDone`""")
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
                            .setTitle("<:check:950493473436487760> | Zahl generiert")
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
                            .setTitle("<:check:950493473436487760> | Zahl generiert")
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

                event.getMessage().delete().queue(unused -> vote.getMessage().winnerChooses());
            }
            case "map_pick" -> {
                if (!Objects.equals(vote.getPhase(), MapVote.Phase.WINNER_CHOOSES)) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht kann nicht verwendet werden, da diese Phase nicht mehr läuft.

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> vote.getMessage().firstTwoBans());

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Map auswählen")
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

                                    > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMapVote#phaseDone`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                vote.setNextPhase();

                event.getMessage().delete().queue(unused -> {
                    vote.changeTurn();
                    vote.getMessage().firstTwoBans();
                });

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Map auswählen")
                        .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                        .setDescription("> " + member.getAsMention() + " wählt die **Seite** aus.")
                        .build()).queue();
            }
        }
    }

    private void buttonTemplate(ButtonInteractionEvent event) {

        if (event.getComponent().getId() == null) {
            return;
        }

        Member member = event.getMember();

        if (member == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.
                                                        
                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonTemplate#noMember`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        String id = event.getComponent().getId().split(":")[2].toLowerCase();

        switch (id) {
            case "verify" -> {
                if (member.getNickname() == null || !member.getNickname().contains("#")) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Bitte folge dem 1. Schritt. Du **musst** deinen **VALORANT-Tag** als Nicknamen setzen.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                String tag = member.getNickname();

                event.replyEmbeds(new EmbedBuilder()
                        .setDescription("<a:loading:393852367751086090> **Lade Informationen...**")
                        .build()).setEphemeral(true).queue(interactionHook ->
                        this.event.getTournament().getPlayer().getPlayer(member.getId()).whenCompleteAsync((player, throwable) -> {

                            if (Objects.equals(player.getValorantTag(), tag)) {
                                interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                        .setTitle(":no_entry: | Bereits verifiziert")
                                        .setDescription(String.format("""
                                                > Du bist bereits mit dem VALORANT-Account **%s** verknüpft.
                                                                                            
                                                > Falls Du deinen **Account ändern** willst, ändere deinen **Nicknamen** und **klicke den Knopf** erneut.
                                                > Verwende den Befehl `/updaterank`, um Deinen Rang neu zu laden.""", tag))
                                        .build()).queue();
                                return;
                            }

                            this.event.getRankAPI().getRank(tag).whenCompleteAsync((response, throwable1) -> {
                                if (throwable1 != null) {
                                    throwable1.printStackTrace();
                                }

                                if (response.status() != 200) {
                                    switch (response.status()) {
                                        case 429 -> interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                                .setTitle(":no_entry: | Fehler")
                                                .setDescription("""
                                                        > **Es ist ein Fehler aufgetreten!**
                                                        > Die **API** ist zurzeit überlastet. Bitte versuche es gleich erneut.""")
                                                .build()).queue();
                                        case 404 -> interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                                .setTitle(":no_entry: | Fehler")
                                                .setDescription("""
                                                        > **Es ist ein Fehler aufgetreten!**
                                                        > Dieser Account konnte **nicht gefunden** werden.""")
                                                .build()).queue();
                                        default -> interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                                .setTitle(":no_entry: | Fehler")
                                                .setDescription("""
                                                        > **Es ist ein Fehler aufgetreten!**
                                                        > Unbekannter Fehler: \s""" + response.status())
                                                .build()).queue();
                                    }
                                    return;
                                }

                                Rank rank = response.rank();

                                player.setValorantTag(tag);
                                player.setRank(rank);

                                GuildHelper helper = this.event.getDiscord().getGuildHelper();

                                member.getGuild().addRoleToMember(User.fromId(member.getId()), helper.getTeamSplitterRole())
                                        .and(member.getGuild().addRoleToMember(User.fromId(member.getId()), helper.getRankSplitterRole()))
                                        .and(member.getGuild().addRoleToMember(User.fromId(member.getId()), helper.getVerifiedRole())).queue();

                                interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                        .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                                        .setTitle(":shield: | Rang")
                                        .setDescription("> Dein Account wurde **erfolgreich verknüpft**." +
                                                "\n\n> Account: **" + tag + "**" +
                                                "\n> Rank: **" + rank.getName() + "** " + rank.getEmoji())
                                        .build()).queue();
                            });
                        }));
            }
            case "createteam" -> {
                if (this.event.getTournament().getTeam().getTeamByPlayer(member) != null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist bereits in einem Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                event.replyEmbeds(new EmbedBuilder()
                        .setDescription("<a:loading:393852367751086090> **Team wird erstellt...**")
                        .build()).setEphemeral(true).queue(interactionHook ->
                        this.event.getTournament().getTeam().createTeam().whenCompleteAsync((team, throwable) -> {
                            if (team == null) {
                                event.replyEmbeds(new EmbedBuilder()
                                        .setTitle(":no_entry: | Fehler")
                                        .setDescription("""
                                                > **Es ist ein Fehler aufgetreten!**
                                                > Das Team konnte nicht erstellt werden.
                                                                            
                                                > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonTemplate#teamIsNull`""")
                                        .build()).setEphemeral(true).queue();
                                return;
                            }

                            team.addPlayer(member.getId());
                            team.setCaptain(member.getId());

                            interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                    .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                                    .setTitle(":love_letter: | Team")
                                    .setDescription("> Dein Team wurde **erfolgreich erstellt**." +
                                            "\n\n> Name: **" + team.getName() + "**")
                                    .build()).queue();
                        }));
            }
        }
    }

    private void buttonMatch(ButtonInteractionEvent event) {

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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMatch#noMatch`""")
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMatch#noMember`""")
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

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonMatch#noTeam`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        String id = event.getComponent().getId().split(":")[2].toLowerCase();

        if (!id.equals("transcript")) {
            return;
        }

        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();

        StringBuilder team1_transcript = new StringBuilder();
        StringBuilder team2_transcript = new StringBuilder();

        Map<Integer, Boolean> team1_challenges = match.getTeam1Points().getChallengeSuccess();
        Map<Integer, Boolean> team2_challenges = match.getTeam2Points().getChallengeSuccess();

        Map<Integer, Boolean> team1_wins = match.getTeam1Points().getRoundWon();
        Map<Integer, Boolean> team2_wins = match.getTeam2Points().getRoundWon();

        for (int round = 1; round < 30; round++) {

            if (team1_wins.size() <= round || team2_wins.size() <= round ||
                    team1_challenges.size() <= round || team2_challenges.size() <= round) {
                break;
            }

            boolean won1 = team1_wins.get(round);
            boolean challenge1 = team1_challenges.get(round);

            team1_transcript.append("Runde **").append(round).append("**: ");

            if (won1 && challenge1) {
                team1_transcript.append(":crown:\n");
            } else if (won1) {
                team1_transcript.append(":skull:\n");
            } else if (challenge1) {
                team1_transcript.append(":military_medal:\n");
            } else {
                team1_transcript.append(":clown:\n");
            }

            boolean won2 = team2_wins.get(round);
            boolean challenge2 = team2_challenges.get(round);

            team2_transcript.append("Runde **").append(round).append("**: ");

            if (won2 && challenge2) {
                team2_transcript.append(":crown:\n");
            } else if (won2) {
                team2_transcript.append(":skull:\n");
            } else if (challenge2) {
                team2_transcript.append(":military_medal:\n");
            } else {
                team2_transcript.append(":clown:\n");
            }
        }

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("<:check:950493473436487760> | Match Transcript")
                .setDescription("> **" + team1.getName() + "**: " + match.getTeam1Points().getPoints() +
                        "\n> **" + team2.getName() + "**: " + match.getTeam2Points().getPoints() +
                        "\n" +
                        "\n> :crown: **Challenge abgeschlossen, Runde gewonnen**: +2 Punkte" +
                        "\n> :military_medal: **Challenge abgeschlossen, Runde verloren**: +1 Punkt" +
                        "\n> :skull: **Challenge abgebrochen, Runde gewonnen**: -1 Punkt" +
                        "\n> :clown: **Challenge abgebrochen, Runde verloren**: -1 Punkt")
                .addField(team1.getName(), team1_transcript.toString(), false)
                .addField(team2.getName(), team2_transcript.toString(), false)
                .build()).setEphemeral(true).queue();
    }

    private void buttonCommittee(ButtonInteractionEvent event) {

        if (event.getComponent().getId() == null) {
            return;
        }

        Match match = this.event.getTournament().getMatch().getMatchByCommitteeId(event.getChannel().getId());

        if (match == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Bitte versuche es erneut.

                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonCommittee#noMatch`""")
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
                                                        
                            > Falls der Fehler erneut auftritt, sende den Code: `InteractionListeners#buttonCommittee#noMember`""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        MessageEmbed embed = event.getMessage().getEmbeds().stream().findFirst().orElse(null);
        int round = 0;

        if (embed != null && embed.getFooter() != null && embed.getFooter().getText() != null) {
            round = Integer.parseInt(embed.getFooter().getText());
        }

        String id = event.getComponent().getId().split(":")[2].toLowerCase();

        switch (id) {
            case "start_map_vote" -> {
                match.getMapVote().getMessage().roleDice();
                event.replyEmbeds(new EmbedBuilder()
                        .setDescription("> **Map-Vote gestartet**.")
                        .build()).queue(interactionHook -> event.getMessage().delete().queue());
            }
            case "start_match" -> {
                match.getCommittee().getMessage().setPoints();
                match.getChannel().sendChallenge(2);

                event.replyEmbeds(new EmbedBuilder()
                        .setDescription("> **Match gestartet**.")
                        .build()).queue(interactionHook -> event.getMessage().delete().queue());
            }
            case "delete_match" -> {
                match.getChannel().getChannel().delete().queue();
                match.getCommittee().getChannel().delete().queue();
            }
            case "team1" -> match.getCommittee().getMessage().setTeam1(event, round);
            case "team2" -> match.getCommittee().getMessage().setTeam2(event, round);
            default -> {

                if (!id.startsWith("team1_") && !id.startsWith("team2_")) {
                    return;
                }

                if (!event.getMessage().getContentRaw().equals(member.getAsMention().replace("!", ""))) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Nachricht wurde nicht an dich gesendet.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                MatchPoints points;

                if (id.startsWith("team1_")) {
                    points = match.getTeam1Points();
                } else {
                    points = match.getTeam2Points();
                }

                if (id.endsWith("10") || id.endsWith("11")) {
                    points.setChallengeSuccess(round);
                } else {
                    points.setChallengeFailure(round);
                }

                if (id.endsWith("01") || id.endsWith("11")) {
                    points.setRoundWon(round);
                } else {
                    points.setRoundLost(round);
                }

                event.replyEmbeds(new EmbedBuilder()
                        .setDescription("> Erfolgreich **" + id.split("_")[0] + "** auf **" +
                                points.getPoints() + "** Punkte gesetzt.")
                        .build()).setEphemeral(true).queue();
                event.getMessage().delete().queue();

                checkNextRound(match, round);
            }
        }
    }

    private void checkNextRound(Match match, int roundId) {
        if (!(match.getTeam1Points().hasRound(roundId) && match.getTeam2Points().hasRound(roundId))) {
            return;
        }

        if (match.getRounds() != roundId) {
            return;
        }

        match.addRound();

        if (match.isFinished()) {
            return;
        }

        match.getChannel().sendChallenge(match.getRounds() + 1);

        match.getCommittee().getMessage().setPoints();
    }

}
