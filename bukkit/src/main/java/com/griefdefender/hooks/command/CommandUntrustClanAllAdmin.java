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
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.griefdefender.api.Clan;
import com.griefdefender.api.CommandResult;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.config.MessageConfig;
import com.griefdefender.hooks.event.GDClanTrustClaimEvent;
import com.griefdefender.hooks.permission.GDHooksPermissions;
import com.griefdefender.hooks.util.HooksUtil;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

@CommandAlias("gdhooks")
@CommandPermission(GDHooksPermissions.COMMAND_UNTRUSTALL_CLAN)
public class CommandUntrustClanAllAdmin extends BaseCommand {

    @CommandCompletion("@gdclans @gdtrusttypes @gddummy")
    @CommandAlias("untrustallclanadmin")
    @Description("%untrust-clan-all")
    @Syntax("<clan> [<accessor|builder|container|manager>]")
    @Subcommand("untrustalladmin clan")
    public void execute(Player player, String clanTag, @Optional String type, @Optional String identifier) {
        TrustType trustType = null;
        final Audience audience = GriefDefender.getAudienceProvider().getSender(player);
        if (type == null) {
            trustType = TrustTypes.BUILDER;
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

        Set<Claim> claimList = new HashSet<>();
        final ClaimManager claimManager = GriefDefender.getCore().getClaimManager(player.getWorld().getUID());
        for (Claim claim : claimManager.getWorldClaims()) {
            if (claim.isAdminClaim()) {
                claimList.add(claim);
            }
        }

        if (claimList == null || claimList.size() == 0) {
            audience.sendMessage(MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.TRUST_NO_CLAIMS));
            return;
        }

        GriefDefender.getEventManager().getCauseStackManager().pushCause(player);
        GDClanTrustClaimEvent.Remove
            event = new GDClanTrustClaimEvent.Remove(new ArrayList<>(claimList), ImmutableSet.of(clan), trustType);
        GriefDefender.getEventManager().post(event);
        GriefDefender.getEventManager().getCauseStackManager().popCause();
        if (event.cancelled()) {
            audience.sendMessage(event.getMessage().orElse(MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.TRUST_PLUGIN_CANCEL,
                    ImmutableMap.of("target", clan.getTag()))));
            return;
        }

        for (Claim claim : claimList) {
            claim.removeClanTrust(clan, trustType);
        }

        final Component message = MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.UNTRUST_INDIVIDUAL_ALL_CLAIMS,
                ImmutableMap.of(
                "player", clan.getTag()));
        audience.sendMessage(message);
    }
}