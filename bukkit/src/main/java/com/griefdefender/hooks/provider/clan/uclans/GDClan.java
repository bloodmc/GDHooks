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
package com.griefdefender.hooks.provider.clan.uclans;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.clan.ClanHome;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.provider.clan.GDClanHome;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

import me.ulrich.clans.data.HomesData;
import me.ulrich.clans.packets.interfaces.UClans;
import com.griefdefender.lib.kyori.adventure.text.Component;

public class GDClan implements Clan {

    private final UClans plugin;
    private final me.ulrich.clans.data.ClanData pluginClan;

    public GDClan(me.ulrich.clans.data.ClanData clan) {
        this.pluginClan = clan;
        this.plugin  = (UClans) Bukkit.getPluginManager().getPlugin("UltimateClans");
    }

    @Override
    public String getId() {
        return "uclans:" + this.pluginClan.getTagNoColor().toLowerCase();
    }

    @Override
    public String getName() {
        return this.pluginClan.getTagNoColor();
    }

    @Override
    public Component getNameComponent() {
        return Component.text(this.pluginClan.getDesc());
    }

    @Override
    public Component getTagComponent() {
        return Component.text(this.pluginClan.getTag());
    }

    @Override
    public String getTag() {
        return this.pluginClan.getTagNoColor();
    }

    @Override
    public String getDescription() {
        return this.pluginClan.getDesc();
    }

    @Override
    public UUID getBaseWorldUniqueId() {
        return null;
    }

    @Override
    public Vector3i getBasePos() {
        return null;
    }

    @Override
    public List<ClanHome> getHomes() {
        final List<ClanHome> locations = new ArrayList<>();
        for (HomesData homeData : this.pluginClan.getHome()) {
            final Location location = UClansProvider.locFromString(homeData.getLoc());
            if (location != null) {
                locations.add(new GDClanHome(homeData.getName(), location));
            }
        }
        return locations;
    }

    @Override
    public List<Clan> getAllies() {
        List<Clan> clans = new ArrayList<>();
        for (UUID clanUniqueId : this.pluginClan.getRivalAlly().getAlly()) {
            final me.ulrich.clans.data.ClanData clanData = this.plugin.getClanAPI().getClan(clanUniqueId);
            if (clanData == null) {
                continue;
            }
            final Clan clan = GDHooks.getInstance().getClanProvider().getClan(clanData.getTagNoColor().toLowerCase());
            if (clan != null) {
                clans.add(clan);
            }
        }
        return clans;
    }

    @Override
    public List<Clan> getRivals() {
        List<Clan> clans = new ArrayList<>();
        for (UUID clanUniqueId : this.pluginClan.getRivalAlly().getRival()) {
            final me.ulrich.clans.data.ClanData clanData = this.plugin.getClanAPI().getClan(clanUniqueId);
            if (clanData == null) {
                continue;
            }
            final Clan clan = GDHooks.getInstance().getClanProvider().getClan(clanData.getTagNoColor().toLowerCase());
            if (clan != null) {
                clans.add(clan);
            }
        }
        return clans;
    }

    @Override
    public List<ClanPlayer> getMembers(boolean onlineOnly) {
        final List<ClanPlayer> clanPlayers = new ArrayList<>();
        final List<UUID> members = onlineOnly ? this.pluginClan.getOnlineMembers() : this.pluginClan.getMembers();
        for (UUID clanMemberUniqueId : members) {
            final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(clanMemberUniqueId);
            if (clanPlayer != null) {
                clanPlayers.add(clanPlayer);
            }
        }

        return clanPlayers;
    }

    @Override
    public List<ClanPlayer> getLeaders(boolean onlineOnly) {
        final List<ClanPlayer> clanPlayers = new ArrayList<>();
        final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(this.pluginClan.getLeader());
        if (clanPlayer != null) {
            clanPlayers.add(clanPlayer);
        }
        return clanPlayers;
    }

    @Override
    public List<com.griefdefender.api.clan.Rank> getRanks() {
        return UClansProvider.UC_RANK_LIST;
    }

    public me.ulrich.clans.data.ClanData getInternalClan() {
        return this.pluginClan;
    }

    @Override
    public @Nullable Rank getRank(String rank) {
        if (rank == null) {
            return null;
        }

        if (rank.equalsIgnoreCase("member")) {
            return UClansProvider.MEMBER_RANK;
        }
        if (rank.equalsIgnoreCase("moderator")) {
            return UClansProvider.MODERATOR_RANK;
        }
        if (rank.equalsIgnoreCase("leader")) {
            return UClansProvider.LEADER_RANK;
        }
        return null;
    }

    @Override
    public boolean isAlly(String tag) {
        final List<Clan> clans = this.getAllies();
        for (Clan clan : clans) {
            if (clan.getTag().equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRival(String tag) {
        final List<Clan> clans = this.getRivals();
        for (Clan clan : clans) {
            if (clan.getTag().equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }
}
