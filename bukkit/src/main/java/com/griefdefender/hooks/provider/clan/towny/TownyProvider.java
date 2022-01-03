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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.Tristate;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimResult;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.Event;
import com.griefdefender.api.event.QueryPermissionEvent;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.ClanConfig;
import com.griefdefender.hooks.provider.clan.BaseClanProvider;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.PreNewTownEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.permissions.TownyPerms;

import net.kyori.event.EventBus;
import net.kyori.event.EventSubscriber;

public class TownyProvider extends BaseClanProvider implements Listener {

    public final TownyAPI plugin;
    private Map<String, Rank> clanRankMap = new HashMap<>();
    private final List<Rank> ranks = new ArrayList<>();
    public static final UUID TEMP_USER_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    public TownyProvider() {
        super("towny");
        this.plugin = TownyAPI.getInstance();
        // Populate Clans
        for (String role : TownyPerms.getTownRanks()) {
            final GDRank rank = new GDRank(role);
            this.clanRankMap.put(role.toLowerCase(), rank);
            this.ranks.add(rank);
        }
        for (Town town : TownyUniverse.getInstance().getTowns()) {
            final GDClan gdClan = new GDClan(town);
            final String townName = getTownFriendlyName(town.getName());
            this.clanMap.put(townName, gdClan);
            final Path clanConfigPath = CLAN_DATA_PATH.resolve(townName + ".conf");
            final ClanConfig clanConfig = new ClanConfig(clanConfigPath);
            GDHooks.getInstance().getClanConfigMap().put(townName, clanConfig);
            for (Resident clanPlayer : town.getResidents()) {
                final GDClanPlayer gdClanPlayer = new GDClanPlayer(clanPlayer);
                this.clanPlayerMap.put(clanPlayer.getUUID(), gdClanPlayer);
            }
        }

        this.registerEvents();
    }

    public void registerEvents() {
        super.registerEvents();
        GriefDefender.getRegistry().registerClanProvider(this);
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
        new CreateClaimEventListener();
        new QueryPermissionEventListener();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTownCreate(TownPreClaimEvent event) {
        final int cx = event.getTownBlock().getX();
        final int cz = event.getTownBlock().getZ();
        final World world = event.getPlayer().getWorld();
        if (!this.canCreateTown(world, cx, cz)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTownCreate(PreNewTownEvent event) {
        final World world = event.getPlayer().getWorld();
        final Coord coord = Coord.parseCoord(event.getPlayer());
        if (!this.canCreateTown(world, coord.getX(), coord.getZ())) {
            event.setCancelled(true);
        }
    }

    private boolean canCreateTown(World world, int townBlockX, int townBlockZ) {
        final int sx = townBlockX << 4;
        final int sz = townBlockZ << 4;
        final int bx = sx + 15;
        final int bz = sz + 15;
        final Vector3i lesserBoundary = new Vector3i(sx, -64, sz);
        final Vector3i greaterBoundary = new Vector3i(bx, 319, bz);
        final ClaimResult result = GriefDefender.getRegistry().createBuilder(Claim.Builder.class)
                .bounds(lesserBoundary, greaterBoundary)
                .cuboid(false)
                .owner(TEMP_USER_UUID)
                .type(ClaimTypes.BASIC)
                .world(world.getUID())
                .build();
        if (!result.successful()) {
            return false;
        }
        return true;
    }

    public static String getTownFriendlyName(String name) {
        return name.replaceAll(" ", "\\_").replaceAll("[^A-Za-z0-9\\_]", "").toLowerCase();
    }

    @Override
    public List<ClanPlayer> getClanPlayers(String tag) {
        final String townName = getTownFriendlyName(tag);
        final Clan clan = this.getClan(townName);
        if (clan == null) {
            return Collections.emptyList();
        }
        return clan.getMembers();
    }

    @Override
    public List<Rank> getClanRanks(String tag) {
        return this.ranks;
    }

    @Override
    public @Nullable Clan getClan(String tag) {
        final String townName = getTownFriendlyName(tag);
        return this.clanMap.get(townName);
    }

    private class CreateClaimEventListener {

        public CreateClaimEventListener() {
            final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

            eventBus.subscribe(CreateClaimEvent.Pre.class, new EventSubscriber<CreateClaimEvent.Pre>() {

                @Override
                public void on(CreateClaimEvent.Pre event) throws Throwable {
                    final World world = Bukkit.getWorld(event.getClaim().getWorldUniqueId());
                    for (Vector3i chunkPos : event.getClaim().getChunkPositions()) {
                        final Town town = TownyAPI.getInstance().getTown(new Location(world, chunkPos.getX() << 4, 0, chunkPos.getZ() << 4));
                        if (town != null) {
                            event.cancelled(true);
                            return;
                        }
                    }
                }
            });
        }
    }

    private class QueryPermissionEventListener {

        public QueryPermissionEventListener() {
            final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

            eventBus.subscribe(QueryPermissionEvent.Pre.class, new EventSubscriber<QueryPermissionEvent.Pre>() {

                @Override
                public void on(QueryPermissionEvent.Pre event) throws Throwable {
                    if (event.getLocation() == null) {
                        return;
                    }

                    final Location location = ((Location) event.getLocation());
                    final Town town = TownyAPI.getInstance().getTown(new Location(location.getWorld(), location.getBlockX(), 0, location.getBlockZ()));
                    if (town == null) {
                        return;
                    }
                    // defer to Towny
                    event.setFinalResult(Tristate.TRUE);
                }
            });
        }
    }
}
