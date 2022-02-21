package tv.banko.valorantevent.database;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.database.collection.MatchCollection;
import tv.banko.valorantevent.database.collection.PlayerCollection;
import tv.banko.valorantevent.database.collection.TeamCollection;

public class Database {

    private final ValorantEvent event;

    private final MongoClient client;
    private final MongoDatabase database;

    private final TeamCollection team;
    private final MatchCollection match;
    private final PlayerCollection player;

    public Database(ValorantEvent event) {
        this.event = event;
        this.client = MongoClients.create("mongodb://mongodb:27017");

        this.database = client.getDatabase("event");

        this.team = new TeamCollection(this);
        this.match = new MatchCollection(this);
        this.player = new PlayerCollection(this);
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public TeamCollection getTeam() {
        return team;
    }

    public MatchCollection getMatch() {
        return match;
    }

    public PlayerCollection getPlayer() {
        return player;
    }

    public ValorantEvent getEvent() {
        return event;
    }
}
