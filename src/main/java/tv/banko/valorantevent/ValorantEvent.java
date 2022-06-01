package tv.banko.valorantevent;

import tv.banko.valorantevent.api.RankAPI;
import tv.banko.valorantevent.database.Database;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.Tournament;

import javax.security.auth.login.LoginException;

public class ValorantEvent {

    private final Database database;
    private final RankAPI rankAPI;

    private Discord discord;
    private Tournament tournament;

    public ValorantEvent() {

        this.database = new Database(this);
        this.rankAPI = new RankAPI();

        try {
            this.discord = new Discord(this);
            this.tournament = new Tournament(this);

            this.tournament.load();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public RankAPI getRankAPI() {
        return rankAPI;
    }

    public Discord getDiscord() {
        return discord;
    }

    public Database getDatabase() {
        return database;
    }

    public Tournament getTournament() {
        return tournament;
    }
}
