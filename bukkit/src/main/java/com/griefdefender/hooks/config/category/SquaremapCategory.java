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
package com.griefdefender.hooks.config.category;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.ClaimType;
import com.griefdefender.api.claim.ClaimTypes;

@ConfigSerializable
public class SquaremapCategory {

    @Setting("enabled")
    @Comment("Set to true to enable GriefDefender Squaremap integration. (Default: true)")
    public boolean enabled = true;

    @Setting("control-label")
    @Comment("The control bar label")
    public String control_label = "GriefDefender";

    @Setting("control-show")
    @Comment("The control bar label")
    public boolean control_show = true;

    @Setting("control-hide")
    @Comment("The control bar label")
    public boolean control_hide = false;

    @Setting("update-interval")
    @Comment("The interval between update claims draw")
    public int UPDATE_INTERVAL = 300;

    @Setting("claimtype-styles")
    public Map<String, SquaremapOwnerStyleCategory> claimTypeStyles = new HashMap<>();

    @Setting("owner-styles")
    public Map<String, SquaremapOwnerStyleCategory> ownerStyles = new HashMap<>();

    @Setting("claim-tooltip")
    public String CLAIM_TOOLTIP = "Name: <span style=\"font-weight:bold;\">%claimname%</span><br/>"
            + "Owner: <span style=\"font-weight:bold;\">%owner%</span><br/>"
            + "OwnerUUID: <span style=\"font-weight:bold;\">%owneruuid%</span><br/>"
            + "Type: <span style=\"font-weight:bold;\">%gdtype%</span><br/>"
            + "Last Seen: <span style=\"font-weight:bold;\">%lastseen%</span><br/>"
            + "Manager Trust: <span style=\"font-weight:bold;\">%managers%</span><br/>"
            + "Builder Trust: <span style=\"font-weight:bold;\">%builders%</span><br/>"
            + "Container Trust: <span style=\"font-weight:bold;\">%containers%</span><br/>"
            + "Resident Trust: <span style=\"font-weight:bold;\">%residents%</span>"
            + "Access Trust: <span style=\"font-weight:bold;\">%accessors%</span>";

    @Setting("claim-tooltip-admin")
    public String ADMIN_CLAIM_TOOLTIP = "<span style=\"font-weight:bold;\">%claimname%</span><br/>"
            + "Manager Trust: <span style=\"font-weight:bold;\">%managers%</span><br/>"
            + "Builder Trust: <span style=\"font-weight:bold;\">%builders%</span><br/>"
            + "Container Trust: <span style=\"font-weight:bold;\">%containers%</span><br/>"
            + "Resident Trust: <span style=\"font-weight:bold;\">%residents%</span> "
            + "Access Trust: <span style=\"font-weight:bold;\">%accessors%</span> ";

    public SquaremapCategory() {
        for (ClaimType type : GriefDefender.getRegistry().getAllOf(ClaimType.class)) {
            if (type == ClaimTypes.WILDERNESS) {
                continue;
            }
            if (this.claimTypeStyles.get(type.getName().toLowerCase()) == null) {
                this.claimTypeStyles.put(type.getName().toLowerCase(), new SquaremapOwnerStyleCategory(type));
            }
        }
    }
}
