package com.griefdefender.hooks.provider.clan.simpleclans;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import com.griefdefender.api.claim.Claim;

public class CreateClaimBukkitEvent extends Event implements Cancellable {

    public static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Claim claim;
    private Player player;

    public CreateClaimBukkitEvent(Claim claim, Player creator) {
        this.claim = claim;
        this.player = creator;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Player getCreator() {
        return this.player;
    }

    public Claim getClaim() {
        return this.claim;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
