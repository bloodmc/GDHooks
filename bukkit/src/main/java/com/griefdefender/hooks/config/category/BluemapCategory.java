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
public class BluemapCategory {

    @Setting("enabled")
    @Comment("Set to true to enable GriefDefender Bluemap integration. (Default: true)")
    public boolean enabled = true;

    @Setting("markerset-admin-label")
    @Comment("The admin marker set label.")
    public String markerSetAdminLabel = "GriefDefender Admin Claims";

    @Setting("markerset-basic-label")
    @Comment("The basic marker set name.")
    public String markerSetBasicLabel = "GriefDefender Basic Claims";

    @Setting("markerset-subdivision-label")
    @Comment("The subdivision marker set label.")
    public String markerSetSubdivisionLabel = "GriefDefender Subdivision Claims";

    @Setting("markerset-town-label")
    @Comment("The town marker set label.")
    public String markerSetTownLabel = "GriefDefender Town Claims";

    @Setting("hide-by-default-admin")
    @Comment("Whether to hide admin claim markers by default. (Default: false)")
    public boolean hideByDefaultAdmin = false;

    @Setting("hide-by-default-basic")
    @Comment("Whether to hide basic claim markers by default. (Default: false)")
    public boolean hideByDefaultBasic = false;

    @Setting("hide-by-default-subdivision")
    @Comment("Whether to hide subdivision claim markers by default. (Default: false)")
    public boolean hideByDefaultSubdivision = false;

    @Setting("hide-by-default-town")
    @Comment("Whether to hide town claim markers by default. (Default: false)")
    public boolean hideByDefaultTown = false;

    @Setting("depth-check")
    @Comment("Whether markers should be hidden behind map terrain. (Default: true)")
    public boolean depthCheck = true;

    @Setting("claimtype-styles")
    public Map<String, BluemapOwnerStyleCategory> claimTypeStyles = new HashMap<>();

    @Setting("owner-styles")
    public Map<String, BluemapOwnerStyleCategory> ownerStyles = new HashMap<>();

    @Setting("info-window-basic")
    public String infoWindowBasic = "<div class=\"infowindow\">"
            + "Name: <span style=\"font-weight:bold;\">%claimname%</span><br/>"
            + "Owner: <span style=\"font-weight:bold;\">%owner%</span><br/>"
            + "OwnerUUID: <span style=\"font-weight:bold;\">%owneruuid%</span><br/>"
            + "Type: <span style=\"font-weight:bold;\">%gdtype%</span><br/>"
            + "Last Seen: <span style=\"font-weight:bold;\">%lastseen%</span><br/>"
            + "Manager Trust: <span style=\"font-weight:bold;\">%managers%</span><br/>"
            + "Builder Trust: <span style=\"font-weight:bold;\">%builders%</span><br/>"
            + "Container Trust: <span style=\"font-weight:bold;\">%containers%</span><br/>"
            + "Access Trust: <span style=\"font-weight:bold;\">%accessors%</span></div>";

    @Setting("info-window-admin")
    public String infoWindowAdmin = "<div class=\"infowindow\">"
            + "<span style=\"font-weight:bold;\">%claimname%</span><br/>"
            + "Manager Trust: <span style=\"font-weight:bold;\">%managers%</span><br/>"
            + "Builder Trust: <span style=\"font-weight:bold;\">%builders%</span><br/>"
            + "Container Trust: <span style=\"font-weight:bold;\">%containers%</span><br/>"
            + "Access Trust: <span style=\"font-weight:bold;\">%accessors%</span></div>";

    public BluemapCategory() {
        for (ClaimType type : GriefDefender.getRegistry().getAllOf(ClaimType.class)) {
            if (type == ClaimTypes.WILDERNESS) {
                continue;
            }
            if (this.claimTypeStyles.get(type.getName().toLowerCase()) == null) {
                this.claimTypeStyles.put(type.getName().toLowerCase(), new BluemapOwnerStyleCategory(type));
            }
        }
    }
}
