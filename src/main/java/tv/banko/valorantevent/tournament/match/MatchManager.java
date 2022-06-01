package tv.banko.valorantevent.tournament.match;

import org.jetbrains.annotations.Nullable;
import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.tournament.team.Team;
import tv.banko.valorantevent.util.GameId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MatchManager {

    private final ValorantEvent event;
    private final List<Match> list;

    public MatchManager(ValorantEvent event) {
        this.event = event;

        this.list = new ArrayList<>();
        event.getDatabase().getMatch().loadMatches(this);
    }

    public CompletableFuture<Match> createMatch(Team team1, Team team2) {
        CompletableFuture<Match> future = new CompletableFuture<>();

        getRandom().whenCompleteAsync((gameId, throwable) -> {
            Match match = new Match(event.getTournament(), gameId, team1, team2);

            this.list.add(match);

            future.complete(match);
        });

        return future;
    }

    @Nullable
    public Match getMatchById(GameId gameId) {
        return list.stream().filter(match -> match.getId().equals(gameId)).findFirst().orElse(null);
    }

    @Nullable
    public Match getMatchById(String id) {
        return getMatchById(GameId.of(id));
    }

    @Nullable
    public Match getMatchByChannelId(String channelId) {
        return list.stream().filter(match -> Objects.equals(match.getChannel().getChannelId(), channelId))
                .findFirst().orElse(null);
    }

    @Nullable
    public Match getMatchByCommitteeId(String channelId) {
        return list.stream().filter(match -> Objects.equals(match.getCommittee().getChannelId(), channelId))
                .findFirst().orElse(null);
    }

    @Nullable
    public Match getMatchByTeam(Team team) {
        return list.stream().filter(match -> match.getTeam1().equals(team) || match.getTeam2().equals(team))
                .findFirst().orElse(null);
    }

    public void addMatch(Match match) {
        list.add(match);
    }

    private CompletableFuture<GameId> getRandom() {
        CompletableFuture<GameId> future = new CompletableFuture<>();

        AtomicReference<GameId> gameId = new AtomicReference<>(GameId.random());

        new Thread(() -> {
            AtomicBoolean existing = new AtomicBoolean(true);
            AtomicBoolean checkNew = new AtomicBoolean(true);

            while (existing.get()) {
                if (!checkNew.get()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                checkNew.set(false);

                event.getDatabase().getMatch().hasMatch(gameId.get()).whenCompleteAsync((exist, throwable) -> {
                    if (exist) {
                        gameId.set(GameId.random());
                        checkNew.set(false);
                        return;
                    }

                    existing.set(false);
                    future.complete(gameId.get());
                });
            }
        }).start();
        return future;
    }
}
