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
package com.griefdefender.hooks.config.category;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ProviderCategory extends ConfigCategory {

    // Shop Plugins
    @Setting(value = "BossShopPro")
    public boolean bossShopPro = true;

    @Setting(value = "ChestShop")
    public boolean chestShop = true;

    @Setting(value = "DynamicShop")
    public boolean dynamicShop = true;
	
    @Setting(value = "InsaneShops")
    public boolean insaneShops = true;

    @Setting(value = "QuickShop")
    public boolean quickShop = true;

    @Setting(value = "QuickShop-Hikari")
    public boolean quickShopHikari = true;

    @Setting(value = "ShopChest")
    public boolean shopChest = true;

    @Setting(value = "Shop")
    public boolean shop = true;

    @Setting(value = "Slabbo")
    public boolean slabbo = true;

    @Setting(value = "TradeShop")
    public boolean tradeShop = true;

    @Setting(value = "UltimateShops")
    public boolean ultimateShops = true;

    // Misc

    @Setting(value = "AureliumSkills")
    public boolean aureliumSkills = true;

    @Setting(value = "CustomItems")
    public boolean customItems = true;

    @Setting(value = "EliteMobs")
    public boolean eliteMobs = true;

    @Setting(value = "FurnitureLib")
    public boolean furnitureLib = true;

    @Setting(value = "Guilds")
    public boolean guilds = true;

    @Setting(value = "RevoltCrates")
    public boolean revoltCrates = true;

    @Setting(value = "SimpleClans")
    public boolean simpleClans = true;

    @Setting(value = "UltimateClans")
    public boolean ultimateClans = true;

    @Setting(value = "McMMO")
    public boolean mcMMO = true;

    @Setting(value = "MMOItems")
    public boolean mmoItems = true;

    @Setting(value = "MyPet")
    public boolean myPet = true;

    @Setting(value = "MythicMobs")
    public boolean mythicMobs = true;

    @Setting(value = "Nova")
    public boolean nova = true;

    @Setting(value = "OreRegenerator")
    public boolean oreRegenerator = true;

    @Setting(value = "Towny")
    public boolean towny = true;

    // Map
    @Setting(value = "BlueMap")
    public boolean bluemap = true;

    @Setting(value = "Dynmap")
    public boolean dynmap = true;

    @Setting(value = "Squaremap")
    public boolean squaremap = true;

}
