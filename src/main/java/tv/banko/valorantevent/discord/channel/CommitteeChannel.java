package tv.banko.valorantevent.discord.channel;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.discord.message.CommitteeMessage;
import tv.banko.valorantevent.tournament.match.Match;

public class CommitteeChannel {

    private final Discord discord;
    private final Match match;
    private final CommitteeMessage message;

    private String channelId;

    public CommitteeChannel(Discord discord, Match match) {
        this.discord = discord;
        this.match = match;
        this.message = new CommitteeMessage(match);

        findCategory();
    }

    public CommitteeChannel(Discord discord, Match match, String channelId) {
        this.discord = discord;
        this.match = match;
        this.channelId = channelId;
        this.message = new CommitteeMessage(match);
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

    public CommitteeMessage getMessage() {
        return message;
    }

    private void findCategory() {

        Guild guild = discord.getGuildHelper().getGuild();

        if (guild == null) {
            return;
        }

        String categoryName = "\uD83D\uDC6A | Committee";

        Category category = guild.getCategoriesByName(categoryName, true)
                .stream().findFirst().orElse(null);

        if (category == null) {
            guild.createCategory(categoryName).queue(this::createChannel);
            return;
        }

        createChannel(category);
    }

    private void createChannel(Category category) {
        category.createTextChannel("committee-" + match.getId().toString())
                .queue(textChannel -> {
                    channelId = textChannel.getId();
                    message.startMapVote();
                }, Throwable::printStackTrace);
    }

}
