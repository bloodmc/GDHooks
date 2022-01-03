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
package com.griefdefender.hooks.provider.clan;

import java.util.UUID;

import org.bukkit.Location;

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.clan.ClanHome;

public class GDClanHome implements ClanHome {

    private final String name;
    private final Location location;
    private final Vector3i pos;

    public GDClanHome(String name, Location location) {
        this.name = name;
        this.location = location;
        this.pos = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getHomeWorldUniqueId() {
        return this.location.getWorld().getUID();
    }

    @Override
    public Vector3i getHomePos() {
        return this.pos;
    }

    @Override
    public Object getLocation() {
        return this.location;
    }

}
