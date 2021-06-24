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
package com.griefdefender.hooks.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.provider.simpleclans.GDClan;
import com.griefdefender.hooks.provider.simpleclans.GDClanPlayer;
import com.griefdefender.hooks.provider.simpleclans.SimpleClanProvider;

import net.kyori.adventure.text.Component;
import net.sacredlabyrinth.phaed.simpleclans.events.CreateClanEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.DisbandClanEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerHomeSetEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerJoinedClanEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerKickedClanEvent;
import net.sacredlabyrinth.phaed.simpleclans.events.PreCreateClanEvent;

public class SimpleClansEventHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanSetHome(PlayerHomeSetEvent event) {
        final World world = event.getLocation().getWorld();
        final Player player = Bukkit.getPlayer(event.getClanPlayer().getUniqueId());
        if (player == null || !GDHooks.getInstance().getConfig().getData().clan.clanRequireTown || !GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Claim claim = GriefDefender.getCore().getClaimAt(event.getLocation());
        if (!claim.isInTown()) {
            event.setCancelled(true);
            GriefDefender.getAudienceProvider().getSender(player).sendMessage(Component.text("You must be in a town in order to set your clan home."));
            return;
        }

        if (!claim.isUserTrusted(player.getUniqueId(), TrustTypes.MANAGER)) {
            event.setCancelled(true);
            GriefDefender.getAudienceProvider().getSender(player).sendMessage(Component.text("You do not own this town."));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanCreate(PreCreateClanEvent event) {
        if (!GDHooks.getInstance().getConfig().getData().clan.clanRequireTown) {
            return;
        }

        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final PlayerData playerData  = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        for (Claim claim : playerData.getClaims()) {
            if (claim.isTown()) {
                return;
            }
        }
        event.setCancelled(true);
        GriefDefender.getAudienceProvider().getSender(player).sendMessage(Component.text("You must own a town in order to create a clan."));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanCreatePost(CreateClanEvent event) {
        ((SimpleClanProvider) GDHooks.getInstance().getClanProvider()).getClanMap().put(event.getClan().getTag(), new GDClan(event.getClan()));
        GDHooks.getInstance().updateClanCompletions();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanDisband(DisbandClanEvent event) {
        ((SimpleClanProvider) GDHooks.getInstance().getClanProvider()).getClanMap().remove(event.getClan().getTag());
        GDHooks.getInstance().updateClanCompletions();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanPlayerJoin(PlayerJoinedClanEvent event) {
        ((SimpleClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().put(event.getClanPlayer().getUniqueId(), new GDClanPlayer(event.getClanPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanPlayerRemove(PlayerKickedClanEvent event) {
        ((SimpleClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().remove(event.getClanPlayer().getUniqueId());
    }
}
