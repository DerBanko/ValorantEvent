package tv.banko.valorantevent.discord.role;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import tv.banko.valorantevent.discord.Discord;
import tv.banko.valorantevent.tournament.team.Team;

import java.awt.*;

public class TeamRole {

    private final Discord discord;
    private final Team team;

    private String roleId;

    public TeamRole(Discord discord, Team team) {
        this.discord = discord;
        this.team = team;

        createRole();
    }

    public TeamRole(Discord discord, Team team, String roleId) {
        this.discord = discord;
        this.team = team;
        this.roleId = roleId;
    }

    public void setRole(String userId) {
        Guild guild = discord.getGuildHelper().getGuild();

        if (guild == null) {
            return;
        }

        if (roleId == null) {
            return;
        }

        Role role = guild.getRoleById(roleId);

        if(role == null) {
            createRole();
            return;
        }

        guild.addRoleToMember(userId, role).queue();
    }

    public void removeRole(String userId) {
        Guild guild = discord.getGuildHelper().getGuild();

        if (guild == null) {
            return;
        }

        if (roleId == null) {
            return;
        }

        Role role = guild.getRoleById(roleId);

        if(role == null) {
            createRole();
            return;
        }

        guild.removeRoleFromMember(userId, role).queue();
    }

    public String getRoleId() {
        return roleId;
    }

    public long getId() {
        return Long.parseLong(roleId);
    }

    public void editRoleName(String name) {
        Guild guild = discord.getGuildHelper().getGuild();

        if (guild == null) {
            return;
        }

        if (roleId == null) {
            return;
        }

        Role role = guild.getRoleById(roleId);

        if(role == null) {
            return;
        }

        role.getManager().setName(name).queue();
    }

    private void createRole() {

        Guild guild = discord.getGuildHelper().getGuild();

        if (guild == null) {
            return;
        }

        Role role = guild.getRolesByName(team.getName(), true).stream().findFirst().orElse(null);

        if (role == null) {
            guild.createRole().setName(team.getName()).setColor(Color.decode("#292B2F")).queue(newRole -> {
                this.roleId = newRole.getId();
                team.save();
                team.getPlayers().forEach(this::setRole);
            });
            return;
        }

        this.roleId = role.getId();
        team.save();
        team.getPlayers().forEach(this::setRole);
    }

}
