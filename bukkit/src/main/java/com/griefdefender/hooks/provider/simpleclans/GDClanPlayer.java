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
package com.griefdefender.hooks.provider.simpleclans;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.hooks.GDHooks;

public class GDClanPlayer implements ClanPlayer {

    private final net.sacredlabyrinth.phaed.simpleclans.ClanPlayer pluginClanPlayer;
    private final User user;

    public GDClanPlayer(net.sacredlabyrinth.phaed.simpleclans.ClanPlayer clanPlayer) {
        this.pluginClanPlayer = clanPlayer;
        this.user = GriefDefender.getCore().getUser(clanPlayer.getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return this.pluginClanPlayer.getUniqueId();
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
        return GDHooks.getInstance().getClanProvider().getClan(this.pluginClanPlayer.getTag());
    }

    @Override
    public void setClan(Clan clan) {
        final GDClan gdClan = (GDClan) clan;
        this.pluginClanPlayer.setClan(gdClan.getInternalClan());
    }

    @Override
    public @Nullable Object getOnlinePlayer() {
        return user.getOnlinePlayer();
    }

    @Override
    public String getRank() {
        return this.pluginClanPlayer.getRankId().toLowerCase();
    }

}
