package tv.banko.valorantevent.tournament;

import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.tournament.challenge.ChallengeManager;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.match.MatchManager;
import tv.banko.valorantevent.tournament.player.PlayerManager;
import tv.banko.valorantevent.tournament.team.TeamManager;

public class Tournament {

    private final ValorantEvent event;

    private TeamManager team;
    private MatchManager match;
    private PlayerManager player;
    private ChallengeManager challenge;

    public Tournament(ValorantEvent event) {
        this.event = event;
    }

    public ValorantEvent getEvent() {
        return event;
    }

    public TeamManager getTeam() {
        return team;
    }

    public MatchManager getMatch() {
        return match;
    }

    public PlayerManager getPlayer() {
        return player;
    }

    public ChallengeManager getChallenge() {
        return challenge;
    }

    public Discord getDiscord() {
        return event.getDiscord();
    }

    public void load() {
        this.team = new TeamManager(event);
        this.match = new MatchManager(event);
        this.player = new PlayerManager(event);
        this.challenge = new ChallengeManager();
    }
}
