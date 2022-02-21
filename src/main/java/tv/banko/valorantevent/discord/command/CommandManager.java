package tv.banko.valorantevent.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import tv.banko.valorantevent.discord.Discord;

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    private final List<CommandObject> list;

    public CommandManager() {
        this.list = new ArrayList<>();
    }

    public void load(Discord discord) {
        list.add(new LinkCommand(discord));
        list.add(new UpdateRankCommand(discord));

        CommandListUpdateAction commands = discord.getBot().updateCommands();

        for (CommandObject object : list) {
            commands = commands.addCommands(object.getCommand());
        }

        commands.queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for (CommandObject object : list) {
            if (!object.getCommand().getName().equalsIgnoreCase(event.getInteraction().getName())) {
                continue;
            }

            object.respond(event);
        }
    }
}
