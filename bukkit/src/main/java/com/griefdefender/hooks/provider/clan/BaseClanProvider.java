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
package com.griefdefender.hooks.provider.clan;

import java.nio.file.Path;
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
import com.griefdefender.api.User;
import com.griefdefender.api.claim.TrustResult;
import com.griefdefender.api.claim.TrustResultTypes;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.api.event.Event;
import com.griefdefender.api.event.ProcessTrustUserEvent;
import com.griefdefender.api.provider.ClanProvider;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksAttributes;
import com.griefdefender.hooks.config.ClanConfig;

import com.griefdefender.lib.kyori.event.EventBus;
import com.griefdefender.lib.kyori.event.EventSubscriber;

public abstract class BaseClanProvider implements ClanProvider {

    protected final Map<String, Clan> clanMap = new HashMap<>();
    protected final Map<UUID, ClanPlayer> clanPlayerMap = new HashMap<>();
    protected final Path CLAN_DATA_PATH;

    public BaseClanProvider(String pluginId) {
        CLAN_DATA_PATH = GDHooks.getInstance().getConfigPath().resolve("clandata").resolve(pluginId);
    }

    protected void registerEvents() {
        new ProcessTrustUserEventListener();
    }

    @Override
    public @Nullable Clan getClan(String tag) {
        return this.clanMap.get(tag);
    }

    @Override
    public List<Clan> getAllClans() {
        final List<Clan> clans = new ArrayList<>(this.clanMap.values());
        return Collections.unmodifiableList(clans);
    }

    @Override
    public @Nullable ClanPlayer getClanPlayer(UUID playerUniqueId) {
        return this.clanPlayerMap.get(playerUniqueId);
    }

    @Override
    public List<ClanPlayer> getAllClanPlayers() {
        final List<ClanPlayer> clanPlayers = new ArrayList<>(this.clanPlayerMap.values());
        return Collections.unmodifiableList(clanPlayers);
    }

    @Override
    public List<ClanPlayer> getClanPlayers(String tag) {
        final Clan clan = this.getClan(tag);
        if (clan == null) {
            return Collections.emptyList();
        }

        return clan.getMembers();
    }

    public Map<String, Clan> getClanMap() {
        return this.clanMap;
    }

    public Map<UUID, ClanPlayer> getClanPlayerMap() {
        return this.clanPlayerMap;
    }

    @Override
    public List<Rank> getClanRanks(String tag) {
        final Clan clan = this.getClan(tag);
        if (clan == null) {
            return new ArrayList<>();
        }

        return clan.getRanks();
    }

    private class ProcessTrustUserEventListener {

        public ProcessTrustUserEventListener() {
            final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

            eventBus.subscribe(ProcessTrustUserEvent.class, new EventSubscriber<ProcessTrustUserEvent>() {

                @Override
                public void on(@NonNull ProcessTrustUserEvent event) throws Throwable {
                    if (event.getFinalTrustResult().successful() || !event.getClaim().hasAttribute(GDHooksAttributes.ATTRIBUTE_CLAN)) {
                        return;
                    }

                    final User user = event.getUser();
                    if (user == null) {
                        return;
                    }
                    final Player player = Bukkit.getPlayer(user.getUniqueId());
                    if (player == null) {
                        return;
                    }

                    final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(user.getUniqueId());
                    if (clanPlayer == null) {
                        return;
                    }

                    final Clan clan = clanPlayer.getClan();
                    if (clan == null) {
                        return;
                    }

                    final ClanPlayer ownerPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(event.getClaim().getOwnerUniqueId());
                    if (ownerPlayer == null) {
                        return;
                    }
                    final Clan ownerClan = ownerPlayer.getClan();
                    if (ownerClan == null) {
                        return;
                    }
                    if (!ownerClan.getTag().equals(clan.getTag()) && !ownerClan.isAlly(clan.getTag())) {
                    }

                    final ClanConfig clanConfig = GDHooks.getInstance().getClanConfigMap().get(ownerClan.getTag());
                    if (clanConfig == null) {
                        return;
                    }

                    final TrustType trustType = clanConfig.getData().getRankTrust(clanPlayer.getRank().getName());
                    if (trustType == TrustTypes.ACCESSOR && event.getTrustType() != TrustTypes.ACCESSOR) {
                        return;
                    }
                    if (trustType == TrustTypes.RESIDENT && event.getTrustType() != TrustTypes.RESIDENT) {
                        return;
                    }
                    if (trustType == TrustTypes.BUILDER && event.getTrustType() == TrustTypes.MANAGER) {
                        return;
                    }
                    if (trustType == TrustTypes.CONTAINER && (event.getTrustType() == TrustTypes.BUILDER || event.getTrustType() == TrustTypes.MANAGER)) {
                        return;
                    }
                    if (trustType == TrustTypes.RESIDENT && !event.getClaim().isAdminClaim() && !event.getClaim().isTown()) {
                        return;
                    }

                    if (clan.getId().equalsIgnoreCase(ownerClan.getId())) {
                        final TrustResult trustResult = TrustResult.builder().user(event.getUser()).claims(event.getClaims()).trust(event.getTrustType()).type(TrustResultTypes.TRUSTED).build();
                        event.setNewTrustResult(trustResult);
                        return;
                    }

                    // check owner allies
                    for (Clan ally : ownerClan.getAllies()) {
                        if (clan.getId().equalsIgnoreCase(ally.getId())) {
                            final TrustResult trustResult = TrustResult.builder().user(event.getUser()).claims(event.getClaims()).trust(event.getTrustType()).type(TrustResultTypes.TRUSTED).build();
                            event.setNewTrustResult(trustResult);
                            return;
                        }
                    }
                }
            });
        }
    }
}
