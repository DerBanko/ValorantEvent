package tv.banko.valorantevent.tournament.match;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchPoints {

    private final Map<Integer, Boolean> challengeSuccess;
    private final Map<Integer, Boolean> roundWon;

    public MatchPoints() {
        this.challengeSuccess = new HashMap<>();
        this.roundWon = new HashMap<>();
    }

    public MatchPoints(String challenges, String wins) {
        this.challengeSuccess = new HashMap<>();
        this.roundWon = new HashMap<>();

        for(String round : challenges.split(";")) {
            String[] array = round.split(":");

            int roundId = Integer.parseInt(array[0]);
            boolean b = Boolean.parseBoolean(array[1]);

            this.challengeSuccess.put(roundId, b);
        }

        for(String round : wins.split(";")) {
            String[] array = round.split(":");

            int roundId = Integer.parseInt(array[0]);
            boolean b = Boolean.parseBoolean(array[1]);

            this.roundWon.put(roundId, b);
        }
    }

    public void setChallengeSuccess(int round) {
        if(round <= 0) {
            throw new IllegalArgumentException("round <= 0 (" + round + ")");
        }

        this.challengeSuccess.put(round, true);
    }

    public void setChallengeFailure(int round) {
        if(round <= 0) {
            throw new IllegalArgumentException("round <= 0 (" + round + ")");
        }

        this.challengeSuccess.put(round, false);
    }

    public void setRoundWon(int round) {
        if(round <= 0) {
            throw new IllegalArgumentException("round <= 0 (" + round + ")");
        }

        this.roundWon.put(round, true);
    }

    public void setRoundLost(int round) {
        if(round <= 0) {
            throw new IllegalArgumentException("round <= 0 (" + round + ")");
        }

        this.roundWon.put(round, false);
    }

    public int getPoints() {
        int points = 0;

        for (int round = 1; round <= 30; round++) {

            if(!this.challengeSuccess.containsKey(round) &&!
                this.roundWon.containsKey(round)) {
                break;
            }

            boolean challenge = this.challengeSuccess.get(round);
            boolean win = this.roundWon.get(round);

            if(challenge && win) {
                points += 2;
            } else if(challenge) {
                points++;
            } else {
                points--;
            }
        }

        return points;
    }

    public int getWins() {
        AtomicInteger wins = new AtomicInteger();

        this.roundWon.forEach((integer, won) -> {
            if(!won) {
                return;
            }

            wins.getAndIncrement();
        });

        return wins.get();
    }

    public Map<Integer, Boolean> getRoundWon() {
        return roundWon;
    }

    public int getChallenges() {
        AtomicInteger challenges = new AtomicInteger();

        this.challengeSuccess.forEach((integer, won) -> {
            if(!won) {
                return;
            }

            challenges.getAndIncrement();
        });

        return challenges.get();
    }

    public Map<Integer, Boolean> getChallengeSuccess() {
        return challengeSuccess;
    }

    public String getChallengesAsString() {
        StringBuilder builder = new StringBuilder();

        this.challengeSuccess.forEach((integer, success) -> {
            if (!builder.isEmpty()) {
                builder.append(";");
            }
            builder.append(integer).append(":").append(success);
        });

        return builder.toString();
    }

    public String getWinsAsString() {
        StringBuilder builder = new StringBuilder();

        this.roundWon.forEach((integer, won) -> {
            if (!builder.isEmpty()) {
                builder.append(";");
            }
            builder.append(integer).append(":").append(won);
        });

        return builder.toString();
    }

    public boolean hasRound(int roundId) {
        return challengeSuccess.containsKey(roundId) && roundWon.containsKey(roundId);
    }
}