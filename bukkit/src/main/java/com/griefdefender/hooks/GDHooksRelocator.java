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
package com.griefdefender.hooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.griefdefender.hooks.plugin.ClassPathAppender;

import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;

public class GDHooksRelocator {

    private List<Relocation> rules;
    private Set<String> relocationKeys = new HashSet<>();
    private final ClassPathAppender classPathAppender;

    public GDHooksRelocator(ClassPathAppender classPathAppender) {
        this.classPathAppender = classPathAppender;
        this.rules = new ArrayList<>();
        for (String name : GDHooksBootstrap.getInstance().getRelocateList()) {
            final String[] relocations = name.split("\\|");
            for (String relocation : relocations) {
                final String[] parts = relocation.split(":");
                final String key = parts[0];
                final String relocated = parts[1];
                if (!this.relocationKeys.contains(key)) {
                    this.relocationKeys.add(key);
                    this.rules.add(new Relocation(key, "com.griefdefender.lib." + relocated));
                }
            }
        }
    }

    public void relocateJars(Map<String, File> jarMap) {
        for (Map.Entry<String, File> mapEntry : jarMap.entrySet()) {
            final String name = mapEntry.getKey();
            final File input = mapEntry.getValue();
            final File output = Paths.get(input.getParentFile().getPath()).resolve(input.getName().replace(".jar", "") + "-shaded.jar").toFile();
            if (!output.exists()) {
                // Relocate
                JarRelocator relocator = new JarRelocator(input, output, this.rules);
        
                try {
                    relocator.run();
                } catch (IOException e) {
                    throw new RuntimeException("Unable to relocate", e);
                }
            }
            this.classPathAppender.addJarToClasspath(output.toPath());
        }
    }
}
