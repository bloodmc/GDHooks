package com.griefdefender.hooks.config.category;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ClanCategory extends ConfigCategory {

    @Setting(value = "clan-require-town")
    @Comment(value = "If true, requires a town to be owned in order to create a clan.")
    public boolean clanRequireTown = false;
}
