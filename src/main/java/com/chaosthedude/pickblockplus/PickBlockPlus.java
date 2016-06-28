package com.chaosthedude.pickblockplus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.client.settings.KeyBinding;

@Mod(modid = PickBlockPlus.MODID, name = PickBlockPlus.NAME, version = PickBlockPlus.VERSION, acceptedMinecraftVersions = "[1.7.10]")

public class PickBlockPlus {

	public static final String MODID = "PickBlockPlus";
	public static final String NAME = "Pick Block Plus";
	public static final String VERSION = "1.1.1";

	public static final Logger logger = LogManager.getLogger(MODID);

	public static KeyBinding pickBlockPlus = new KeyBinding("key.PickBlockPlus", 0, "key.categories.gameplay");

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ClientRegistry.registerKeyBinding(pickBlockPlus);
		FMLCommonHandler.instance().bus().register(new ClientTickHandler());
	}

}
