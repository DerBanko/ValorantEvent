package tv.banko.valorantevent.discord.channel;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.match.Match;

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

        Guild guild = discord.getBot().getGuildById(discord.getGuildId());

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

    private void findCategory() {

        System.out.println("2");

        Guild guild = discord.getBot().getGuildById(discord.getGuildId());

        if (guild == null) {
            System.out.println("3");
            return;
        }

        System.out.println("4");

        String categoryName = "\uD83C\uDFB3 | Matches";

        Category category = guild.getCategoriesByName(categoryName, true)
                .stream().findFirst().orElse(null);

        if (category == null) {
            System.out.println("5");
            guild.createCategory(categoryName).queue(this::createChannel);
            return;
        }

        System.out.println("6");

        createChannel(category);
    }

    private void createChannel(Category category) {
        System.out.println("7");

        Role publicRole = category.getGuild().getPublicRole();

        System.out.println("7,5");

        category.createTextChannel("match-" + match.getId().toString())
                .addPermissionOverride(publicRole, List.of(Permission.MESSAGE_SEND), List.of(Permission.VIEW_CHANNEL))
                .addRolePermissionOverride(match.getTeam1().getRole().getId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                .addRolePermissionOverride(match.getTeam2().getRole().getId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                .queue(textChannel -> channelId = textChannel.getId(), Throwable::printStackTrace);

        System.out.println("8");
    }

}
