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
import org.spongepowered.configurate.objectmapping.meta.Setting;

import com.google.common.collect.ImmutableMap;
import com.griefdefender.hooks.config.category.ConfigCategory;
import com.griefdefender.hooks.util.LegacyHexSerializer;

import com.griefdefender.lib.kyori.adventure.text.Component;

@ConfigSerializable
public class MessageConfigData extends ConfigCategory {

    @Setting("descriptions")
    public Map<String, String> descriptionMap = new HashMap<>();

    @Setting("messages")
    public Map<String, String> messageMap = new HashMap<>();

    public Component getDescription(String message) {
        String rawMessage = this.descriptionMap.get(message);
        if (rawMessage == null) {
            // Should never happen but in case it does, return empty
            return Component.empty();
        }

        return LegacyHexSerializer.deserialize(rawMessage);
    }

    public Component getMessage(String message) {
        return this.getMessage(message, ImmutableMap.of());
    }

    public Component getMessage(String message, Map<String, Object> paramMap) {
        String rawMessage = this.messageMap.get(message);
        if (rawMessage == null) {
            // Should never happen but in case it does, return empty
            return Component.empty();
        }
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            final String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Component) {
                value = LegacyHexSerializer.serialize((Component) value);
            }
            rawMessage = rawMessage.replace("{" + key + "}", value.toString()); 
        }

        return LegacyHexSerializer.deserialize(rawMessage);
    }
}
