package tv.banko.valorantevent.util;

import java.util.UUID;

public class GameId {

    public static GameId of(String s) {
        return new GameId(s);
    }

    public static GameId random() {
        return new GameId(UUID.randomUUID().toString().split("-")[0]);
    }

    private final String s;

    public GameId(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s;
    }
}
