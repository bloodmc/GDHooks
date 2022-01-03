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
package com.griefdefender.hooks.provider.clan.towny;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.hooks.GDHooks;
import com.palmergames.bukkit.towny.object.Resident;

public class GDClanPlayer implements ClanPlayer {

    private final Resident pluginClanPlayer;
    private final User user;

    public GDClanPlayer(Resident clanPlayer) {
        this.pluginClanPlayer = clanPlayer;
        this.user = GriefDefender.getCore().getUser(clanPlayer.getUUID());
    }

    @Override
    public UUID getUniqueId() {
        return this.pluginClanPlayer.getUUID();
    }

    @Override
    public PlayerData getPlayerData() {
        return this.user.getPlayerData();
    }

    @Override
    public boolean isOnline() {
        return this.user.isOnline();
    }

    @Override
    public String getFriendlyName() {
        return this.user.getFriendlyName();
    }

    @Override
    public String getIdentifier() {
        return this.user.getIdentifier();
    }

    @Override
    public Clan getClan() {
        final com.palmergames.bukkit.towny.object.Town town = this.getTown();
        if (town == null) {
            return null;
        }
        return GDHooks.getInstance().getClanProvider().getClan(town.getName());
    }

    @Override
    public @Nullable Object getOnlinePlayer() {
        return user.getOnlinePlayer();
    }

    @Override
    public @Nullable Claim getCurrentClaim() {
        return this.getPlayerData().getCurrentClaim();
    }

    @Override
    public boolean isLeader() {
        return this.pluginClanPlayer.isMayor();
    }

    public com.palmergames.bukkit.towny.object.Town getTown() {
        com.palmergames.bukkit.towny.object.Town town = null;
        try {
            town = this.pluginClanPlayer.getTown();
        } catch (Throwable t) {
        }
        return town;
    }

    @Override
    public void setRank(Rank rankName) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Rank getRank() {
        // TODO Auto-generated method stub
        return null;
    }
}
