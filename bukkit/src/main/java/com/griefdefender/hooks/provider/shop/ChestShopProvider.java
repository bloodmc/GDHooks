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
package com.griefdefender.hooks.provider.shop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Signs.ChestShopSign;

public class ChestShopProvider implements GDShopProvider {

    private final ChestShop plugin;

    public ChestShopProvider() {
        this.plugin = ChestShop.getPlugin();
    }

    @Override
    public boolean isLocationShop(Location location) {
        final Block block = location.getBlock();
        if (isShopSign(block)) {
            return true;
        }
        return ChestShopSign.isShopBlock(block);
    }

    private boolean isShopSign(Block block) {
        if (block == null) {
            return false;
        }

        final BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return false;
        }

        final String signData = (ChestShopSign.getItem((Sign) state));
        if (signData != null && !signData.isEmpty()) {
            return true;
        }
        return false;
    }
}
