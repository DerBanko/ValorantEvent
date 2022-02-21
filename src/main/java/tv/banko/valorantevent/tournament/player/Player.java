package tv.banko.valorantevent.tournament.player;

import net.dv8tion.jda.api.entities.Guild;
import org.bson.Document;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.rank.Rank;

import java.util.Objects;

public class Player {

    private final Discord discord;
    private final String id;

    private String valorantTag;
    private Rank rank;

    public Player(Discord discord, String id) {
        this.discord = discord;
        this.id = id;
        this.rank = null;
        this.valorantTag = null;
    }

    public Player(Discord discord, Document document) {
        this.discord = discord;
        this.id = document.getString("id");
        this.valorantTag = document.getString("valorantTag");
        this.rank = document.getString("rank") == null ? null : Rank.valueOf(document.getString("rank"));
    }

    public String getId() {
        return id;
    }

    public String getValorantTag() {
        return valorantTag;
    }

    public void setValorantTag(String valorantTag) {
        this.valorantTag = valorantTag;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        if (Objects.equals(this.rank, rank)) {
            return;
        }

        Guild guild = discord.getBot().getGuildById(discord.getGuildId());

        if (guild == null) {
            this.rank = rank;
            save();
            return;
        }

        if (this.rank != null) {
            guild.getRolesByName(this.rank.getName(), true).stream().findFirst().ifPresent(role ->
                    guild.removeRoleFromMember(id, role).queue());
        }

        this.rank = rank;
        save();

        guild.getRolesByName(rank.getName(), true).stream().findFirst().ifPresent(role ->
                guild.addRoleToMember(id, role).queue());
    }

    public void save() {
        discord.getEvent().getDatabase().getPlayer().setPlayer(this);
    }

    public Document toDocument() {
        return new Document()
                .append("id", id)
                .append("valorantTag", valorantTag)
                .append("rank", rank == null ? null : rank.name());
    }
}
