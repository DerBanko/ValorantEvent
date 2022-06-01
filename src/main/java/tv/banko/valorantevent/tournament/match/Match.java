package tv.banko.valorantevent.tournament.match;

import org.bson.Document;
import tv.banko.valorantevent.discord.channel.CommitteeChannel;
import tv.banko.valorantevent.discord.channel.MatchChannel;
import tv.banko.valorantevent.tournament.Tournament;
import tv.banko.valorantevent.tournament.challenge.Challenges;
import tv.banko.valorantevent.tournament.team.Team;
import tv.banko.valorantevent.util.GameId;

import java.util.ArrayList;
import java.util.List;

public class Match {

    private final Tournament tournament;
    private final GameId id;
    private final MatchChannel channel;
    private final CommitteeChannel committee;

    private final Team team1;
    private final Team team2;

    private final MapVote mapVote;

    private final MatchPoints team1Points;
    private final MatchPoints team2Points;

    private final List<Challenges.Round> challenges;

    private GameMap gameMap;
    private Team defender;
    private int rounds;

    private boolean finished;

    public Match(Tournament tournament, GameId id, Team team1, Team team2) {
        this.tournament = tournament;

        this.id = id;
        this.team1 = team1;
        this.team2 = team2;

        this.mapVote = new MapVote(this);

        this.team1Points = new MatchPoints();
        this.team2Points = new MatchPoints();

        this.challenges = tournament.getChallenge().getRoundChallenges();

        this.gameMap = null;
        this.defender = null;
        this.rounds = 1;
        this.finished = false;

        this.channel = new MatchChannel(tournament.getDiscord(), this);
        this.committee = new CommitteeChannel(tournament.getDiscord(), this);
    }

    public Match(Tournament tournament, Document document) {
        this.tournament = tournament;

        this.id = GameId.of(document.getString("id"));
        this.channel = new MatchChannel(tournament.getDiscord(), this, document.getString("channelId"));
        this.committee = new CommitteeChannel(tournament.getDiscord(), this, document.getString("committeeId"));

        this.team1 = tournament.getTeam().getTeamById(document.getString("team1Id"));
        this.team2 = tournament.getTeam().getTeamById(document.getString("team2Id"));

        this.mapVote = new MapVote(this);

        this.team1Points = new MatchPoints(document.getString("team1Challenges"), document.getString("team1Wins"));
        this.team2Points = new MatchPoints(document.getString("team2Challenges"), document.getString("team2Wins"));

        this.challenges = new ArrayList<>();

        for (String s : document.getString("challenges").split(", ")) {
            this.challenges.add(Challenges.Round.valueOf(s));
        }

        this.gameMap = document.getString("map") == null ? null : GameMap.valueOf(document.getString("map"));
        this.defender = document.getString("defender") == null ? null : tournament.getTeam()
                .getTeamById(document.getString("defender"));
        this.rounds = document.getInteger("rounds");
        this.finished = document.getBoolean("finished");

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

    public CommitteeChannel getCommittee() {
        return committee;
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

    public MatchPoints getTeam1Points() {
        return team1Points;
    }

    public MatchPoints getTeam2Points() {
        return team2Points;
    }

    public Challenges.Round getRoundChallenge(int round) {
        if (challenges.size() <= (round - 1)) {
            return null;
        }

        return challenges.get(round - 1);
    }

    public GameMap getMap() {
        return gameMap;
    }

    public void setMap(GameMap gameMap) {
        this.gameMap = gameMap;
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

        int end = checkForEnd();

        if (end == 0) {
            return;
        }

        this.finished = true;

        channel.sendMatchEnd(end);
        committee.getMessage().deleteMatch();
    }

    public boolean isFinished() {
        return finished;
    }

    public Document toDocument() {
        return new Document()
                .append("id", id.toString())
                .append("channelId", channel == null ? null : channel.getChannelId())
                .append("committeeId", committee == null ? null : committee.getChannelId())
                .append("team1Id", team1.getId().toString())
                .append("team2Id", team2.getId().toString())
                .append("team1Wins", team1Points.getWinsAsString())
                .append("team2Wins", team2Points.getWinsAsString())
                .append("team1Challenges", team1Points.getChallengesAsString())
                .append("team2Challenges", getChallengesAsString())
                .append("challenges", team2Points.getChallengesAsString())
                .append("map", gameMap == null ? null : gameMap.name())
                .append("defender", defender == null ? null : defender.getId().toString())
                .append("rounds", rounds)
                .append("finished", finished);
    }

    private String getChallengesAsString() {
        StringBuilder builder = new StringBuilder();

        for (Challenges.Round challenge : challenges) {
            if (!builder.isEmpty()) {
                builder.append(", ");
            }

            builder.append(challenge.name());
        }

        return builder.toString();
    }

    private int checkForEnd() {
        int wins1 = getTeam1Points().getWins();
        int wins2 = getTeam2Points().getWins();

        int points1 = getTeam1Points().getPoints();
        int points2 = getTeam1Points().getPoints();

        if (rounds <= 24) {
            return checkForEnd0(13, wins1, wins2, points1, points2);
        }

        if (rounds <= 26) {
            return checkForEnd0(15, wins1, wins2, points1, points2);
        }

        if (rounds <= 28) {
            return checkForEnd0(17, wins1, wins2, points1, points2);
        }

        if (rounds <= 30) {
            return checkForEnd0(19, wins1, wins2, points1, points2);
        }

        return -1;
    }

    private int checkForEnd0(int requiredWins, int wins1, int wins2, int points1, int points2) {
        if (wins1 == requiredWins || wins2 == requiredWins) {
            if (points1 > points2) {
                return 1;
            }

            if (points2 > points1) {
                return 2;
            }

            return wins1 == requiredWins ? 1 : 2;
        }

        return 0;
    }
}
