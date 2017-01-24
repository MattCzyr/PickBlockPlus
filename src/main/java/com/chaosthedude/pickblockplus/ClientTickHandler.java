package com.chaosthedude.pickblockplus;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
		checkPickBlockKey();
	}

	private void checkPickBlockKey() {
		EntityPlayer player = mc.player;
		if (player == null) {
			return;
		}

		if (!PickBlockPlus.pickBlockPlus.isKeyDown()) {
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

		RayTraceResult target = mc.objectMouseOver;
		if (target == null) {
			return;
		}

		if (player.capabilities.isCreativeMode) {
			ForgeHooks.onPickBlock(target, player, mc.world);
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			return;
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

			if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
				World world = player.world;
				BlockPos pos = target.getBlockPos();
				IBlockState state = player.world.getBlockState(pos);

				validItems.add(world.getBlockState(pos).getBlock().getPickBlock(state, target, world, pos, player));
				validItems.add(Util.getBrokenBlock(world, pos));
				validItems.add(new ItemStack(world.getBlockState(pos).getBlock(), 1));
			} else if (target.typeOfHit == RayTraceResult.Type.ENTITY) {
				validItems.add(target.entityHit.getPickedResult(target));
			}

			ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
			for (ItemStack stack : validItems) {
				for (int invSlot = 0; invSlot < player.inventory.mainInventory.size(); invSlot++) {
					if (stack != null) {
						ItemStack possibleItem = player.inventory.mainInventory.get(invSlot);
						if (possibleItem != null) {
							if (possibleItem.isItemEqual(stack)) {
								mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
			IBlockState state = null;
			if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
				state = player.world.getBlockState(target.getBlockPos());
			} else if (target.typeOfHit == RayTraceResult.Type.ENTITY) {
				targetIsEntity = true;
			}

			if (!targetIsEntity && state == null) {
				return;
			}

			ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
			int bestSlot = -1;
			if (targetIsEntity) {
				bestSlot = Util.getHighestDamageItemSlot(player);
			} else {
				bestSlot = Util.getMostEffectiveItemSlot(player, state);
			}
			
			if (bestSlot == -1) {
				return;
			}

			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
