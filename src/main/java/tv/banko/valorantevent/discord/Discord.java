package tv.banko.valorantevent.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import tv.banko.valorantevent.ValorantEvent;
import tv.banko.valorantevent.discord.command.CommandManager;
import tv.banko.valorantevent.discord.listener.InteractionListeners;

import javax.security.auth.login.LoginException;

public class Discord {

    private final ValorantEvent event;
    private final JDA bot;
    private final CommandManager command;

    private final String guildId;

    public Discord(ValorantEvent event) throws LoginException, InterruptedException {
        this.event = event;

        this.guildId = System.getenv("GUILD_ID");
        this.command = new CommandManager();

        JDABuilder builder = JDABuilder.createDefault(System.getenv("TOKEN"));

        builder.addEventListeners(new InteractionListeners(event));
        builder.addEventListeners(command);
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setActivity(Activity.watching("your VALORANT Matches"));

        bot = builder.build().awaitReady();

        command.load(this);
    }

    public ValorantEvent getEvent() {
        return event;
    }

    public JDA getBot() {
        return bot;
    }

    public String getGuildId() {
        return guildId;
    }

}
