package tv.banko.valorantevent.discord.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;

import java.time.OffsetDateTime;

public class TemplateMessage {

    public void sendDiscordRules(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":receipt: | Regeln des Discord-Servers")
                .setDescription("Im folgenden Text werden die Regeln des Discord Servers aufgelistet. " +
                        "Wenn du mit dem Discord Server interagierst (schreiben, reden, lesen, usw.), stimmst Du den Regeln zu!")
                .addField("1. | Discord-Richtlinien", "Die [**Discord Nutzungsbedingungen**](https://discord.com/terms) " +
                                "müssen, wie die [**Discord Community-Richtlinien**](https://discord.com/guidelines) befolgt werden.",
                        false)
                .addField("2. | Verhalten", "Folgendes wird **nicht** toleriert: **Diskriminierung**, **Beleidigung**, " +
                                "und **unmenschliches Verhalten**.",
                        false)
                .addField("3. | Verbreitung", "Folgendes darf **nicht** verbreitet werden: **Werbung**, **Hacks für " +
                                "z.B. VALORANT**, **N**ot**S**afe**F**or**W**ork und **schädliche Links / Bilder**.",
                        false)
                .addField("4. | Sonstiges", "An **selbstverständliche Regeln** sollte sich trotzdem gehalten werden, " +
                                "auch wenn sie hier nicht aufgelistet sind. Das **Team** ist von den Regeln **ausgeschlossen**. " +
                                "Diese Regeln gelten **auch** in **Direktnachrichten**. Das **Betteln** nach Rollen ist untersagt.",
                        false)
                .addField("5. | Bestrafung", "Das Team kann die Bestrafung je nach Ausmaß **selbst** entscheiden. " +
                                "__Mögliche Bestrafungen__: **Nachricht löschen**, **aus Voice-Channel kicken**, " +
                                "**in Time-Out versetzen**, **von Discord-Server kicken** und **von Discord-Server bannen**.",
                        false)
                .addField("6. | Umgehung einer Bestrafung", "Wenn eine Bestrafung mit einem **Zweitaccount umgangen** wird, " +
                                "wird ein **Ausschluss** von allen bisherigen und allen weiteren Accounts **folgen**.",
                        false)
                .setFooter("Die Regeln können jederzeit verändert oder erweitert werden")
                .setTimestamp(OffsetDateTime.now())
                .build()).queue();
    }

    public void sendGameRules(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":video_game: | Regeln im Turnier")
                .setDescription("Im folgenden Text werden die Regeln des Turniers aufgelistet. " +
                        "Wenn du am Turnier teilnimmst, stimmst Du den Regeln zu!")
                .build()).queue();
    }

    public void sendGameOrder(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":video_game: | Regeln im Turnier")
                .setDescription("Im folgenden Text werden die Regeln des Turniers aufgelistet. " +
                        "Wenn du beim Turnier teilnimmst, stimmst Du den Regeln zu!")
                .build()).queue();
    }

}
