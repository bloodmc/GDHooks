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
package com.griefdefender.hooks.event;

import com.google.common.collect.ImmutableList;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.event.ClaimEvent;
import com.griefdefender.api.event.EventCause;

import com.griefdefender.lib.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GDClaimEvent implements ClaimEvent {

    private List<Claim> claims;
    private Component message;
    private boolean isCancelled = false;
    private final Object source;
    private final User user;
    private final EventCause cause;

    public GDClaimEvent(Object source, Claim claim) {
        this.source = source;
        this.claims = ImmutableList.of(claim);
        this.cause = EventCause.of(GriefDefender.getEventManager().getCauseStackManager().getCurrentCause().root());
        if (source instanceof User) {
            this.user = (User) source;
        } else if (source instanceof OfflinePlayer) {
            this.user = GriefDefender.getCore().getUser(((OfflinePlayer) source).getUniqueId());
        } else {
            this.user = null;
        }
    }

    public GDClaimEvent(Object source, List<Claim> claims) {
        this.source = source;
        this.claims = ImmutableList.copyOf(claims);
        this.cause = EventCause.of(GriefDefender.getEventManager().getCauseStackManager().getCurrentCause().root());
        if (source instanceof User) {
            this.user = (User) source;
        } else if (source instanceof OfflinePlayer) {
            this.user = GriefDefender.getCore().getUser(((OfflinePlayer) source).getUniqueId());
        } else {
            this.user = null;
        }
    }

    public boolean cancelled() {
        return this.isCancelled;
    }

    public void cancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public Claim getClaim() {
        return this.getClaims().get(0);
    }

    @Override
    public List<Claim> getClaims() {
        return this.claims;
    }

    @Override
    public void setMessage(Component message) {
        this.message = message;
    }

    @Override
    public Optional<Component> getMessage() {
        return Optional.ofNullable(this.message);
    }

    @Override
    public EventCause getCause() {
        return this.cause;
    }

    @Override
    public Object getSource() {
        return this.source;
    }

    @Override
    public @Nullable User getSourceUser() {
        return this.user;
    }

}
