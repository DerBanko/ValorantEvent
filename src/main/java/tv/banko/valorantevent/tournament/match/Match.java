package tv.banko.valorantevent.tournament.match;

import org.bson.Document;
import tv.banko.valorantevent.discord.channel.MatchChannel;
import tv.banko.valorantevent.tournament.Tournament;
import tv.banko.valorantevent.tournament.team.Team;
import tv.banko.valorantevent.util.GameId;

public class Match {

    private final Tournament tournament;
    private final GameId id;
    private final MatchChannel channel;

    private final Team team1;
    private final Team team2;

    private final MapVote mapVote;

    private int team1Wins;
    private int team1Points;

    private int team2Wins;
    private int team2Points;

    private Map map;
    private Team defender;
    private int rounds;

    public Match(Tournament tournament, GameId id, Team team1, Team team2) {
        this.tournament = tournament;

        this.id = id;
        this.team1 = team1;
        this.team2 = team2;

        this.mapVote = new MapVote(this);

        this.team1Wins = 0;
        this.team1Points = 0;

        this.team2Wins = 0;
        this.team2Points = 0;

        this.map = null;
        this.defender = null;
        this.rounds = 1;

        this.channel = new MatchChannel(tournament.getDiscord(), this);
    }

    public Match(Tournament tournament, Document document) {
        this.tournament = tournament;

        this.id = GameId.of(document.getString("id"));
        this.channel = new MatchChannel(tournament.getDiscord(), this, document.getString("channelId"));

        this.team1 = tournament.getTeam().getTeamById(document.getString("team1Id"));
        this.team2 = tournament.getTeam().getTeamById(document.getString("team2Id"));

        this.mapVote = new MapVote(this);

        this.team1Wins = document.getInteger("team1Wins");
        this.team1Points = document.getInteger("team1Points");

        this.team2Wins = document.getInteger("team2Wins");
        this.team2Points = document.getInteger("team2Points");

        this.map = document.getString("map") == null ? null : Map.valueOf(document.getString("map"));
        this.defender = document.getString("defender") == null ? null : tournament.getTeam()
                .getTeamById(document.getString("defender"));
        this.rounds = document.getInteger("rounds");

    }

    public Tournament getTournament() {
        return tournament;
    }

    public GameId getId() {
        return id;
    }

    public MatchChannel getChannel() {
        return channel;
    }

    public MapVote getMapVote() {
        return mapVote;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public int getTeam1Wins() {
        return team1Wins;
    }

    public void addTeam1Win() {
        this.team1Wins++;
        addTeam1Point();
    }

    public int getTeam1Points() {
        return team1Points;
    }

    public void addTeam1Point() {
        this.team1Points++;
    }

    public void removeTeam1Point() {
        this.team1Points--;
    }

    public int getTeam2Wins() {
        return team2Wins;
    }

    public void addTeam2Win() {
        this.team2Wins++;
        addTeam2Point();
    }

    public int getTeam2Points() {
        return team2Points;
    }

    public void addTeam2Point() {
        this.team2Points++;
    }

    public void removeTeam2Point() {
        this.team2Points--;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Team getDefender() {
        return defender;
    }

    public void setDefender(Team defender) {
        this.defender = defender;
    }

    public Team getAttacker() {
        return team1.equals(defender) ? team2 : team1;
    }

    public void changeDefender() {
        this.defender = defender.equals(team2) ? team1 : team2;
    }

    public int getRounds() {
        return rounds;
    }

    public void addRound() {
        this.rounds++;

        switch (this.rounds) {
            case 13, 27 -> changeDefender();
        }
    }

    public Document toDocument() {
        return new Document()
                .append("id", id.toString())
                .append("channelId", channel == null ? null : channel.getChannelId())
                .append("team1Id", team1.getId().toString())
                .append("team2Id", team2.getId().toString())
                .append("team1Wins", team1Wins)
                .append("team2Wins", team2Wins)
                .append("team1Points", team1Points)
                .append("team2Points", team2Points)
                .append("map", map == null ? null : map.name())
                .append("defender", defender == null ? null : defender.getId().toString())
                .append("rounds", rounds);
    }
}
