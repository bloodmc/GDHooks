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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.category.SquaremapCategory;
import com.griefdefender.hooks.config.category.SquaremapOwnerStyleCategory;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;
import xyz.jpenilla.squaremap.api.marker.Rectangle;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class SquaremapProvider {

    private final Logger logger;
    private final SquaremapCategory cfg;
    private Squaremap api;
    private final Map<String, SquaremapTask> provider = new HashMap<>();
    public boolean disabled = false;
    private boolean reload = false;

    public SquaremapProvider() {
        this.logger = GDHooks.getInstance().getLogger();
        this.cfg = GDHooks.getInstance().getConfig().getData().squaremap;
        logger.info("Initializing GriefDefender Squaremap provider...");
        activate();
    }

    public void activate() {
        try {
            this.api = xyz.jpenilla.squaremap.api.SquaremapProvider.get();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            logger.severe("Could not initialize GriefDefender Squaremap provider.");
            disabled = true;
            return;
        }

        if (this.reload) {
            GDHooks.getInstance().getConfig().reload();
            if (!provider.isEmpty()) {
                provider.values().forEach(SquaremapTask::disable);
                provider.clear();
            }
        } else {
            this.reload = true;
        }

        xyz.jpenilla.squaremap.api.SquaremapProvider.get().mapWorlds().forEach(world -> {
            final World bukkitWorld = Bukkit.getWorld(world.name());
            if (bukkitWorld == null) {
                return;
            }
            if (GriefDefender.getCore().isEnabled(bukkitWorld.getUID())) {
                SimpleLayerProvider provider = SimpleLayerProvider
                        .builder(cfg.control_label)
                        .showControls(cfg.control_show)
                        .defaultHidden(cfg.control_hide)
                        .build();
                
                world.layerRegistry().register(Key.of("griefdefender_" + world.name().toLowerCase()), provider);
                SquaremapTask task = new SquaremapTask(world, provider);
                task.runTaskTimerAsynchronously(GDHooksBootstrap.getInstance().getLoader(), 0, 20L * cfg.UPDATE_INTERVAL);
                this.provider.put(world.name().toLowerCase(), task);
            }
    });
        new ClaimEventListener();
    }

    public void onDisable() {
        disabled = true;
        provider.values().forEach(SquaremapTask::disable);
        provider.clear();
    }

    private class SquaremapTask extends BukkitRunnable {
        private final MapWorld world;
        private final SimpleLayerProvider provider;

        private boolean stop;

        public SquaremapTask(MapWorld world, SimpleLayerProvider provider) {
            this.world = world;
            this.provider = provider;
        }

        @Override
        public void run() {
            if (stop) {
                cancel();
            }
            updateClaims();
        }

        void updateClaims() {
            List<Claim> claims = GriefDefender.getCore().getAllClaims();
            if (claims != null) {
                claims.stream()
                        .filter(claim -> claim.getWorldName().equals(this.world.name().toLowerCase()))
                        .forEach(this::handleClaim);
            }
        }

        void deleteClaim(Claim claim) {
            String markerid = "griefdefender_" + world.name() + "_region_" + claim.getUniqueId();
            if (provider.hasMarker(Key.of(markerid))) {
                provider.removeMarker(Key.of(markerid));
            }
        }

        private void handleClaim(Claim claim) {

            if (claim.getLesserBoundaryCorner() == null) {
                return;
            }

            Rectangle rect = Marker.rectangle(Point.of(claim.getLesserBoundaryCorner().getX(), claim.getLesserBoundaryCorner().getZ()),
                    Point.of(claim.getGreaterBoundaryCorner().getX() + 1, claim.getGreaterBoundaryCorner().getZ() + 1));

            final List<UUID> builders = new ArrayList<>(claim.getUserTrusts(TrustTypes.BUILDER));
            final List<UUID> containers = new ArrayList<>(claim.getUserTrusts(TrustTypes.CONTAINER));
            final List<UUID> accessors = new ArrayList<>(claim.getUserTrusts(TrustTypes.ACCESSOR));
            final List<UUID> residents = new ArrayList<>(claim.getUserTrusts(TrustTypes.RESIDENT));
            final List<UUID> managers = new ArrayList<>(claim.getUserTrusts(TrustTypes.MANAGER));

            SquaremapOwnerStyleCategory ownerStyle = null;
            if (!cfg.ownerStyles.isEmpty()) {
                ownerStyle = cfg.ownerStyles.get(claim.getOwnerName().toLowerCase());
            }

            if (ownerStyle == null) {
                ownerStyle = cfg.claimTypeStyles.get(claim.getType().getName().toLowerCase());
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

            MarkerOptions.Builder options = MarkerOptions.builder()
                    .strokeColor(hex2Rgb(sc))
                    .strokeWeight(ownerStyle.Stroke_Weight)
                    .strokeOpacity(ownerStyle.Stroke_Opacity)
                    .fillColor(hex2Rgb(fc))
                    .fillOpacity(ownerStyle.Fill_Opacity)
                    .clickTooltip((claim.isAdminClaim() ? cfg.ADMIN_CLAIM_TOOLTIP : cfg.CLAIM_TOOLTIP)
                            .replace("%world%", world.name())
                            .replace("%uuid%", claim.getUniqueId().toString())
                            .replace("%owner%", claim.getOwnerName())
                            .replace("%owneruuid%", claim.getOwnerUniqueId().toString())
                            .replace("%claimname%",
                                    claim.getData().getDisplayNameComponent().isPresent()
                                            ? PlainTextComponentSerializer.plainText().serialize(claim.getDisplayNameComponent().get())
                                            : "none")
                            .replace("%lastseen%", claim.getData().getDateLastActive().toString())
                            .replace("%gdtype%", claim.getType().toString())
                            .replace("%managers%", getNames(managers))
                            .replace("%builders%", getNames(builders))
                            .replace("%containers%", getNames(containers))
                            .replace("%residents%", getNames(residents))
                            .replace("%accessors%", getNames(accessors))
                            .replace("%area%", Integer.toString(claim.getArea()))
                            .replace("%width%", Integer.toString(claim.getWidth()))
                            .replace("%height%", Integer.toString(claim.getHeight()))
                    );

            rect.markerOptions(options);

            String markerid = "griefdefender_" + world.name() + "_region_" + claim.getUniqueId();
            this.provider.addMarker(Key.of(markerid), rect);
        }

        private String getNames(List<UUID> list) {
            List<String> names = new ArrayList<>();
            for (UUID str : list) {
                final String userName = GriefDefender.getRegistry().lookupUsername(str);
                if (userName.equalsIgnoreCase("unknown")) {
                    names.add(str.toString());
                } else {
                    names.add(userName);
                }
            }
            return String.join(", ", names);
        }

        public void disable() {
            cancel();
            this.stop = true;
            this.provider.clearMarkers();
        }
    }

    private static Color hex2Rgb(String hexColor) {
        return new Color(
                Integer.valueOf(hexColor.substring(1, 3), 16),
                Integer.valueOf(hexColor.substring(3, 5), 16),
                Integer.valueOf(hexColor.substring(5, 7), 16)
        );
    }

  public static class ClaimEventListener {
      public ClaimEventListener() {
          GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, event -> new GriefDefenderUpdate(event.getClaims(), 20L, false));
          GriefDefender.getEventManager().getBus().subscribe(RemoveClaimEvent.class, event -> new GriefDefenderUpdate(event.getClaims(), 20L, true));
          GriefDefender.getEventManager().getBus().subscribe(ChangeClaimEvent.class, event -> new GriefDefenderUpdate(event.getClaims(), 20L, false));
     }

      private class GriefDefenderUpdate extends BukkitRunnable {

          private final List<Claim> claims;
          private boolean delete = false;

          public GriefDefenderUpdate(List<Claim> claims, long delay, boolean delete) {
              this.delete = delete;
              this.claims = claims;
              this.runTaskLater(GDHooksBootstrap.getInstance().getLoader(), delay);
          }

          @Override
          public void run() {
              if (!GDHooks.getInstance().getSquaremapProvider().disabled) {
                  if (this.delete) {
                      GDHooks.getInstance().getSquaremapProvider().deleteClaims(this.claims);
                  } else {
                      GDHooks.getInstance().getSquaremapProvider().updateClaims(this.claims);
                  }
              } else {
                  this.cancel();
              }
          }
      }
  }

  public void deleteClaims(List<Claim> claims) {
      if (provider.isEmpty()) {
          return;
      }

      claims.forEach(claim ->{
          final World world = Bukkit.getWorld(claim.getWorldUniqueId());
          final UUID worldUniqueId = claim.getWorldUniqueId();
          final MapWorld mapWorld = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(Bukkit.getWorld(worldUniqueId))).orElse(null);
          if (mapWorld == null) {
              return;
          }
          provider.get(world.getName().toLowerCase()).deleteClaim(claim);
      });
    }

  public void updateClaims(List<Claim> claims) {
      if (provider.isEmpty()) {
          return;
      }

      claims.forEach(claim ->{
          final World world = Bukkit.getWorld(claim.getWorldUniqueId());
          final UUID worldUniqueId = claim.getWorldUniqueId();
          final MapWorld mapWorld = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(Bukkit.getWorld(worldUniqueId))).orElse(null);
          if (mapWorld == null) {
              return;
          }
          provider.get(world.getName().toLowerCase()).handleClaim(claim);
      });
    }
}

