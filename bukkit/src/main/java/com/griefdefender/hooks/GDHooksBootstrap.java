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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.griefdefender.hooks.loader.LoaderBootstrap;
import com.griefdefender.hooks.plugin.ClassPathAppender;
import com.griefdefender.hooks.plugin.JarInJarClassPathAppender;

public class GDHooksBootstrap implements LoaderBootstrap {

    private Map<String, File> jarMap = new HashMap<>();
    private Map<String, File> relocateMap = new HashMap<>();
    private List<String> relocateList = new ArrayList<>();
    private final JavaPlugin loader;
    private final ClassPathAppender classPathAppender;
    private final Logger logger;
    private static GDHooksBootstrap instance;
    private static final String LIB_ROOT_PATH = "./plugins/GDHooks/lib/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.75 Safari/535.7";

    public static GDHooksBootstrap getInstance() {
        return instance;
    }

    public GDHooksBootstrap(JavaPlugin loader) {
        instance = this;
        this.loader = loader;
        this.classPathAppender = new JarInJarClassPathAppender(getClass().getClassLoader());
        this.logger = loader.getLogger();
    }

    @Override
    public void onEnable() {
        final JSONParser parser = new JSONParser();
        String bukkitJsonVersion = null;
        this.logger.info("Loading libraries...");
        if (Bukkit.getVersion().contains("1.12.2")) {
            bukkitJsonVersion = "1.12.2";
        } else if (Bukkit.getVersion().contains("1.13.2")) {
            bukkitJsonVersion = "1.13.2";
        } else if (Bukkit.getVersion().contains("1.14.4")) {
            bukkitJsonVersion = "1.14.4";
        } else if (Bukkit.getVersion().contains("1.15.2")) {
            bukkitJsonVersion = "1.15.2";
        } else if (Bukkit.getVersion().contains("1.16.1")) {
            bukkitJsonVersion = "1.16.1";
        } else if (Bukkit.getVersion().contains("1.16.2") || Bukkit.getVersion().contains("1.16.3")) {
            bukkitJsonVersion = "1.16.2";
        } else if (Bukkit.getVersion().contains("1.16.4") || Bukkit.getVersion().contains("1.16.5")) {
            bukkitJsonVersion = "1.16.4";
        } else if (Bukkit.getVersion().contains("1.17")) {
            bukkitJsonVersion = "1.17.0";
        } else if (Bukkit.getVersion().contains("1.18")) {
            bukkitJsonVersion = "1.18.0";
        } else {
            this.logger.severe("Detected unsupported version '" + Bukkit.getVersion() + "'. GDHooks only supports 1.12.2, 1.13.2, 1.14.4, 1.15.2, 1.16.X, 1.17.X, 1.18.X GDHooks will NOT load.");
            return;
        }
        try {
            final InputStream in = getClass().getResourceAsStream("/" + bukkitJsonVersion + ".json");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            final JSONObject a = (JSONObject) parser.parse(reader);
            final JSONArray libraries = (JSONArray) a.get("libraries");
            if (libraries == null) {
                this.logger.severe("Resource " + bukkitJsonVersion + ".json is corrupted!. Please contact author for assistance.");
                return;
            }
            final Iterator<JSONObject> iterator = libraries.iterator();
            final int javaVersion = getJavaVersion();
            while (iterator.hasNext()) {
                JSONObject lib = iterator.next();
                final String name = (String) lib.get("name");
                if (name.equals("com.griefdefender:reflect-helper:1.0") && javaVersion >= 11) {
                    continue;
                } else if (name.equals("com.griefdefender:reflect-helper:2.0") && (javaVersion < 11 || javaVersion >= 16)) {
                    continue;
                }
                final String sha1 = (String) lib.get("sha1");
                final String path = (String) lib.get("path");
                final String relocate = (String) lib.get("relocate");
                final String url = (String) lib.get("url");
                final Path libPath = Paths.get(LIB_ROOT_PATH).resolve(path);
                final File file = libPath.toFile();
                downloadLibrary(name, relocate, sha1, url, libPath);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // Inject jar-relocator
        injectRelocatorDeps();
        final GDHooksRelocator relocator = new GDHooksRelocator(classPathAppender);
        // Relocate all GD dependencies and inject
        relocator.relocateJars(this.relocateMap);
        // Boot GDHooks
        GDHooks.getInstance().onEnable();
    }

    public List<String> getRelocateList() {
        return this.relocateList;
    }

    @Override
    public void onDisable() {
        GDHooks.getInstance().onDisable();
        this.classPathAppender.close();
    }

    private void injectRelocatorDeps() {
        //String name = "me.lucko:jar-relocator:1.4";
        //File file = this.relocateMap.get(name);
        //this.getClassPathAppender().addJarToClasspath(file.toPath());
    }

    public void downloadLibrary(String name, String relocate, String sha1, String url, Path libPath) {
        final File file = libPath.toFile();
        if (!Files.exists(libPath)) {
            this.logger.info("Downloading library " + name + " ...");
            try {
                URL website = new URL(url);
                URLConnection urlConnection = website.openConnection();
                // Some maven repos like nexus require a user agent so we just pass one to satisfy it
                urlConnection.setRequestProperty("User-Agent", USER_AGENT);
                ReadableByteChannel rbc = Channels.newChannel(urlConnection.getInputStream());
                if (!Files.exists(libPath)) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
            } catch (IOException e) {
                this.logger.severe("An error occured while downloading library '" + name + "'. Skipping...");
                e.printStackTrace();
                return;
            }

            
            final String hash = getLibraryHash(file);
            
            if (hash == null || !sha1.equals(hash)) {
                this.logger.severe("Detected invalid hash '" + hash + "' for file '" + libPath + "'. Expected '" + sha1 + "'. Skipping...");
                try {
                    Files.delete(libPath);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        if (relocate != null && !relocate.isEmpty() && relocate.contains(":")) {
            this.relocateList.add(relocate);
            this.relocateMap.put(name, file);
        } else {
            this.jarMap.put(name, file);
            this.getClassPathAppender().addJarToClasspath(file.toPath());
        }
    }

    private String getLibraryHash(File file) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            final byte[] data = Files.readAllBytes(file.toPath());
            final byte[] b = md.digest(data); 
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if ((0xff & b[i]) < 0x10) {
                    buffer.append("0" + Integer.toHexString((0xFF & b[i])));
                } else {
                    buffer.append(Integer.toHexString(0xFF & b[i]));
                }
            }
            return buffer.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version").replaceAll("[^\\d.]", "");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            final int dot = version.indexOf(".");
            if(dot != -1) { 
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public JavaPlugin getLoader() {
        return this.loader;
    }

    public ClassPathAppender getClassPathAppender() {
        return this.classPathAppender;
    }

    public PluginDescriptionFile getDescription() {
        return this.loader.getDescription();
    }

    @Override
    public void onLoad() {
        // TODO Auto-generated method stub
        
    }
}
