package tv.banko.valorantevent.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.rank.Rank;

import java.util.Objects;

public class LinkCommand extends CommandObject {

    public LinkCommand(Discord discord) {
        super(discord);
    }

    @Override
    public CommandData getCommand() {
        return Commands.slash("link", "Setze deinen Tag von VALORANT, um deinen Rang zu verknüpfen.")
                .addOptions(new OptionData(OptionType.STRING, "tag", "Gib deinen VALORANT-Tag ein (z.B. User#ROFL)")
                        .setRequired(true));
    }

    @Override
    public void respond(SlashCommandInteractionEvent event) {

        if (event.getGuild() == null) {
            return;
        }

        OptionMapping tagOption = event.getOption("tag");

        if (tagOption == null) {
            return;
        }

        String tag = tagOption.getAsString();

        if (!tag.contains("#")) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":no_entry: | Fehler")
                    .setDescription("""
                            > **Es ist ein Fehler aufgetreten!**
                            > Dein Tag enthält kein `#`. (Ein Tag sieht so aus: **User#Tag**)""")
                    .build()).setEphemeral(true).queue();
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
                    try {
                        if (Objects.equals(player.getValorantTag(), tag)) {
                            interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                    .setTitle(":warning: | Hinweis")
                                    .setDescription("""
                                            > Du hast **bereits** diesen **Account verknüpft**.
                                            > Falls du einen neuen Account verknüpfen möchtest, gib den Namen des neuen Accounts ein.
    
                                            > Update den Rang des **verknüpften Accounts** mit `/updaterank`.""")
                                    .build()).queue();
                            return;
                        }

                        discord.getEvent().getRankAPI().getRank(tag).whenCompleteAsync((response, throwable1) -> {
                            if(throwable1 != null) {
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

                            interactionHook.editOriginalEmbeds(new EmbedBuilder()
                                    .setAuthor(member.getUser().getName(), null, member.getEffectiveAvatarUrl())
                                    .setTitle(":shield: | Rang")
                                    .setDescription("> Dein Account wurde **erfolgreich verknüpft**." +
                                            "\n\n> Account: **" + tag + "**" +
                                            "\n> Rank: **" + rank.getName() + "** " + rank.getEmoji())
                                    .build()).queue();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
    }
}
