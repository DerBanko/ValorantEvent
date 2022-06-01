package tv.banko.valorantevent.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.team.Team;

import java.util.Objects;

public class TeamCommand extends CommandObject {

    public TeamCommand(Discord discord) {
        super(discord);
    }

    @Override
    public CommandData getCommand() {
        return Commands.slash("team", "Befehl, um dein Team zu managen")
                .addSubcommands(new SubcommandData("rename", "Ändere den Namen deines Teams.")
                        .addOption(OptionType.STRING, "name", "Der neue Name", true))
                .addSubcommands(new SubcommandData("invite", "Lade Mitspieler:innen in dein Team ein")
                        .addOption(OptionType.USER, "user", "Mitspieler:in", true))
                .addSubcommands(new SubcommandData("kick", "Entferne Mitspieler:innen aus deinem Team ein")
                        .addOption(OptionType.USER, "user", "Mitspieler:in", true))
                .addSubcommands(new SubcommandData("join", "Betrete ein Team")
                        .addOption(OptionType.STRING, "code", "Team-Id", false))
                .addSubcommands(new SubcommandData("leave", "Verlasse dein Team"))
                .addSubcommands(new SubcommandData("info", "Informationen des Teams"));
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

        if (event.getSubcommandName() == null) {
            return;
        }

        Team team = discord.getEvent().getTournament().getTeam().getTeamByPlayer(member);

        switch (event.getSubcommandName().toLowerCase()) {
            case "rename" -> {

                if (team == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist in keinem Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (!team.getCaptain().equalsIgnoreCase(member.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist nicht der Captain des Teams.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                OptionMapping messageOption = event.getOption("name");

                if (messageOption == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du hast keinen Namen angegeben.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                team.setName(messageOption.getAsString());

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Erfolg")
                        .setDescription("> Das Team wurde erfolgreich zu **" + team.getName() + " umbenannt**.")
                        .build()).setEphemeral(true).queue();
            }
            case "invite" -> {

                if (team == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist in keinem Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (!team.getCaptain().equalsIgnoreCase(member.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist nicht der Captain des Teams.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                OptionMapping userOption = event.getOption("user");

                if (userOption == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du hast keinen User angegeben.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                User user = userOption.getAsUser();

                if (team.isPlayer(user.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Person ist bereits im Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (team.isInvited(user.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Person ist bereits eingeladen.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                team.addInvite(user.getId());

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Erfolg")
                        .setDescription("> Du hast erfolgreich " + user.getAsMention() + " in dein Team **eingeladen**.")
                        .build()).setEphemeral(true).queue();
            }
            case "kick" -> {
                if (team == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist in keinem Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (!team.getCaptain().equalsIgnoreCase(member.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist nicht der Captain des Teams.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                OptionMapping userOption = event.getOption("user");

                if (userOption == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du hast keinen User angegeben.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                User user = userOption.getAsUser();

                if (!team.isPlayer(user.getId()) && !team.isInvited(user.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Diese Person ist weder im Team noch von dir eingeladen worden.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (team.isPlayer(user.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("<:check:950493473436487760> | Erfolg")
                            .setDescription("> Du hast erfolgreich " + user.getAsMention() + " aus deinem Team **entfernt**.")
                            .build()).setEphemeral(true).queue();
                }

                if (team.isInvited(user.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("<:check:950493473436487760> | Erfolg")
                            .setDescription("> Die Einladung an " + user.getAsMention() + " wurde erfolgreich **entfernt**.")
                            .build()).setEphemeral(true).queue();
                }

                team.removeInvite(user.getId());
                team.removePlayer(user.getId());
            }
            case "join" -> {
                if (team != null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist bereits in einem Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                OptionMapping codeOption = event.getOption("code");

                if (codeOption == null) {
                    StringBuilder builder = new StringBuilder();

                    for (Team invite : discord.getEvent().getTournament().getTeam().getTeamsByInvite(member)) {
                        builder.append("\n> `").append(invite.getId()).append("`: Team " +
                                "von <@").append(invite.getCaptain()).append(">");
                    }

                    if (builder.isEmpty()) {
                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle(":no_entry: | Fehler")
                                .setDescription("""
                                        > **Es ist ein Fehler aufgetreten!**
                                        > Du wurdest in kein Team eingeladen.""")
                                .build()).setEphemeral(true).queue();
                        return;
                    }

                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":scroll: | Einladungen")
                            .setDescription("> Hier siehst du die Einladungen an dich. Du kannst ein Team betreten, indem du " +
                                    "den `Code` des jeweiligen Teams bei **/team join <Code>** eingibst.\n" + builder)
                            .build()).setEphemeral(true).queue();
                    return;
                }

                Team invite = discord.getEvent().getTournament().getTeam().getTeamById(codeOption.getAsString());

                if (invite == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Dieses Team existiert nicht.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (!invite.isInvited(member.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du wurdest von diesem Team nicht eingeladen.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                invite.removeInvite(member.getId());
                invite.addPlayer(member.getId());

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Erfolg")
                        .setDescription("> Du hast das Team **" + invite.getName() + "** erfolgreich **betreten**.")
                        .build()).setEphemeral(true).queue();
            }
            case "info" -> {

                if (team == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist in keinem Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                StringBuilder players = new StringBuilder();

                for (String player : team.getPlayers()) {
                    players.append("\n> - <@").append(player).append(">");
                }

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle(":revolving_hearts: | Team-Informationen")
                        .setDescription("> Name: **" + team.getName() + "**" +
                                "\n> Id: **" + team.getId() + "**" +
                                "\n> Captain: <@" + team.getCaptain() + ">" +
                                "\n> Spieler: " + players)
                        .build()).setEphemeral(true).queue();
            }
            case "leave" -> {

                if (team == null) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist in keinem Team.""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                if (Objects.equals(team.getCaptain(), member.getId())) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle(":no_entry: | Fehler")
                            .setDescription("""
                                    > **Es ist ein Fehler aufgetreten!**
                                    > Du bist der Team-Captain. Du kannst das Team nicht verlassen!""")
                            .build()).setEphemeral(true).queue();
                    return;
                }

                team.removePlayer(member.getId());

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("<:check:950493473436487760> | Erfolg")
                        .setDescription("> Du hast dein Team erfolgreich **verlassen**.")
                        .build()).setEphemeral(true).queue();
            }
        }
    }
}
