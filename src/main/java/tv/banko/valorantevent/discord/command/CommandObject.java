package tv.banko.valorantevent.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import tv.banko.valorantevent.discord.Discord;

public abstract class CommandObject {

    protected final Discord discord;

    public CommandObject(Discord discord) {
        this.discord = discord;
    }

    public abstract CommandData getCommand();

    public abstract void respond(SlashCommandInteractionEvent event);
}
