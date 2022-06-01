package tv.banko.valorantevent.discord.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import tv.banko.valorantevent.discord.Discord;

import java.time.OffsetDateTime;

public record TemplateMessage(Discord discord) {

    public void sendDiscordRules(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":receipt: | Regeln des Discord-Servers")
                .setDescription("> Im folgenden Text werden die Regeln des Discord Servers aufgelistet. " +
                        "Wenn Du mit dem Discord Server interagierst (schreiben, reden, lesen, usw.), stimmst Du den Regeln zu!")
                .addField("1. | Discord-Richtlinien", "> Die [**Discord Nutzungsbedingungen**](https://discord.com/terms) " +
                                "müssen, wie die [**Discord Community-Richtlinien**](https://discord.com/guidelines) befolgt werden.",
                        false)
                .addField("2. | Verhalten", "> Folgendes wird **nicht** toleriert: **Diskriminierung**, **Beleidigung**, " +
                                "und **unmenschliches Verhalten**.",
                        false)
                .addField("3. | Verbreitung", "> Folgendes darf **nicht** verbreitet werden: **Werbung**, **Hacks für " +
                                "z.B. VALORANT**, **N**ot**S**afe**F**or**W**ork und **schädliche Links / Bilder**.",
                        false)
                .addField("4. | Sonstiges", "> An **selbstverständliche Regeln** sollte sich trotzdem gehalten werden, " +
                                "auch wenn sie hier nicht aufgelistet sind. Das **Team** ist von den Regeln **ausgeschlossen**. " +
                                "Diese Regeln gelten **auch** in **Direktnachrichten**. Das **Betteln** nach Rollen ist untersagt.",
                        false)
                .addField("5. | Bestrafung", "> Das Team kann die Bestrafung je nach Ausmaß **selbst** entscheiden. " +
                                "__Mögliche Bestrafungen__: **Nachricht löschen**, **aus Voice-Channel kicken**, " +
                                "**in Time-Out versetzen**, **von Discord-Server kicken** und **von Discord-Server bannen**.",
                        false)
                .addField("6. | Umgehung einer Bestrafung", "> Wenn eine Bestrafung mit einem **Zweitaccount umgangen** " +
                                "wird, wird ein **Ausschluss** von allen bisherigen und allen weiteren Accounts **folgen**.",
                        false)
                .setFooter("Die Regeln können jederzeit verändert oder erweitert werden")
                .setTimestamp(OffsetDateTime.now())
                .build()).queue();
    }

    public void sendGameRules(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":video_game: | Regeln im Turnier")
                .setDescription("> Im folgenden Text werden die Regeln des Turniers aufgelistet. " +
                        "Wenn Du am Turnier teilnimmst, stimmst Du den Regeln zu!")
                .setFooter("Die Regeln können jederzeit verändert oder erweitert werden")
                .build()).queue();
    }

    public void sendGameOrder(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":video_game: | Regeln im Turnier")
                .setDescription("> Im folgenden Text wird der Ablauf des Turniers aufgelistet.")
                .addField("1. | Vor einem Match", """
                                > **Team-Leader** müssen **10 Minuten** vor dem Match in der Party des Matches (in VALORANT) sein.
                                > Andere Mitspieler:innen müssen **spätestens 5 Minuten** vor dem Match in der Party sein.
                                > Im Party-Chat darf **nicht gechattet** werden.
                                > **Map-Vote** beginnt, wenn **beide Team-Leader** in der Party sind.""",
                        false)
                .addField("2. | Map-Vote", """
                                **__Best of 1__**:
                                > **Beide Team-Leader** werfen den Würfel. *(Zahl zwischen 0 und 100)*
                                > Die Person mit der **höheren Zahl** wählt aus, ob er/sie die **Map** oder die **Start-Seite** auswählen will.
                                > Die Person, die die Map auswählt, **bannt** erst **2 Maps**.
                                > Daraufhin bannt die andere Person **2 weitere Maps**.
                                > Danach wird die **Map** aus den **übrigen Maps** ausgewählt.
                                > Zuletzt wird die **Start-Seite** ausgewählt.""",
                        false)
                .addField("3. | Challenges", """
                                > Challenges werden eine Runde im **Voraus** gepostet. *(kann verzögert sein, wenn das Komitee überlastet ist: bitte frühzeitig im Ingame Chat Bescheid geben)*
                                > """,
                        false)
                .addField("4. | Punkte", """
                                > Eine **erfolgreich abgeschlossene Challenge** bringt **+1 Punkt**.
                                > Ein **Rundensieg** bringt **+1 Punkt**, wenn die Challenge erfolgreich abgeschlossen wurde.
                                > Eine **versagte Challenge** bringt **-1 Punkt**.""",
                        false)
                .addField("5. | Bestrafung", "> Das Team kann die Bestrafung je nach Ausmaß **selbst** entscheiden. " +
                                "__Mögliche Bestrafungen__: **Nachricht löschen**, **aus Voice-Channel kicken**, " +
                                "**in Time-Out versetzen**, **von Discord-Server kicken** und **von Discord-Server bannen**.",
                        false)
                .addField("6. | Umgehung einer Bestrafung", "> Wenn eine Bestrafung mit einem **Zweitaccount umgangen** " +
                                "wird, wird ein **Ausschluss** von allen bisherigen und allen weiteren Accounts **folgen**.",
                        false)
                .setFooter("Die Regeln können jederzeit verändert oder erweitert werden")
                .build()).queue();
    }

    public void sendVerification(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":link: | VALORANT verknüpfen")
                .setDescription("""
                        > Wie **verknüpfe** ich meinen **VALORANT**-Account?
                        
                        > **1)** Ändere Deinen **Nicknamen** auf dem Server zu deinem **VALORANT**-Tag [(Wie? *Klick*)](https://google.com/)
                        > **2)** Klicke den **Knopf** (`Account verknüpfen`) unter dieser Nachricht.
                        """)
                .build()).setActionRow(Button.secondary("template:button:verify", "Account verknüpfen")).queue();
    }

    public void sendCreateTeam(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":love_letter: | Team erstellen")
                .setDescription("""
                        > Wie **erstelle** ich ein **Team**?
                        
                        > **1)** Klicke den **Knopf** (`Team erstellen`) unter dieser Nachricht.
                        
                        > __Optional:__ (nur Team-Captain)\s
                        > \s
                        > **/team rename <Name>**: Benenne dein Team um
                        > **/team invite <User>**: Lade jemanden in dein Team ein
                        > **/team kick <User>**: Wirf jemanden aus deinem Team
                        
                        > __Weitere Befehle:__
                        > \s
                        > **/team info**: Erhalte Informationen über dein Team
                        """)
                .build()).setActionRow(Button.secondary("template:button:createteam", "Team erstellen"),
                Button.link(discord.getGuildHelper().getTeamJoinLink(), "Team beitreten")).queue();
    }

    public void sendJoinTeam(GuildMessageChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":love_letter: | Team beitreten")
                .setDescription("""
                        > Wie **trete** ich einem **Team** bei?
                        
                        > **1)** Der Team-Captain muss dich mit **/team invite <User>** einladen.
                        > **2)** Verwende **/team join**, um anzuzeigen, welches Team dich eingeladen hat.
                        > **3)** Verwende **/team join <Code>**, um dem jeweiligen Team beizutreten.
                        """)
                .build()).queue();
    }
}
