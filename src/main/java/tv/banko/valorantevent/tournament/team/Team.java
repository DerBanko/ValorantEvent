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
    private final Collection<String> invitations;
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
        this.invitations = new ArrayList<>();
        this.role = new TeamRole(event.getDiscord(), this);
        this.channel = new TeamChannel(event.getDiscord(), this);

        calculateTeamRank();
        save();
    }

    public Team(ValorantEvent event, Document document) {
        this.event = event;
        this.id = GameId.of(document.getString("id"));

        this.captain = document.getString("captain");

        this.name = document.getString("name");

        this.players = new ArrayList<>(Arrays.asList(document.getString("players").split(",")));
        this.invitations = new ArrayList<>(Arrays.asList(document.getString("invitations").split(",")));
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
        calculateTeamRank();
        save();
    }

    public void removePlayer(String id) {
        players.remove(id);
        role.removeRole(id);
        calculateTeamRank();
        save();
    }

    public boolean isPlayer(String id) {
        return players.contains(id);
    }

    public void addInvite(String id) {
        if (invitations.contains(id)) {
            return;
        }
        invitations.add(id);
        save();
    }

    public void removeInvite(String id) {
        invitations.remove(id);
        save();
    }

    public boolean isInvited(String id) {
        return invitations.contains(id);
    }

    public Rank getTeamRank() {
        return rank;
    }

    public String getCaptain() {
        return captain;
    }

    public void setCaptain(String captain) {
        this.captain = captain;
        save();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        channel.getText().getManager().setName(name).queue();
        channel.getVoice().getManager().setName(name).queue();
        role.editRoleName(name);
        save();
    }

    public void save() {
        event.getDatabase().getTeam().setTeam(this);
    }

    public void calculateTeamRank() {
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

    }

    public Document toDocument() {
        StringBuilder players = new StringBuilder();

        for (String player : this.players) {

            if (!players.isEmpty()) {
                players.append(",");
            }

            players.append(player);
        }

        StringBuilder invitations = new StringBuilder();

        for (String invitation : this.invitations) {

            if (!invitations.isEmpty()) {
                invitations.append(",");
            }

            invitations.append(invitation);
        }

        return new Document()
                .append("id", id.toString())
                .append("name", name)
                .append("players", players.toString())
                .append("invitations", invitations.toString())
                .append("captain", captain)
                .append("roleId", role == null ? null : role.getRoleId())
                .append("voiceId", channel == null ? null : channel.getVoiceId())
                .append("textId", channel == null ? null : channel.getTextId());
    }
}
