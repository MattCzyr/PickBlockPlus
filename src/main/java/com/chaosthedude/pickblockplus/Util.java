package com.chaosthedude.pickblockplus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHardenedClay;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Util {

	public static boolean areItemStacksIdentical(ItemStack a, ItemStack b) {
		if (a == null && b == null) {
			return true;
		}

		if (a == null || b == null) {
			return false;
		}

		return canItemStacksMerge(a, b);
	}

	public static boolean canItemStacksMerge(ItemStack a, ItemStack b) {
		if (a == null || b == null) {
			return true;
		}

		return a.getItemDamage() == b.getItemDamage() && a.getItem() == b.getItem() && ItemStack.areItemStackTagsEqual(a, b);
	}

	public static ItemStack getBrokenBlock(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		if (b == null) {
			return null;
		}

		ArrayList<ItemStack> dropped = b.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
		if (dropped == null || dropped.isEmpty()) {
			return null;
		}

		ItemStack main = (ItemStack) dropped.remove(0);
		for (int i = 0; i < dropped.size(); i++) {
			ItemStack other = (ItemStack) dropped.get(i);
			if (!canItemStacksMerge(main, other)) {
				return null;
			}

			main.stackSize += other.stackSize;
		}

		return main;
	}

	public static int getMostEffectiveItemSlot(EntityPlayer player, Block block, int metadata) {
		List<Integer> possibleItems = new ArrayList();
		for (int invSlot = 0; invSlot < player.inventory.mainInventory.length; invSlot++) {
			ItemStack possibleItem = player.inventory.mainInventory[invSlot];
			if (possibleItem != null) {
				Set<String> toolClasses = possibleItem.getItem().getToolClasses(possibleItem);
				for (String toolClass : toolClasses) {
					if (block.isToolEffective(toolClass, metadata)) {
						possibleItems.add(invSlot);
					} else if (block.getHarvestLevel(metadata) == -1) {
						if (block.getMaterial() == Material.rock && toolClasses.contains("pickaxe")) {
							possibleItems.add(invSlot);
						}
					}
				}
			}
		}

		int bestSlot = -1;
		for (int invSlot : possibleItems) {
			ItemStack stack = player.inventory.mainInventory[invSlot];
			if (stack != null) {
				if (bestSlot == -1) {
					bestSlot = invSlot;
				} else {
					ItemStack bestStack = player.inventory.mainInventory[bestSlot];
					Item possibleTool = stack.getItem();
					Item bestTool = bestStack.getItem();
					if (possibleTool.getDigSpeed(stack, block, metadata) > bestTool.getDigSpeed(bestStack, block, metadata)) {
						bestSlot = invSlot;
					}
				}
			}
		}

		return bestSlot;
	}

	public static int getHighestDamageItemSlot(EntityPlayer player) {
		int highestDamageSlot = -1;
		double highestDamage = -1D;
		for (int invSlot = 0; invSlot < player.inventory.mainInventory.length; invSlot++) {
			ItemStack stack = player.inventory.mainInventory[invSlot];
			if (stack != null) {
				if (highestDamageSlot == -1) {
					highestDamageSlot = invSlot;
				} else {
					double damage = -1D;
					Multimap attributes = stack.getAttributeModifiers();
					Collection collection = attributes.get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
					for (Object o : collection) {
						if (o instanceof AttributeModifier) {
							AttributeModifier modifier = (AttributeModifier) o;
							if (modifier.getName().equals("Weapon modifier") || modifier.getName().equals("generic.attackDamage") || modifier.getName().equals("Tool modifier")) {
								damage = modifier.getAmount();
							}
						}
					}

					if (damage > highestDamage) {
						highestDamage = damage;
						highestDamageSlot = invSlot;
					}
				}
			}
		}

		return highestDamageSlot;
	}

	public static void swapItems(EntityPlayer player, ItemStack held, int invSlot, ItemStack[] hotbar) {
		int targetSlot = player.inventory.currentItem;
		Minecraft.getMinecraft().playerController.windowClick(player.inventoryContainer.windowId, invSlot, targetSlot, 2, player);
		if (held == null) {
			return;
		}

		if (hotbar[targetSlot] == null) {
			hotbar[targetSlot] = held;
			return;
		}

		boolean canReplace = false;
		for (int barSlot = 0; barSlot < 9; barSlot++) {
			ItemStack barItem = player.inventory.getStackInSlot(barSlot);
			if (barItem != null && barItem == hotbar[targetSlot]) {
				canReplace = true;
				break;
			}
		}

		if (canReplace) {
			hotbar[targetSlot] = held;
		}
	}

}
