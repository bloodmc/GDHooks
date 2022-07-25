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
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyRank;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.ClanConfig;
import com.griefdefender.hooks.listener.PartiesEventHandler;
import com.griefdefender.hooks.provider.clan.BaseClanProvider;
import org.bukkit.Bukkit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PartiesClanProvider extends BaseClanProvider {

    private final PartiesAPI plugin;
    private Map<String, Rank> clanRankMap = new HashMap<>();
    private final List<Rank> ranks = new ArrayList<>();

    public PartiesClanProvider() {
        super("parties");
        this.plugin = Parties.getApi();
        // Populate Parties
        for (PartyRank partyRank : this.plugin.getRanks()) {
            final GDRank rank = new GDRank(partyRank);
            this.clanRankMap.put(partyRank.getConfigName().toLowerCase(), rank);
            this.ranks.add(rank);
        }
        for (Party party : this.plugin.getOnlineParties()) {
            final GDClan gdClan = new GDClan(party);
            this.clanMap.put(getNameOrId(party), gdClan);
            final Path clanConfigPath = CLAN_DATA_PATH.resolve(party.getId() + ".conf");
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(getNameOrId(party), clanConfig);
            for (UUID member : party.getMembers()) {
                final GDClanPlayer gdClanPlayer = new GDClanPlayer(party, member);
                this.clanPlayerMap.put(member, gdClanPlayer);
            }
        }
        this.registerEvents();
    }

    private String getNameOrId(Party party) {
        return party.getName() != null ? party.getName() : party.getId().toString();
    }
    
    public void registerEvents() {
        super.registerEvents();
        GriefDefender.getRegistry().registerClanProvider(this);
        Bukkit.getPluginManager().registerEvents(new PartiesEventHandler(), GDHooksBootstrap.getInstance().getLoader());
    }

    public void addClan(Party clan) {
        this.getClanMap().put(getNameOrId(clan), new GDClan(clan));
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getId() + ".conf");
        if (!clanConfigPath.toFile().exists()) {
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(getNameOrId(clan), clanConfig);
        }
    }

    public void removeClan(Party clan) {
        this.getClanMap().remove(clan.getId().toString());
        GDHooks.getInstance().getClanConfigMap().remove(getNameOrId(clan));
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getId() + ".conf");
        clanConfigPath.toFile().delete();
    }

    @Override
    public List<Rank> getClanRanks(String tag) {
        return Collections.unmodifiableList(this.ranks);
    }
}
