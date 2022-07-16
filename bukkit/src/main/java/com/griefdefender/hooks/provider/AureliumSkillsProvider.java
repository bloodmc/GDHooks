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

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.api.event.CustomRegenEvent;
import com.archyx.aureliumskills.api.event.ManaAbilityActivateEvent;
import com.archyx.aureliumskills.api.event.ManaRegenerateEvent;
import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import com.archyx.aureliumskills.api.event.XpGainEvent;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.Tristate;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.flag.Flag;
import com.griefdefender.api.permission.option.Option;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.lib.geantyref.TypeToken;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class AureliumSkillsProvider implements Listener {

    private final AureliumSkills plugin;
    private final Option<Double> XP_GAIN_MODIFIER;
    public final Flag ABILITY_ACTIVATE;
    public final Flag ABILITY_REFRESH;
    public final Flag LOOT_DROP;
    public final Flag MANA_REGEN;
    public final Flag HEALTH_REGEN;

    public AureliumSkillsProvider() {
        this.plugin = (AureliumSkills) Bukkit.getPluginManager().getPlugin("AureliumSkills");
        XP_GAIN_MODIFIER = Option.builder(Double.class)
                .id("aureliumskills:xp-gain-modifier")
                .name("aureliumskills:xp-gain-modifier")
                .defaultValue(1.0)
                .build();
        GriefDefender.getRegistry().getRegistryModuleFor(Option.class).get().registerCustomType(XP_GAIN_MODIFIER);
        GDHooks.getInstance().getLogger().info("Registered Aurelium option '" + XP_GAIN_MODIFIER.getName() + "'.");
        ABILITY_ACTIVATE = Flag.builder()
                .id("aureliumskills:ability-activate")
                .name("ability-activate")
                .permission("griefdefender.flag.aureliumskills.ability-activate")
                .build();
        ABILITY_REFRESH = Flag.builder()
                .id("aureliumskills:ability-refresh")
                .name("ability-refresh")
                .permission("griefdefender.flag.aureliumskills.ability-refresh")
                .build();
        HEALTH_REGEN = Flag.builder()
                .id("aureliumskills:health-regen")
                .name("health-regen")
                .permission("griefdefender.flag.aureliumskills.mana-regen")
                .build();
        LOOT_DROP = Flag.builder()
                .id("aureliumskills:loot-drop")
                .name("loot-drop")
                .permission("griefdefender.flag.aureliumskills.loot-drop")
                .build();
        MANA_REGEN = Flag.builder()
                .id("aureliumskills:mana-regen")
                .name("mana-regen")
                .permission("griefdefender.flag.aureliumskills.mana-regen")
                .build();
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(ABILITY_ACTIVATE);
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(ABILITY_REFRESH);
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(HEALTH_REGEN);
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(LOOT_DROP);
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(MANA_REGEN);
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerXpGain(XpGainEvent event) {
        final String skillType = event.getSkill().name().toLowerCase().replace(" ", "_");
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Set<Context> contexts = new HashSet<>();
        if (event.getSkill().getManaAbility() != null) {
            contexts.add(new Context("aureliumskills:mana_ability", String.valueOf(event.getSkill().getManaAbility().name().toLowerCase())));
        }
        contexts.add(new Context("aureliumskills:skill_type", skillType));
        contexts.add(new Context("aureliumskills:skill_level", String.valueOf(AureliumAPI.getSkillLevel(player, event.getSkill()))));
        final com.archyx.aureliumskills.data.PlayerData aureliumPlayerData = this.plugin.getPlayerManager().getPlayerData(player);
        if (aureliumPlayerData != null) {
            contexts.add(new Context("aureliumskills:power_level", String.valueOf(aureliumPlayerData.getPowerLevel())));
        }
        final Location location = player.getLocation();
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        final Double xpGained = GriefDefender.getPermissionManager().getActiveOptionValue(TypeToken.get(Double.class), XP_GAIN_MODIFIER, playerData.getUser(), claim, contexts);
        if (xpGained != null && xpGained != 1.0) {
            event.setAmount(event.getAmount() * xpGained.floatValue());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerAbilityActivate(ManaAbilityActivateEvent event) {
       final Player player = event.getPlayer();
       final World world = player.getWorld();
       if (!GriefDefender.getCore().isEnabled(world.getUID())) {
           return;
       }

       final Location location = player.getLocation();
       final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
       final Claim claim = GriefDefender.getCore().getClaimAt(location);
       final String ability = event.getManaAbility().name().toLowerCase().replace(" ", "_");
       final Set<Context> contexts = new HashSet<>();
       contexts.add(new Context("aureliumskills:skill_type", event.getManaAbility().getSkill().name().toLowerCase()));
       contexts.add(new Context("aureliumskills:skill_level", String.valueOf(AureliumAPI.getSkillLevel(player, event.getManaAbility().getSkill()))));
       final com.archyx.aureliumskills.data.PlayerData aureliumPlayerData = this.plugin.getPlayerManager().getPlayerData(player);
       if (aureliumPlayerData != null) {
           contexts.add(new Context("aureliumskills:power_level", String.valueOf(aureliumPlayerData.getPowerLevel())));
       }

       final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), ABILITY_ACTIVATE, player, ability, contexts, null, true);
       if (result == Tristate.FALSE) {
           event.setCancelled(true);
       }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHealthRegen(CustomRegenEvent event) {
       final Player player = event.getPlayer();
       final World world = player.getWorld();
       if (!GriefDefender.getCore().isEnabled(world.getUID())) {
           return;
       }

       final Location location = player.getLocation();
       final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
       final Claim claim = GriefDefender.getCore().getClaimAt(location);
       final Set<Context> contexts = new HashSet<>();
       contexts.add(new Context("aureliumskills:regen_amount", String.valueOf(event.getAmount())));
       final com.archyx.aureliumskills.data.PlayerData aureliumPlayerData = this.plugin.getPlayerManager().getPlayerData(player);
       if (aureliumPlayerData != null) {
           contexts.add(new Context("aureliumskills:power_level", String.valueOf(aureliumPlayerData.getPowerLevel())));
       }

       final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), HEALTH_REGEN, player, player, contexts, null, true);
       if (result == Tristate.FALSE) {
           event.setCancelled(true);
       }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onManaRegen(ManaRegenerateEvent event) {
       final Player player = event.getPlayer();
       final World world = player.getWorld();
       if (!GriefDefender.getCore().isEnabled(world.getUID())) {
           return;
       }

       final Location location = player.getLocation();
       final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
       final Claim claim = GriefDefender.getCore().getClaimAt(location);
       final Set<Context> contexts = new HashSet<>();
       contexts.add(new Context("aureliumskills:mana_amount", String.valueOf(event.getAmount())));
       final com.archyx.aureliumskills.data.PlayerData aureliumPlayerData = this.plugin.getPlayerManager().getPlayerData(player);
       if (aureliumPlayerData != null) {
           contexts.add(new Context("aureliumskills:power_level", String.valueOf(aureliumPlayerData.getPowerLevel())));
       }

       final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), MANA_REGEN, player, player, contexts, null, true);
       if (result == Tristate.FALSE) {
           event.setCancelled(true);
       }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLootDrop(PlayerLootDropEvent event) {
       final Player player = event.getPlayer();
       final World world = player.getWorld();
       if (!GriefDefender.getCore().isEnabled(world.getUID())) {
           return;
       }

       final Location location = player.getLocation();
       final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
       final Claim claim = GriefDefender.getCore().getClaimAt(location);
       final Set<Context> contexts = new HashSet<>();
       contexts.add(new Context("aureliumskills:loot_drop_cause", String.valueOf(event.getCause().name().toLowerCase())));
       final com.archyx.aureliumskills.data.PlayerData aureliumPlayerData = this.plugin.getPlayerManager().getPlayerData(player);
       if (aureliumPlayerData != null) {
           contexts.add(new Context("aureliumskills:power_level", String.valueOf(aureliumPlayerData.getPowerLevel())));
       }

       final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), LOOT_DROP, player, event.getItemStack(), contexts, TrustTypes.ACCESSOR, true);
       if (result == Tristate.FALSE) {
           event.setCancelled(true);
       }
    }
}
