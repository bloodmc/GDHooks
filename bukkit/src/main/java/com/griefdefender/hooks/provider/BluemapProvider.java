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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimType;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.LoadClaimEvent;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.category.BluemapCategory;
import com.griefdefender.hooks.config.category.BluemapOwnerStyleCategory;
import com.griefdefender.hooks.provider.DynmapProvider.ClaimEventListener;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.marker.ExtrudeMarker;
import de.bluecolored.bluemap.api.marker.Marker;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import com.griefdefender.lib.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import com.griefdefender.lib.kyori.event.EventSubscriber;

public class BluemapProvider {

    private final Logger logger;
    private final BluemapCategory cfg;
    private BlueMapAPI api;
    private MarkerAPI markerapi;
    private Map<ClaimType, MarkerSet> markerSetMap = new HashMap<>();
    private boolean disabled = false;

    public BluemapProvider() {
        logger = GDHooks.getInstance().getLogger();
        this.cfg = GDHooks.getInstance().getConfig().getData().bluemap;
        BlueMapAPI.onEnable(api -> {
            this.disabled = false;
            logger.info("Initializing GriefDefender Bluemap provider...");
            this.api = api;
            activate();
        });
        BlueMapAPI.onDisable(api -> {
            this.clearMarkers();
            this.disabled = true;
        });
    }

    private void clearMarkers() {
        if (markerapi.getMarkerSets() != null) {
            for (MarkerSet set : markerapi.getMarkerSets()) {
                if (set.getId().equals("griefdefender-markers")) {
                    markerapi.removeMarkerSet(set);
                }
            }
        }
    }

    private void activate() {
        try {
            markerapi = api.getMarkerAPI();
            markerapi.load();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Could not initialize GriefDefender Bluemap provider.");
            return;
        }
        if (markerapi == null) {
            logger.severe("Error loading Bluemap Provider! Could not locate Marker API.");
            return;
        }

        this.clearMarkers();
        markerSetMap.clear();
        for (ClaimType claimType : GriefDefender.getRegistry().getAllOf(ClaimType.class)) {
            if (claimType == ClaimTypes.WILDERNESS) {
                continue;
            }
            final MarkerSet markerSet = markerapi.createMarkerSet("griefdefender-marker-" + claimType.getName().toLowerCase());
            this.markerSetMap.put(claimType, markerSet);
            if (claimType == ClaimTypes.ADMIN) {
                markerSet.setLabel(this.cfg.markerSetAdminLabel);
                markerSet.setDefaultHidden(this.cfg.hideByDefaultAdmin);
            } else if (claimType == ClaimTypes.BASIC) {
                markerSet.setLabel(this.cfg.markerSetBasicLabel);
                markerSet.setDefaultHidden(this.cfg.hideByDefaultBasic);
            } else if (claimType == ClaimTypes.SUBDIVISION) {
                markerSet.setLabel(this.cfg.markerSetSubdivisionLabel);
                markerSet.setDefaultHidden(this.cfg.hideByDefaultSubdivision);
            } else if (claimType == ClaimTypes.TOWN) {
                markerSet.setLabel(this.cfg.markerSetTownLabel);
                markerSet.setDefaultHidden(this.cfg.hideByDefaultTown);
            }
        }

        if (this.markerSetMap.isEmpty()) {
            this.logger.severe("Error creating marker set");
            return;
        }
       /* this.markerSet.setLabel(this.cfg.markerSetName);
        this.markerSet.setDefaultHidden(this.cfg.hideByDefault);*/

        new GriefDefenderFullUpdate(40L);
        new ClaimEventListener();
        this.logger.info("Bluemap provider is activated");
    }

    public void deleteClaims(List<Claim> claims) {
        if(markerapi == null) {
            return;
        }

        claims.forEach(claim ->{
            final BlueMapWorld blueMapWorld = api.getWorld(claim.getWorldUniqueId()).orElse(null);
            if (blueMapWorld == null) {
                return;
            }
    
            blueMapWorld.getMaps().forEach(map ->{
                final MarkerSet markerSet = this.markerSetMap.get(claim.getType());
                markerSet.removeMarker("#griefdefender:" + map.getId() + "_" + claim.getUniqueId());
            });
        });
        this.save();
    }

    public void updateClaims(List<Claim> claims){
        if(markerapi == null) {
            return;
        }

        claims.forEach(claim ->{
            this.updateClaim(claim);
        });
        this.save();
    }

    private void updateClaim(Claim claim) {
        final BlueMapWorld blueMapWorld = api.getWorld(claim.getWorldUniqueId()).orElse(null);
        if(blueMapWorld != null){
            blueMapWorld.getMaps().forEach(map ->{
                final MarkerSet markerSet = this.markerSetMap.get(claim.getType());
                final Marker marker = markerSet.getMarker("#griefdefender:" + map.getId() + "_" + claim.getUniqueId()).orElse(null);
                final Shape shape = Shape.createRect(claim.getLesserBoundaryCorner().getX(), claim.getLesserBoundaryCorner().getZ(), claim.getGreaterBoundaryCorner().getX(), claim.getGreaterBoundaryCorner().getZ());
                ExtrudeMarker shapeMarker;
                if (marker != null) {
                    shapeMarker = (ExtrudeMarker) marker;
                    shapeMarker.setShape(shape, claim.getLesserBoundaryCorner().getY(), claim.getGreaterBoundaryCorner().getY());
                } else {
                    shapeMarker = markerSet.createExtrudeMarker("#griefdefender:" + map.getId() + "_" + claim.getUniqueId(), map, shape, claim.getLesserBoundaryCorner().getY(), claim.getGreaterBoundaryCorner().getY());
                }
                shapeMarker.setLabel(this.getWindowInfo(claim));
                BluemapOwnerStyleCategory ownerStyle = null;
                if (!this.cfg.ownerStyles.isEmpty()) {
                    ownerStyle = this.cfg.ownerStyles.get(claim.getOwnerName().toLowerCase());
                }

                if (ownerStyle == null) {
                    ownerStyle = this.cfg.claimTypeStyles.get(claim.getType().getName().toLowerCase());
                }

                String sc = ownerStyle.lineColor;
                String fc = ownerStyle.fillColor;

                try {
                    Integer.parseInt(ownerStyle.lineColor.replaceAll("#", ""), 16);
                    Integer.parseInt(ownerStyle.fillColor.replaceAll("#", ""), 16);
                } catch (NumberFormatException e) {
                    if (claim.getType().equals(ClaimTypes.ADMIN)) {
                        sc = "#FF0000";
                        fc = "#FF0000";
                    } else if (claim.getType().equals(ClaimTypes.BASIC)) {
                        sc = "#FFFF00";
                        fc = "#FFFF00";
                    } else if (claim.getType().equals(ClaimTypes.TOWN)) {
                        sc = "#00FF00";
                        fc = "#00FF00";
                    } else if (claim.getType().equals(ClaimTypes.SUBDIVISION)) {
                        sc = "#FF9C00";
                        fc = "#FF9C00";
                    } else {
                        sc = "#FF0000";
                        fc = "#FF0000";
                    }
                }
                shapeMarker.setColors(hex2Rgb(sc, ownerStyle.strokeOpacity), hex2Rgb(fc, ownerStyle.fillOpacity));
                shapeMarker.setDepthTestEnabled(this.cfg.depthCheck);
            });
        }
    }

    private void save() {
        try {
            markerapi.save();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Error saving Bluemap Markers!");
        }
    }

    private String getWindowInfo(Claim claim) {
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
        final List<UUID> residentList = new ArrayList<>(claim.getUserTrusts(TrustTypes.RESIDENT));
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
        for (int i = 0; i < residentList.size(); i++) {
            if (i > 0) {
                trusted += ", ";
            }
            final UUID uuid = residentList.get(i);
            final String userName = GriefDefender.getRegistry().lookupUsername(uuid);
            if (userName.equalsIgnoreCase("unknown")) {
                trusted += uuid.toString();
            } else {
                trusted += userName;
            }
        }
        info = info.replace("%residents%", trusted);

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

    private static Color hex2Rgb(String hexColor, float opacity) {
        return new Color(
                Integer.valueOf(hexColor.substring(1, 3), 16),
                Integer.valueOf(hexColor.substring(3, 5), 16),
                Integer.valueOf(hexColor.substring(5, 7), 16),
                (int) (opacity * 255));
    }

    private class GriefDefenderFullUpdate extends BukkitRunnable {

        public GriefDefenderFullUpdate(long delay) {
            this.runTaskLaterAsynchronously(GDHooksBootstrap.getInstance().getLoader(), delay);
        }

        @Override
        public void run() {
            if (!disabled) {
                updateClaims(GriefDefender.getCore().getAllClaims());
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
        }

        private class GriefDefenderUpdate extends BukkitRunnable {

            private final List<Claim> claims;
            private boolean delete = false;

            public GriefDefenderUpdate(List<Claim> claims, long delay, boolean delete) {
                this.delete = delete;
                this.claims = claims;
                this.runTaskLaterAsynchronously(GDHooksBootstrap.getInstance().getLoader(), delay);
            }

            @Override
            public void run() {
                if (!GDHooks.getInstance().getBluemapProvider().disabled) {
                    if (this.delete) {
                        GDHooks.getInstance().getBluemapProvider().deleteClaims(this.claims);
                    } else {
                        GDHooks.getInstance().getBluemapProvider().updateClaims(this.claims);
                    }
                } else {
                    this.cancel();
                }
            }
        }
    }
}
