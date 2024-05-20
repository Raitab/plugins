package com.pricegraphopener;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemMapping;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.LinkBrowser;
import okhttp3.HttpUrl;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@PluginDescriptor(
    name = "Price Graph Opener"
)
public class PriceGraphOpenerPlugin extends Plugin {
    static final HttpUrl GE_TRACKER_BASE = HttpUrl.get("https://www.ge-tracker.com/item");
    static final HttpUrl RS_WIKI_BASE = HttpUrl.get("https://prices.runescape.wiki/osrs/item");
    static final HttpUrl RS_GE_BASE = HttpUrl.get("https://secure.runescape.com/m=itemdb_oldschool/viewitem");

    private static final String GE_TRACKER_TITLE = "GE Tracker";
    private static final String RS_WIKI_TITLE = "OSRS Wiki";
    private static final String RS_GE_TITLE = "OSRS GE";
    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;
    @Inject
    private PriceGraphOpenerConfig config;

    @Inject
    @Getter
    private ChatboxItemSearch itemSearch;

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        addMenuEntriesToItems(event);
        addMenuEntriesToGeClerk(event);
    }

    void addMenuEntriesToItems(MenuEntryAdded event) {
        if (showMenuEntries(event)) {
            Widget container = client.getWidget(event.getActionParam1());
            Widget item = container.getChild(event.getActionParam0());
            String text = "Price History";
            MenuEntry menuEntry = null;

            boolean itemIsTradeable = itemManager.getItemComposition(item.getItemId()).isTradeable();
            final Collection<ItemMapping> mappedItems = itemIsTradeable ? Collections.emptyList() : ItemMapping.map(item.getItemId());
            boolean itemHasComponents = mappedItems != null && !mappedItems.isEmpty();

            if (config.folding() && (itemIsTradeable || (!itemIsTradeable && itemHasComponents))) {
                menuEntry = client.createMenuEntry(1)
                    .setOption(text)
                    .setTarget(ColorUtil.prependColorTag(item.getName(), JagexColors.MENU_TARGET))
                    .setType(MenuAction.RUNELITE_SUBMENU);
            }
            if (!itemIsTradeable) {
                if (mappedItems == null) {
                    return;
                }
                for (ItemMapping mappedItem : mappedItems) {
                    createMenusForTradeableItems(mappedItem.getTradeableItem(), menuEntry);
                }
                ;
            } else {
                createMenusForTradeableItems(item.getItemId(), menuEntry);
            }
        }
    }

    boolean showMenuEntries(MenuEntryAdded event) {
        return ((event.getActionParam1() == ComponentID.BANK_ITEM_CONTAINER
            || event.getActionParam1() == ComponentID.BANK_INVENTORY_ITEM_CONTAINER
            || event.getActionParam1() == ComponentID.GRAND_EXCHANGE_INVENTORY_INVENTORY_ITEM_CONTAINER
            || event.getActionParam1() == ComponentID.GROUP_STORAGE_ITEM_CONTAINER
            || event.getActionParam1() == ComponentID.SHOP_INVENTORY_ITEM_CONTAINER
            || event.getActionParam1() == 19726336 // store inventory
            || event.getActionParam1() == 19660816 // store
        )
            || (event.getActionParam1() == ComponentID.INVENTORY_CONTAINER
                && (config.shiftInventory() ? client.isKeyPressed(KeyCode.KC_SHIFT) : true)))
                && event.getOption().equals("Examine");
    }

    void createMenusForTradeableItems(int itemId, MenuEntry parentMenuEntry) {
        if (config.geTracker()) {
            createMenuEntry(GE_TRACKER_TITLE, itemId, parentMenuEntry, MenuAction.RUNELITE)
                .onClick(e -> LinkBrowser.browse(
                    GE_TRACKER_BASE.newBuilder().addPathSegment(String.valueOf(e.getParam0())).build().toString()));
        }
        if (config.osrsWiki()) {
            createMenuEntry(RS_WIKI_TITLE, itemId, parentMenuEntry, MenuAction.RUNELITE)
                .onClick(e -> LinkBrowser.browse(
                    RS_WIKI_BASE.newBuilder().addPathSegment(String.valueOf(e.getParam0())).build().toString()));
        }
        if (config.osrsGrandExchange()) {
            createMenuEntry(RS_GE_TITLE, itemId, parentMenuEntry, MenuAction.RUNELITE)
                .onClick(e -> LinkBrowser.browse(
                    RS_GE_BASE.newBuilder().addQueryParameter("obj", String.valueOf(e.getParam0())).build().toString()));
        }
    }

    MenuEntry createMenuEntry(String title, int item, MenuEntry parentMenuEntry, MenuAction type) {
        return client.createMenuEntry(1)
            .setOption(title)
            .setTarget(ColorUtil.prependColorTag(
                itemManager.getItemComposition(item).getMembersName(), JagexColors.MENU_TARGET))
            .setParam0(item)
            .setParent(parentMenuEntry)
            .setType(type);
    }

    public void addMenuEntriesToGeClerk(MenuEntryAdded event) {
        if (isNpcMatch(event) || isObjectMatch(event)) {
            addSearchMenuEntries(event);
        }
    }

    boolean isNpcMatch(MenuEntryAdded event) {
        MenuEntry menuEntry = event.getMenuEntry();
        final MenuAction menuAction = menuEntry.getType();
        final NPC npc = menuEntry.getNpc();
        if (npc == null) {
            return false;
        }
        if (menuAction == MenuAction.EXAMINE_NPC) {
            final String npcName = npc.getName();
            final boolean nameMatch = "Grand Exchange Clerk".equals(npcName);
            return npcName != null && nameMatch;
        }
        return false;
    }

    boolean isObjectMatch(MenuEntryAdded event) {
        return event.getMenuEntry().getType() == MenuAction.EXAMINE_OBJECT
            && event.getTarget() != null
            && event.getTarget().contains("Grand Exchange booth");
    }

    void addSearchMenuEntries(MenuEntryAdded event) {
        int idx = -1;
        if (config.geTracker()) {
            client.createMenuEntry(idx--)
                .setOption("Search " + GE_TRACKER_TITLE)
                .setTarget(ColorUtil.prependColorTag(event.getTarget(), JagexColors.MENU_TARGET))
                .setIdentifier(event.getIdentifier())
                .setType(MenuAction.RUNELITE)
                .onClick(e -> {
                    itemSearch
                        .tooltipText("Open GE Tracker to")
                        .onItemSelected(itemId -> LinkBrowser.browse(
                            GE_TRACKER_BASE.newBuilder().addPathSegment(String.valueOf(itemId)).build().toString()))
                        .build();
                });
        }
        if (config.osrsWiki()) {
            client.createMenuEntry(idx--)
                .setOption("Search " + RS_WIKI_TITLE)
                .setTarget(ColorUtil.prependColorTag(event.getTarget(), JagexColors.MENU_TARGET))
                .setIdentifier(event.getIdentifier())
                .setType(MenuAction.RUNELITE)
                .onClick(e -> {
                    itemSearch
                        .tooltipText("Open OSRS Wiki to")
                        .onItemSelected(itemId -> LinkBrowser.browse(
                            GE_TRACKER_BASE.newBuilder().addPathSegment(String.valueOf(itemId)).build().toString()))
                        .build();
                });
        }
        if (config.osrsGrandExchange()) {
            client.createMenuEntry(idx--)
                .setOption("Search " + RS_GE_TITLE)
                .setTarget(ColorUtil.prependColorTag(event.getTarget(), JagexColors.MENU_TARGET))
                .setIdentifier(event.getIdentifier())
                .setType(MenuAction.RUNELITE)
                .onClick(e -> {
                    itemSearch
                        .tooltipText("Open OSRS Grand Exchange to")
                        .onItemSelected(itemId -> LinkBrowser.browse(
                            GE_TRACKER_BASE.newBuilder().addPathSegment(String.valueOf(itemId)).build().toString()))
                        .build();
                });
        }
    }



    @Provides
    PriceGraphOpenerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PriceGraphOpenerConfig.class);
    }
}
