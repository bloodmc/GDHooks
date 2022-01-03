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
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;

import com.griefdefender.hooks.GDHooks;

public class MessageConfig {

    private static final ConfigurationOptions LOADER_OPTIONS = ConfigurationOptions.defaults()
            .header(GDHooks.CONFIG_HEADER);
    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode root;
    private ObjectMapper<MessageConfigData> configMapper;
    private MessageConfigData data;
    public static MessageConfigData MESSAGE_DATA;

    public static final String CLAN_TRUST_RANK = "clan-trust-rank";
    public static final String CLAN_UNTRUST_RANK= "clan-untrust-rank";
    public static final String DESCRIPTION_RELOAD = "reload";
    public static final String DESCRIPTION_CLAN_CLAIM = "clan-claim";
    public static final String DESCRIPTION_CLAN_TRUST = "clan-trust";
    public static final String DESCRIPTION_CLAN_TRUST_ALL = "clan-trust-all";
    public static final String DESCRIPTION_CLAN_TRUST_ALL_ADMIN = "clan-trust-all-admin";
    public static final String DESCRIPTION_CLAN_TRUST_RANK = "clan-trust-rank";
    public static final String DESCRIPTION_CLAN_UNTRUST = "clan-untrust";
    public static final String DESCRIPTION_CLAN_UNTRUST_ALL = "clan-untrust-all";
    public static final String DESCRIPTION_CLAN_UNTRUST_ALL_ADMIN = "clan-untrust-all-admin";
    public static final String DESCRIPTION_CLAN_UNTRUST_RANK = "clan-untrust-rank";
    public static final String DESCRIPTION_VERSION = "version";
    public static final String CLAIM_DISABLED_WORLD = "claim-disabled-world";
    public static final String COMMAND_INVALID_CLAN = "command-invalid-clan";
    public static final String COMMAND_INVALID_GROUP = "command-invalid-group";
    public static final String PERMISSION_COMMAND_TRUST = "permission-command-trust";
    public static final String PERMISSION_TRUST = "permission-trust";
    public static final String PLUGIN_EVENT_CANCEL = "plugin-event-cancel";
    public static final String PLUGIN_RELOAD = "plugin-reload";
    public static final String TRUST_ALREADY_HAS = "trust-already-has";
    public static final String TRUST_PLUGIN_CANCEL = "trust-plugin-cancel";
    public static final String TRUST_GRANT = "trust-grant";
    public static final String TRUST_INDIVIDUAL_ALL_CLAIMS = "trust-individual-all-claims";
    public static final String TRUST_INVALID = "trust-invalid";
    public static final String TRUST_NO_CLAIMS = "trust-no-claims";
    public static final String UNTRUST_INDIVIDUAL_ALL_CLAIMS = "untrust-individual-all-claims";
    public static final String UNTRUST_INDIVIDUAL_SINGLE_CLAIM = "untrust-individual-single-claim";

    public MessageConfig(Path path) {

        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().path(path).defaultOptions(LOADER_OPTIONS).build();
            this.configMapper = GDHooks.OBJECTMAPPER_FACTORY.get(MessageConfigData.class);
            this.data = this.configMapper.load(CommentedConfigurationNode.root(LOADER_OPTIONS).node(GDHooks.MOD_ID));

            if (reload()) {
                save();
            }
        } catch (Exception e) {
            GDHooks.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize configuration", e);
        }
    }

    public MessageConfigData getData() {
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
            MESSAGE_DATA = this.data;
        } catch (Exception e) {
            GDHooks.getInstance().getLogger().log(Level.SEVERE, "Failed to load configuration", e);
            return false;
        }
        return true;
    }
}
