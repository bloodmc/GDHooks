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
package com.griefdefender.hooks.provider.clan.parties;

import com.alessiodp.parties.api.interfaces.PartyRank;
import com.griefdefender.api.clan.Rank;

import java.util.HashSet;
import java.util.Set;

public class GDRank implements Rank {

    private final PartyRank pluginRank;
    private final Set<String> permissions = new HashSet<>();

    public GDRank(PartyRank rank) {
        this.pluginRank = rank;
        for (String rankPerm : this.pluginRank.getPermissions()) {
            permissions.add(rankPerm.toLowerCase());
        }
    }

    @Override
    public String getName() {
        return this.pluginRank.getConfigName();
    }

    @Override
    public String getDisplayName() {
        return this.pluginRank.getName();
    }

    @Override
    public Set<String> getPermissions() {
        return this.permissions;
    }

}
