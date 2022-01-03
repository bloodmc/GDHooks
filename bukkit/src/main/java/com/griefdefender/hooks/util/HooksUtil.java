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
package com.griefdefender.hooks.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.claim.TrustTypes;

public class HooksUtil {

    public static Sign getSign(Location location) {
        if (location == null) {
            return null;
        }

        final Block block = location.getBlock();
        final BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return null;
        }

        return (Sign) state;
    }

    public static TrustType getTrustType(String type) {
        switch (type.toLowerCase()) {
            case "accessor" :
                return TrustTypes.ACCESSOR;
            case "resident" :
                return TrustTypes.RESIDENT;
            case "builder" :
                return TrustTypes.BUILDER;
            case "container" :
                return TrustTypes.CONTAINER;
            case "manager" :
                return TrustTypes.MANAGER;
            case "none" :
                return TrustTypes.NONE;
            default :
                return null;
        }
    }
}
