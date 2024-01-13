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

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.LoadClaimEvent;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.api.event.TransferClaimEvent;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.category.DynmapCategory;
import com.griefdefender.hooks.config.category.DynmapOwnerStyleCategory;

import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import com.griefdefender.lib.kyori.adventure.text.Component;
import com.griefdefender.lib.kyori.adventure.text.TextComponent;
import com.griefdefender.lib.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import com.griefdefender.lib.kyori.event.EventSubscriber;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DynmapProvider {

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

    private Map<String, AreaMarker> areaMarkers = new ConcurrentHashMap<>();

    private String getWindowInfo(Claim claim, AreaMarker marker) {
        String template = claim.isAdminClaim() ? this.cfg.infoWindowAdmin : this.cfg.infoWindowBasic;

        // Claim Details
        String displayName = claim.getData().getDisplayName();
        template = template.replace("%claimname%", displayName != null ? displayName : cfg.missingNamePlaceholder);
        template = template.replace("%gdtype%", claim.getType().toString());

        // Claim Owner Details
        template = template.replace("%owner%", claim.getOwnerName());
        template = template.replace("%owneruuid%", claim.getOwnerUniqueId().toString());
        template = template.replace("%lastseen%", (Date.from(claim.getData().getDateLastActive())).toString());

        // Claim Sizes
        template = template.replace("%width%", Integer.toString(claim.getWidth()));
        template = template.replace("%length%", Integer.toString(claim.getLength()));
        template = template.replace("%height%", Integer.toString(claim.getHeight()));
        template = template.replace("%area%", Integer.toString(claim.getArea()));
        template = template.replace("%volume%", Integer.toString(claim.getVolume()));

        // Claim Members
        template = template.replace("%builders%", composePlayerList(claim, TrustTypes.BUILDER));
        template = template.replace("%containers%", composePlayerList(claim, TrustTypes.CONTAINER));
        template = template.replace("%accessors%", composePlayerList(claim, TrustTypes.ACCESSOR));
        template = template.replace("%residents%", composePlayerList(claim, TrustTypes.RESIDENT));
        template = template.replace("%managers%", composePlayerList(claim, TrustTypes.MANAGER));

        return String.format("<div class=\"%s\">%s</div>", "regioninfo", template);
    }

    private String composePlayerList(Claim claim, TrustType trustType) {
        List<String> names = new ArrayList<>();
        Set<UUID> uuids = claim.getUserTrusts(trustType);

        for (UUID uuid : uuids) {
            String username = GriefDefender.getRegistry().lookupUsername(uuid);

            // While the previously called #lookupUsername() method is expected to just
            // return the string "unknown" when a user couldn't be found, its return value
            // is marked as nullable, so we're comparing for both.
            if (username == null || username.equalsIgnoreCase("unknown")) {
                username = uuid.toString();
            }

            names.add(username);
        }

        return String.join(", ", names);
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

    private void updateClaimMarker(Claim claim) {
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
            this.areaMarkers.put(markerid, marker);
        }
    }

    private void updateClaims(List<Claim> claims, boolean delete) {
        if (delete) {
            for (Claim claim : claims) {
                final UUID id = claim.getUniqueId();
                final String markerid = "GD_" + id;
                final AreaMarker marker = this.areaMarkers.get(markerid);
                if (marker != null) {
                    marker.deleteMarker();
                    this.areaMarkers.remove(markerid);
                }
            }
            // Validate existing markers
            final Iterator<AreaMarker> iterator = this.areaMarkers.values().iterator();
            while (iterator.hasNext()) {
                final AreaMarker marker = iterator.next();
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(marker.getMarkerID().replace("GD_", ""));
                    // If claim does not exist, delete marker
                    if (GriefDefender.getCore().getClaim(uuid) == null) {
                        marker.deleteMarker();
                        iterator.remove();
                    }
                } catch (Throwable t) {
                    
                }
            }
            return;
        }

        if (claims.isEmpty()) {
            for (Claim claim : GriefDefender.getCore().getAllClaims()) {
                if (claim.isWilderness()) {
                    continue;
                }
                updateClaimMarker(claim);
                for (Claim child : claim.getChildren(true)) {
                    updateClaimMarker(child);
                }
            }
        } else {
            for (Claim claim : claims) {
                updateClaimMarker(claim);
                for (Claim child : claim.getChildren(true)) {
                    updateClaimMarker(child);
                }
            }
        }
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
            this.set = this.markerapi.createMarkerSet("griefdefender.markerset", cfg.markerSetLabel, null, false);
        } else {
            this.set.setMarkerSetLabel(cfg.markerSetLabel);
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

        new GriefDefenderUpdate(new ArrayList<>(), 40L);
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

        private List<Claim> claims;

        public GriefDefenderUpdate(List<Claim> claims, long delay) {
            this.claims = claims;
            this.runTaskLaterAsynchronously(GDHooksBootstrap.getInstance().getLoader(), delay);
        }

        @Override
        public void run() {
            if (!disabled) {
                updateClaims(claims, false);
            } else {
                this.cancel();
            }
        }
    }

    public static class ClaimEventListener {
        public ClaimEventListener() {
            GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, new EventSubscriber<CreateClaimEvent>() {
                @Override
                public void on(CreateClaimEvent event) throws Throwable {
                    if (event instanceof CreateClaimEvent.Pre) {
                        return;
                    }
                    new ClaimEventListener.GriefDefenderUpdate(event.getClaims(), 20L, false);
                }
            });
            GriefDefender.getEventManager().getBus().subscribe(LoadClaimEvent.class, new EventSubscriber<LoadClaimEvent>() {
                @Override
                public void on(LoadClaimEvent event) throws Throwable {
                    if (event instanceof LoadClaimEvent.Pre) {
                        return;
                    }
                    new ClaimEventListener.GriefDefenderUpdate(event.getClaims(), 20L, false);
                }
            });
            GriefDefender.getEventManager().getBus().subscribe(RemoveClaimEvent.class, event -> new GriefDefenderUpdate(event.getClaims(), 20L, true));
            GriefDefender.getEventManager().getBus().subscribe(ChangeClaimEvent.class, event -> new GriefDefenderUpdate(event.getClaims(), 20L, false));
            GriefDefender.getEventManager().getBus().subscribe(TransferClaimEvent.class, event -> new GriefDefenderUpdate(event.getClaims(), 20L, false));
        }

        private class GriefDefenderUpdate extends BukkitRunnable {

            private List<Claim> claims;
            private boolean delete = false;

            public GriefDefenderUpdate(List<Claim> claims, long delay, boolean delete) {
                this.claims = claims;
                this.delete = delete;
                this.runTaskLaterAsynchronously(GDHooksBootstrap.getInstance().getLoader(), delay);
            }

            @Override
            public void run() {
                if (!GDHooks.getInstance().getDynmapProvider().disabled) {
                    GDHooks.getInstance().getDynmapProvider().updateClaims(this.claims, this.delete);
                } else {
                    Thread.dumpStack();
                    this.cancel();
                }
            }
        }
    }
}