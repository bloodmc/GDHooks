package com.griefdefender.hooks.provider;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksBootstrap;
import com.griefdefender.hooks.config.category.Pl3xmapCategory;
import com.griefdefender.hooks.config.category.Pl3xmapOwnerStyleCategory;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.*;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import net.pl3x.map.api.marker.Rectangle;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;


public class Pl3xmapProvider {

    private final Logger logger;
    private final Pl3xmapCategory cfg;
    private final Pl3xMap api = Pl3xMapProvider.get();
    private final Core GDCore = GriefDefender.getCore();
    private final Map<UUID, Pl3xmapTask> provider = new HashMap<>();
    public boolean disabled = false;
    private boolean reload = false;
    private Object Pl3xmapProvider;

    public Pl3xmapProvider() {
        this.logger = GDHooks.getInstance().getLogger();
        this.cfg = GDHooks.getInstance().getConfig().getData().pl3xmap;
        logger.info("Initializing GriefDefender Pl3xmap provider...");
        activate();
    }

    public void activate() {
        try {
            Pl3xMapProvider.get();
            disabled = false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            logger.severe("Could not initialize GriefDefender Pl3xMap provider.");
            disabled = true;
            return;
        }

        if (this.reload) {
            GDHooks.getInstance().getConfig().reload();
            if (!provider.isEmpty()) {
                provider.values().forEach(Pl3xmapTask::disable);
                provider.clear();
            }
        } else {
            this.reload = true;
        }

        Pl3xMapProvider.get().mapWorlds().forEach(world -> {
            if (GDCore.isEnabled(world.uuid())) {
                SimpleLayerProvider provider = SimpleLayerProvider
                        .builder(cfg.control_label)
                        .showControls(cfg.control_show)
                        .defaultHidden(cfg.control_hide)
                        .build();
                world.layerRegistry().register(Key.of("griefdefender_" + world.uuid()), provider);
                Pl3xmapTask task = new Pl3xmapTask(world, provider);
                task.runTaskTimerAsynchronously((Plugin) Pl3xmapProvider, 0, 20L * cfg.UPDATE_INTERVAL);
                this.provider.put(world.uuid(), task);
            }
    });
        new ClaimEventListener();
    }

    public void onDisable() {
        disabled = true;
        provider.values().forEach(Pl3xmapTask::disable);
        provider.clear();
    }

    private class Pl3xmapTask extends BukkitRunnable {
        private final MapWorld world;
        private final SimpleLayerProvider provider;

        private boolean stop;

        public Pl3xmapTask(MapWorld world, SimpleLayerProvider provider) {
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
            List<Claim> claims = GDCore.getAllClaims();
            if (claims != null) {
                claims.stream()
                        .filter(claim -> claim.getWorldUniqueId().equals(this.world.uuid()))
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
            final List<UUID> managers = new ArrayList<>(claim.getUserTrusts(TrustTypes.MANAGER));

            Pl3xmapOwnerStyleCategory ownerStyle = null;
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
                            .replace("{world}", world.name())
                            .replace("{uuid}", claim.getUniqueId().toString())
                            .replace("{owner}", claim.getOwnerName())
                            .replace("{owner_uuid}", claim.getOwnerUniqueId().toString())
                            .replace("{claimname}",
                                    claim.getData().getDisplayNameComponent().isPresent()
                                            ? PlainComponentSerializer.plain().serialize(claim.getDisplayNameComponent().get())
                                            : "none")
                            .replace("{lastseen}", claim.getData().getDateLastActive())
                            .replace("{gd_type}", claim.getType().toString())
                            .replace("{managers}", getNames(managers))
                            .replace("{builders}", getNames(builders))
                            .replace("{containers}", getNames(containers))
                            .replace("{accessors}", getNames(accessors))
                            .replace("{area}", Integer.toString(claim.getArea()))
                            .replace("{width}", Integer.toString(claim.getWidth()))
                            .replace("{height}", Integer.toString(claim.getHeight()))
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
              if (!GDHooks.getInstance().getPl3xmapProvider().disabled) {
                  if (this.delete) {
                      GDHooks.getInstance().getPl3xmapProvider().deleteClaims(this.claims);
                  } else {
                      GDHooks.getInstance().getPl3xmapProvider().updateClaims(this.claims);
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
          UUID claimWorld = claim.getWorldUniqueId();
          final MapWorld pl3xMapWorld = api.getWorldIfEnabled(claimWorld).orElse(null);
          if (pl3xMapWorld == null) {
              return;
          }
          provider.get(claimWorld).deleteClaim(claim);
      });
    }

  public void updateClaims(List<Claim> claims) {
      if (provider.isEmpty()) {
          return;
      }

      claims.forEach(claim ->{
          UUID claimWorld = claim.getWorldUniqueId();
          final MapWorld pl3xMapWorld = api.getWorldIfEnabled(claimWorld).orElse(null);
          if (pl3xMapWorld == null) {
              return;
          }
          provider.get(claimWorld).handleClaim(claim);
      });
    }
}

