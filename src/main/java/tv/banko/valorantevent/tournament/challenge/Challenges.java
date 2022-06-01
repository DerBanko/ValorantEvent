package tv.banko.valorantevent.tournament.challenge;

import org.jetbrains.annotations.Nullable;
import tv.banko.valorantevent.tournament.match.Match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Challenges {

    public enum Round {
        NONE(Challenge.from("keine Challenge", null, null, null, "keine")),
        KNIFE(Challenge.from("Knife", "nur Knife", "keine",
                false, null, 1, 13)),
        KNIFE_PLUS(Challenge.from("Knife+",
                "nur Knife",
                "nicht-schadende Fähigkeiten *(Smokes, Aufdeckungspfeil, Aufdeckungsdrohne, Alarm Bot, " +
                        "Wall, Sage Ult, Cypher Ult, Suck, TP, Movement, Fake Steps, Flashes, ...)*", null, null)),
        ARES(Challenge.from("Ares", "nur Ares (keine Pistolen)", "keine",
                null, null)),
        SNIPER(Challenge.from("Sniper", "nur Operator, Marshall (keine Pistolen)",
                "nur Chamber Ultimate", false, null)),
        NO_WEAPON_PICKUP(Challenge.from("Keine Waffen aufheben", "nur Waffen, welche beim Ablauf der Kaufphase im " +
                        "Inventar waren", null, null,
                "Es dürfen keine Waffen aufgehoben werden (auswechseln auch verboten). " +
                        "Falls eine Waffe aufgehoben wurde, darf sie **nicht verwendet** UND **nicht** in die " +
                        "**nächste Runde mitgenommen** werden.")),
        SPOT(Challenge.from("Spot", null, null, null, "Es wird ein Spot festgelegt.")),
        CROUCH_ONLY(Challenge.from("Nur Crouchen", null, null, null,
                "Nach Ablauf der Kaufphase darf nur noch gecroucht werden. (Ausgenommen beim Stehen)")),
        PLANT_SPIKE_AND_GO(Challenge.from("Spike platzieren und Go!", null,
                "Angreifer: keine, Verteidiger: alle", null, "Alle Defender bleiben in ihrem Spawn stehen und dürfen erst loslaufen, " +
                        "wenn die Spike platziert wurde. In der Zeit vor dem Plant darf nicht geschossen werden. " +
                        "Jeder Angreifer muss beim Planten auf dem Spot stehen, auf welchem geplantet wird.")),
        ONE_VS_ONE(Challenge.from("1vs1", null, null, null,
                "Der Match-Timer wird pausiert. Alle Spieler bleiben ihrem Spawn. Die Reihenfolge der 1vs1s ist " +
                        "die Reihenfolge auf dem Scoreboard (von oben nach unten, Tote ausgenommen). Der Kampf findet immer " +
                        "in der Mitte statt. Die Person, die aus dem 1vs1 überlebt, geht zurück an ihren Spawn. Dies läuft " +
                        "solange bis alle Spieler eines Teams gestorben sind.")),
        BURSTDOG(Challenge.from("Burstdog", "nur Bulldog (keine Pistolen)", "keine",
                null, "Bulldog darf nur im Scope benutzt werden.")),
        UNBIND_W(Challenge.from("W verboten", null, null,
                null, "Bewegen ist nur mit ASD erlaubt (seitlich und rückwärts).")),
        BUCKYS_BUNNY(Challenge.from("Bucky's Bunny", "nur Bucky (keine Pistolen)", null,
                null, "Es muss DAUERHAFT gesprungen werden (ausgenommen Plant, Defuse und Fähigkeiten, " +
                        "die Springen unterbrechen)")),
        SMOCKY_GHOST(Challenge.from("Smocky Ghost", "nur Ghost", "nur Smokes",
                null, null, 1, 2, 3, 4, 5, 6, 13, 14, 15, 16, 17, 18)),
        POLONAISE(Challenge.from("Polonaise", null, null,
                null, "Alle Spieler eines Teams müssen in einer Schlange laufen")),
        CRY_FOR_JUSTICE(Challenge.from("Schrei nach Gerechtigkeit", "nur Judge (keine Pistolen)",
                "nur die Abilities der Agents, die auch im gegnerischen Team vertreten sind", null, null)),
        ABILITIES_ONLY(Challenge.from("Abilities only", "nur Classic", null,
                null, null)),
        BACTRIAN_CAMEL(Challenge.from("Trampeltier", null, null,
                null, "Es darf weder geshiftet noch gecroucht werden.")),
        SAFE_THE_QUEEN(Challenge.from("Beschütze den*die König*in", null, null,
                null, "Jedes Team wählt in der Kaufphase eine*n König*in, der*die über die " +
                        "Runde keinen Schaden nehmen darf. Einem Komitee-Mitglied muss dies per Whisper im VALORANT-Chat " +
                        "in der Kaufphase mitgeteilt werden.")),
        NO_MOUSE_MOVE(Challenge.from("Keine Mausbewegung", null, null,
                null, "Die Maus darf nicht bewegt werden (wird über Minimap kontrolliert).")),
        VANDAL_ONLY(Challenge.from("Nur Vandal", "nur Vandal und Classic", "Movement",
                null, null)),
        PHANTOM_ONLY(Challenge.from("Nur Phantom", "nur Phantom und Classic", "Smokes",
                null, null)),
        HIDE_AND_SEEK(Challenge.from("Hide and Seek", "Verstecker: nur Messer, Sucker: alle",
                "keine", null, "Die Sucher müssen bis 1:00 (Match-Timer) in ihrem Spawn warten. " +
                        "Danach dürfen die Sucher die Verstecker suchen. Dabei dürfen die Verstecker ihr Versteck nicht " +
                        "mehr wechseln."));

        private final Challenge challenge;

        Round(Challenge challenge) {
            this.challenge = challenge;
        }

        public Challenge getChallenge() {
            return challenge;
        }

        @Nullable
        public String getAdditionalInformation(Match match) {
            switch (this) {
                case SPOT -> {
                    if (match.getMap() == null) {
                        return "Spot wird vom Komitee kurzfristig ausgewählt";
                    }
                    List<String> list = Arrays.asList(match.getMap().getSpots());
                    return list.get(ThreadLocalRandom.current().nextInt(list.size()));
                }
                case HIDE_AND_SEEK -> {
                    boolean defenderAreSeeker = ThreadLocalRandom.current().nextBoolean();
                    if (defenderAreSeeker) {
                        return "Sucher: **" + match.getDefender().getName() + "**";
                    }
                    return "Sucher: **" + match.getAttacker().getName() + "**";
                }
                default -> {
                    return null;
                }
            }
        }

        public static List<Round> getByRound(int roundId) {
            List<Round> list = new ArrayList<>();
            for (Round round : values()) {
                int[] rounds = round.getChallenge().getRounds();

                if (rounds.length == 0) {
                    continue;
                }

                for (int i : rounds) {
                    if (i != roundId) {
                        continue;
                    }

                    list.add(round);
                    break;
                }
            }

            if (list.isEmpty()) {
                return getAllAndRound(roundId);
            }

            return list;
        }

        public static List<Round> getAllAndRound(int roundId) {
            List<Round> list = new ArrayList<>();
            for (Round round : values()) {
                int[] rounds = round.getChallenge().getRounds();

                if (rounds.length == 0) {
                    list.add(round);
                    continue;
                }

                for (int i : rounds) {
                    if (i != roundId) {
                        continue;
                    }

                    list.add(round);
                    break;
                }
            }

            return list;
        }
    }

    public enum WholeMatch {
        NONE(Challenge.from("keine Challenge", null, null,
                null, "keine")),
        AGENT_SELECTION(Challenge.from("Festgelegte Agents", null, null, null,
                "Nur die genannten Agents dürfen verwendet werden.")),
        AGENT_BAN(Challenge.from("Agent bannen", null, null,
                null, "Jedes Team kann vor dem Start der Agenten-Auswahl einen Agenten bannen.")),
        INVERTED_MOUSE(Challenge.from("Invertierte Maus", null, null, null,
                "Das komplette Match muss mit invertierter Maus gespielt werden."));

        private final Challenge challenge;

        WholeMatch(Challenge challenge) {
            this.challenge = challenge;
        }

        public Challenge getChallenge() {
            return challenge;
        }

        @Nullable
        public String getAdditionalInformation() {
            if (this == AGENT_SELECTION) {
                List<String> defaultAgents = Arrays.asList("Phoenix", "Sova", "Brimstone", "Jett", "Sage");
                List<String> extraAgents = Arrays.asList("Viper", "Cypher", "Reyna", "Killjoy", "Breach", "Omen", "Raze",
                        "Skye", "Yoru", "Astra", "KAY/O", "Chamber", "Neon");

                StringBuilder agents = new StringBuilder();
                Random random = ThreadLocalRandom.current();

                for (int i = 0; i < 2; i++) {
                    if (!agents.isEmpty()) {
                        agents.append(", ");
                    }

                    String agent = defaultAgents.get(random.nextInt(defaultAgents.size()));
                    defaultAgents.remove(agent);

                    agents.append("**").append(agent).append("**");
                }

                for (int i = 0; i < 3; i++) {
                    if (!agents.isEmpty()) {
                        agents.append(", ");
                    }

                    String agent = extraAgents.get(random.nextInt(extraAgents.size()));
                    extraAgents.remove(agent);

                    agents.append("**").append(agent).append("**");
                }

                return agents.toString();
            }
            return null;
        }
    }
}
