package com.pricegraphopener;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PriceGraphOpenerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PriceGraphOpenerPlugin.class);
		RuneLite.main(args);
	}
}