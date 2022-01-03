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
package com.griefdefender.hooks.config;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.hooks.config.category.ConfigCategory;

@ConfigSerializable
public class ClanConfigData extends ConfigCategory {

    @Setting(value = "rank-trust-map")
    @Comment(value = "A clan rank to trust map allowing clan leaders to manage trust per rank. \nNote: Trust applies to all claims owned by clan.")
    public Map<String, String> clanRankTrustMap = new HashMap<>();

    @Setting(value = "ally-trust-map")
    @Comment(value = "A clan ally trust map allowing clan leaders to manage trust per ally. \nNote: Trust applies to all claims owned by clan.")
    public Map<String, String> clanAllyTrustMap = new HashMap<>();

    public ClanConfigData() {
        
    }

    public Map<String, String> getRankTrustMap() {
        return this.clanRankTrustMap;
    }

    public Map<String, String> getAllyTrustMap() {
        return this.clanAllyTrustMap;
    }

    public TrustType getRankTrust(String rank) {
        if (rank == null || rank.isEmpty()) {
            return TrustTypes.ACCESSOR;
        }

        final String trust = this.clanRankTrustMap.get(rank.toLowerCase());
        if (trust == null) {
            return TrustTypes.ACCESSOR;
        }
        try {
            return GriefDefender.getRegistry().getType(TrustType.class, trust).orElse(TrustTypes.ACCESSOR);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return TrustTypes.ACCESSOR;
    }

    public void addRankTrust(String rank, TrustType trustType) {
        this.clanRankTrustMap.put(rank.toLowerCase(), trustType.getName().toLowerCase());
    }

    public void deleteRankTrust(String rank) {
        this.clanRankTrustMap.remove(rank.toLowerCase());
    }

    public TrustType getAllyTrust(String clanTag) {
        if (clanTag == null || clanTag.isEmpty()) {
            return TrustTypes.ACCESSOR;
        }

        final String trust = this.clanAllyTrustMap.get(clanTag.toLowerCase());
        if (trust == null) {
            return TrustTypes.ACCESSOR;
        }
        try {
            return GriefDefender.getRegistry().getType(TrustType.class, trust).orElse(TrustTypes.ACCESSOR);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return TrustTypes.ACCESSOR;
    }

    public void addAllyTrust(String clanTag, TrustType trustType) {
        this.clanAllyTrustMap.put(clanTag.toLowerCase(), trustType.getName().toLowerCase());
    }

    public void deleteAllyTrust(String clanTag) {
        this.clanAllyTrustMap.remove(clanTag.toLowerCase());
    }
}
