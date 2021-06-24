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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.Tristate;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.flag.Flag;
import com.griefdefender.hooks.GDHooksBootstrap;

import net.Indyuce.mmoitems.api.event.AbilityUseEvent;

public class MMOItemsProvider implements Listener {

    private Flag ABILITY_USE;

    public MMOItemsProvider() {
        ABILITY_USE = Flag.builder()
                .id("mmoitems:ability-use")
                .name("ability-use")
                .permission("griefdefender.flag.mmoitems.ability-use")
                .build();
        GriefDefender.getRegistry().getRegistryModuleFor(Flag.class).get().registerCustomType(ABILITY_USE);
        Bukkit.getPluginManager().registerEvents(this, GDHooksBootstrap.getInstance().getLoader());
    }

    public String getItemId(ItemStack itemstack) {
        final String id = GriefDefender.getNBTUtil().getItemNBTValue(itemstack, "MMOITEMS_ITEM_ID");
        if (id != null) {
            return "mmoitems:" + id;
        }

        return null;
    }

    public String getItemTypeId(ItemStack itemstack) {
        final String id = GriefDefender.getNBTUtil().getItemNBTValue(itemstack, "MMOITEMS_ITEM_TYPE");
        if (!id.isEmpty()) {
            return "mmoitems:" + id;
        }

        return "";
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAbilityUseEvent(AbilityUseEvent event) {
        final Entity targetEntity = event.getTarget();
        if (targetEntity == null) {
            return;
        }

        final Player player = event.getPlayer();
        final World world = player.getWorld();
        if (!GriefDefender.getCore().isEnabled(world.getUID())) {
            return;
        }

        final Location location = targetEntity.getLocation();
        final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), player.getUniqueId());
        final ClaimManager claimManager = GriefDefender.getCore().getClaimManager(player.getWorld().getUID());
        final Claim claim = claimManager.getClaimAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final String ability = event.getAbility().getAbility().getLowerCaseID().replace(" ", "_");
        final Set<Context> contexts = new HashSet<>();
        final String targetEntityId = GriefDefender.getRegistry().lookupId(event.getTarget());
        contexts.add(new Context("mmoitems:target_entity", targetEntityId));

        final Tristate result = GriefDefender.getPermissionManager().getActiveFlagPermissionValue(event, location, claim, playerData.getUser(), ABILITY_USE, player, ability, contexts, null, true);
        if (result == Tristate.FALSE) {
            event.setCancelled(true);
        }
    }
}
