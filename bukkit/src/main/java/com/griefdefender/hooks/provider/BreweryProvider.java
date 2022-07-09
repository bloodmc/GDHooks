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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.dre.brewery.BCauldron;
import com.dre.brewery.Barrel;
import com.dre.brewery.api.events.barrel.BarrelAccessEvent;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.hooks.GDHooksBootstrap;

public class BreweryProvider implements Listener {

    public BreweryProvider() {
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
    }

    public void onBarrelAccess(BarrelAccessEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(player.getWorld().getUID(), player.getUniqueId());
        final Location location = event.getClickedBlock().getLocation();
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        if (claim == null || !GriefDefender.getCore().isEnabled(player.getWorld().getUID()) || playerData.canIgnoreClaim(claim)) {
            return;
        }

        if (playerData.inPvpCombat()) {
            event.setCancelled(true);
            return;
        }

        if (!playerData.canUseBlock(location, TrustTypes.CONTAINER, false, false)) {
            event.setCancelled(true);
            return;
        }
    }

    public String getBlockId(Block block) {
        final Barrel barrel = Barrel.get(block);
        if (barrel != null) {
            return "brewery:barrel";
        }
        final BCauldron cauldron = BCauldron.get(block);
        if (cauldron != null) {
            return "brewery:cauldron";
        }
        return null;
    }
}
