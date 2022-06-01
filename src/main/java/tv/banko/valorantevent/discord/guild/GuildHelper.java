package tv.banko.valorantevent.discord.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import tv.banko.valorantevent.discord.Discord;

public record GuildHelper(Discord discord) {

    public Guild getGuild() {
        return discord.getBot().getGuildById("943836100282175508");
    }

    public Role getVerifiedRole() {
        return discord.getBot().getRoleById("944013373824172093");
    }

    public Role getRankSplitterRole() {
        return discord.getBot().getRoleById("944013217091448923");
    }

    public Role getTeamSplitterRole() {
        return discord.getBot().getRoleById("944014157303390218");
    }

    public String getTeamJoinLink() {
        return "https://discord.com/channels/943836100282175508/948949609307320430";
    }
}
