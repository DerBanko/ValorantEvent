package tv.banko.valorantevent.tournament.team;

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.util.GameId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TeamManager {

    private final ValorantEvent event;
    private final List<Team> list;

    public TeamManager(ValorantEvent event) {
        this.event = event;

        this.list = new ArrayList<>();
        event.getDatabase().getTeam().loadTeams(this);
    }

    public CompletableFuture<Team> createTeam() {
        CompletableFuture<Team> future = new CompletableFuture<>();

        getRandom().whenCompleteAsync((gameId, throwable) -> {
            Team team = new Team(event, gameId);

            this.list.add(team);

            future.complete(team);
        });
        return future;
    }

    @Nullable
    public Team getTeamById(GameId gameId) {
        return list.stream().filter(team -> team.getId().equals(gameId)).findFirst().orElse(null);
    }

    @Nullable
    public Team getTeamById(String id) {
        return getTeamById(GameId.of(id));
    }

    @Nullable
    public Team getTeamByPlayer(Member member) {
        return list.stream().filter(team -> team.isPlayer(member.getId())).findFirst().orElse(null);
    }

    public List<Team> getTeamsByInvite(Member member) {
        return list.stream().filter(team -> team.isInvited(member.getId())).toList();
    }

    @Nullable
    public Team getTeamByPlayer(String id) {
        return list.stream().filter(team -> team.isPlayer(id)).findFirst().orElse(null);
    }

    public void addTeam(Team team) {
        if (list.contains(team)) {
            return;
        }

        list.add(team);
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

                event.getDatabase().getTeam().hasTeam(gameId.get()).whenCompleteAsync((exist, throwable) -> {
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
