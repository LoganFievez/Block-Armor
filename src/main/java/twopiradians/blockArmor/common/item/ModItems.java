package twopiradians.blockArmor.common.item;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import twopiradians.blockArmor.common.BlockArmor;

public class ModItems
{
	public static ArrayList<ItemBlockArmor> allArmors = new ArrayList<ItemBlockArmor>();

	public static void postInit() 
	{
		ArrayList<ArmorSet> generatedSets = new ArrayList<ArmorSet>();
		ArrayList<ArmorSet> setsToRemove = new ArrayList<ArmorSet>();
		ArrayList<String> registeredNames = new ArrayList<String>();

		for (ArmorSet set : ArmorSet.allSets) {
			String name = set.item.getRegistryName().getResourcePath().toLowerCase().replace(" ", "_");
			if (registeredNames.contains(name))
				setsToRemove.add(set);
			else if (!ArmorSet.autoGeneratedSets.containsKey(set) || ArmorSet.autoGeneratedSets.get(set) == true) {
				if (set.stack.getHasSubtypes())
					name += "_"+set.meta;
				set.helmet = (ItemBlockArmor) registerItem(new ItemBlockArmor(set.material, 0, EntityEquipmentSlot.HEAD), name+"_helmet", true);
				set.chestplate = (ItemBlockArmor) registerItem(new ItemBlockArmor(set.material, 0, EntityEquipmentSlot.CHEST), name+"_chestplate", true);
				set.leggings = (ItemBlockArmor) registerItem(new ItemBlockArmor(set.material, 0,EntityEquipmentSlot.LEGS), name+"_leggings", true);
				set.boots = (ItemBlockArmor) registerItem(new ItemBlockArmor(set.material, 0, EntityEquipmentSlot.FEET), name+"_boots", true);
				registeredNames.add(name);
				generatedSets.add(set);
			}
		}

		//remove sets with duplicate registry names after loop to prevent concurrency issues
		for (ArmorSet set : setsToRemove)
			ArmorSet.allSets.remove(set);

		BlockArmor.logger.info("Created "+generatedSets.size()+" Block Armor items");
		/*for (ArmorSet set : generatedSets) 
			BlockArmor.logger.info("- "+set.stack.getDisplayName());*/
	}

	public static void registerRenders()
	{
		for (ItemBlockArmor item : allArmors)
			registerRender(item);
	}

	private static Item registerItem(Item item, String unlocalizedName, boolean addToTab) 
	{
		if (item instanceof ItemBlockArmor)
			allArmors.add((ItemBlockArmor) item);
		item.setUnlocalizedName(unlocalizedName);
		item.setRegistryName(BlockArmor.MODID, unlocalizedName);
		if (addToTab)
			item.setCreativeTab(BlockArmor.tab);
		GameRegistry.register(item);
		return item;
	}

	private static void registerRender(Item item)
	{		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(BlockArmor.MODID+":" + item.getUnlocalizedName().substring(5), "inventory"));
	}
}