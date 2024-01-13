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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.collect.ImmutableList;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.ClanConfig;
import com.griefdefender.hooks.listener.UClansEventHandler;
import com.griefdefender.hooks.provider.clan.BaseClanProvider;

import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.interfaces.UClans;

public class UClansProvider extends BaseClanProvider {

    private final UClans plugin;
    public static final GDRank MEMBER_RANK = new GDRank("member");
    public static final GDRank MODERATOR_RANK = new GDRank("moderator");
    public static final GDRank LEADER_RANK = new GDRank("leader");
    public static final Map<String, Rank> UC_RANKS = new HashMap<>();
    public static final List<com.griefdefender.api.clan.Rank> UC_RANK_LIST = ImmutableList.copyOf(UC_RANKS.values());

    static {
            UC_RANKS.put("member", MEMBER_RANK);
            UC_RANKS.put("moderator", MODERATOR_RANK);
            UC_RANKS.put("leader", LEADER_RANK);
    }
    public UClansProvider() {
        super("uclans");
        this.plugin  = (UClans) Bukkit.getPluginManager().getPlugin("UltimateClans");
        // Populate Clans
        for (Entry<UUID, ClanData> mapEntry : this.plugin.getClanAPI().getClanData().entrySet()) {
            final ClanData clanData = mapEntry.getValue();
            final GDClan gdClan = new GDClan(clanData);
            this.clanMap.put(clanData.getTag().toLowerCase(), gdClan);
            final Path clanConfigPath = CLAN_DATA_PATH.resolve(clanData.getTag().toLowerCase() + ".conf");
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(clanData.getTag().toLowerCase(), clanConfig);
            for (UUID memberUniqueId : clanData.getMembers()) {
                final me.ulrich.clans.data.PlayerData playerData = this.plugin.getPlayerAPI().getPlayerData(memberUniqueId).orElse(null);
                if (playerData == null) {
                    continue;
                }
                this.clanPlayerMap.put(memberUniqueId, new GDClanPlayer(playerData));
            }
        }

        this.registerEvents();
    }

    public void registerEvents() {
        super.registerEvents();
        GriefDefender.getRegistry().registerClanProvider(this);
        Bukkit.getPluginManager().registerEvents(new UClansEventHandler(), GDHooksBootstrap.getInstance().getLoader());
    }

    public void addClan(me.ulrich.clans.data.ClanData clan) {
        this.getClanMap().put(clan.getTag(), new GDClan(clan));
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getTag() + ".conf");
        if (!clanConfigPath.toFile().exists()) {
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(clan.getTag(), clanConfig);
        }
    }

    public void removeClan(me.ulrich.clans.data.ClanData clan) {
        this.getClanMap().remove(clan.getTag());
        GDHooks.getInstance().getClanConfigMap().remove(clan.getTag());
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getTag() + ".conf");
        clanConfigPath.toFile().delete();
    }

    public static Location locFromString(String string) {
        String[] elements = string.split(";");

        if (elements.length < 3) {
            return null;
        }

        try {
            String worldName = elements[0];
            final World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }
            String xString = elements[1];
            String yString = elements[2];
            String zString = elements[3];

            int x = Integer.parseInt(xString);
            int y = Integer.parseInt(yString);
            int z = Integer.parseInt(zString);
            return new Location(world, x, y, z);
        } catch (Throwable t) {

        }

        return null;
    }
}
