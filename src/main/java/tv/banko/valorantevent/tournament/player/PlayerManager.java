package tv.banko.valorantevent.tournament.player;

import tv.banko.valorantevent.ValorantEvent;

import java.util.concurrent.CompletableFuture;

public record PlayerManager(ValorantEvent event) {
    public CompletableFuture<Player> getPlayer(String id) {
        CompletableFuture<Player> future = new CompletableFuture<>();

        event.getDatabase().getPlayer().getPlayer(id).whenCompleteAsync((player, throwable) -> {
            if (player == null) {
                future.complete(new Player(event.getDiscord(), id));
                return;
            }

            future.complete(player);
        });
        return future;
    }
}
