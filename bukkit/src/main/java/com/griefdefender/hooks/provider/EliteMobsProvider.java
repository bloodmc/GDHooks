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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.Tristate;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.flag.Flags;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.context.ContextGroups;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.powers.ElitePower;

public class EliteMobsProvider implements Listener {

    private final static Context ELITE_TYPE = new Context("elitemobs_type", "elite");

    public EliteMobsProvider() {
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEliteMobSpawn(EliteMobSpawnEvent event) {
        final Entity entity = event.getEntity();
        final World world = entity.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final EliteMobEntity eliteMobEntity = event.getEliteMobEntity();
        final Claim claim = GriefDefender.getCore().getClaimAt(entity.getLocation());
        final Set<Context> contexts = this.getEliteMobContexts(entity, eliteMobEntity);
        contexts.add(new Context("source", "spawnreason:" + event.getReason().name().toLowerCase()));
        contexts.add(new Context("source", "spawnreason:" + event.getReason().name().toLowerCase()));
        final EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(entity.getType());
        final String name = "elitemobs:" + ChatColor.stripColor(eliteMobProperties.getName()).replace(" ", "_").replace(String.valueOf(eliteMobEntity.getLevel()), "").replace("Lvl_$level_", "").replaceAll("[^A-Za-z0-9_]", "").toLowerCase();
        contexts.add(new Context("target", name));
        final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, entity.getLocation(), claim, null, Flags.ENTITY_SPAWN, event.getReason(), entity, contexts, null, true);
        if (result == Tristate.FALSE) {
            event.setCancelled(true);
        }
    }

    public boolean isEliteMob(Entity entity) {
        return EntityTracker.isEliteMob(entity);
    }

    public Set<Context> getEliteMobContexts(Entity entity) {
        return this.getEliteMobContexts(entity, null);
    }

    public Set<Context> getEliteMobContexts(Entity entity, EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity == null) {
            eliteMobEntity = EntityTracker.getEliteMobEntity(entity);
            if (eliteMobEntity == null) {
                return Collections.emptySet();
            }
        }

        final Set<Context> contexts = new HashSet<>();
        contexts.add(ContextGroups.TARGET_ELITEMOBS);
        contexts.add(new Context("elitemobs_level", String.valueOf(eliteMobEntity.getLevel())));
        contexts.add(new Context("elitemobs_has_special_loot", String.valueOf(eliteMobEntity.getHasSpecialLoot())));
        contexts.add(new Context("regional_boss", String.valueOf(eliteMobEntity.isRegionalBoss())));
        // add powers
        for (ElitePower power : eliteMobEntity.getPowers()) {
            contexts.add(new Context("elitemobs_power", power.getName().toLowerCase()));
        }
        if (eliteMobEntity.customBossEntity != null) {
            contexts.add(new Context("elitemobs_type", eliteMobEntity.customBossEntity.customBossConfigFields.getFileName().replace(".yml", "").toLowerCase()));
            if (eliteMobEntity.customBossEntity.customBossMount != null) {
                contexts.add(new Context("elitemobs_mount", eliteMobEntity.customBossEntity.customBossMount.customBossConfigFields.getFileName().replace(".yml", "").toLowerCase()));
            }
        } else {
            contexts.add(ELITE_TYPE);
        }
        return contexts;
    }

    public String getEliteMobId(Entity entity) {
        final EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(entity);
        if (eliteMobEntity == null) {
            return null;
        }
        String eliteMobName = eliteMobEntity.getName();
        if (eliteMobName == null) {
            final EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(entity.getType());
            eliteMobName = eliteMobProperties.getName();
        }
        String id = "elitemobs:" + ChatColor.stripColor(eliteMobName).replace(" ", "_").replace(String.valueOf(eliteMobEntity.getLevel()), "").replace("Lvl_$level_", "").replaceAll("[^A-Za-z0-9_]", "").toLowerCase();
        if (id.startsWith("_")) {
            id = id.substring(1);
        }
        return id;
    }
}
