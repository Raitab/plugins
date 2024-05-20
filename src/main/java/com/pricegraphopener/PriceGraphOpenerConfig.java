package com.pricegraphopener;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("price-graph-opener")
public interface PriceGraphOpenerConfig extends Config
{
    @ConfigItem(
        keyName = "ge-tracker",
        name = "GE Tracker",
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
        keyName = "rs-grandexchange",
        name = "OSRS Grand Exchange",
        description = "Display option to open the runescape.com GE page"
    )
    default boolean osrsGrandExchange() { return true; }

    @ConfigItem(
        keyName = "folding",
        name = "Combine menu options",
        description = "Collect menu options into a submenu when clicking on an item."
    )
    default boolean folding() { return true; }

    @ConfigItem(
        keyName = "shift-click",
        name = "Shift Click for Inventory",
        description = "Only show the price check on right click if shift is held when outside of a bank or other menu."
    )
    default boolean shiftInventory() { return true; }
}
