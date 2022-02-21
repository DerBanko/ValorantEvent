package tv.banko.valorantevent.database.collection;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import tv.banko.valorantevent.database.Database;
import tv.banko.valorantevent.tournament.player.Player;

import java.util.concurrent.CompletableFuture;

public record PlayerCollection(Database database) {

    public CompletableFuture<Player> getPlayer(String id) {
        CompletableFuture<Player> future = new CompletableFuture<>();
        getCollection().find(Filters.eq("id", id)).first((document, throwable) -> {
            if (throwable != null || document == null) {
                future.complete(null);
                return;
            }

            future.complete(new Player(database.getEvent().getDiscord(), document));
        });
        return future;
    }

    public void setPlayer(Player player) {
        getPlayer(player.getId()).whenCompleteAsync((t, throwable) -> {
            if (t == null) {
                getCollection().insertOne(player.toDocument(), (unused, throwable1) -> {
                });
                return;
            }

            getCollection().updateOne(Filters.eq("id", player.getId()),
                    new Document().append("$set", player.toDocument()), (unused, throwable1) -> {
                        if(throwable1 == null) {
                            return;
                        }
                        throwable1.printStackTrace();
                    });
        });
    }

    public void deletePlayer(Player player) {
        getCollection().deleteOne(Filters.eq("id", player.getId()), (unused, throwable1) -> {
        });
    }

    public CompletableFuture<Boolean> hasTeam(String id) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getPlayer(id).whenCompleteAsync((player, throwable) -> future.complete(player != null));

        return future;
    }

    public MongoCollection<Document> getCollection() {
        return database.getDatabase().getCollection("player");
    }

}
