package tv.banko.valorantevent.database.collection;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import tv.banko.valorantevent.database.Database;
import tv.banko.valorantevent.tournament.match.Match;
import tv.banko.valorantevent.tournament.match.MatchManager;
import tv.banko.valorantevent.util.GameId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record MatchCollection(Database database) {

    public CompletableFuture<Match> getMatchList(GameId gameId) {
        CompletableFuture<Match> future = new CompletableFuture<>();
        getCollection().find(Filters.eq("id", gameId.toString())).first((document, throwable) -> {
            if (throwable == null) {
                future.complete(null);
                return;
            }

            future.complete(new Match(database.getEvent().getTournament(), document));
        });
        return future;
    }

    public void loadMatches(MatchManager manager) {
        getCollection().find().forEach(document -> {
            manager.addMatch(new Match(database.getEvent().getTournament(), document));
        }, (unused, throwable) -> {
        });
    }

    public void setMatch(Match match) {
        getMatchList(match.getId()).whenCompleteAsync((t, throwable) -> {
            if (t == null) {
                getCollection().insertOne(match.toDocument(), (unused, throwable1) -> {
                });
                return;
            }

            getCollection().updateOne(Filters.eq("id", match.getId().toString()),
                    new Document().append("$set", match.toDocument()), (unused, throwable1) -> {
                    });
        });
    }

    public void deleteMatch(Match match) {
        getCollection().deleteOne(Filters.eq("id", match.getId().toString()), (unused, throwable) -> {
        });
    }

    public CompletableFuture<Boolean> hasMatch(GameId gameId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getMatchList(gameId).whenCompleteAsync((match, throwable) -> future.complete(match != null));

        return future;
    }

    public MongoCollection<Document> getCollection() {
        return database.getDatabase().getCollection("match");
    }

}
