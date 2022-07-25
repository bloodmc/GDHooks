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
package com.griefdefender.hooks.provider.clan.parties;

import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyHome;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.clan.ClanHome;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.provider.clan.GDClanHome;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import com.griefdefender.lib.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GDClan implements Clan {

    private Party pluginClan;
    private List<Rank> ranks;

    public GDClan(Party clan) {
        this.pluginClan = clan;
    }

    @Override
    public String getId() {
        return "parties:" + this.pluginClan.getId();
    }

    @Override
    public String getName() {
        return this.pluginClan.getName() != null ? this.pluginClan.getName().toLowerCase() : "";
    }

    @Override
    public Component getNameComponent() {
        return Component.text(this.pluginClan.getName() != null ? this.pluginClan.getName() : "");
    }

    @Override
    public Component getTagComponent() {
        return getNameComponent();
    }

    @Override
    public String getTag() {
        return getName();
    }

    @Override
    public String getDescription() {
        return this.pluginClan.getDescription();
    }

    @Override
    public UUID getBaseWorldUniqueId() {
        Optional<PartyHome> home = this.pluginClan.getHomes().stream().findFirst();
        World world = home.map(partyHome -> Bukkit.getWorld(partyHome.getWorld())).orElse(null);
        return world != null ? world.getUID() : null;
    }

    @Override
    public Vector3i getBasePos() {
        return this.pluginClan.getHomes().stream().findFirst()
                .map(partyHome -> new Vector3i(partyHome.getX(), partyHome.getY(), partyHome.getZ()))
                .orElse(null);
    }

    @Override
    public List<Clan> getAllies() {
        return Collections.emptyList();
    }

    @Override
    public List<Clan> getRivals() {
        return Collections.emptyList();
    }

    @Override
    public List<ClanPlayer> getMembers(boolean onlineOnly) {
        final List<ClanPlayer> clanPlayers = new ArrayList<>();
        final Set<UUID> members = onlineOnly ? this.pluginClan.getOnlineMembers().stream().map(PartyPlayer::getPlayerUUID).collect(Collectors.toSet()) : this.pluginClan.getMembers();
        for (UUID uuid : members) {
            final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(uuid);
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
        if (clanPlayer != null && (!onlineOnly || clanPlayer.isOnline())) {
            clanPlayers.add(clanPlayer);
        }

        return clanPlayers;
    }
    
    @Override
    public List<Rank> getRanks() {
        if (this.ranks == null) {
            this.ranks = GriefDefender.getCore().getClanProvider().getClanRanks(this.getTag());
        }
        return Collections.unmodifiableList(this.ranks);
    }

    @Override
    public @Nullable Rank getRank(String rankName) {
        if (this.ranks == null) {
            this.ranks = GriefDefender.getCore().getClanProvider().getClanRanks(this.getTag());
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
        return false;
    }

    @Override
    public boolean isRival(String tag) {
        return false;
    }

    @Override
    public List<ClanHome> getHomes() {
        final List<ClanHome> locations = new ArrayList<>();
        for (PartyHome partyHome : this.pluginClan.getHomes()) {
            locations.add(new GDClanHome(partyHome.getName(), new Location(
                    Bukkit.getWorld(partyHome.getWorld()),
                    partyHome.getX(),
                    partyHome.getY(),
                    partyHome.getZ()
            )));
        }
        return locations;
    }
}
