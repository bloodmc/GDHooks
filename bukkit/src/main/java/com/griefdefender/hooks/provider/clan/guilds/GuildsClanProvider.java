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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.ClanConfig;
import com.griefdefender.hooks.listener.GuildsEventHandler;
import com.griefdefender.hooks.provider.clan.BaseClanProvider;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;

public class GuildsClanProvider extends BaseClanProvider {

    private final GuildsAPI plugin;
    private Map<String, Rank> clanRankMap = new HashMap<>();
    private final List<Rank> ranks = new ArrayList<>();

    public GuildsClanProvider() {
        super("guilds");
        this.plugin = Guilds.getApi();
        // Populate Clans
        for (GuildRole role : this.plugin.getGuildHandler().getRoles()) {
            if (role.getLevel() == 0) {
                //ignore GM
                continue;
            }
            final GDRank rank = new GDRank(role);
            this.clanRankMap.put(role.getName().toLowerCase(), rank);
            this.ranks.add(rank);
        }
        for (Guild clan : this.plugin.getGuildHandler().getGuilds().values()) {
            final GDClan gdClan = new GDClan(clan);
            this.clanMap.put(clan.getPrefix().toLowerCase(), gdClan);
            final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getPrefix().toLowerCase() + ".conf");
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(clan.getPrefix().toLowerCase(), clanConfig);
            for (GuildMember clanPlayer : clan.getMembers()) {
                final GDClanPlayer gdClanPlayer = new GDClanPlayer(clan, clanPlayer.getUuid());
                this.clanPlayerMap.put(clanPlayer.getUuid(), gdClanPlayer);
            }
        }
        this.registerEvents();
    }

    public void registerEvents() {
        super.registerEvents();
        GriefDefender.getRegistry().registerClanProvider(this);
        Bukkit.getPluginManager().registerEvents(new GuildsEventHandler(), GDHooksBootstrap.getInstance().getLoader());
    }

    public boolean clanExists(String clanName) {
        for (Guild clan : this.plugin.getGuildHandler().getGuilds().values()) {
            if (clan.getName().equalsIgnoreCase(clanName)) {
                return true;
            }
        }

        return false;
    }

    public void addClan(Guild clan) {
        this.getClanMap().put(clan.getPrefix(), new GDClan(clan));
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getPrefix() + ".conf");
        if (!clanConfigPath.toFile().exists()) {
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(clan.getPrefix(), clanConfig);
        }
    }

    public void removeClan(Guild clan) {
        this.getClanMap().remove(clan.getPrefix());
        GDHooks.getInstance().getClanConfigMap().remove(clan.getPrefix());
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getPrefix() + ".conf");
        clanConfigPath.toFile().delete();
    }

    @Override
    public List<Rank> getClanRanks(String tag) {
        return Collections.unmodifiableList(this.ranks);
    }
}
