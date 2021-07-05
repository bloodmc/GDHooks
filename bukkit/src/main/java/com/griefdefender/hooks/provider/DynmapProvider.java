/*
 * This file is part of GDHooks, licensed under the MIT License (MIT).
 *
 * Copyright (c) bloodmc
 * Copyright (c) Dockter
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

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.category.DynmapCategory;
import com.griefdefender.hooks.config.category.DynmapOwnerStyleCategory;

import net.kyori.event.EventSubscriber;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class DynmapProvider {

    private static final String MOD_ID = "GriefDefender";
    private final Logger logger;
    private DynmapCommonAPI dynmap;
    private MarkerAPI markerapi;
    private DynmapCategory cfg;
    private MarkerSet set;
    public boolean disabled = false;
    private boolean reload = false;

    public DynmapProvider() {
        this.logger = GDHooks.getInstance().getLogger();
        logger.info("Initializing GriefDefender Dynmap provider...");
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI api) {
                dynmap = api;
                cfg = GDHooks.getInstance().getConfig().getData().dynmap;
                activate();
            }
        });
    }

    private Map<String, AreaMarker> areaMarkers = new HashMap<String, AreaMarker>();

    private String getWindowInfo(Claim claim, AreaMarker marker) {
        String info;
        if (claim.isAdminClaim()) {
            info = "<div class=\"regioninfo\">" + this.cfg.infoWindowAdmin + "</div>";
        } else {
            info = "<div class=\"regioninfo\">" + this.cfg.infoWindowBasic + "</div>";
        }
        info = info.replace("%owner%", claim.getOwnerName());
        info = info.replace("%owneruuid%", claim.getOwnerUniqueId().toString());
        info = info.replace("%area%", Integer.toString(claim.getArea()));
        info = info.replace("%claimname%",
                claim.getData().getDisplayNameComponent().isPresent()
                        ? PlainComponentSerializer.plain().serialize(claim.getDisplayNameComponent().get())
                        : "none");
        info = info.replace("%lastseen%", claim.getData().getDateLastActive().toString());
        info = info.replace("%gdtype%", claim.getType().toString());

        final List<UUID> builderList = new ArrayList<>(claim.getUserTrusts(TrustTypes.BUILDER));
        final List<UUID> containerList = new ArrayList<>(claim.getUserTrusts(TrustTypes.CONTAINER));
        final List<UUID> accessorList = new ArrayList<>(claim.getUserTrusts(TrustTypes.ACCESSOR));
        final List<UUID> managerList = new ArrayList<>(claim.getUserTrusts(TrustTypes.MANAGER));

        String trusted = "";
        for (int i = 0; i < builderList.size(); i++) {
            if (i > 0) {
                trusted += ", ";
            }
            final UUID uuid = builderList.get(i);
            final String userName = GriefDefender.getRegistry().lookupUsername(uuid);
            if (userName.equalsIgnoreCase("unknown")) {
                trusted += uuid.toString();
            } else {
                trusted += userName;
            }
        }
        info = info.replace("%builders%", trusted);

        trusted = "";
        for (int i = 0; i < containerList.size(); i++) {
            if (i > 0) {
                trusted += ", ";
            }
            final UUID uuid = containerList.get(i);
            final String userName = GriefDefender.getRegistry().lookupUsername(uuid);
            if (userName.equalsIgnoreCase("unknown")) {
                trusted += uuid.toString();
            } else {
                trusted += userName;
            }
        }
        info = info.replace("%containers%", trusted);

        trusted = "";
        for (int i = 0; i < accessorList.size(); i++) {
            if (i > 0) {
                trusted += ", ";
            }
            final UUID uuid = accessorList.get(i);
            final String userName = GriefDefender.getRegistry().lookupUsername(uuid);
            if (userName.equalsIgnoreCase("unknown")) {
                trusted += uuid.toString();
            } else {
                trusted += userName;
            }
        }
        info = info.replace("%accessors%", trusted);

        trusted = "";
        for (int i = 0; i < managerList.size(); i++) {
            if (i > 0) {
                trusted += ", ";
            }
            final UUID uuid = managerList.get(i);
            final String userName = GriefDefender.getRegistry().lookupUsername(uuid);
            if (userName.equalsIgnoreCase("unknown")) {
                trusted += uuid.toString();
            } else {
                trusted += userName;
            }
        }
        info = info.replace("%managers%", trusted);

        return info;
    }

    private boolean isVisible(Claim claim, String owner, String worldname) {
        if (!this.cfg.hiddenRegions.isEmpty()) {
            if (this.cfg.hiddenRegions.contains(claim.getUniqueId().toString()) || this.cfg.hiddenRegions.contains(owner) || 
                    this.cfg.hiddenRegions.contains("world:" + worldname) || this.cfg.hiddenRegions.contains(worldname + "/" + owner))
                return false;
        }
        return true;
    }

    private void addClaimStyle(Claim claim, AreaMarker marker, String worldid, String owner) {
        DynmapOwnerStyleCategory ownerStyle = null;

        if (!this.cfg.ownerStyles.isEmpty()) {
            ownerStyle = this.cfg.ownerStyles.get(owner.toLowerCase());
        }

        if (ownerStyle == null) {
            ownerStyle = this.cfg.claimTypeStyles.get(claim.getType().getName().toLowerCase());
        }

        int sc;
        int fc;
        try {
            sc = Integer.parseInt(ownerStyle.strokeColor.replaceAll("#", ""), 16);
            fc = Integer.parseInt(ownerStyle.fillColor.replaceAll("#", ""), 16);
        } catch (NumberFormatException e) {
            if (claim.getType().equals(ClaimTypes.ADMIN)) {
                sc = 0xFF0000;
                fc = 0xFF0000;
            } else if (claim.getType().equals(ClaimTypes.BASIC)) {
                sc = 0xFFFF00;
                fc = 0xFFFF00;
            } else if (claim.getType().equals(ClaimTypes.TOWN)) {
                sc = 0x00FF00;
                fc = 0x00FF00;
            } else if (claim.getType().equals(ClaimTypes.SUBDIVISION)) {
                sc = 0xFF9C00;
                fc = 0xFF9C00;
            } else {
                sc = 0xFF0000;
                fc = 0xFF0000;
            }
        }

        marker.setLineStyle(ownerStyle.strokeWeight, ownerStyle.strokeOpacity, sc);
        marker.setFillStyle(ownerStyle.fillOpacity, fc);
        if (ownerStyle.label != null && !ownerStyle.label.isEmpty() && !ownerStyle.label.equalsIgnoreCase("none")) {
            marker.setLabel(ownerStyle.label);
        }
    }

    private void updateClaimMarker(Claim claim, Map<String, AreaMarker> markerMap) {
        final World world = Bukkit.getWorld(claim.getWorldUniqueId());
        if (world == null) {
            return;
        }
        final String worldName = world.getName();
        final String owner = claim.getOwnerName();
        if (isVisible(claim, owner, worldName)) {
            final Vector3i lesserPos = claim.getLesserBoundaryCorner();
            final Vector3i greaterPos = claim.getGreaterBoundaryCorner();
            final double[] x = new double[4];
            final double[] z = new double[4];
            x[0] = lesserPos.getX();
            z[0] = lesserPos.getZ();
            x[1] = lesserPos.getX();
            z[1] = greaterPos.getZ() + 1.0;
            x[2] = greaterPos.getX() + 1.0;
            z[2] = greaterPos.getZ() + 1.0;
            x[3] = greaterPos.getX() + 1.0;
            z[3] = lesserPos.getZ();
            final UUID id = claim.getUniqueId();
            final String markerid = "GD_" + id;
            AreaMarker marker = this.areaMarkers.remove(markerid);
            if (marker == null) {
                marker = this.set.createAreaMarker(markerid, owner, false, worldName, x, z, false);
                if (marker == null) {
                    return;
                }
            } else {
                marker.setCornerLocations(x, z);
                marker.setLabel(owner);
            }
            if (this.cfg.use3dRegions) {
                marker.setRangeY(greaterPos.getY() + 1.0, lesserPos.getY());
            }

            addClaimStyle(claim, marker, worldName, owner);
            String desc = getWindowInfo(claim, marker);
            marker.setDescription(desc);
            markerMap.put(markerid, marker);
        }
    }

    private void updateClaims() {
        Map<String, AreaMarker> newmap = new HashMap<String, AreaMarker>();
        Bukkit.getServer().getWorlds().stream().map(w -> GriefDefender.getCore().getClaimManager(w.getUID()))
                .map(ClaimManager::getWorldClaims).forEach(claims -> {
                    for (Claim claim : claims) {
                        updateClaimMarker(claim, newmap);
                        for (Claim child : claim.getChildren(true)) {
                            updateClaimMarker(child, newmap);
                        }
                    }
                });

        for (AreaMarker oldm : this.areaMarkers.values()) {
            oldm.deleteMarker();
        }

        this.areaMarkers = newmap;
    }

    private void activate() {
        this.markerapi = this.dynmap.getMarkerAPI();
        if (this.markerapi == null) {
            this.logger.severe("Error loading Dynmap Provider! Could not locate Marker API.");
            return;
        }
        if (this.reload) {
            GDHooks.getInstance().getConfig().reload();
            if (this.set != null) {
                this.set.deleteMarkerSet();
                this.set = null;
            }
            this.areaMarkers.clear();
        } else {
            this.reload = true;
        }

        this.set = this.markerapi.getMarkerSet("griefdefender.markerset");
        if (this.set == null) {
            this.set = this.markerapi.createMarkerSet("griefdefender.markerset", MOD_ID, null, false);
        } else {
            this.set.setMarkerSetLabel(MOD_ID);
        }
        if (this.set == null) {
            this.logger.severe("Error creating marker set");
            return;
        }

        int minzoom = this.cfg.minzoom;
        if (minzoom > 0) {
            this.set.setMinZoom(minzoom);
        }

        this.set.setLayerPriority(this.cfg.layerPriority);
        this.set.setHideByDefault(this.cfg.layerHideByDefault);

        new GriefDefenderUpdate(40L);
        new ClaimEventListener();
        this.logger.info("Dynmap provider is activated");
    }

    public void onDisable() {
        if (this.set != null) {
            this.set.deleteMarkerSet();
            this.set = null;
        }
        this.areaMarkers.clear();
        this.disabled = true;
    }

    private class GriefDefenderUpdate extends BukkitRunnable {

        public GriefDefenderUpdate(long delay) {
            this.runTaskLater(GDHooksBootstrap.getInstance().getLoader(), delay);
        }

        @Override
        public void run() {
            if (!disabled) {
                updateClaims();
            } else {
                this.cancel();
            }
        }
    }

    public static class ClaimEventListener {
        public ClaimEventListener() {
            GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, new EventSubscriber<CreateClaimEvent>() {
                @Override
                public void on(@NonNull CreateClaimEvent event) throws Throwable {
                    new ClaimEventListener.GriefDefenderUpdate(20L);
                }
            });
            GriefDefender.getEventManager().getBus().subscribe(RemoveClaimEvent.class, new EventSubscriber<RemoveClaimEvent>() {
                @Override
                public void on(@NonNull RemoveClaimEvent event) throws Throwable {
                    new ClaimEventListener.GriefDefenderUpdate(20L);
                }
            });
            GriefDefender.getEventManager().getBus().subscribe(ChangeClaimEvent.class, new EventSubscriber<ChangeClaimEvent>() {
                @Override
                public void on(@NonNull ChangeClaimEvent event) throws Throwable {
                    new ClaimEventListener.GriefDefenderUpdate(20L);
                }
            });
        }

        private class GriefDefenderUpdate extends BukkitRunnable {

            public GriefDefenderUpdate(long delay) {
                this.runTaskLater(GDHooksBootstrap.getInstance().getLoader(), delay);
            }

            @Override
            public void run() {
                if (!GDHooks.getInstance().getDynmapProvider().disabled) {
                    GDHooks.getInstance().getDynmapProvider().updateClaims();
                } else {
                    this.cancel();
                }
            }
        }
    }
}