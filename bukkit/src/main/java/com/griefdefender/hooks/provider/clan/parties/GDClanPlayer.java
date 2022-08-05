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

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.alessiodp.parties.api.interfaces.PartyRank;
import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.api.data.PlayerData;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class GDClanPlayer implements ClanPlayer {

    private final GDClan clan;
    private final Party pluginParty;
    private final User user;

    public GDClanPlayer(Party party, UUID playerUniqueId) {
        this.clan = new GDClan(party);
        this.pluginParty = party;
        this.user = GriefDefender.getCore().getUser(playerUniqueId);
    }

    @Override
    public UUID getUniqueId() {
        return this.user.getUniqueId();
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
        return this.clan;
    }

    @Override
    public @Nullable Object getOnlinePlayer() {
        return user.getOnlinePlayer();
    }

    @Override
    public Rank getRank() {
        PartyPlayer partyPlayer = Parties.getApi().getPartyPlayer(getUniqueId());
        if (partyPlayer != null) {
            PartyRank rank = Parties.getApi().getRanks().stream().filter(r -> r.getLevel() == partyPlayer.getRank()).findFirst().orElse(null);
            return rank != null ? this.getClan().getRank(rank.getName().toLowerCase()) : null;
        }
        return null;
    }

    @Override
    public void setRank(Rank rank) {
        for (PartyRank r : Parties.getApi().getRanks()) {
            if (r.getConfigName().equalsIgnoreCase(rank.getName())) {
                PartyPlayer partyPlayer = Parties.getApi().getPartyPlayer(getUniqueId());
                if (partyPlayer != null)
                    partyPlayer.setRank(r.getLevel());
                return;
            }
        }
    }

    @Override
    public @Nullable Claim getCurrentClaim() {
        return this.getPlayerData().getCurrentClaim();
    }

    @Override
    public boolean isLeader() {
        return this.pluginParty.getLeader() != null && this.pluginParty.getLeader().equals(this.getUniqueId());
    }
}
