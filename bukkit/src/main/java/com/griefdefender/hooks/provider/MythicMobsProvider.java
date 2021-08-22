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

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.Tristate;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.ContextKeys;
import com.griefdefender.api.permission.flag.Flags;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.context.ContextGroups;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;

public class MythicMobsProvider implements Listener {

    public MythicMobsProvider() {
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
    }

    public boolean isMythicMob(Entity entity) {
        final ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(entity);
        if (activeMob == null) {
            return false;
        }

        return true;
    }

    public String getMobType(Entity entity) {
        final ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(entity);
        if (activeMob == null) {
            return null;
        }

        return activeMob.getMobType();
    }

    public Set<Context> getMobContexts(Entity entity) {
        final ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(entity);
        if (activeMob == null) {
            return null;
        }

        final Set<Context> contexts = new HashSet<>();
        contexts.add(new Context("mythicmobs_level", String.valueOf(activeMob.getLevel())));
        contexts.add(new Context("mythicmobs_power", String.valueOf(activeMob.getPower())));
        return contexts;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpawnEvent(MythicMobSpawnEvent event) {
        final Entity entity = event.getEntity();
        final World world = entity.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final String source = event.getMythicSpawner() != null ? event.getMythicSpawner().getName().toLowerCase() : null;
        final Set<Context> contexts = new HashSet<>();
        contexts.add(new Context("mythicmobs_level", String.valueOf(event.getMobLevel())));
        contexts.add(ContextGroups.TARGET_MYTHICMOBS);
        contexts.add(ContextGroups.TARGET_MONSTER);
        contexts.add(new Context(ContextKeys.TARGET, "#mythicmobs:monster"));
        final String id = "mythicmobs:" + event.getMobType().getInternalName().replace(" ", "_").toLowerCase();
        final Location location = entity.getLocation();
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, null, Flags.ENTITY_SPAWN, source, id, contexts, null, true);
        if (result == Tristate.FALSE) {
            event.setCancelled();
        }
    }
}
