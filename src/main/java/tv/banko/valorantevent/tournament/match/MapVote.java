package tv.banko.valorantevent.tournament.match;

import org.jetbrains.annotations.Nullable;
import tv.banko.valorantevent.discord.message.MapVoteMessage;
import tv.banko.valorantevent.tournament.team.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MapVote {

    private final Match match;
    private final MapVoteMessage message;

    private final List<Map> maps;
    private Phase phase;

    private int team1Number;
    private int team2Number;

    private Team turn;

    public MapVote(Match match) {
        this.match = match;
        this.message = new MapVoteMessage(match);

        this.maps = new ArrayList<>(Arrays.asList(Map.values()));
        this.phase = Phase.HIGHER_RANDOM_NUMBER_WINS;

        this.team1Number = -1;
        this.team2Number = -1;

        this.turn = null;
    }

    public Match getMatch() {
        return match;
    }

    public MapVoteMessage getMessage() {
        return message;
    }

    public Phase getPhase() {
        return phase;
    }

    public List<Map> getMaps() {
        return maps;
    }

    public void setNextPhase() {
        this.phase = this.phase.getNext();
    }

    public Team getNumberWinner() {
        return team1Number > team2Number ? match.getTeam1() : match.getTeam2();
    }

    public int getTeam1Number() {
        if (team1Number != -1) {
            return team1Number;
        }

        int i = ThreadLocalRandom.current().nextInt(100);

        if (team2Number == i) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                i += 1;
            } else {
                i -= 1;
            }
        }

        team1Number = i;
        return team1Number;
    }

    public boolean hasTeam1Number() {
        return team1Number != -1;
    }

    public int getTeam2Number() {
        if (team2Number != -1) {
            return team2Number;
        }

        int i = ThreadLocalRandom.current().nextInt(100);

        if (team1Number == i) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                i += 1;
            } else {
                i -= 1;
            }
        }

        team2Number = i;
        return team2Number;
    }

    public boolean hasTeam2Number() {
        return team2Number != -1;
    }

    public Team getTurn() {
        return turn;
    }

    public void setTurn(Team turn) {
        this.turn = turn;
    }

    public void changeTurn() {
        this.turn = turn.equals(match.getTeam1()) ? match.getTeam2() : match.getTeam1();
    }

    public void banMaps(Map... maps) {
        this.maps.removeAll(Arrays.asList(maps));
    }

    public void setSide(Team team, boolean defending) {
        if (!turn.equals(team)) {
            return;
        }

        this.match.setDefender(defending ? team : team.equals(match.getTeam1()) ? match.getTeam2() : match.getTeam1());
    }

    public Map getMap() {
        if (maps.size() != 1) {
            return null;
        }

        return maps.get(0);
    }

    public enum Phase {

        /* Every team leader draws a random number, higher number wins */
        HIGHER_RANDOM_NUMBER_WINS,

        /* Winner of higher number chooses if they want to ban first or choose side later */
        WINNER_CHOOSES,

        /* Team 1 bans 2 maps */
        TEAM_1_BANS,

        /* Team 2 bans 2 maps */
        TEAM_2_BANS,

        /* Team 1 chooses play map of 3 maps */
        TEAM_1_CHOOSES_MAP,

        /* Team 2 decides whether they start on defender or attacker side */
        TEAM_2_CHOOSES_STARTING_SIDE;

        @Nullable
        public Phase getNext() {
            return switch (this) {
                case HIGHER_RANDOM_NUMBER_WINS -> WINNER_CHOOSES;
                case WINNER_CHOOSES -> TEAM_1_BANS;
                case TEAM_1_BANS -> TEAM_2_BANS;
                case TEAM_2_BANS -> TEAM_1_CHOOSES_MAP;
                case TEAM_1_CHOOSES_MAP -> TEAM_2_CHOOSES_STARTING_SIDE;
                default -> null;
            };
        }

    }

}
