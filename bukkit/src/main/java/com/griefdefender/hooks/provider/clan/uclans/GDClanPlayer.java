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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.hooks.GDHooks;

import me.ulrich.clans.packets.interfaces.UClans;

public class GDClanPlayer implements ClanPlayer {

    private final UClans plugin;
    private final me.ulrich.clans.data.PlayerData pluginClanPlayer;
    private final User user;

    public GDClanPlayer(me.ulrich.clans.data.PlayerData clanPlayer) {
        this.plugin  = (UClans) Bukkit.getPluginManager().getPlugin("UltimateClans");
        this.pluginClanPlayer = clanPlayer;
        this.user = GriefDefender.getCore().getUser(clanPlayer.getUuid());
    }

    @Override
    public UUID getUniqueId() {
        return this.pluginClanPlayer.getUuid();
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
        final me.ulrich.clans.data.ClanData clanData = this.plugin.getClanAPI().getClan(this.pluginClanPlayer.getClan_id());
        if (clanData == null) {
            return null;
        }
        return GDHooks.getInstance().getClanProvider().getClan(clanData.getTag());
    }

    @Override
    public @Nullable Object getOnlinePlayer() {
        return user.getOnlinePlayer();
    }

    @Override
    public Rank getRank() {
        final String role = this.plugin.getPlayerAPI().getPlayerRole(this.getUniqueId());
        if (role == null) {
            return null;
        }
        return this.getClan().getRank(role);
    }

    @Override
    public void setRank(Rank rank) {
       //
    }

    @Override
    public @Nullable Claim getCurrentClaim() {
        return this.getPlayerData().getCurrentClaim();
    }

    @Override
    public boolean isLeader() {
        return this.plugin.getClanAPI().isLeader(this.user.getUniqueId());
    }
}
