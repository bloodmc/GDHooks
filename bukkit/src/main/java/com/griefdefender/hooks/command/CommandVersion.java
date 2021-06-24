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
package com.griefdefender.hooks.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.permission.GDHooksPermissions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("gdhooks")
@CommandPermission(GDHooksPermissions.COMMAND_VERSION)
public class CommandVersion extends BaseCommand {

    @CommandAlias("gdhooksversion")
    @Description("%version")
    @Subcommand("version")
    public void execute(CommandSender src) {
        Component gpVersion = Component.text()
                .append(GDHooks.GDHOOKS_TEXT)
                .append(Component.text("Running "))
                .append(Component.text("GDHooks " + GDHooks.IMPLEMENTATION_VERSION, NamedTextColor.AQUA))
                .build();
        Component bukkitVersion = Component.text()
                .append(GDHooks.GDHOOKS_TEXT)
                .append(Component.text("Running "))
                .append(Component.text(Bukkit.getVersion(), NamedTextColor.YELLOW))
                .build();

        GriefDefender.getAudienceProvider().getSender(src).sendMessage(Component.text()
                .append(gpVersion)
                .append(Component.newline())
                .append(bukkitVersion)
                .build());
    }
}
