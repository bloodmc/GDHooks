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

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;

public class ItemsAdderProvider {

    public ItemsAdderProvider() {
    }

    public String getItemId(ItemStack itemstack) {
        final CustomStack customStack = CustomStack.byItemStack(itemstack);
        if (customStack == null) {
            return null;
        }

        String name = customStack.getDisplayName();
        name = name.replaceAll(" ", "\\_");
        name = name.replaceAll("[^A-Za-z0-9\\_]", "");
        return "itemsadder:" + name.toLowerCase();
    }

    public String getBlockId(Block block) {
        final CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null) {
            return null;
        }

        String name = customBlock.getDisplayName();
        name = name.replaceAll(" ", "\\_");
        name = name.replaceAll("[^A-Za-z0-9\\_]", "");
        return "itemsadder:" + name.toLowerCase();
    }
}
