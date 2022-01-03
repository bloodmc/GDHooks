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
package com.griefdefender.hooks.command.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;

import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;
import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.CommandResult;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.GDHooksAttributes;
import com.griefdefender.hooks.config.MessageConfig;
import com.griefdefender.hooks.permission.GDHooksPermissions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

@CommandAlias("gdhooks")
@CommandPermission(GDHooksPermissions.COMMAND_CLAN_CLAIM)
public class CommandClanClaim extends BaseCommand {

    @CommandCompletion("@gdidentifiers")
    @CommandAlias("clanclaim")
    @Description("%clan-claim")
    @Syntax("[identifier]")
    @Subcommand("clan claim")
    public void execute(Player player, @Optional String identifier) {
        final ClanPlayer clanPlayer = GDHooks.getInstance().getClanProvider().getClanPlayer(player.getUniqueId());
        if (clanPlayer == null) {
            return;
        }
        final Clan clan = clanPlayer.getClan();
        if (clan == null) {
            return;
        }

        if (!clanPlayer.isLeader()) {
            return;
        }

        final Audience audience = GriefDefender.getAudienceProvider().getSender(player);
        final CommandResult result = GriefDefender.getCore().canUseCommand(player, TrustTypes.MANAGER, identifier);
        if (!result.successful()) {
            if (result.getClaim() != null) {
                final Component message = MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.PERMISSION_TRUST,
                        ImmutableMap.of(
                        "owner", result.getClaim().getOwnerDisplayName()));
                audience.sendMessage(message);
            } else {
                audience.sendMessage(MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.PERMISSION_COMMAND_TRUST));
            }
            return;
        }

        final Claim claim = result.getClaim();
        if (claim.hasAttribute(GDHooksAttributes.ATTRIBUTE_CLAN)) {
            claim.removeAttribute(GDHooksAttributes.ATTRIBUTE_CLAN);
            audience.sendMessage(Component.text("Removed clan attribute."));
        } else {
            claim.addAttribute(GDHooksAttributes.ATTRIBUTE_CLAN);
            audience.sendMessage(Component.text("Added clan attribute."));
        }
    }
}