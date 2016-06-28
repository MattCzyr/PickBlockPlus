package com.chaosthedude.pickblockplus;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class ClientTickHandler {

	private final Minecraft mc = Minecraft.getMinecraft();
	private ItemStack[] hotbar = new ItemStack[9];
	private boolean activated = true;
	private int ticksSincePressed = 0;

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START || event.type != TickEvent.Type.CLIENT) {
			return;
		}

		overrideVanillaPickBlock();
		handlePickBlock();
	}

	private void handlePickBlock() {
		EntityPlayer player = mc.thePlayer;
		if (player == null) {
			return;
		}

		if (!PickBlockPlus.pickBlockPlus.getIsKeyPressed()) {
			activated = false;
			ticksSincePressed++;
			if (ticksSincePressed > 20) {
				ticksSincePressed = 20;
			}

			return;
		}

		if (activated) {
			return;
		}

		activated = true;
		if (mc.currentScreen != null) {
			return;
		}

		MovingObjectPosition target = mc.objectMouseOver;
		if (target == null) {
			return;
		}

		if (player.capabilities.isCreativeMode) {
			if (!ForgeHooks.onPickBlock(target, player, mc.theWorld)) {
				return;
			}

			int slot = player.inventoryContainer.inventorySlots.size() - 9 + player.inventory.currentItem;
			mc.playerController.sendSlotPacket(player.inventory.getStackInSlot(player.inventory.currentItem), slot);
		}

		int slot = player.inventory.currentItem;
		if (hotbar.length != InventoryPlayer.getHotbarSize()) {
			hotbar = new ItemStack[InventoryPlayer.getHotbarSize()];
		}

		if (ticksSincePressed > 10) {
			ticksSincePressed = 0;
			List<ItemStack> validItems = new ArrayList();
			if (slot >= 0 && 0 < hotbar.length && hotbar[slot] != null) {
				ItemStack original = hotbar[slot];
				hotbar[slot] = null;
				boolean moved = false;
				for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
					if (Util.areItemStacksIdentical(original, player.inventory.getStackInSlot(i))) {
						moved = true;
						break;
					}
				}

				if (!moved) {
					validItems.add(original);
				}
			}

			if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				World world = player.worldObj;
				int x = target.blockX;
				int y = target.blockY;
				int z = target.blockZ;

				validItems.add(world.getBlock(x, y, z).getPickBlock(target, world, x, y, z, player));
				validItems.add(Util.getBrokenBlock(world, x, y, z));
				validItems.add(new ItemStack(world.getBlock(x, y, z), 1, world.getBlockMetadata(x, y, z)));
			} else if (target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
				validItems.add(target.entityHit.getPickedResult(target));
			}

			ItemStack held = player.getHeldItem();
			for (ItemStack stack : validItems) {
				for (int invSlot = 0; invSlot < player.inventory.mainInventory.length; invSlot++) {
					if (stack != null) {
						ItemStack possibleItem = player.inventory.mainInventory[invSlot];
						if (possibleItem != null) {
							if (possibleItem.isItemEqual(stack)) {
								mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
								if (invSlot < 9) {
									player.inventory.currentItem = invSlot;
									return;
								}

								Util.swapItems(player, held, invSlot, hotbar);
								return;
							}
						}
					}
				}
			}
		} else {
			ticksSincePressed = 0;
			boolean targetIsEntity = false;
			Block block = null;
			int metadata = 0;
			if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				block = player.worldObj.getBlock(target.blockX, target.blockY, target.blockZ);
				metadata = player.worldObj.getBlockMetadata(target.blockX, target.blockY, target.blockZ);
			} else if (target.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
				targetIsEntity = true;
			}

			if (!targetIsEntity && block == null) {
				return;
			}

			ItemStack held = player.getHeldItem();
			int bestSlot = -1;
			if (targetIsEntity) {
				bestSlot = Util.getHighestDamageItemSlot(player);
			} else {
				bestSlot = Util.getMostEffectiveItemSlot(player, block, metadata);
			}

			if (bestSlot == -1) {
				return;
			}

			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			if (bestSlot < 9) {
				player.inventory.currentItem = bestSlot;
				return;
			}

			Util.swapItems(player, held, bestSlot, hotbar);
			return;
		}
	}

	private void overrideVanillaPickBlock() {
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		if (settings.keyBindPickBlock.getKeyCode() != 0 && PickBlockPlus.pickBlockPlus.getKeyCode() == 0) {
			PickBlockPlus.logger.info("Overriding vanilla pick block");
			settings.setOptionKeyBinding(PickBlockPlus.pickBlockPlus, settings.keyBindPickBlock.getKeyCode());
			settings.setOptionKeyBinding(settings.keyBindPickBlock, 0);
			KeyBinding.resetKeyBindingArrayAndHash();
		}
	}

}
