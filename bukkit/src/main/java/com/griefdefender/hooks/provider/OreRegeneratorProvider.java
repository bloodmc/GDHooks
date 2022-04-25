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
package com.griefdefender.hooks.provider;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimResult;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.Event;
import com.griefdefender.api.event.QueryPermissionEvent;
import com.griefdefender.api.permission.Context;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;

import dev.mrshawn.oreregenerator.OreRegenerator;
import dev.mrshawn.oreregenerator.api.events.RegionCreateEvent;
import dev.mrshawn.oreregenerator.api.utils.RegionUtils;
import dev.mrshawn.oreregenerator.regions.Region;
import com.griefdefender.lib.kyori.adventure.text.Component;
import com.griefdefender.lib.kyori.adventure.text.format.NamedTextColor;
import com.griefdefender.lib.kyori.event.EventBus;
import com.griefdefender.lib.kyori.event.EventSubscriber;

public class OreRegeneratorProvider implements Listener {

    private static final UUID TEMP_USER_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final String ORE_REGEN_ADMIN = "oreregen.admin";

    private final OreRegenerator plugin;

    public OreRegeneratorProvider() {
        this.plugin = (OreRegenerator) Bukkit.getPluginManager().getPlugin("OreRegenerator");
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
        new CreateClaimEventListener();
        new QueryPermissionEventListener();
    }

    public boolean isOreRegenBlock(Block block) {
        if (this.plugin.getRegionManager().isInRegion(block)) {
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateRegion(RegionCreateEvent event) {
        final Player player = event.getCreator();
        final World world = event.getCreator().getWorld();
        final int x1 = event.getLocationOne().getBlockX();
        final int x2 = event.getLocationTwo().getBlockX();
        final int y1 = event.getLocationOne().getBlockY();
        final int y2 = event.getLocationTwo().getBlockY();
        final int z1 = event.getLocationOne().getBlockZ();
        final int z2 = event.getLocationTwo().getBlockZ();
        final Claim currentClaim = GriefDefender.getCore().getClaimAt(event.getLocationOne());
        if (currentClaim.isWilderness()) {
            final ClaimResult result = Claim.builder().world(world.getUID()).bounds(x1, x2, y1, y2, z1, z2).owner(TEMP_USER_UUID).type(ClaimTypes.BASIC).cuboid(true).build();
            if (!result.successful()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnable to create ore region. Reason: " + result.getResultType().name()));
            }
            return;
        } else {
            if (!currentClaim.isUserTrusted(player.getUniqueId(), TrustTypes.BUILDER)) {
                event.setCancelled(true);
                event.setReason(ChatColor.translateAlternateColorCodes('&', "&cYou do not have " + currentClaim.getOwnerName() + "'s permission to build here."));
                return;
            }

            final ClaimResult result = Claim.builder().world(world.getUID()).bounds(x1, x2, y1, y2, z1, z2).owner(TEMP_USER_UUID).type(ClaimTypes.SUBDIVISION).cuboid(true).parent(currentClaim).build();
            if (!result.successful()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUnable to create ore region. Reason: " + result.getResultType().name()));
            }
        }
    }

    private class QueryPermissionEventListener {

        public QueryPermissionEventListener() {
            final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

            eventBus.subscribe(QueryPermissionEvent.Pre.class, new EventSubscriber<QueryPermissionEvent.Pre>() {

                @Override
                public void on(QueryPermissionEvent.Pre event) throws Throwable {
                    if (GDHooks.getInstance().getOreRegeneratorProvider() != null) {
                        final Location location = (Location) event.getLocation();
                        if (GDHooks.getInstance().getOreRegeneratorProvider().isOreRegenBlock(location.getBlock())) {
                            event.getContexts().add(new Context("ore_regen_claim", "true"));
                        } else {
                            event.getContexts().add(new Context("ore_regen_claim", "false"));
                        }
                    }
                }
            });
        }
    }

    private class CreateClaimEventListener {

        public CreateClaimEventListener() {
            GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, new EventSubscriber<CreateClaimEvent>() {
                @Override
                public void on(CreateClaimEvent event) throws Throwable {
                    if (!(event instanceof CreateClaimEvent.Pre)) {
                        return;
                    }

                    final User user = event.getSourceUser();
                    if (user == null) {
                        return;
                    }
                    final Player player = Bukkit.getPlayer(user.getUniqueId());
                    if (player == null) {
                        return;
                    }

                    final Claim claim = event.getClaim();
                    final World world = Bukkit.getWorld(claim.getWorldUniqueId());
                    final Location lesser = new Location(world, claim.getLesserBoundaryCorner().getX(), claim.getLesserBoundaryCorner().getY(), claim.getLesserBoundaryCorner().getZ());
                    final Location greater = new Location(world, claim.getGreaterBoundaryCorner().getX(), claim.getGreaterBoundaryCorner().getY(), claim.getGreaterBoundaryCorner().getZ());
                    final List<Region> regions = RegionUtils.getOverlappingRegions(lesser, greater);
                    if (!regions.isEmpty()) {
                        if (!player.hasPermission(ORE_REGEN_ADMIN) && !user.canManageAdminClaims()) {
                            event.cancelled(true);
                            event.setMessage(Component.text("You do not have permission to create a claim where an Ore Regenerator region exists!.", NamedTextColor.RED));
                        }
                    }
                }
            });
        }
    }

}
