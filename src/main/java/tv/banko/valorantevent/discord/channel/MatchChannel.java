package tv.banko.valorantevent.discord.channel;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.match.Match;
import tv.banko.valorantevent.tournament.team.Team;

import java.util.Collections;
import java.util.List;

public class MatchChannel {

    private final Discord discord;
    private final Match match;

    private String channelId;

    public MatchChannel(Discord discord, Match match) {
        this.discord = discord;
        this.match = match;

        findCategory();
    }

    public MatchChannel(Discord discord, Match match, String channelId) {
        this.discord = discord;
        this.match = match;
        this.channelId = channelId;
    }

    public TextChannel getChannel() {

        Guild guild = discord.getGuildHelper().getGuild();

        if (guild == null) {
            return null;
        }

        if (channelId == null) {
            return null;
        }

        return guild.getTextChannelById(channelId);
    }

    public String getChannelId() {
        return channelId;
    }

    public void sendChallenge(int round) {
        try {
            getChannel().sendMessageEmbeds(match.getRoundChallenge(round).getChallenge().getEmbedBuilder(round, match))
                    .content("Challenge der " + round + ". Runde <@&" + match.getTeam1().getRole().getRoleId() + "> <@&" +
                            match.getTeam2().getRole().getRoleId() + ">").queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMatchEnd(int end) {
        try {
            Team winner;

            Team team1 = match.getTeam1();
            Team team2 = match.getTeam2();

            int wins1 = match.getTeam1Points().getWins();
            int wins2 = match.getTeam2Points().getWins();

            int challenges1 = match.getTeam1Points().getChallenges();
            int challenges2 = match.getTeam2Points().getChallenges();

            switch (end) {
                case 1 -> winner = team1;
                case 2 -> winner = team2;
                default -> {
                    if (challenges1 > challenges2) {
                        winner = team1;
                        break;
                    }

                    if (challenges2 > challenges1) {
                        winner = team2;
                        break;
                    }

                    getChannel().sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle("<:check:950493473436487760> | Match beendet")
                                    .setDescription("> Gewinner: *unentschieden*" +

                                            "\n\n__**" + team1.getName() + "**__:" +
                                            "\n> Gewonnene Runden: **" + wins1 +
                                            "\n> Abgeschlossene Challenges: **" + challenges1 +

                                            "\n\n__**" + team2.getName() + "**__:" +
                                            "\n> Gewonnene Runden: **" + wins2 +
                                            "\n> Abgeschlossene Challenges: **" + challenges2)
                                    .build())
                            .content("Match beendet <@&" + match.getTeam1().getRole().getRoleId() + "> <@&" +
                                    match.getTeam2().getRole().getRoleId() + ">")
                            .setActionRow(Button.primary("match:button:transcript", "Transcript anzeigen")).queue();
                    return;
                }
            }

            getChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("<:check:950493473436487760> | Match beendet")
                            .setDescription("> Gewinner: **" + winner.getName() + "**" +

                                    "\n\n__**" + team1.getName() + "**__:" +
                                    "\n> Gewonnene Runden: **" + wins1 +
                                    "\n> Abgeschlossene Challenges: **" + challenges1 +

                                    "\n\n__**" + team2.getName() + "**__:" +
                                    "\n> Gewonnene Runden: **" + wins2 +
                                    "\n> Abgeschlossene Challenges: **" + challenges2)
                            .build())
                    .content("Match beendet <@&" + match.getTeam1().getRole().getRoleId() + "> <@&" +
                            match.getTeam2().getRole().getRoleId() + ">")
                    .setActionRow(Button.primary("match:button:transcript", "Transcript anzeigen")).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findCategory() {

        Guild guild = discord.getGuildHelper().getGuild();

        if (guild == null) {
            return;
        }

        String categoryName = "\uD83C\uDFB3 | Matches";

        Category category = guild.getCategoriesByName(categoryName, true)
                .stream().findFirst().orElse(null);

        if (category == null) {
            guild.createCategory(categoryName).queue(this::createChannel);
            return;
        }

        createChannel(category);
    }

    private void createChannel(Category category) {
        new Thread(() -> {
            Role publicRole = category.getGuild().getPublicRole();

            while (match.getTeam1().getRole().getRoleId() == null || match.getTeam2().getRole().getRoleId() == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            category.createTextChannel("match-" + match.getId().toString())
                    .addPermissionOverride(publicRole, List.of(Permission.MESSAGE_SEND), List.of(Permission.VIEW_CHANNEL))
                    .addRolePermissionOverride(match.getTeam1().getRole().getId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                    .addRolePermissionOverride(match.getTeam2().getRole().getId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                    .queue(textChannel -> {
                        channelId = textChannel.getId();
                        match.getCommittee().getMessage().updateURL();
                    }, Throwable::printStackTrace);
        }).start();
    }

}
