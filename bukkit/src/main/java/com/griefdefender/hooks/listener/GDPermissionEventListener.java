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

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.event.BorderClaimEvent;
import com.griefdefender.api.event.PermissionIdentifyEvent;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.ContextKeys;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.context.ContextGroups;

import net.kyori.event.EventSubscriber;

public class GDPermissionEventListener {

    public GDPermissionEventListener() {
        GriefDefender.getEventManager().getBus().subscribe(PermissionIdentifyEvent.class, new EventSubscriber<PermissionIdentifyEvent>() {
            @Override
            public void on(@NonNull PermissionIdentifyEvent event) throws Throwable {
                final Object object = event.getPermissionObject();
                final Set<Context> contexts = event.getContexts();
                final User user = event.getUser();
                if (user != null && GDHooks.getInstance().getClanProvider() != null) {
                    // add clan tag context
                    final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(user.getUniqueId());
                    if (clanPlayer != null && clanPlayer.getClan() != null) {
                        contexts.add(new Context("clan_tag", clanPlayer.getClan().getTag().toLowerCase()));
                        if (clanPlayer.getRank() != null) {
                            contexts.add(new Context("clan_rank", clanPlayer.getRank().getName().toLowerCase()));
                        }
                    }
                }
                if (object instanceof Block) {
                    final Block block = (Block) object;
                    if (GDHooks.getInstance().getNovaProvider() != null) {
                        final String blockId = GDHooks.getInstance().getNovaProvider().getTileEntityId(block.getState());
                        if (blockId != null) {
                            event.setNewIdentifier(blockId);
                            return;
                        }
                    }
                }
                if (object instanceof BlockState) {
                    final BlockState state = (BlockState) object;
                    if (GDHooks.getInstance().getNovaProvider() != null) {
                        final String blockId = GDHooks.getInstance().getNovaProvider().getTileEntityId(state);
                        if (blockId != null) {
                            event.setNewIdentifier(blockId);
                            return;
                        }
                    }
                }
                if (object instanceof ItemStack) {
                    final ItemStack itemstack = (ItemStack) object;
                    if (GDHooks.getInstance().getMMOItemsProvider() != null) {
                        final String itemId = GDHooks.getInstance().getMMOItemsProvider().getItemId(itemstack);
                        if (itemId != null) {
                            final String itemType = GDHooks.getInstance().getMMOItemsProvider().getItemTypeId(itemstack);
                            if (itemType != null) {
                                contexts.add(new Context("mmoitems:item_type", itemType));
                            }
                            event.setNewIdentifier(itemId);
                            return;
                        }
                    }
                    if (GDHooks.getInstance().getRevoltCratesProvider() != null) {
                        final String itemId = GDHooks.getInstance().getRevoltCratesProvider().getItemId(itemstack);
                        if (itemId != null) {
                            event.setNewIdentifier(itemId);
                            return;
                        }
                    }
                    if (GDHooks.getInstance().getFurnitureLibProvider() != null) {
                        final String itemId = GDHooks.getInstance().getFurnitureLibProvider().getItemId(itemstack);
                        if (itemId != null) {
                            event.setNewIdentifier(itemId);
                            return;
                        }
                    }
                    if (GDHooks.getInstance().getNovaProvider() != null) {
                        final String itemId = GDHooks.getInstance().getNovaProvider().getItemId(itemstack);
                        if (itemId != null) {
                            event.setNewIdentifier(itemId);
                            return;
                        }
                    }
                }
                if (object instanceof Entity) {
                    final Entity targetEntity = (Entity) object;
                    if (targetEntity instanceof Player) {
                        return;
                    }
                    if (GDHooks.getInstance().getMyPetProvider() != null && GDHooks.getInstance().getMyPetProvider().isMyPetEntity(targetEntity)) {
                        if (event.isSource()) {
                            contexts.add(ContextGroups.SOURCE_MYPET);
                            contexts.add(ContextGroups.SOURCE_ANY);
                        } else {
                            contexts.add(ContextGroups.TARGET_MYPET);
                            contexts.add(ContextGroups.TARGET_ANY);
                        }
                        final String petOwner = GDHooks.getInstance().getMyPetProvider().getPetOwner(targetEntity);
                        if (petOwner != null) {
                            contexts.add(new Context("owner", petOwner));
                        }
                        final String id = "mypet:" + GDHooks.getInstance().getMyPetProvider().getPetType(targetEntity).toLowerCase();
                        event.setNewIdentifier(id);
                    } else if (GDHooks.getInstance().getMythicMobsProvider() != null && GDHooks.getInstance().getMythicMobsProvider().isMythicMob(targetEntity)) {
                        final Set<Context> mobContexts = GDHooks.getInstance().getMythicMobsProvider().getMobContexts(targetEntity);
                        contexts.addAll(mobContexts);
                        if (event.isSource()) {
                            contexts.add(ContextGroups.SOURCE_MYTHICMOBS);
                            contexts.add(ContextGroups.SOURCE_ANY);
                            contexts.add(ContextGroups.SOURCE_MONSTER);
                            contexts.add(new Context(ContextKeys.SOURCE, "#mythicmobs:monster"));
                        } else {
                            contexts.add(ContextGroups.TARGET_MYTHICMOBS);
                            contexts.add(ContextGroups.TARGET_ANY);
                            contexts.add(ContextGroups.TARGET_MONSTER);
                            contexts.add(new Context(ContextKeys.TARGET, "#mythicmobs:monster"));
                        }
                        final String id = "mythicmobs:" + GDHooks.getInstance().getMythicMobsProvider().getMobType(targetEntity).toLowerCase();
                        event.setNewIdentifier(id);
                    } else if (GDHooks.getInstance().getEliteMobsProvider() != null && GDHooks.getInstance().getEliteMobsProvider().isEliteMob(targetEntity)) {
                        final Set<Context> mobContexts = GDHooks.getInstance().getEliteMobsProvider().getEliteMobContexts(targetEntity);
                        contexts.addAll(mobContexts);
                        event.setNewIdentifier(GDHooks.getInstance().getEliteMobsProvider().getEliteMobId(targetEntity));
                    }
                }
            }
        });
    }
}
