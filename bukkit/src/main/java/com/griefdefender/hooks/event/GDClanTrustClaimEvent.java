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
package com.griefdefender.hooks.event;

import java.util.List;
import java.util.Set;

import com.griefdefender.api.Clan;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustType;
import com.griefdefender.api.event.ClanTrustClaimEvent;

public class GDClanTrustClaimEvent extends GDTrustClaimEvent implements ClanTrustClaimEvent {

    private Set<Clan> clans;

    public GDClanTrustClaimEvent(Claim claim, Set<Clan> clans, TrustType trustType) {
        super(claim, trustType);
        this.clans = clans;
    }

    public GDClanTrustClaimEvent(List<Claim> claims, Set<Clan> clans, TrustType trustType) {
        super(claims, trustType);
        this.clans = clans;
    }

    @Override
    public Set<Clan> getClans() {
        return this.clans;
    }

    public static class Add extends GDClanTrustClaimEvent implements ClanTrustClaimEvent.Add {
        public Add(List<Claim> claims, Set<Clan> clans, TrustType trustType) {
            super(claims, clans, trustType);
        }

        public Add(Claim claim, Set<Clan> clans, TrustType trustType) {
            super(claim, clans, trustType);
        }
    }

    public static class Remove extends GDClanTrustClaimEvent implements ClanTrustClaimEvent.Remove {
        public Remove(List<Claim> claims, Set<Clan> clans, TrustType trustType) {
            super(claims, clans, trustType);
        }

        public Remove(Claim claim, Set<Clan> clans, TrustType trustType) {
            super(claim, clans, trustType);
        }
    }
}
