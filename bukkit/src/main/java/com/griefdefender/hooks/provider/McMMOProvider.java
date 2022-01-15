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

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.events.hardcore.McMMOPlayerDeathPenaltyEvent;
import com.gmail.nossr50.events.party.McMMOPartyAllianceChangeEvent;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;
import com.gmail.nossr50.events.skills.McMMOPlayerSkillEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityDeactivateEvent;
import com.gmail.nossr50.events.skills.alchemy.McMMOPlayerBrewEvent;
import com.gmail.nossr50.events.skills.alchemy.McMMOPlayerCatalysisEvent;
import com.gmail.nossr50.events.skills.fishing.McMMOPlayerFishingEvent;
import com.gmail.nossr50.events.skills.repair.McMMOPlayerRepairCheckEvent;
import com.gmail.nossr50.events.skills.rupture.McMMOEntityDamageByRuptureEvent;
import com.gmail.nossr50.events.skills.salvage.McMMOPlayerSalvageCheckEvent;
import com.gmail.nossr50.events.skills.unarmed.McMMOPlayerDisarmEvent;
import com.gmail.nossr50.runnables.skills.AbilityDisableTask;
import com.gmail.nossr50.util.player.UserManager;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.Tristate;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustResult;
import com.griefdefender.api.claim.TrustResultTypes;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.event.BorderClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.Event;
import com.griefdefender.api.event.ProcessTrustUserEvent;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.flag.Flag;
import com.griefdefender.api.permission.option.Option;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.permission.GDHooksPermissions;
import com.griefdefender.hooks.provider.mcmmo.McMMOPlayerAbilityData;

import com.griefdefender.lib.geantyref.TypeToken;
import com.griefdefender.lib.kyori.event.EventBus;
import com.griefdefender.lib.kyori.event.EventSubscriber;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class McMMOProvider implements Listener {

    private final Option<Boolean> MCMMO_DEATH_PENALTY;
    private final Option<Double> MCMMO_XP_GAIN_MODIFIER;
    //private final Option<Double> MCMMO_SKILL_RESULT_MODIFIER;
    public final Flag ABILITY_ACTIVATE;
    public final Flag PLAYER_DISARM;
    public final Flag SKILL_USE;
    public final Flag RUPTURE_DAMAGE;
    //private final Option<List<String>> MCMMO_DENY_SKILLS;
    private final Map<UUID, McMMOPlayerAbilityData> playerAbilityMap = new HashMap<>();

    public McMMOProvider() {
        // register custom mcmmo options
        MCMMO_DEATH_PENALTY = Option.builder(Boolean.class)
                .id("mcmmo:death-penalty")
                .name("mcmmo:death-penalty")
                .defaultValue(true)
                .build();
        MCMMO_XP_GAIN_MODIFIER = Option.builder(Double.class)
                .id("mcmmo:xp-gain-modifier")
                .name("mcmmo:xp-gain-modifier")
                .defaultValue(1.0)
                .build();
        GriefDefender.getRegistry().getRegistryModuleFor(Option.class).get().registerCustomType(MCMMO_DEATH_PENALTY);
        GDHooks.getInstance().getLogger().info("Registered McMMO option '" + MCMMO_DEATH_PENALTY.getName() + "'.");
        GriefDefender.getRegistry().getRegistryModuleFor(Option.class).get().registerCustomType(MCMMO_XP_GAIN_MODIFIER);
        GDHooks.getInstance().getLogger().info("Registered McMMO option '" + MCMMO_XP_GAIN_MODIFIER.getName() + "'.");
        ABILITY_ACTIVATE = Flag.builder()
                .id("mcmmo:ability-activate")
                .name("ability-activate")
                .permission("griefdefender.flag.mcmmo.ability-activate")
                .build();
        PLAYER_DISARM = Flag.builder()
                .id("mcmmo:player-disarm")
                .name("player-disarm")
                .permission("griefdefender.flag.mcmmo.player-disarm")
                .build();
        RUPTURE_DAMAGE = Flag.builder()
                .id("mcmmo:rupture-damage")
                .name("rupture-damage")
                .permission("griefdefender.flag.mcmmo.rupture-damage")
                .build();
        SKILL_USE = Flag.builder()
                .id("mcmmo:skill-use")
                .name("skill-use")
                .permission("griefdefender.flag.mcmmo.skill-use")
                .build();
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(ABILITY_ACTIVATE);
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(PLAYER_DISARM);
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(RUPTURE_DAMAGE);
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(SKILL_USE);
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
        new BorderClaimEventListener();
        new CreateClaimEventListener();
        new ProcessTrustUserEventListener();
    }

    public Map<UUID, McMMOPlayerAbilityData> getPlayerAbilityMap() {
        return this.playerAbilityMap;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerXpGain(McMMOPlayerXpGainEvent event) {
        final String skillType = event.getSkill().name().toLowerCase().replace(" ", "_");
        final String reason = event.getXpGainReason().name().toLowerCase();
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Set<Context> contexts = new HashSet<>();
        contexts.add(new Context("mcmmo:skill_level", String.valueOf(event.getSkillLevel())));
        contexts.add(new Context("mcmmo:skill_type", skillType));
        contexts.add(new Context("mcmmo:reason", reason));
        final Location location = player.getLocation();
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        final Double xpGained = GriefDefender.getPermissionManager().getActiveOptionValue(TypeToken.get(Double.class), MCMMO_XP_GAIN_MODIFIER, playerData.getUser(), claim, contexts);
        if (xpGained != null && xpGained != 1.0) {
            event.setRawXpGained(event.getRawXpGained() * xpGained.floatValue());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeathPenalty(McMMOPlayerDeathPenaltyEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Location location = player.getLocation();
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        final Boolean deathPenalty = GriefDefender.getPermissionManager().getActiveOptionValue(TypeToken.get(Boolean.class), MCMMO_DEATH_PENALTY, playerData.getUser(), claim, new HashSet<>());
        if (deathPenalty != null && !deathPenalty) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerAbilityActivate(McMMOPlayerAbilityActivateEvent event) {
       final Player player = event.getPlayer();
       final World world = player.getWorld();
       if (!GriefDefender.getCore().isEnabled(world.getUID())) {
           return;
       }

       final Location location = player.getLocation();
       final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
       final Claim claim = GriefDefender.getCore().getClaimAt(location);
       final String ability = event.getAbility().name().toLowerCase().replace(" ", "_");
       final String skillType = event.getSkill().name().toLowerCase().replace(" ", "_");
       final Set<Context> contexts = new HashSet<>();
       contexts.add(new Context("mcmmo:skill_level", String.valueOf(event.getSkillLevel())));
       contexts.add(new Context("mcmmo:skill_type", skillType));

       final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), ABILITY_ACTIVATE, player, ability, contexts, null, true);
       if (result == Tristate.FALSE) {
           event.setCancelled(true);
       }

       this.playerAbilityMap.put(player.getUniqueId(), new McMMOPlayerAbilityData(player.getUniqueId(),  event.getAbility(), event.getSkill(), event.getSkillLevel(), contexts));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerAbilityDeactivate(McMMOPlayerAbilityDeactivateEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        this.playerAbilityMap.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAlchemyBrewEvent(McMMOPlayerBrewEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Set<Context> contexts = new HashSet<>();
        final String brewingBlockId = GriefDefender.getRegistry().lookupId(event.getBrewingStandBlock());
        if (brewingBlockId != null) {
            contexts.add(new Context("mcmmo:brewing_block", brewingBlockId));
        }
        final String brewingIngredientId = GriefDefender.getRegistry().lookupId(event.getBrewingStand().getInventory().getIngredient());
        if (brewingIngredientId != null) {
            contexts.add(new Context("mcmmo:brewing_ingredient", brewingIngredientId));
        }
        if (!onPlayerSkillEvent(event, "brewing", new HashSet<>())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAlchemyCatalysisEvent(McMMOPlayerCatalysisEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Set<Context> contexts = new HashSet<>();
        contexts.add(new Context("mcmmo:speed", String.valueOf(event.getSpeed())));
        if (!onPlayerSkillEvent(event, "catalysis", contexts)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFishingSkillEvent(McMMOPlayerFishingEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        if (!onPlayerSkillEvent(event, "fishing", new HashSet<>())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRepairCheckEvent(McMMOPlayerRepairCheckEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Set<Context> contexts = new HashSet<>();
        contexts.add(new Context("mcmmo:repair_amount", String.valueOf(event.getRepairAmount())));
        final String repairMaterialId = GriefDefender.getRegistry().lookupId(event.getRepairMaterial());
        if (repairMaterialId != null) {
            contexts.add(new Context("mcmmo:repair_material", repairMaterialId));
        }
        final String repairObjectId = GriefDefender.getRegistry().lookupId(event.getRepairedObject());
        if (repairObjectId != null) {
            contexts.add(new Context("mcmmo:repair_object", repairObjectId));
        }

        if (!onPlayerSkillEvent(event, "repair", contexts)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSalvageCheckEvent(McMMOPlayerSalvageCheckEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Set<Context> contexts = new HashSet<>();
        GriefDefender.getPermissionManager().addItemEnchantmentContexts(event.getEnchantedBook(), contexts);
        final String salvageItemId = GriefDefender.getRegistry().lookupId(event.getSalvageItem());
        if (salvageItemId != null) {
            
        }
        contexts.add(new Context("mcmmo:salvage_item", salvageItemId));
        if (!onPlayerSkillEvent(event, "salvage", contexts)) {
            event.setCancelled(true);
        }
    }

    public boolean onPlayerSkillEvent(McMMOPlayerSkillEvent event, Object target, Set<Context> contexts) {
       final Player player = event.getPlayer();
       final World world = player.getWorld();
       if (!GriefDefender.getCore().isEnabled(world.getUID())) {
           return true;
       }

       final Location location = player.getLocation();
       final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
       final Claim claim = GriefDefender.getCore().getClaimAt(location);
       final String skillType = event.getSkill().name().toLowerCase().replace(" ", "_");
       contexts.add(new Context("mcmmo:skill_level", String.valueOf(event.getSkillLevel())));
       contexts.add(new Context("mcmmo:skill_type", skillType));

       final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), SKILL_USE, player, target, contexts, null, true);
       if (result == Tristate.FALSE) {
           return false;
       }

       return true;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDisarmEvent(McMMOPlayerDisarmEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Player targetPlayer = event.getDefender();
        final Location location = targetPlayer.getLocation();
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        final String skillType = event.getSkill().name().toLowerCase().replace(" ", "_");
        final Set<Context> contexts = new HashSet<>();
        contexts.add(new Context("mcmmo:skill_level", String.valueOf(event.getSkillLevel())));
        contexts.add(new Context("mcmmo:skill_type", skillType));

        final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), PLAYER_DISARM, player, targetPlayer, contexts, null, true);
        if (result == Tristate.FALSE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRuptureDamageEvent(McMMOEntityDamageByRuptureEvent event) {
        final McMMOPlayer mcmmoPlayer = event.getMcMMODamager();
        final Player player = mcmmoPlayer.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Entity targetEntity = event.getEntity();
        final Location location = targetEntity.getLocation();
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        final Claim claim = GriefDefender.getCore().getClaimAt(location);
        final Set<Context> contexts = new HashSet<>();
        contexts.add(new Context("mcmmo:damage_amount", String.valueOf(event.getFinalDamage())));

        final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), RUPTURE_DAMAGE, player, targetEntity, contexts, null, true);
        if (result == Tristate.FALSE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPartyChangeEvent(McMMOPartyChangeEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPermission(GDHooksPermissions.PROVIDER_MCMMO_AUTO_PARTY_TRUST)) {
            return;
        }
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(player.getWorld().getUID(), player.getUniqueId());
        for (Claim claim : playerData.getClaims()) {
            if (event.getOldParty() != null) {
                claim.removeGroupTrust(event.getOldParty().toLowerCase().replace(" ", "_"), TrustTypes.NONE);
            }
            if (event.getNewParty() != null) {
                claim.addGroupTrust(event.getNewParty().toLowerCase().replace(" ", "_"), TrustTypes.ACCESSOR);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAllianceChangeEvent(McMMOPartyAllianceChangeEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPermission(GDHooksPermissions.PROVIDER_MCMMO_AUTO_PARTY_TRUST)) {
            return;
        }
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(player.getWorld().getUID(), player.getUniqueId());
        for (Claim claim : playerData.getClaims()) {
            if (event.getOldAlly() != null) {
                claim.removeGroupTrust(event.getOldAlly().toLowerCase().replace(" ", "_"), TrustTypes.NONE);
            }
            if (event.getNewAlly() != null) {
                claim.addGroupTrust(event.getNewAlly().toLowerCase().replace(" ", "_"), TrustTypes.ACCESSOR);
            }
        }
    }

    private class BorderClaimEventListener {

        public BorderClaimEventListener() {
            final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

            eventBus.subscribe(BorderClaimEvent.class, new EventSubscriber<BorderClaimEvent>() {

                @Override
                public void on(@NonNull BorderClaimEvent event) throws Throwable {
                    if (event.getExitClaim() == event.getEnterClaim()) {
                        return;
                    }

                    final Claim claim = event.getEnterClaim();
                    final Player player = (Player) event.getUser().getOnlinePlayer();
                    if (player == null) {
                        return;
                    }

                    final McMMOPlayerAbilityData playerAbilityData = GDHooks.getInstance().getMcMMOProvider().getPlayerAbilityMap().get(player.getUniqueId());
                    if (playerAbilityData == null) {
                        return;
                    }

                    final McMMOPlayer mcmmoPlayer = UserManager.getPlayer(player);
                    if (mcmmoPlayer == null) {
                        return;
                    }

                    final boolean activeAbility = mcmmoPlayer.getAbilityMode(playerAbilityData.ability);
                    if (activeAbility) {
                        final Location location = player.getLocation();
                        final String playerAbility = "mcmmo:" + playerAbilityData.ability.getName().toLowerCase().replace(" ", "_");
                        final String skillType = playerAbilityData.skillType.name().toLowerCase().replace(" ", "_");
                        final Set<Context> contexts = new HashSet<>();
                        contexts.add(new Context("mcmmo:skill_level", String.valueOf(playerAbilityData.skillLevel)));
                        contexts.add(new Context("mcmmo:skill_type", skillType));

                        final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(null, location, claim, event.getUser(), GDHooks.getInstance().getMcMMOProvider().ABILITY_ACTIVATE, player, playerAbility, contexts, null, true);
                        if (result == Tristate.FALSE) {
                            new AbilityDisableTask(mcmmoPlayer, playerAbilityData.ability).runTaskLater(GDHooksBootstrap.getInstance().getLoader(), 1);
                        }
                    }
                }
            });
        }
    }

    private class CreateClaimEventListener {

        public CreateClaimEventListener() {
            final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

            eventBus.subscribe(CreateClaimEvent.Post.class, new EventSubscriber<CreateClaimEvent.Post>() {

                @Override
                public void on(CreateClaimEvent.Post event) throws Throwable {
                    final User user = event.getCause().first(User.class).orElse(null);
                    if (user == null) {
                        return;
                    }
                    final Player player = Bukkit.getPlayer(user.getUniqueId());
                    if (player == null) {
                        return;
                    }
                    final McMMOPlayer mcmmoPlayer = UserManager.getPlayer(player);
                    final Party party = mcmmoPlayer.getParty();
                    if (party == null) {
                        return;
                    }

                    for (Claim claim : event.getClaims()) {
                        claim.addGroupTrust(party.getName().toLowerCase().replace(" ", "_"), TrustTypes.ACCESSOR);
                    }
                }
                
            });
        }
    }

    private class ProcessTrustUserEventListener {

        public ProcessTrustUserEventListener() {
            final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

            eventBus.subscribe(ProcessTrustUserEvent.class, new EventSubscriber<ProcessTrustUserEvent>() {

                @Override
                public void on(@NonNull ProcessTrustUserEvent event) throws Throwable {
                    if (event.getFinalTrustResult().successful()) {
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
                    final McMMOPlayer mcmmoPlayer = UserManager.getPlayer(player);
                    if (mcmmoPlayer == null) {
                        return;
                    }
                    final Party party = mcmmoPlayer.getParty();
                    if (party == null) {
                        return;
                    }

                    if (event.getClaim().isGroupTrusted(party.getName().toLowerCase().replace(" ", "_"), event.getTrustType())) {
                        final TrustResult trustResult = TrustResult.builder().user(event.getUser()).claims(event.getClaims()).trust(event.getTrustType()).type(TrustResultTypes.TRUSTED).build();
                        event.setNewTrustResult(trustResult);
                    }
                }
            });
        }
    }
}
