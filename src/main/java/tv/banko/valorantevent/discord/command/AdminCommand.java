package tv.banko.valorantevent.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.team.Team;

public class AdminCommand extends CommandObject {

    public AdminCommand(Discord discord) {
        super(discord);
    }

    @Override
    public CommandData getCommand() {
        return Commands.slash("admin", "Command zur Administration.")
                .addSubcommands(new SubcommandData("template", "Sende eine Template-Nachricht in den Chat.")
                        .addOption(OptionType.STRING, "message", "Nachrichten-ID", true))
                .addSubcommands(new SubcommandData("create", "Create")
                        .addOption(OptionType.STRING, "team1", "ID des Team1", true)
                        .addOption(OptionType.STRING, "team2", "ID des Team2", true));
    }

    @Override
    public void respond(SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) {
            return;
        }

        Member member = event.getMember();

        if (member == null) {
            return;
        }

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Du hast keine Berechtigung.""")
                    .build()).setEphemeral(true).queue();
            return;
        }

        if (event.getSubcommandName() == null) {
            return;
        }

        switch (event.getSubcommandName().toLowerCase()) {
            case "template" -> {
                OptionMapping messageOption = event.getOption("message");

                if (messageOption == null) {
                    return;
                }

                switch (messageOption.getAsString()) {
                    case "game_rules" -> this.discord.getTemplate().sendGameRules(event.getGuildChannel());
                    case "order" -> this.discord.getTemplate().sendGameOrder(event.getGuildChannel());
                    case "verify" -> this.discord.getTemplate().sendVerification(event.getGuildChannel());
                    case "discord_rules" -> this.discord.getTemplate().sendDiscordRules(event.getGuildChannel());
                    case "create_team" -> this.discord.getTemplate().sendCreateTeam(event.getGuildChannel());
                    case "join_team" -> this.discord.getTemplate().sendJoinTeam(event.getGuildChannel());
                    default -> {
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle(":no_entry: | Fehler")
                                .setDescription("""
                                        > **Es ist ein Fehler aufgetreten!**
                                        > Keine Nachricht mit dieser Id gefunden.`""")
                                .build()).setEphemeral(true).queue();
                        return;
                    }
                }

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Erfolg")
                        .setDescription("> **Nachricht erfolgreich versandt**.")
                        .build()).setEphemeral(true).queue();
            }
            case "create" -> {
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Erfolg")
                        .setDescription("> **Creating...**")
                        .build()).setEphemeral(true).queue();

                OptionMapping team1Option = event.getOption("team1");
                OptionMapping team2Option = event.getOption("team2");

                if (team1Option == null || team2Option == null) {
                    return;
                }

                Team team1 = discord.getEvent().getTournament().getTeam().getTeamById(team1Option.getAsString());
                Team team2 = discord.getEvent().getTournament().getTeam().getTeamById(team2Option.getAsString());

                if (team1 == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                        > **Es ist ein Fehler aufgetreten!**
                                        > Team 1 existiert nicht.`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (team2 == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                        > **Es ist ein Fehler aufgetreten!**
                                        > Team 2 existiert nicht.`""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                discord.getEvent().getTournament().getMatch().createMatch(team1, team2);
            }
        }
    }
}
