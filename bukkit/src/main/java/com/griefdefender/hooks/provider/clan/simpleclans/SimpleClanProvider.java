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
package com.griefdefender.hooks.provider.clan.simpleclans;

import java.nio.file.Path;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.ClanConfig;
import com.griefdefender.hooks.listener.SimpleClansEventHandler;
import com.griefdefender.hooks.provider.clan.BaseClanProvider;

import com.griefdefender.lib.kyori.event.EventSubscriber;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class SimpleClanProvider extends BaseClanProvider {

    private final SimpleClans plugin;

    public SimpleClanProvider() {
        super("simpleclans");
        this.plugin = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
        // Populate Clans
        for (net.sacredlabyrinth.phaed.simpleclans.Clan clan : this.plugin.getClanManager().getClans()) {
            final GDClan gdClan = new GDClan(clan);
            this.clanMap.put(clan.getTag().toLowerCase(), gdClan);
            final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getTag().toLowerCase() + ".conf");
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(clan.getTag().toLowerCase(), clanConfig);
        }
        for (net.sacredlabyrinth.phaed.simpleclans.ClanPlayer clanPlayer : this.plugin.getClanManager().getAllClanPlayers()) {
            final GDClanPlayer gdClanPlayer = new GDClanPlayer(clanPlayer);
            this.clanPlayerMap.put(clanPlayer.getUniqueId(), gdClanPlayer);
        }

        this.registerEvents();
        this.plugin.getProtectionManager().registerProvider(new SimpleClanProtectionProvider());
    }

    public void registerEvents() {
        super.registerEvents();
        new CreateClaimEventListener();
        GriefDefender.getRegistry().registerClanProvider(this);
        Bukkit.getPluginManager().registerEvents(new SimpleClansEventHandler(), GDHooksBootstrap.getInstance().getLoader());
    }

    public void addClan(net.sacredlabyrinth.phaed.simpleclans.Clan clan) {
        this.clanMap.put(clan.getTag(), new GDClan(clan));
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getTag() + ".conf");
        if (!clanConfigPath.toFile().exists()) {
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(clan.getTag(), clanConfig);
        }
    }

    public void removeClan(net.sacredlabyrinth.phaed.simpleclans.Clan clan) {
        this.clanMap.remove(clan.getTag());
        GDHooks.getInstance().getClanConfigMap().remove(clan.getTag());
        final Path clanConfigPath = CLAN_DATA_PATH.resolve(clan.getTag() + ".conf");
        clanConfigPath.toFile().delete();
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
