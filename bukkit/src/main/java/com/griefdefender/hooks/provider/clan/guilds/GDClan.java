/*
 * This file is part of GDHooks, licensed under the MIT License (MIT).
 *
 * Copyright (c) bloodmc
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.griefdefender.hooks.provider.clan.guilds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.clan.ClanHome;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.provider.clan.GDClanHome;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import com.griefdefender.lib.kyori.adventure.text.Component;

public class GDClan implements Clan {

    private Guild pluginClan;
    private List<Rank> ranks;

    public GDClan(Guild clan) {
        this.pluginClan = clan;
    }

    @Override
    public String getId() {
        return "guilds:" + this.pluginClan.getName().toLowerCase();
    }

    @Override
    public String getName() {
        return this.pluginClan.getName().toLowerCase();
    }

    @Override
    public Component getNameComponent() {
        return Component.text(this.pluginClan.getName());
    }

    @Override
    public Component getTagComponent() {
        return Component.text(this.pluginClan.getPrefix());
    }

    @Override
    public String getTag() {
        return this.pluginClan.getPrefix();
    }

    @Override
    public String getDescription() {
        return this.pluginClan.getName();
    }

    @Override
    public UUID getBaseWorldUniqueId() {
        return this.pluginClan.getHome().getAsLocation().getWorld().getUID();
    }

    @Override
    public Vector3i getBasePos() {
        final Location home = this.pluginClan.getHome().getAsLocation();
        return new Vector3i(home.getBlockX(), home.getBlockY(), home.getBlockZ());
    }

    @Override
    public List<Clan> getAllies() {
        List<Clan> clans = new ArrayList<>();
        for (UUID guildUniqueId : this.pluginClan.getAllies()) {
            final Guild ally = Guilds.getApi().getGuild(guildUniqueId);
            final Clan clan = GDHooks.getInstance().getClanProvider().getClan(ally.getPrefix());
            if (clan != null) {
                clans.add(clan);
            }
        }
        return clans;
    }

    @Override
    public List<Clan> getRivals() {
        return Collections.emptyList();
    }

    @Override
    public List<ClanPlayer> getMembers(boolean onlineOnly) {
        final List<ClanPlayer> clanPlayers = new ArrayList<>();
        final List<GuildMember> members = new ArrayList<>(onlineOnly ? this.pluginClan.getOnlineMembers() : this.pluginClan.getMembers());
        for (GuildMember pluginClanPlayer : members) {
            final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(pluginClanPlayer.getUuid());
            if (clanPlayer != null) {
                clanPlayers.add(clanPlayer);
            }
        }

        return clanPlayers;
    }

    @Override
    public List<ClanPlayer> getLeaders(boolean onlineOnly) {
        final List<ClanPlayer> clanPlayers = new ArrayList<>();
        final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(this.pluginClan.getGuildMaster().getUuid());
        if (clanPlayer != null) {
            clanPlayers.add(clanPlayer);
        }

        return clanPlayers;
    }

    @Override
    public List<com.griefdefender.api.clan.Rank> getRanks() {
        if (this.ranks == null) {
            this.ranks = GriefDefender.getCore().getClanProvider().getClanRanks(this.getTag());
        }
        return Collections.unmodifiableList(this.ranks);
    }

    public Guild getInternalClan() {
        return this.pluginClan;
    }

    @Override
    public @Nullable Rank getRank(String rankName) {
        if (this.ranks == null) {
            this.ranks = GriefDefender.getCore().getClanProvider().getClan(this.pluginClan.getPrefix()).getRanks();
        }
        for (Rank rank : this.ranks) {
            if (rank.getName().equalsIgnoreCase(rankName)) {
                return rank;
            }
        }
        return null;
    }

    @Override
    public boolean isAlly(String tag) {
        if (!this.pluginClan.hasAllies()) {
            return false;
        }
        for (UUID guildUniqueId : this.pluginClan.getAllies()) {
            final Guild ally = Guilds.getApi().getGuild(guildUniqueId);
            if (ally == null) {
                continue;
            }
            if (ally.getPrefix().equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRival(String tag) {
        return false;
    }

    @Override
    public List<ClanHome> getHomes() {
        final List<ClanHome> locations = new ArrayList<>();
        locations.add(new GDClanHome("home", this.pluginClan.getHome().getAsLocation()));
        return locations;
    }
}
