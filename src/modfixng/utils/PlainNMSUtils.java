/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package modfixng.utils;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlainNMSUtils {

	protected static boolean isInventoryOpen(Player p) 
	{
		net.minecraft.server.v1_5_R3.EntityHuman nmshuman = getNMSHuman(p);
		return !nmshuman.activeContainer.getClass().getName().equals(nmshuman.defaultContainer.getClass().getName());
	}
	
	protected static void findAndFixOpenCropanalyzer(Player p, List<ItemStack> drops) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
		net.minecraft.server.v1_5_R3.Container container = getPlayerContainer(p);
		Field cropanalyzerField = container.getClass().getDeclaredField("cropnalyzer");
		cropanalyzerField.setAccessible(true);
		Object cropanalyzer = cropanalyzerField.get(container);
		Field itemStackField = cropanalyzer.getClass().getDeclaredField("itemStack");
		itemStackField.setAccessible(true);
		net.minecraft.server.v1_5_R3.ItemStack oldcropanalyzeritemstack = (net.minecraft.server.v1_5_R3.ItemStack) itemStackField.get(cropanalyzer);
		Field inventoryField = cropanalyzer.getClass().getDeclaredField("inventory");
		inventoryField.setAccessible(true);
		net.minecraft.server.v1_5_R3.ItemStack[] oldcropanalyzerinventory = (net.minecraft.server.v1_5_R3.ItemStack[]) inventoryField.get(cropanalyzer);
		int cropanalyzeritemstackuid = oldcropanalyzeritemstack.getTag().getInt("uid");
		for (ItemStack item : drops) 
		{
			if (item.getTypeId() == oldcropanalyzeritemstack.id) {
				net.minecraft.server.v1_5_R3.ItemStack nmsi = getNMSItemStack(item);
				if (nmsi.hasTag() && nmsi.getTag().hasKey("uid")) 
				{
					int nmsiuid = nmsi.getTag().getInt("uid");
					if (nmsiuid == cropanalyzeritemstackuid) 
					{
						net.minecraft.server.v1_5_R3.NBTTagCompound cropanalyzeritemstacktagcompound = new net.minecraft.server.v1_5_R3.NBTTagCompound();
						net.minecraft.server.v1_5_R3.NBTTagList taglist = new net.minecraft.server.v1_5_R3.NBTTagList();
						for (int i = 0; i < oldcropanalyzerinventory.length; i++) 
						{
							net.minecraft.server.v1_5_R3.ItemStack itemstack = oldcropanalyzerinventory[i];
							if (itemstack != null) 
							{
								net.minecraft.server.v1_5_R3.NBTTagCompound nbtTagCompoundSlot = new net.minecraft.server.v1_5_R3.NBTTagCompound();
								nbtTagCompoundSlot.setByte("Slot", (byte) i);
								itemstack.save(nbtTagCompoundSlot);
								taglist.add(nbtTagCompoundSlot);
							}
						}
						cropanalyzeritemstacktagcompound.set("Items", taglist);
						nmsi.setTag(cropanalyzeritemstacktagcompound);
						return;
					}
				}
			}
		}
	}

	protected static boolean isTryingToDropOpenCropanalyzer(Player p, ItemStack item) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
		if (item.equals(p.getItemOnCursor()))
		{
			return false;
		}
		net.minecraft.server.v1_5_R3.ItemStack clickeditem = getNMSItemStack(item);
		if (clickeditem.hasTag() && clickeditem.getTag().hasKey("uid")) 
		{
			int clickeduid = clickeditem.getTag().getInt("uid");
			net.minecraft.server.v1_5_R3.Container container = getPlayerContainer(p);
			Field cropanalyzerField = container.getClass().getDeclaredField("cropnalyzer");
			cropanalyzerField.setAccessible(true);
			Object cropanalyzer = cropanalyzerField.get(container);
			Field itemStackField = cropanalyzer.getClass().getDeclaredField("itemStack");
			itemStackField.setAccessible(true);
			net.minecraft.server.v1_5_R3.ItemStack opencropanalyzeritemstack = (net.minecraft.server.v1_5_R3.ItemStack) itemStackField.get(cropanalyzer);
			int openuid = opencropanalyzeritemstack.getTag().getInt("uid");
			return openuid == clickeduid;
		}
		return false;
	}

	protected static boolean isTryingToDropOpenToolBox(Player p, ItemStack item) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
		if (item.equals(p.getItemOnCursor()))
		{
			return false;
		}
		net.minecraft.server.v1_5_R3.ItemStack clickeditem = getNMSItemStack(item);
		if (clickeditem.hasTag() && clickeditem.getTag().hasKey("uid")) 
		{
			int clickeduid = clickeditem.getTag().getInt("uid");
			net.minecraft.server.v1_5_R3.Container container = getPlayerContainer(p);
			Field tooboxField = container.getClass().getDeclaredField("Toolbox");
			tooboxField.setAccessible(true);
			Object toolbox = tooboxField.get(container);
			Field itemStackField = toolbox.getClass().getSuperclass().getDeclaredField("itemStack");
			itemStackField.setAccessible(true);
			net.minecraft.server.v1_5_R3.ItemStack opentoolbox = (net.minecraft.server.v1_5_R3.ItemStack) itemStackField.get(toolbox);
			int openuid = opentoolbox.getTag().getInt("uid");
			return openuid == clickeduid;
		}
		return false;
	}

	protected static boolean isClickedNonEmptyArmorSlot(Player p, int clickedslot) 
	{
		net.minecraft.server.v1_5_R3.Container container = getPlayerContainer(p);
		if (container.c.get(clickedslot).getClass().getName().equals("ic2.core.slot.SlotArmor"))
		{
			return container.b.get(clickedslot) != null;
		}
		return false;
	}
	
	protected static net.minecraft.server.v1_5_R3.Container getPlayerContainer(Player p) 
	{
		return getNMSHuman(p).activeContainer;
	}

	protected static net.minecraft.server.v1_5_R3.ItemStack getNMSItemStack(ItemStack i) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException 
	{
		Field handleField = i.getClass().getDeclaredField("handle");
		handleField.setAccessible(true);
		return (net.minecraft.server.v1_5_R3.ItemStack) handleField.get(i);
	}
	
	private static net.minecraft.server.v1_5_R3.EntityHuman getNMSHuman(Player p)
	{
		org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer cplayer = (org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer) p;
		net.minecraft.server.v1_5_R3.EntityPlayer nmsplayer = cplayer.getHandle();
		return (net.minecraft.server.v1_5_R3.EntityHuman) nmsplayer;
	}

}
