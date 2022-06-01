package tv.banko.valorantevent.util;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameId gameId = (GameId) o;
        return Objects.equals(s, gameId.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(s);
    }
}
