package tv.banko.valorantevent.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.rank.Rank;

public class UpdateRankCommand extends CommandObject {

    public UpdateRankCommand(Discord discord) {
        super(discord);
    }

    @Override
    public CommandData getCommand() {
        return Commands.slash("updaterank", "Update deinen VALORANT-Rang.");
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

        event.replyEmbeds(new EmbedBuilder()
                .setDescription("<a:loading:393852367751086090> **Lade Informationen...**")
                .build()).setEphemeral(true).queue(interactionHook ->
                discord.getEvent().getTournament().getPlayer().getPlayer(member.getId()).whenCompleteAsync((player, throwable) -> {

                    if (player.getValorantTag() == null) {
                        interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                .setTitle(":no_entry: | Fehler")
                                .setDescription("""
                                        > **Es ist ein Fehler aufgetreten!**
                                        > Du hast noch keinen Tag gesetzt.
                                                                                    
                                        > Verwende `/link <Tag>`""")
                                .build()).queue();
                    }

                    discord.getEvent().getRankAPI().getRank(player.getValorantTag()).whenCompleteAsync((response, throwable1) -> {
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

                        player.setRank(rank);

                        interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                                .setTitle(":shield: | Rang")
                                .setDescription("> Dein Rang wurde **neu geladen**." +
                                        "\n\n> Account: **" + player.getValorantTag() + "**" +
                                        "\n> Rank: **" + rank.getName() + "** " + rank.getEmoji())
                                .build()).queue();
                    });
                }));
    }
}
