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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;

import net.sacredlabyrinth.phaed.simpleclans.hooks.protection.Land;
import net.sacredlabyrinth.phaed.simpleclans.hooks.protection.ProtectionProvider;

public class SimpleClanProtectionProvider implements ProtectionProvider {

    @Override
    public void setup() {
    }

    @Override
    public @NotNull Set<Land> getLandsAt(@NotNull Location location) {
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        if (claim == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(getLand(claim));
    }

    @Nullable
    private Land getLand(@Nullable Claim claim) {
        if (claim == null) {
            return null;
        }
        return new Land(claim.getUniqueId().toString(), Collections.singleton(claim.getOwnerUniqueId()));
    }

    @Override
    public @NotNull Set<Land> getLandsOf(@NotNull OfflinePlayer player, @NotNull World world) {
        HashSet<Land> lands = new HashSet<>();
        for (Claim claim : GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId()).getClaims()) {
            if (claim == null) {
                continue;
            }
            lands.add(getLand(claim));
        }
        return lands;
    }

    @Override
    public @NotNull String getIdPrefix() {
        return "gd";
    }

    @Override
    public void deleteLand(@NotNull String id, @NotNull World world) {
        final ClaimManager claimManager = GriefDefender.getCore().getClaimManager(world.getUID());
        UUID uuid = null;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return;
        }
        final Claim claim = claimManager.getClaimByUUID(uuid);
        if (claim != null) {
            claimManager.deleteClaim(claim, false);
        }
    }

    @Override
    public @Nullable Class<? extends Event> getCreateLandEvent() {
        return CreateClaimBukkitEvent.class;
    }

    @Override
    public @Nullable Player getPlayer(Event event) {
        if (!(event instanceof CreateClaimBukkitEvent)) {
            return null;
        }

        final CreateClaimBukkitEvent createEvent = (CreateClaimBukkitEvent) event;
        return createEvent.getCreator();
    }

    @Override
    public @Nullable String getRequiredPluginName() {
        return "GriefDefender";
    }

}
