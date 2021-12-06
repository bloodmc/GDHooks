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

import com.google.common.collect.ImmutableMap;
import com.griefdefender.api.Clan;
import com.griefdefender.api.CommandResult;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimResult;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.config.MessageConfig;
import com.griefdefender.hooks.permission.GDHooksPermissions;
import com.griefdefender.hooks.util.HooksUtil;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import org.bukkit.command.CommandSender;

@CommandAlias("gdhooks")
@CommandPermission(GDHooksPermissions.COMMAND_UNTRUST_CLAN)
public class CommandUntrustClan extends BaseCommand {

    @CommandCompletion("@gdclans @gdtrusttypes @gddummy")
    @CommandAlias("untrustclan")
    @Description("%untrust-clan")
    @Syntax("<clan> [<accessor|builder|container|manager>]")
    @Subcommand("untrust clan")
    public void execute(CommandSender sender, String clanTag, @Optional String type, @Optional String identifier) {
        TrustType trustType = null;
        final Audience audience = GriefDefender.getAudienceProvider().getSender(sender);
        if (type == null) {
            trustType = TrustTypes.NONE;
        } else {
            trustType = HooksUtil.getTrustType(type);
            if (trustType == null) {
                audience.sendMessage(MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.TRUST_INVALID));
                return;
            }
        }

        final Clan clan = GDHooks.getInstance().getClanProvider().getClan(clanTag);
        if (clan == null) {
            audience.sendMessage(MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.COMMAND_INVALID_CLAN, ImmutableMap.of(
                    "clan", clanTag)));
            return;
        }

        final CommandResult result = GriefDefender.getCore().canUseCommand(sender, TrustTypes.MANAGER, identifier);
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
        GriefDefender.getEventManager().getCauseStackManager().pushCause(sender);
        final ClaimResult claimResult = claim.removeClanTrust(clanTag, trustType);
        GriefDefender.getEventManager().getCauseStackManager().popCause();
        if (!claimResult.successful()) {
            audience.sendMessage(claimResult.getMessage().orElse(MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.TRUST_PLUGIN_CANCEL,
                    ImmutableMap.of("target", clan.getTag()))));
            return;
        }

        final Component message = MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.UNTRUST_INDIVIDUAL_SINGLE_CLAIM, ImmutableMap.of(
                "target", clan.getTag()));
        audience.sendMessage(message);
    }
}