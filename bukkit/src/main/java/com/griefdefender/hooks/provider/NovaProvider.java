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
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;

import xyz.xenondevs.nova.api.Nova;
import xyz.xenondevs.nova.api.material.NovaMaterial;
import xyz.xenondevs.nova.api.protection.ProtectionIntegration;
import xyz.xenondevs.nova.api.tileentity.TileEntity;

public class NovaProvider {

    public NovaProvider() {
        Nova.getNova().registerProtectionIntegration(new GDProtectionHandler());
    }

    public String getTileEntityId(BlockState state) {
        final TileEntity tileEntity = Nova.getNova().getTileEntityManager().getTileEntityAt(state.getLocation());
        return tileEntity != null ? tileEntity.getMaterial().getId() : null;
    }

    public String getItemId(ItemStack itemStack) {
        final NovaMaterial material = Nova.getNova().getMaterialRegistry().getOrNull(itemStack);
        return material != null ? material.getId() : null;
    }

    public static class GDProtectionHandler implements ProtectionIntegration {

        @Override
        public boolean canBreak(TileEntity tileEntity, ItemStack item, Location location) {
            final User user = GriefDefender.getCore().getUser(tileEntity.getOwner().getUniqueId());
            final Claim claim = GriefDefender.getCore().getClaimAt(location);
            return claim.canBreak(tileEntity.getMaterial().getId(), location, user);
        }

        @Override
        public boolean canBreak(OfflinePlayer player, ItemStack item, Location location) {
            final User user = GriefDefender.getCore().getUser(player.getUniqueId());
            return user.canBreak(location);
        }

        @Override
        public boolean canHurtEntity(OfflinePlayer player, Entity entity, ItemStack item) {
            final User user = GriefDefender.getCore().getUser(player.getUniqueId());
            return user.canHurtEntity(item, entity);
        }

        @Override
        public boolean canHurtEntity(TileEntity tileEntity, Entity entity, ItemStack item) {
            final User user = GriefDefender.getCore().getUser(tileEntity.getOwner().getUniqueId());
            final Claim claim = GriefDefender.getCore().getClaimAt(entity.getLocation());
            return claim.canHurtEntity(tileEntity.getMaterial().getId(), item, entity, user);
        }

        @Override
        public boolean canInteractWithEntity(OfflinePlayer player, Entity entity, ItemStack item) {
            final User user = GriefDefender.getCore().getUser(player.getUniqueId());
            return user.canInteractWithEntity(item, entity, TrustTypes.ACCESSOR);
        }

        @Override
        public boolean canInteractWithEntity(TileEntity tileEntity, Entity entity, ItemStack item) {
            final User user = GriefDefender.getCore().getUser(tileEntity.getOwner().getUniqueId());
            final Claim claim = GriefDefender.getCore().getClaimAt(entity.getLocation());
            return claim.canInteractWithEntity(tileEntity.getMaterial().getId(), item, entity, user, TrustTypes.ACCESSOR);
        }

        @Override
        public boolean canPlace(OfflinePlayer player, ItemStack item, Location location) {
            final User user = GriefDefender.getCore().getUser(player.getUniqueId());
            return user.canPlace(item, location);
        }

        @Override
        public boolean canUseBlock(TileEntity tileEntity, ItemStack item, Location location) {
            final User user = GriefDefender.getCore().getUser(tileEntity.getOwner().getUniqueId());
            final Claim claim = GriefDefender.getCore().getClaimAt(location);
            return claim.canUseBlock(tileEntity.getMaterial().getId(), location, user, TrustTypes.CONTAINER, false, false);
        }

        @Override
        public boolean canUseBlock(OfflinePlayer player, ItemStack item, Location location) {
            final User user = GriefDefender.getCore().getUser(player.getUniqueId());
            return user.canUseBlock(location, TrustTypes.CONTAINER, false, false);
        }

        @Override
        public boolean canUseItem(TileEntity tileEntity, ItemStack item, Location location) {
            final User user = GriefDefender.getCore().getUser(tileEntity.getOwner().getUniqueId());
            final Claim claim = GriefDefender.getCore().getClaimAt(location);
            return claim.canUseItem(tileEntity.getMaterial().getId(), item, location, user, TrustTypes.ACCESSOR, false, false);
        }

        @Override
        public boolean canUseItem(OfflinePlayer player, ItemStack item, Location location) {
            final User user = GriefDefender.getCore().getUser(player.getUniqueId());
            return user.canUseItem(item, location, TrustTypes.ACCESSOR, false, false);
        }
    }
}
