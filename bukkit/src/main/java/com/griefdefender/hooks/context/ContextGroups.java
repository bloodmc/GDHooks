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
package com.griefdefender.hooks.context;

import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.ContextKeys;

public class ContextGroups {

    public static final Context SOURCE_ANY = new Context(ContextKeys.SOURCE, "#any");
    public static final Context TARGET_ANY = new Context(ContextKeys.TARGET, "#any");
    public static final Context SOURCE_MONSTER = new Context(ContextKeys.SOURCE, "#monster");
    public static final Context TARGET_MONSTER = new Context(ContextKeys.TARGET, "#monster");
    // Custom mod groups
    public static final Context SOURCE_MYPET = new Context(ContextKeys.SOURCE, "#mypet:any");
    public static final Context TARGET_MYPET = new Context(ContextKeys.TARGET, "#mypet:any");
    public static final Context SOURCE_MYTHICMOBS = new Context(ContextKeys.SOURCE, "#mythicmobs:any");
    public static final Context TARGET_MYTHICMOBS = new Context(ContextKeys.TARGET, "#mythicmobs:any");
}
