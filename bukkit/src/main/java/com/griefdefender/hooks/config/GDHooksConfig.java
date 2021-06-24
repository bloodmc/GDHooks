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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;

import com.griefdefender.hooks.GDHooks;

public class GDHooksConfig {

    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode root;
    private ObjectMapper<GDHooksConfigData> configMapper;
    private GDHooksConfigData data;

    public GDHooksConfig(Path path) {

        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().path(path).build();
            this.configMapper = GDHooks.OBJECTMAPPER_FACTORY.get(GDHooksConfigData.class);

            if (reload()) {
                save();
            }
        } catch (Exception e) {
            GDHooks.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize configuration", e);
        }
    }

    public GDHooksConfigData getData() {
        return this.data;
    }

    public void save() {
        try {
            this.configMapper.save(this.data, this.root.node(GDHooks.MOD_ID));
            this.loader.save(this.root);
        } catch (IOException e) {
            GDHooks.getInstance().getLogger().log(Level.SEVERE, "Failed to save configuration", e);
        }
    }

    public boolean reload() {
        try {
            this.root = this.loader.load();
            this.data = this.configMapper.load(this.root.node(GDHooks.MOD_ID));
        } catch (Exception e) {
            GDHooks.getInstance().getLogger().log(Level.SEVERE, "Failed to load configuration", e);
            return false;
        }
        return true;
    }
}
