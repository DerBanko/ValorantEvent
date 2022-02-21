package tv.banko.valorantevent.database.collection;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import tv.banko.valorantevent.database.Database;
import tv.banko.valorantevent.tournament.team.Team;
import tv.banko.valorantevent.tournament.team.TeamManager;
import tv.banko.valorantevent.util.GameId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record TeamCollection(Database database) {

    public CompletableFuture<Team> getTeam(GameId gameId) {
        CompletableFuture<Team> future = new CompletableFuture<>();
        getCollection().find(Filters.eq("id", gameId.toString())).first((document, throwable) -> {
            if (throwable == null) {
                future.complete(null);
                return;
            }

            future.complete(new Team(database.getEvent(), document));
        });
        return future;
    }

    public void loadTeams(TeamManager manager) {
        getCollection().find().forEach(document -> {
            manager.addTeam(new Team(database.getEvent(), document));
        }, (unused, throwable) -> {
        });
    }

    public void setTeam(Team team) {
        getTeam(team.getId()).whenCompleteAsync((t, throwable) -> {
            if (t == null) {
                getCollection().insertOne(team.toDocument(), (unused, throwable1) -> {
                });
                return;
            }

            getCollection().updateOne(Filters.eq("id", team.getId().toString()),
                    new Document().append("$set", team.toDocument()), (unused, throwable1) -> {
                    });
        });
    }

    public void deleteTeam(Team team) {
        getCollection().deleteOne(Filters.eq("id", team.getId().toString()), (unused, throwable1) -> {
        });
    }

    public CompletableFuture<Boolean> hasTeam(GameId gameId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        getTeam(gameId).whenCompleteAsync((team, throwable) -> future.complete(team != null));

        return future;
    }

    public MongoCollection<Document> getCollection() {
        return database.getDatabase().getCollection("team");
    }

}
