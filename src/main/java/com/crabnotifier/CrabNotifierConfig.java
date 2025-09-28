package com.crabnotifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("crab-notifier")
public interface CrabNotifierConfig extends Config
{
    @ConfigItem(
        keyName = "discordToken",
        name = "Discord Token",
        description = "Secret token from discord API",
        secret = true,
        position = 0
    )
    default String discordToken()
    {
        return "";
    }

    @ConfigItem(
        keyName = "applicationId",
        name = "Application Id",
        description = "Your application id",
        secret = true,
        position = 1
    )
    default String applicationId() { return ""; }

    @ConfigItem(
        keyName = "userId",
        name = "User Id",
        description = "Your Discord user id",
        secret = true,
        position = 2
    )
    default String userId() { return ""; }
}
