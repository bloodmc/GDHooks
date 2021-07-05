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

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import com.griefdefender.api.claim.ClaimType;
import com.griefdefender.api.claim.ClaimTypes;

@ConfigSerializable
public class Pl3xmapOwnerStyleCategory {

    @Setting("line-color")
    public String lineColor = "#FF0000";

    @Setting("stroke-weight")
    public int Stroke_Weight = 1;

    @Setting("stroke-opacity")
    public double Stroke_Opacity = 1.0D;

    @Setting("fill-color")
    public String fillColor = "#FF0000";

    @Setting("fill-opacity")
    public double Fill_Opacity = 0.2D;

    @Setting("label")
    public String label = "none";

    public Pl3xmapOwnerStyleCategory() {
    }

    public Pl3xmapOwnerStyleCategory(ClaimType type) {
        if (type.equals(ClaimTypes.ADMIN)) {
            this.lineColor = "#FF0000";
            this.fillColor = "#FF0000";
        } else if (type.equals(ClaimTypes.BASIC)) {
            this.lineColor = "#FFFF00";
            this.fillColor = "#FFFF00";
        } else if (type.equals(ClaimTypes.TOWN)) {
            this.lineColor = "#00FF00";
            this.fillColor = "#00FF00";
        } else if (type.equals(ClaimTypes.SUBDIVISION)) {
            this.lineColor = "#FF9C00";
            this.fillColor = "#FF9C00";
        } else {
            this.lineColor = "#FF0000";
            this.fillColor = "#FF0000";
        }
    }
}
