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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.permission.option.Option;
import com.griefdefender.hooks.GDHooks;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.util.hooks.PluginHookName;
import de.Keyle.MyPet.api.util.hooks.types.FlyHook;
import de.Keyle.MyPet.api.util.hooks.types.PlayerVersusEntityHook;
import de.Keyle.MyPet.api.util.hooks.types.PlayerVersusPlayerHook;
import io.leangen.geantyref.TypeToken;

public class MyPetProvider {

    private final Option<Boolean> MYPET_DAMAGE;
    private final Option<Boolean> MYPET_FLY;

    public MyPetProvider() {
        MYPET_DAMAGE = Option.builder(Boolean.class)
                .id("mypet:damage")
                .name("mypet:damage")
                .defaultValue(true)
                .build();
        MYPET_FLY = Option.builder(Boolean.class)
                .id("mypet:fly")
                .name("mypet:fly")
                .defaultValue(true)
                .build();
        GriefDefender.getRegistry().getRegistryModuleFor(Option.class).get().registerCustomType(MYPET_DAMAGE);
        GDHooks.getInstance().getLogger().info("Registered MyPet option '" + MYPET_DAMAGE.getName() + "'.");
        GriefDefender.getRegistry().getRegistryModuleFor(Option.class).get().registerCustomType(MYPET_FLY);
        GDHooks.getInstance().getLogger().info("Registered MyPet option '" + MYPET_FLY.getName() + "'.");
        MyPetApi.getPluginHookManager().enableHook(new GDMyPetFlyHook());
    }

    public boolean isMyPetEntity(Entity entity) {
        return entity instanceof MyPetBukkitEntity;
    }

    public String getPetType(Entity entity) {
        if (!(entity instanceof MyPetBukkitEntity)) {
            return null;
        }

        final MyPetBukkitEntity myPet = (MyPetBukkitEntity) entity;
        return myPet.getPetType().name().toLowerCase();
    }

    public String getPetOwner(Entity entity) {
        if (!(entity instanceof MyPetBukkitEntity)) {
            return null;
        }

        final MyPetBukkitEntity myPet = (MyPetBukkitEntity) entity;
        final MyPetPlayer petOwner = myPet.getOwner();
        if (petOwner == null) {
            return null;
        }
        return petOwner.getName();
    }

    @PluginHookName(value = "GDHooks", classPath = "")
    public class GDMyPetFlyHook implements FlyHook, PlayerVersusEntityHook, PlayerVersusPlayerHook {

        @Override
        public boolean canFly(Location location) {
            final World world = location.getWorld();
            if (!GriefDefender.getCore().isEnabled(world.getUID())) {
                return true;
            }

            final Claim claim = GriefDefender.getCore().getClaimAt(location);
            final Boolean myPetFly = GriefDefender.getPermissionManager().getActiveOptionValue(TypeToken.get(Boolean.class), MYPET_FLY, GriefDefender.getCore().getDefaultSubject(), claim);
            if (myPetFly != null) {
                return myPetFly;
            }

            return true;
        }

        @Override
        public boolean canHurt(Player attacker, Entity defender) {
            return this.canHurtEntity(attacker, defender);
        }

        @Override
        public boolean canHurt(Player attacker, Player defender) {
            return this.canHurtEntity(attacker, defender);
        }

        private boolean canHurtEntity(Player attacker, Entity defender) {
            final Location location = defender.getLocation();
            final World world = location.getWorld();
            if (!GriefDefender.getCore().isEnabled(world.getUID())) {
                return true;
            }

            final PlayerData playerData = GriefDefender.getCore().getPlayerData(world.getUID(), attacker.getUniqueId());
            final Claim claim = GriefDefender.getCore().getClaimAt(location);
            final Boolean myPetDamage = GriefDefender.getPermissionManager().getActiveOptionValue(TypeToken.get(Boolean.class), MYPET_DAMAGE, playerData.getUser(), claim);
            if (myPetDamage != null) {
                return myPetDamage;
            }

            return true;
        }
    }
}
