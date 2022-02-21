package tv.banko.valorantevent.discord.channel;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.team.Team;

import java.util.Collections;
import java.util.List;

public class TeamChannel {

    private final Discord discord;
    private final Team team;

    private String textId;
    private String voiceId;

    public TeamChannel(Discord discord, Team team) {
        this.discord = discord;
        this.team = team;

        new Thread(() -> {
            while (team.getRole().getRoleId() == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            findCategory();
        }).start();
    }

    public TeamChannel(Discord discord, Team team, String textId, String voiceId) {
        this.discord = discord;
        this.team = team;
        this.textId = textId;
        this.voiceId = voiceId;
    }

    public TextChannel getText() {

        Guild guild = discord.getBot().getGuildById(discord.getGuildId());

        if (guild == null) {
            return null;
        }

        if (textId == null) {
            return null;
        }

        return guild.getTextChannelById(textId);
    }

    public VoiceChannel getVoice() {

        Guild guild = discord.getBot().getGuildById(discord.getGuildId());

        if (guild == null) {
            return null;
        }

        if (voiceId == null) {
            return null;
        }

        return guild.getVoiceChannelById(voiceId);
    }

    public String getTextId() {
        return textId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    private void findCategory() {

        Guild guild = discord.getBot().getGuildById(discord.getGuildId());

        if (guild == null) {
            return;
        }

        String categoryName = "\uD83D\uDC8C | Teams";

        Category category = guild.getCategoriesByName(categoryName, true)
                .stream().findFirst().orElse(null);

        if (category == null) {
            guild.createCategory(categoryName).queue(this::createChannels);
            return;
        }

        createChannels(category);
    }

    private void createChannels(Category category) {
        Role publicRole = category.getGuild().getPublicRole();

        category.createTextChannel(team.getName())
                .addPermissionOverride(publicRole, List.of(Permission.MESSAGE_SEND), List.of(Permission.VIEW_CHANNEL))
                .addRolePermissionOverride(team.getRole().getId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                .queue(textChannel -> this.textId = textChannel.getId());

        category.createVoiceChannel(team.getName())
                .addPermissionOverride(publicRole, List.of(Permission.VOICE_CONNECT), List.of(Permission.VIEW_CHANNEL))
                .addRolePermissionOverride(team.getRole().getId(), List.of(Permission.VIEW_CHANNEL), Collections.emptyList())
                .setBitrate(category.getGuild().getMaxBitrate())
                .queue(voiceChannel -> this.voiceId = voiceChannel.getId());
    }

}
