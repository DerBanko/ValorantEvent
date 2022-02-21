package tv.banko.valorantevent.tournament.team;

import org.bson.Document;
import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.discord.channel.TeamChannel;
import tv.banko.valorantevent.discord.role.TeamRole;
import tv.banko.valorantevent.tournament.player.Player;
import tv.banko.valorantevent.tournament.rank.Rank;
import tv.banko.valorantevent.util.GameId;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Team {

    private final ValorantEvent event;

    private final GameId id;

    private final Collection<String> players;
    private final TeamRole role;
    private final TeamChannel channel;

    private Rank rank;
    private String captain;

    private String name;

    public Team(ValorantEvent event, GameId id) {
        this.event = event;
        this.id = id;

        this.captain = null;

        this.name = "Team-" + id;

        this.players = new ArrayList<>();
        this.role = new TeamRole(event.getDiscord(), this);
        this.channel = new TeamChannel(event.getDiscord(), this);

        calculateTeamRank();
    }

    public Team(ValorantEvent event, Document document) {
        this.event = event;
        this.id = GameId.of(document.getString("id"));

        this.captain = document.getString("captain");

        this.name = document.getString("name");

        this.players = new ArrayList<>(Arrays.asList(document.getString("id").split(",")));
        this.role = new TeamRole(event.getDiscord(), this, document.getString("roleId"));
        this.channel = new TeamChannel(event.getDiscord(), this, document.getString("textId"),
                document.getString("voiceId"));

        calculateTeamRank();
    }

    public GameId getId() {
        return id;
    }

    public Collection<String> getPlayers() {
        return players;
    }

    public TeamRole getRole() {
        return role;
    }

    public void addPlayer(String id) {
        if (players.contains(id)) {
            return;
        }

        players.add(id);
        role.setRole(id);
    }

    public void removePlayer(String id) {
        players.remove(id);
        role.removeRole(id);
    }

    public boolean isPlayer(String id) {
        return players.contains(id);
    }

    public Rank getTeamRank() {
        return rank;
    }

    public String getCaptain() {
        return captain;
    }

    public void setCaptain(String captain) {
        this.captain = captain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void save() {
        event.getDatabase().getTeam().setTeam(this);
    }

    public CompletableFuture<Rank> calculateTeamRank() {
        CompletableFuture<Rank> future = new CompletableFuture<>();

        List<Player> list = new ArrayList<>();

        for (String id : this.players) {
            this.event.getTournament().getPlayer().getPlayer(id).whenCompleteAsync((p, throwable) -> {
                if (future.isDone()) {
                    return;
                }

                list.add(p);

                if (p.getRank() == null) {
                    future.complete(null);
                    return;
                }

                if (list.size() != this.players.size()) {
                    return;
                }

                Optional<Player> bestPlayerOptional = list.stream().max(Comparator.comparingInt(value -> value.getRank().getId()));

                if (bestPlayerOptional.isEmpty()) {
                    future.complete(null);
                    return;
                }

                Player bestPlayer = bestPlayerOptional.get();

                int highRank = bestPlayer.getRank().getId();

                int difCalc = 0;

                for (Player player : list) {
                    difCalc += highRank - player.getRank().getId();
                }

                difCalc /= 5;

                int rankId = highRank - difCalc;

                if (rankId < 3) {
                    rankId = 3;
                }

                Rank rank = Rank.byId(rankId);

                this.rank = rank;
                future.complete(rank);
            });
        }

        return future;
    }

    public Document toDocument() {
        StringBuilder players = new StringBuilder();

        for (String player : this.players) {

            if (!players.isEmpty()) {
                players.append(",");
            }

            players.append(player);
        }

        return new Document()
                .append("id", id.toString())
                .append("name", name)
                .append("players", players)
                .append("captain", captain)
                .append("roleId", role == null ? null : role.getRoleId())
                .append("voiceId", channel == null ? null : channel.getVoiceId())
                .append("textId", channel == null ? null : channel.getTextId());
    }
}
