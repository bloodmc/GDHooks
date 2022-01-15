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
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;

import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;
import com.griefdefender.api.Clan;
import com.griefdefender.api.ClanPlayer;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.hooks.GDHooks;
import com.griefdefender.hooks.config.ClanConfig;
import com.griefdefender.hooks.config.MessageConfig;
import com.griefdefender.hooks.permission.GDHooksPermissions;
import com.griefdefender.hooks.util.HooksUtil;

import com.griefdefender.lib.kyori.adventure.audience.Audience;
import com.griefdefender.lib.kyori.adventure.text.Component;

@CommandAlias("gdhooks")
@CommandPermission(GDHooksPermissions.COMMAND_TRUST_RANK)
public class CommandClanTrustRank extends BaseCommand {

    @CommandCompletion("@gdclanranks @gdtrusttypes @gddummy")
    @CommandAlias("clantrustrank")
    @Description("%clan-trust-rank")
    @Syntax("<rank> <accessor|builder|container|manager|resident>")
    @Subcommand("clan trust rank")
    public void execute(Player player, String rank, String type) {
        final Audience audience = GriefDefender.getAudienceProvider().getSender(player);
        TrustType trustType = HooksUtil.getTrustType(type);
        if (trustType == null) {
            audience.sendMessage(MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.TRUST_INVALID));
            return;
        }

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

        final ClanConfig clanConfig = GDHooks.getInstance().getClanConfigMap().get(clan.getTag());
        if (clanConfig == null) {
            return;
        }
        clanConfig.getData().addRankTrust(rank, trustType);
        clanConfig.save();
        final Component message = MessageConfig.MESSAGE_DATA.getMessage(MessageConfig.CLAN_TRUST_RANK, ImmutableMap.of(
                "rank", rank,
                "type", trustType.getName()));
        audience.sendMessage(message);
    }
}