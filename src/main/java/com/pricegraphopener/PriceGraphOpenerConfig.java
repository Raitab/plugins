package com.pricegraphopener;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("price-graph-opener")
public interface PriceGraphOpenerConfig extends Config
{
    @ConfigItem(
        keyName = "ge-tracker",
        name = "Ge-Tracker",
        description = "Display option to open ge-tracker.net"
    )
    default boolean geTracker() { return true; }

    @ConfigItem(
        keyName = "rs-wiki",
        name = "OSRS Wiki",
        description = "Display option to open prices.runescape.wiki"
    )
    default boolean osrsWiki() { return true; }

    @ConfigItem(
        keyName = "rs-wiki",
        name = "OSRS Grand Exchange",
        description = "Display option to open the runescape.com GE page"
    )
    default boolean osrsGrandExchange() { return true; }

    @ConfigItem(
        keyName = "folding",
        name = "Combine menu options",
        description = "If more than one above option is selected, combine the menu options"
    )
    default boolean folding() { return true; }
}
