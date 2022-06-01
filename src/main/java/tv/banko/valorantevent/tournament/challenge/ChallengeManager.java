package tv.banko.valorantevent.tournament.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChallengeManager {

    public Challenges.WholeMatch getMatchChallenge() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Challenges.WholeMatch[] challenges = Challenges.WholeMatch.values();

        return challenges[random.nextInt(challenges.length)];
    }

    public List<Challenges.Round> getRoundChallenges() {
        List<Challenges.Round> list = new ArrayList<>();

        for (int round = 1; round <= 30; round++) {
            List<Challenges.Round> roundChallenges = Challenges.Round.getByRound(round);
            Collections.shuffle(roundChallenges);

            boolean success = false;

            for (Challenges.Round challenge : roundChallenges) {
                if (list.contains(challenge)) {
                    continue;
                }

                list.add(challenge);
                success = true;
                break;
            }

            if (success) {
                continue;
            }

            roundChallenges = Challenges.Round.getAllAndRound(round);
            Collections.shuffle(roundChallenges);

            for (Challenges.Round challenge : roundChallenges) {
                if (list.contains(challenge)) {
                    continue;
                }

                list.add(challenge);
                success = true;
                break;
            }

            if (success) {
                continue;
            }

            list.add(roundChallenges.get(0));
        }

        return list;
    }
}
