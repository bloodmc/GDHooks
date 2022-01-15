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
import com.griefdefender.hooks.provider.clan.guilds.GDClanPlayer;
import com.griefdefender.hooks.provider.clan.guilds.GuildsClanProvider;

import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.api.events.GuildKickEvent;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.api.events.GuildSetHomeEvent;
import com.griefdefender.lib.kyori.adventure.text.Component;

public class GuildsEventHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanSetHome(GuildSetHomeEvent event) {
        final World world = event.getLocation().getWorld();
        final Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
        if (player == null || !GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final PlayerData playerData  = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        final Claim claim = GriefDefender.getCore().getClaimAt(event.getLocation());
        if (playerData.canIgnoreClaim(claim)) {
            return;
        }

        if (GDHooks.getInstance().getConfig().getData().clan.clanRequireTown) {
            if (!claim.isInTown()) {
                event.setCancelled(true);
                GriefDefender.getAudienceProvider().getSender(player).sendMessage(Component.text("You must be in a town in order to set your clan home."));
                return;
            }
        }

        if (!claim.isUserTrusted(player.getUniqueId(), TrustTypes.MANAGER)) {
            event.setCancelled(true);
            GriefDefender.getAudienceProvider().getSender(player).sendMessage(Component.text("You do not own this claim."));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanCreate(GuildCreateEvent event) {
        if (!GDHooks.getInstance().getConfig().getData().clan.clanRequireTown) {
            ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).addClan(event.getGuild());
            ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().put(event.getPlayer().getUniqueId(), new GDClanPlayer(event.getGuild(), event.getPlayer().getUniqueId()));
            GDHooks.getInstance().updateClanCompletions();
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
                ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).addClan(event.getGuild());
                ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().put(event.getPlayer().getUniqueId(), new GDClanPlayer(event.getGuild(), event.getPlayer().getUniqueId()));
                GDHooks.getInstance().updateClanCompletions();
                return;
            }
        }
        event.setCancelled(true);
        GriefDefender.getAudienceProvider().getSender(player).sendMessage(Component.text("You must own a town in order to create a clan."));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanDisband(GuildRemoveEvent event) {
        ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).removeClan(event.getGuild());
        GDHooks.getInstance().updateClanCompletions();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanPlayerJoin(GuildJoinEvent event) {
        ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().put(event.getPlayer().getUniqueId(), new GDClanPlayer(event.getGuild(), event.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanPlayerLeave(GuildLeaveEvent event) {
        ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClanPlayerLeave(GuildKickEvent event) {
        ((GuildsClanProvider) GDHooks.getInstance().getClanProvider()).getClanPlayerMap().remove(event.getPlayer().getUniqueId());
    }
}
