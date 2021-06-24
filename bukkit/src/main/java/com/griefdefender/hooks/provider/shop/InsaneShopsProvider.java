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

import java.lang.reflect.Field;

import org.bukkit.Location;

import me.TechsCode.InsaneShops.InsaneShops;
import me.TechsCode.InsaneShops.base.reflection.titleAndActionbar.ActionBar;

public class InsaneShopsProvider implements GDShopProvider {

    private InsaneShops plugin;

    public InsaneShopsProvider() {
        try {
            Field field = ActionBar.class.getDeclaredField("plugin");
            field.setAccessible(true);
            this.plugin = (InsaneShops) field.get(null);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public boolean isLocationShop(Location location) {
        return this.plugin.getShops().location(location).isPresent();
    }
}
