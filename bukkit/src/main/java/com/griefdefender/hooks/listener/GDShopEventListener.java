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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.event.Event;
import com.griefdefender.api.event.ProcessInteractBlockEvent;
import com.griefdefender.hooks.GDHooks;

import net.kyori.event.EventBus;
import net.kyori.event.EventSubscriber;


public class GDShopEventListener {

    public GDShopEventListener() {
        final EventBus<Event> eventBus = GriefDefender.getEventManager().getBus();

        eventBus.subscribe(ProcessInteractBlockEvent.class, new EventSubscriber<ProcessInteractBlockEvent>() {
            @Override
            public void on(@NonNull ProcessInteractBlockEvent event) throws Throwable {
                final Player player = (Player) event.getUser().getOnlinePlayer();
                if (player == null) {
                    return;
                }

                if (!GriefDefender.getCore().isEnabled(player.getWorld().getUID())) {
                    return;
                }

                final Location location = (Location) event.getClickedBlockLocation();
                if (GDHooks.getInstance().getShopProvider() != null) {
                    // Ignore ShopChest locations
                    if (GDHooks.getInstance().getShopProvider().isLocationShop(location)) {
                        event.cancelled(true);
                    }
                }
            }
        });
    }

}
