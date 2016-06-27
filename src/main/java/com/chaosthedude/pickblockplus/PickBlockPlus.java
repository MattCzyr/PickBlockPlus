package com.chaosthedude.pickblockplus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = PickBlockPlus.MODID, name = PickBlockPlus.NAME, version = PickBlockPlus.VERSION, acceptedMinecraftVersions = "[1.9,1.9.4]", clientSideOnly = true)

public class PickBlockPlus {

	public static final String MODID = "PickBlockPlus";
	public static final String NAME = "Pick Block Plus";
	public static final String VERSION = "1.1.1";

	public static final Logger logger = LogManager.getLogger(MODID);

	public static KeyBinding pickBlockPlus = new KeyBinding("key.PickBlockPlus", 0, "key.categories.gameplay");

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ClientRegistry.registerKeyBinding(pickBlockPlus);
		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
	}

}
