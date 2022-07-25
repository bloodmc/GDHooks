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
package com.griefdefender.hooks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMapper.Factory;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.util.NamingSchemes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.griefdefender.api.Clan;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.clan.Rank;
import com.griefdefender.api.provider.ClanProvider;
import com.griefdefender.hooks.command.CommandReload;
import com.griefdefender.hooks.command.CommandVersion;
import com.griefdefender.hooks.command.clan.CommandClanTrustRank;
import com.griefdefender.hooks.command.clan.CommandClanClaim;
import com.griefdefender.hooks.command.clan.CommandClanTrust;
import com.griefdefender.hooks.command.clan.CommandClanTrustAll;
import com.griefdefender.hooks.command.clan.CommandClanTrustAllAdmin;
import com.griefdefender.hooks.command.clan.CommandClanUntrustRank;
import com.griefdefender.hooks.command.clan.CommandClanUntrust;
import com.griefdefender.hooks.command.clan.CommandClanUntrustAll;
import com.griefdefender.hooks.command.clan.CommandClanUntrustAllAdmin;
import com.griefdefender.hooks.config.ClanConfig;
import com.griefdefender.hooks.config.GDHooksConfig;
import com.griefdefender.hooks.config.MessageConfig;
import com.griefdefender.hooks.config.MessageConfigData;
import com.griefdefender.hooks.listener.GDPermissionEventListener;
import com.griefdefender.hooks.listener.GDShopEventListener;
import com.griefdefender.hooks.provider.AureliumSkillsProvider;
import com.griefdefender.hooks.provider.BluemapProvider;
import com.griefdefender.hooks.provider.BreweryProvider;
import com.griefdefender.hooks.provider.CustomItemsProvider;
import com.griefdefender.hooks.provider.DynmapProvider;
import com.griefdefender.hooks.provider.EliteMobsProvider;
import com.griefdefender.hooks.provider.ExcellentCratesProvider;
import com.griefdefender.hooks.provider.FurnitureLibProvider;
import com.griefdefender.hooks.provider.MMOItemsProvider;
import com.griefdefender.hooks.provider.McMMOProvider;
import com.griefdefender.hooks.provider.MyPetProvider;
import com.griefdefender.hooks.provider.MythicMobsProvider;
import com.griefdefender.hooks.provider.NovaProvider;
import com.griefdefender.hooks.provider.OraxenProvider;
import com.griefdefender.hooks.provider.OreRegeneratorProvider;
import com.griefdefender.hooks.provider.SquaremapProvider;
import com.griefdefender.hooks.provider.RevoltCratesProvider;
import com.griefdefender.hooks.provider.clan.guilds.GuildsClanProvider;
import com.griefdefender.hooks.provider.clan.parties.PartiesClanProvider;
import com.griefdefender.hooks.provider.clan.simpleclans.SimpleClanProvider;
import com.griefdefender.hooks.provider.clan.towny.TownyProvider;
import com.griefdefender.hooks.provider.clan.uclans.UClansProvider;
import com.griefdefender.hooks.provider.shop.BossShopProvider;
import com.griefdefender.hooks.provider.shop.ChestShopProvider;
import com.griefdefender.hooks.provider.shop.DynamicShopProvider;
import com.griefdefender.hooks.provider.shop.GDShopProvider;
import com.griefdefender.hooks.provider.shop.InsaneShopsProvider;
import com.griefdefender.hooks.provider.shop.QuickShopProvider;
import com.griefdefender.hooks.provider.shop.QuickShopHikariProvider;
import com.griefdefender.hooks.provider.shop.ShopChestProvider;
import com.griefdefender.hooks.provider.shop.ShopProvider;
import com.griefdefender.hooks.provider.shop.SlabboProvider;
import com.griefdefender.hooks.provider.shop.TradeShopProvider;
import com.griefdefender.hooks.provider.shop.UltimateShopsProvider;

import co.aikar.commands.PaperCommandManager;
import com.griefdefender.lib.kyori.adventure.text.Component;
import com.griefdefender.lib.kyori.adventure.text.format.NamedTextColor;
import com.griefdefender.lib.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class GDHooks {

    private static GDHooks instance;
    public static final String MOD_ID = "GDHooks";
    public static final String IMPLEMENTATION_NAME = GDHooks.class.getPackage().getImplementationTitle();
    public static final String IMPLEMENTATION_VERSION = GDHooks.class.getPackage().getImplementationVersion() == null ? "unknown" : GDHooks.class.getPackage().getImplementationVersion();
    public static final String CONFIG_HEADER = IMPLEMENTATION_VERSION + "\n"
            + "# This plugin requires GriefDefender.\nReport issues to https://github.com/bloodmc/GDHooks/issues.\n"
            + "# Note: Support for this plugin is only provided to patreons. See https://patreon.com/bloodmc if you want to become one.\n";
    public static Factory OBJECTMAPPER_FACTORY = ObjectMapper.factoryBuilder().defaultNamingScheme(NamingSchemes.PASSTHROUGH).addNodeResolver(NodeResolver.onlyWithSetting()).build();
    public static final Component GDHOOKS_TEXT = Component.text()
            .append(Component.text("["))
            .append(Component.text("GDHooks", NamedTextColor.AQUA))
            .append(Component.text("] "))
            .build();
    private Path configPath = Paths.get(".", "plugins", "GDHooks");
    private Map<String, ClanConfig> clanConfigMap = new HashMap<>();
    private GDHooksConfig config;
    private PaperCommandManager commandManager;
    public MessageConfig messageStorage;
    public MessageConfigData messageData;
    private AureliumSkillsProvider aureliumSkillsProvider;
    private BreweryProvider breweryProvider;
    private BluemapProvider bluemapProvider;
    private ClanProvider clanProvider;
    private CustomItemsProvider customItemsProvider;
    private DynmapProvider dynmapProvider;
    private EliteMobsProvider eliteMobsProvider;
    private ExcellentCratesProvider excellentCratesProvider;
    private FurnitureLibProvider furnitureLibProvider;
    private GDShopProvider shopProvider;
    private McMMOProvider mcmmoProvider;
    private MMOItemsProvider mmoItemsProvider;
    private MyPetProvider myPetProvider;
    private MythicMobsProvider mythicMobsProvider;
    private NovaProvider novaProvider;
    private OraxenProvider oraxenProvider;
    private OreRegeneratorProvider oreRegeneratorProvider;
    private SquaremapProvider squaremapProvider;
    private RevoltCratesProvider revoltCratesProvider;

    public static GDHooks getInstance() {
        if (instance == null) {
            instance = new GDHooks();
        }
        return instance;
    }

    public void onEnable() {
        /*final String version = Bukkit.getPluginManager().getPlugin("GriefDefender").getDescription().getVersion().replaceAll("[^\\d.]", "");
        if (version.startsWith("1.")) {
            this.getLogger().info("GDHooks " + IMPLEMENTATION_VERSION + " requires Griefdefender v2.0.5+ to work.");
            return;
        }

        int minorVersion = 0;
        try {
            minorVersion = Integer.parseInt(version.substring(version.length() - 1)); 
        } catch (Throwable t) {
            this.getLogger().info("GDHooks " + IMPLEMENTATION_VERSION + " requires Griefdefender v2.0.5+ to work.");
            return;
        }

        if (minorVersion < 5) {
            this.getLogger().info("GDHooks " + IMPLEMENTATION_VERSION + " requires Griefdefender v2.0.5+ to work.");
            return;
        }*/

        this.getLogger().info("GDHooks boot start.");
        this.loadConfig();
        /*Bukkit.getScheduler().scheduleSyncDelayedTask(GDHooks.getInstance(), new Runnable(){
            public void run(){
                GDHooks.getInstance().onPostWorld();
            }
        });*/
        this.registerBaseCommands();
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            this.getLogger().info("Detected Vault. Checking for compatible shop plugin hooks...");
            // Check for shop plugins
            if (Bukkit.getPluginManager().getPlugin("BossShopPro") != null && Bukkit.getPluginManager().getPlugin("BossShopPro").isEnabled() && this.config.getData().providerCategory.bossShopPro) {
                this.shopProvider = new BossShopProvider();
                this.getLogger().info("BossShop provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("ChestShop") != null && Bukkit.getPluginManager().getPlugin("ChestShop").isEnabled() && this.config.getData().providerCategory.chestShop) {
                this.shopProvider = new ChestShopProvider();
                this.getLogger().info("ChestShop provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("DynamicShop") != null && Bukkit.getPluginManager().getPlugin("DynamicShop").isEnabled() && Bukkit.getPluginManager().getPlugin("DynamicShop").isEnabled() && this.config.getData().providerCategory.dynamicShop) {
                this.shopProvider = new DynamicShopProvider();
                this.getLogger().info("DynamicShop provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("InsaneShops") != null && Bukkit.getPluginManager().getPlugin("InsaneShops").isEnabled() && this.config.getData().providerCategory.insaneShops) {
                this.shopProvider = new InsaneShopsProvider();
                this.getLogger().info("InsaneShops provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("QuickShop") != null && Bukkit.getPluginManager().getPlugin("QuickShop").isEnabled() && this.config.getData().providerCategory.quickShop) {
                this.shopProvider = new QuickShopProvider();
                this.getLogger().info("QuickShop provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("QuickShop-Hikari") != null && Bukkit.getPluginManager().getPlugin("QuickShop-Hikari").isEnabled() && this.config.getData().providerCategory.quickShopHikari) {
                this.shopProvider = new QuickShopHikariProvider();
                this.getLogger().info("QuickShop-Hikari provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("ShopChest") != null && Bukkit.getPluginManager().getPlugin("ShopChest").isEnabled() && this.config.getData().providerCategory.shopChest) {
                this.shopProvider = new ShopChestProvider();
                this.getLogger().info("ShopChest provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("Shop") != null && Bukkit.getPluginManager().getPlugin("Shop").isEnabled() && this.config.getData().providerCategory.shop) {
                this.shopProvider = new ShopProvider();
                this.getLogger().info("Shop provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("Slabbo") != null && Bukkit.getPluginManager().getPlugin("Slabbo").isEnabled() && this.config.getData().providerCategory.slabbo) {
                this.shopProvider = new SlabboProvider();
                this.getLogger().info("Slabbo provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("TradeShop") != null && Bukkit.getPluginManager().getPlugin("TradeShop").isEnabled() && this.config.getData().providerCategory.tradeShop) {
                this.shopProvider = new TradeShopProvider();
                this.getLogger().info("TradeShop provider enabled!");
            }
            if (Bukkit.getPluginManager().getPlugin("UltimateShops") != null && Bukkit.getPluginManager().getPlugin("UltimateShops").isEnabled() && this.config.getData().providerCategory.ultimateShops) {
                this.shopProvider = new UltimateShopsProvider();
                this.getLogger().info("UltimateShops provider enabled!");
            }
            new GDShopEventListener();
        } else {
            this.getLogger().info("Could not locate a compatible economy plugin for Vault. Please check with your server administrator.");
        }
        if(Bukkit.getPluginManager().getPlugin("AureliumSkills") != null && Bukkit.getPluginManager().getPlugin("AureliumSkills").isEnabled() && this.config.getData().providerCategory.aureliumSkills) {
            this.aureliumSkillsProvider = new AureliumSkillsProvider();
            this.getLogger().info("AureliumSkills provider enabled!");
        }
        if(Bukkit.getPluginManager().getPlugin("Brewery") != null && Bukkit.getPluginManager().getPlugin("Brewery").isEnabled() && this.config.getData().providerCategory.aureliumSkills) {
            this.breweryProvider = new BreweryProvider();
            this.getLogger().info("Brewery provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("BlueMap") != null && Bukkit.getPluginManager().getPlugin("BlueMap").isEnabled()
                && this.config.getData().bluemap.enabled && this.config.getData().providerCategory.bluemap) {
            this.bluemapProvider = new BluemapProvider();
        }
        if (Bukkit.getPluginManager().getPlugin("dynmap") != null && Bukkit.getPluginManager().getPlugin("dynmap").isEnabled()
                && this.config.getData().dynmap.enabled && this.config.getData().providerCategory.dynmap) {
            this.dynmapProvider = new DynmapProvider();
            this.getLogger().info("Dynmap provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("squaremap") != null && Bukkit.getPluginManager().getPlugin("squaremap").isEnabled()
                && this.config.getData().squaremap.enabled && this.config.getData().providerCategory.squaremap) {
            this.squaremapProvider = new SquaremapProvider();
            this.getLogger().info("Squaremap provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("CustomItems") != null && Bukkit.getPluginManager().getPlugin("CustomItems").isEnabled() && this.config.getData().providerCategory.customItems) {
            this.customItemsProvider = new CustomItemsProvider();
            this.getLogger().info("CustomItems provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("EliteMobs") != null && Bukkit.getPluginManager().getPlugin("EliteMobs").isEnabled() && this.config.getData().providerCategory.eliteMobs) {
            this.eliteMobsProvider = new EliteMobsProvider();
            this.getLogger().info("EliteMobs provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("ExcellentCrates") != null && Bukkit.getPluginManager().getPlugin("ExcellentCrates").isEnabled() && this.config.getData().providerCategory.excellentCratesProvider) {
            this.excellentCratesProvider = new ExcellentCratesProvider();
            this.getLogger().info("ExcellentCrates provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("FurnitureLib") != null && Bukkit.getPluginManager().getPlugin("FurnitureLib").isEnabled() && this.config.getData().providerCategory.furnitureLib) {
            this.furnitureLibProvider = new FurnitureLibProvider();
            this.getLogger().info("FurnitureLib provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("MMOItems") != null && Bukkit.getPluginManager().getPlugin("MMOItems").isEnabled() && this.config.getData().providerCategory.mmoItems) {
            this.mmoItemsProvider = new MMOItemsProvider();
            this.getLogger().info("MMOItems provider enabled!");
        }
        if(Bukkit.getPluginManager().getPlugin("mcMMO") != null && Bukkit.getPluginManager().getPlugin("mcMMO").isEnabled() && this.config.getData().providerCategory.mcMMO) {
            this.mcmmoProvider = new McMMOProvider();
            this.getLogger().info("McMMO provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("MyPet") != null && Bukkit.getPluginManager().getPlugin("MyPet").isEnabled() && this.config.getData().providerCategory.myPet) {
            this.myPetProvider = new MyPetProvider();
            this.getLogger().info("MyPet provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null && Bukkit.getPluginManager().getPlugin("MythicMobs").isEnabled() && this.config.getData().providerCategory.mythicMobs) {
            this.mythicMobsProvider = new MythicMobsProvider();
            this.getLogger().info("MythicMobs provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("Nova") != null && Bukkit.getPluginManager().getPlugin("Nova").isEnabled() && this.config.getData().providerCategory.nova) {
            this.novaProvider = new NovaProvider();
            this.getLogger().info("Nova provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("Oraxen") != null && Bukkit.getPluginManager().getPlugin("Oraxen").isEnabled() && this.config.getData().providerCategory.oraxen) {
            this.oraxenProvider = new OraxenProvider();
            this.getLogger().info("Oraxen provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("OreRegenerator") != null && Bukkit.getPluginManager().getPlugin("OreRegenerator").isEnabled() && this.config.getData().providerCategory.oreRegenerator) {
            this.oreRegeneratorProvider = new OreRegeneratorProvider();
            this.getLogger().info("OreRegenerator provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("RevoltCrates") != null && Bukkit.getPluginManager().getPlugin("RevoltCrates").isEnabled() && this.config.getData().providerCategory.revoltCrates) {
            this.revoltCratesProvider = new RevoltCratesProvider();
            this.getLogger().info("RevoltCrates provider enabled!");
        }
        // Clan plugins
        if (Bukkit.getPluginManager().getPlugin("Towny") != null && Bukkit.getPluginManager().getPlugin("Towny").isEnabled() && this.config.getData().providerCategory.towny) {
            this.clanProvider = new TownyProvider();
            this.registerClanCommands();
            this.getLogger().info("Towny provider enabled!");
        }
        if (Bukkit.getPluginManager().getPlugin("Guilds") != null && Bukkit.getPluginManager().getPlugin("Guilds").isEnabled() && this.config.getData().providerCategory.guilds) {
            this.clanProvider = new GuildsClanProvider();
            this.registerClanCommands();
            this.getLogger().info("Guilds provider enabled!");
        } else if (Bukkit.getPluginManager().getPlugin("SimpleClans") != null && Bukkit.getPluginManager().getPlugin("SimpleClans").isEnabled() && this.config.getData().providerCategory.simpleClans) {
            this.clanProvider = new SimpleClanProvider();
            this.registerClanCommands();
            this.getLogger().info("SimpleClans provider enabled!");
        } else if (Bukkit.getPluginManager().getPlugin("UltimateClans") != null && Bukkit.getPluginManager().getPlugin("UltimateClans").isEnabled() && this.config.getData().providerCategory.ultimateClans) {
            this.clanProvider = new UClansProvider();
            this.registerClanCommands();
            this.getLogger().info("UltimateClans provider enabled!");
        } else if (Bukkit.getPluginManager().getPlugin("Parties") != null && Bukkit.getPluginManager().getPlugin("Parties").isEnabled() && this.config.getData().providerCategory.parties) {
            this.clanProvider = new PartiesClanProvider();
            this.registerClanCommands();
            this.getLogger().info("Parties provider enabled!");
        }
        new GDPermissionEventListener();
        this.getLogger().info("GDHooks loaded successfully.");
    }

    public void reload() {
        this.loadConfig();
        this.registerCommandCompletions();
    }

    public void loadConfig() {
        this.config = new GDHooksConfig(this.configPath.resolve("config.conf"));
        String localeString = this.config.getData().message.locale;
        try {
            LocaleUtils.toLocale(localeString);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            this.getLogger().severe("Could not validate the locale '" + localeString + "'. Defaulting to 'en_US'...");
            localeString = "en_US";
        }
        Path localePath = this.configPath.resolve("lang").resolve(localeString + ".conf");
        if (!localePath.toFile().exists()) {
            // check for lower case
            Path secondaryPath = this.configPath.resolve("lang").resolve(localeString.toLowerCase() + ".conf");
            if (secondaryPath.toFile().exists()) {
                localePath = secondaryPath;
            }
        }
        if (!localePath.toFile().exists()) {
            // Check for a default locale asset and copy to lang folder
            try {
                final InputStream in = getClass().getResourceAsStream("/assets/lang/" + localeString + ".conf");
                FileUtils.copyInputStreamToFile(in, localePath.toFile());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        messageStorage = new MessageConfig(localePath);
        messageData = messageStorage.getData();
    }

    public void registerBaseCommands() {
        PaperCommandManager manager = new PaperCommandManager(GDHooksBootstrap.getInstance().getLoader());
        this.commandManager = manager;
        manager.getCommandReplacements().addReplacements(
            "reload", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_RELOAD),
            "version", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_VERSION));
        manager.registerCommand(new CommandReload());
        manager.registerCommand(new CommandVersion());
        this.registerCommandCompletions();
    }

    public void registerClanCommands() {
        this.commandManager.getCommandReplacements().addReplacements(
            "clan-claim", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_CLAIM),
            "clan-trust", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_TRUST),
            "clan-trust-all", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_TRUST_ALL),
            "clan-trust-all-admin", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_TRUST_ALL_ADMIN),
            "clan-trust-rank", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_TRUST_RANK),
            "clan-untrust", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_UNTRUST),
            "clan-untrust-all", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_UNTRUST_ALL),
            "clan-untrust-all-admin", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_UNTRUST_ALL_ADMIN),
            "clan-untrust-rank", this.getCommandDescriptionTranslation(MessageConfig.DESCRIPTION_CLAN_UNTRUST_RANK));
        this.commandManager.registerCommand(new CommandClanClaim());
        this.commandManager.registerCommand(new CommandClanTrustRank());
        this.commandManager.registerCommand(new CommandClanTrust());
        this.commandManager.registerCommand(new CommandClanTrustAll());
        this.commandManager.registerCommand(new CommandClanTrustAllAdmin());
        this.commandManager.registerCommand(new CommandClanUntrust());
        this.commandManager.registerCommand(new CommandClanUntrustAll());
        this.commandManager.registerCommand(new CommandClanUntrustAllAdmin());
        this.commandManager.registerCommand(new CommandClanUntrustRank());
        this.registerClanCompletions();
    }

    public void registerCommandCompletions() {
        this.commandManager.getCommandCompletions().registerCompletion("gdtrusttypes", c -> {
            Set<String> tabList = new HashSet<>();
            for (TrustType type : GriefDefender.getRegistry().getRegistryModuleFor(TrustType.class).get().getAll()) {
                tabList.add(type.getName());
            }
            return ImmutableSet.copyOf(tabList);
        });
        this.commandManager.getCommandCompletions().registerCompletion("gdclaimids", c -> {
            Set<String> tabList = new HashSet<>();
            if (c.getPlayer() != null) {
                return this.generateClaimIdTabList(c.getPlayer());
            }

            return tabList;
        });
        this.commandManager.getCommandCompletions().registerCompletion("gddummy", c -> {
            return ImmutableList.of();
        });
    }

    public void registerClanCompletions() {
        this.commandManager.getCommandCompletions().registerCompletion("gdclans", c -> {
            Set<String> tags = new HashSet<>();
            for (Clan clan : this.clanProvider.getAllClans()) {
                tags.add(clan.getTag());
            }
            return ImmutableSet.copyOf(tags);
        });
        this.commandManager.getCommandCompletions().registerCompletion("gdclanranks", c -> {
            Set<String> ranks = new HashSet<>();
            for (Clan clan : this.clanProvider.getAllClans()) {
                for (Rank rank : clan.getRanks()) {
                    ranks.add(rank.getName().toLowerCase());
                }
            }
            return ImmutableSet.copyOf(ranks);
        });
    }

    public void updateClanCompletions() {
        this.commandManager.getCommandCompletions().registerCompletion("gdclans", c -> {
            Set<String> tags = new HashSet<>();
            for (Clan clan : this.clanProvider.getAllClans()) {
                tags.add(clan.getTag());
            }
            return ImmutableSet.copyOf(tags);
        });
    }

    private Set<String> generateClaimIdTabList(Player player) {
        final Set<String> tabList = new HashSet<>();
        final User user = GriefDefender.getCore().getUser(player.getUniqueId());
        if (user.getPlayerData().canManageAdminClaims()) {
            final Map<String, UUID> adminMap = GriefDefender.getCore().getFriendlyIdentifierMapView().get(GriefDefender.getCore().getAdminUser().getUniqueId());
            if (adminMap != null) {
                for (String key : adminMap.keySet()) {
                    tabList.add("admin:" + key);
                }
            }
        }
        if (user.getPlayerData().canManageWildernessClaims()) {
            final Map<String, UUID> worldMap = GriefDefender.getCore().getFriendlyIdentifierMapView().get(GriefDefender.getCore().getWildernessUser().getUniqueId());
            if (worldMap != null) {
                for (String key : worldMap.keySet()) {
                    tabList.add("wilderness:" + key);
                }
            }
        }
        final Map<String, UUID> playerMap = GriefDefender.getCore().getFriendlyIdentifierMapView().get(player.getUniqueId());
        if (playerMap != null) {
            for (String key : playerMap.keySet()) {
                tabList.add(key);
            }
        }

        return tabList;
    }

    private String getCommandDescriptionTranslation(String message) {
        return PlainComponentSerializer.plain().serialize(messageData.getDescription(message));
    }

    public void onDisable() {
        
    }

    public void onPostWorld() {

    }

    public AureliumSkillsProvider getAureliumSkillsProvider() {
        return this.aureliumSkillsProvider;
    }

    public BreweryProvider getBreweryProvider() {
        return this.breweryProvider;
    }

    public ClanProvider getClanProvider() {
        return this.clanProvider;
    }

    public CustomItemsProvider getCustomItemsProvider() {
        return this.customItemsProvider;
    }

    public EliteMobsProvider getEliteMobsProvider() {
        return this.eliteMobsProvider;
    }

    public ExcellentCratesProvider getExcellentCratesProvider() {
        if (this.excellentCratesProvider == null) {
            this.excellentCratesProvider = new ExcellentCratesProvider();
        }
        return this.excellentCratesProvider;
    }

    public FurnitureLibProvider getFurnitureLibProvider() {
        return this.furnitureLibProvider;
    }

    public MMOItemsProvider getMMOItemsProvider() {
        return this.mmoItemsProvider;
    }

    public McMMOProvider getMcMMOProvider() {
        return this.mcmmoProvider;
    }

    public MyPetProvider getMyPetProvider() {
        return this.myPetProvider;
    }

    public MythicMobsProvider getMythicMobsProvider() {
        return this.mythicMobsProvider;
    }

    public NovaProvider getNovaProvider() {
        return this.novaProvider;
    }

    public OraxenProvider getOraxenProvider() {
        return this.oraxenProvider;
    }

    public OreRegeneratorProvider getOreRegeneratorProvider() {
        return this.oreRegeneratorProvider;
    }

    public RevoltCratesProvider getRevoltCratesProvider() {
        return this.revoltCratesProvider;
    }

    public GDShopProvider getShopProvider() {
        return this.shopProvider;
    }

    public BluemapProvider getBluemapProvider() {
        return this.bluemapProvider;
    }

    public DynmapProvider getDynmapProvider() {
        return this.dynmapProvider;
    }

    public SquaremapProvider getSquaremapProvider() {
        return this.squaremapProvider;
    }

    public GDHooksConfig getConfig() {
        return this.config;
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    public Map<String, ClanConfig> getClanConfigMap() {
        return this.clanConfigMap;
    }

    public PaperCommandManager getCommandManager() {
        return this.commandManager;
    }

    public Logger getLogger() {
        return GDHooksBootstrap.getInstance().getLoader().getLogger();
    }
}
