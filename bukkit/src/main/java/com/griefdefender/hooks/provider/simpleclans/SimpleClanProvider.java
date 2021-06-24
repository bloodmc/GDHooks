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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.provider.ClanProvider;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.listener.SimpleClansEventHandler;

import net.kyori.event.EventSubscriber;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class SimpleClanProvider implements ClanProvider {

    private final SimpleClans plugin;
    private Map<String, Clan> clanMap = new HashMap<>();
    private Map<UUID, ClanPlayer> clanPlayerMap = new HashMap<>();

    public SimpleClanProvider() {
        this.plugin = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
        // Populate Clans
        for (net.sacredlabyrinth.phaed.simpleclans.Clan clan : this.plugin.getClanManager().getClans()) {
            final GDClan gdClan = new GDClan(clan);
            this.clanMap.put(clan.getTag(), gdClan);
        }
        for (net.sacredlabyrinth.phaed.simpleclans.ClanPlayer clanPlayer : this.plugin.getClanManager().getAllClanPlayers()) {
            final GDClanPlayer gdClanPlayer = new GDClanPlayer(clanPlayer);
            this.clanPlayerMap.put(clanPlayer.getUniqueId(), gdClanPlayer);
        }

        new CreateClaimEventListener();
        GriefDefender.getRegistry().registerClanProvider(this);
        Bukkit.getPluginManager().registerEvents(new SimpleClansEventHandler(), GDHooksBootstrap.getInstance().getLoader());
    }

    public boolean clanExists(String clanName) {
        for (net.sacredlabyrinth.phaed.simpleclans.Clan clan : this.plugin.getClanManager().getClans()) {
            if (clan.getName().equalsIgnoreCase(clanName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @Nullable Clan getClan(String tag) {
        return this.clanMap.get(tag);
    }

    @Override
    public List<Clan> getClans() {
        final List<Clan> clans = new ArrayList<>(this.clanMap.values());
        return Collections.unmodifiableList(clans);
    }

    @Override
    public @Nullable ClanPlayer getClanPlayer(UUID playerUniqueId) {
        return this.clanPlayerMap.get(playerUniqueId);
    }

    @Override
    public List<ClanPlayer> getClanPlayers() {
        final List<ClanPlayer> clanPlayers = new ArrayList<>(this.clanPlayerMap.values());
        return Collections.unmodifiableList(clanPlayers);
    }

    public Map<String, Clan> getClanMap() {
        return this.clanMap;
    }

    public Map<UUID, ClanPlayer> getClanPlayerMap() {
        return this.clanPlayerMap;
    }

    public static class CreateClaimEventListener {

        public CreateClaimEventListener() {
            GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, new EventSubscriber<CreateClaimEvent>() {
                @Override
                public void on(@NonNull CreateClaimEvent event) throws Throwable {
                    final Player player = event.getCause().first(Player.class).orElse(null);
                    if (player == null) {
                        return;
                    }

                    CreateClaimBukkitEvent bukkitEvent = new CreateClaimBukkitEvent(event.getClaim(), player);
                    Bukkit.getPluginManager().callEvent(bukkitEvent);
                    if (bukkitEvent.isCancelled()) {
                        event.cancelled(true);
                    }
                }
            });
        }
    }
}
