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

import java.time.Duration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class LegacyHexSerializer {

    private static LegacyComponentSerializer HEX_SERIALIZER = LegacyComponentSerializer.builder().hexColors().character(LegacyComponentSerializer.AMPERSAND_CHAR).build();

    public static String serialize(Component component) {
        return HEX_SERIALIZER.serialize(component);
    }

    public static TextComponent deserialize(String componentStr) {
        TextComponent component = null;
        try {
            component = HEX_SERIALIZER.deserialize(componentStr);
            return component;
        } catch (Throwable t) {
            return Component.empty();
        }
    }

    public static Title deserializeTitle(String title) {
        String[] parts = title.split(";");
        Component titleMain = HEX_SERIALIZER.deserialize(parts[0]);
        Component titleSub = HEX_SERIALIZER.deserialize(parts[1]);
        Duration fadeIn = Duration.ofSeconds(Long.valueOf(parts[2]));
        Duration timeStay = Duration.ofSeconds(Long.valueOf(parts[3]));
        Duration fadeOut = Duration.ofSeconds(Long.valueOf(parts[4]));
        return Title.title(titleMain, titleSub, Times.of(fadeIn, timeStay, fadeOut));
    }

    public static String serializeTitle(Title title) {
        String titleMain = HEX_SERIALIZER.serialize(title.title());
        String titleSub = HEX_SERIALIZER.serialize(title.subtitle());
        String fadeIn = String.valueOf(title.times().fadeIn().getSeconds());
        String timeStay = String.valueOf(title.times().stay().getSeconds());
        String fadeOut = String.valueOf(title.times().fadeOut().getSeconds());
        return titleMain + ";" + titleSub + ";" + fadeIn + ";" + timeStay + ";" + fadeOut;
    }
}
