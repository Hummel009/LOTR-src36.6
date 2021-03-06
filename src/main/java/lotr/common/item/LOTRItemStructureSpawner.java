package lotr.common.item;

import java.util.List;

import cpw.mods.fml.relauncher.*;
import lotr.common.*;
import lotr.common.world.structure.LOTRStructures;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class LOTRItemStructureSpawner extends Item {
	@SideOnly(value = Side.CLIENT)
	private IIcon iconBase;
	@SideOnly(value = Side.CLIENT)
	private IIcon iconOverlay;
	@SideOnly(value = Side.CLIENT)
	private IIcon iconVillageBase;
	@SideOnly(value = Side.CLIENT)
	private IIcon iconVillageOverlay;
	public static int lastStructureSpawnTick = 0;

	public LOTRItemStructureSpawner() {
		setHasSubtypes(true);
		setCreativeTab(LOTRCreativeTabs.tabSpawn);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		String s = ("" + StatCollector.translateToLocal(this.getUnlocalizedName() + ".name")).trim();
		String structureName = LOTRStructures.getNameFromID(itemstack.getItemDamage());
		if (structureName != null) {
			s = s + " " + StatCollector.translateToLocal("lotr.structure." + structureName + ".name");
		}
		return s;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public void registerIcons(IIconRegister iconregister) {
		iconBase = iconregister.registerIcon(getIconString() + "_base");
		iconOverlay = iconregister.registerIcon(getIconString() + "_overlay");
		iconVillageBase = iconregister.registerIcon(getIconString() + "_village_base");
		iconVillageOverlay = iconregister.registerIcon(getIconString() + "_village_overlay");
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int i, int pass) {
		LOTRStructures.StructureColorInfo info = LOTRStructures.structureItemSpawners.get(i);
		if (info != null) {
			if (info.isVillage) {
				if (pass == 0) {
					return iconVillageBase;
				}
				return iconVillageOverlay;
			}
			if (pass == 0) {
				return iconBase;
			}
			return iconOverlay;
		}
		return iconBase;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemstack, int pass) {
		LOTRStructures.StructureColorInfo info = LOTRStructures.structureItemSpawners.get(itemstack.getItemDamage());
		if (info != null) {
			if (pass == 0) {
				return info.colorBackground;
			}
			return info.colorForeground;
		}
		return 16777215;
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int side, float f, float f1, float f2) {
		if (world.isRemote) {
			return true;
		}
		if (LOTRLevelData.structuresBanned()) {
			entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.spawnStructure.disabled", new Object[0]));
			return false;
		}
		if (LOTRLevelData.isPlayerBannedForStructures(entityplayer)) {
			entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.spawnStructure.banned", new Object[0]));
			return false;
		}
		if (lastStructureSpawnTick > 0) {
			entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.spawnStructure.wait", new Object[] { lastStructureSpawnTick / 20.0 }));
			return false;
		}
		if (spawnStructure(entityplayer, world, itemstack.getItemDamage(), i += Facing.offsetsXForSide[side], j += Facing.offsetsYForSide[side], k += Facing.offsetsZForSide[side]) && !entityplayer.capabilities.isCreativeMode) {
			--itemstack.stackSize;
		}
		return true;
	}

	private boolean spawnStructure(EntityPlayer entityplayer, World world, int id, int i, int j, int k) {
		if (!LOTRStructures.structureItemSpawners.containsKey(id)) {
			return false;
		}
		LOTRStructures.IStructureProvider strProvider = LOTRStructures.getStructureForID(id);
		if (strProvider != null) {
			boolean generated = strProvider.generateStructure(world, entityplayer, i, j, k);
			if (generated) {
				lastStructureSpawnTick = 20;
				world.playSoundAtEntity(entityplayer, "lotr:item.structureSpawner", 1.0f, 1.0f);
			}
			return generated;
		}
		return false;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (LOTRStructures.StructureColorInfo info : LOTRStructures.structureItemSpawners.values()) {
			if (info.isHidden) {
				continue;
			}
			list.add(new ItemStack(item, 1, info.spawnedID));
		}
	}
}
