package twopiradians.blockArmor.common.item;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import com.google.common.collect.Maps;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.blockArmor.client.ClientProxy;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.config.Config;

public class ArmorSet {

	public static ArrayList<ArmorSet> allSets;
	/**Map of sets that have been auto generated and whether or not they are enabled in config*/
	public static HashMap<ArmorSet, Boolean> autoGeneratedSets = Maps.newHashMap();
	/**Map of sets that have effects and whether or not their effect is enabled*/
	public static HashMap<ArmorSet, Boolean> setsWithEffects = Maps.newHashMap();
	public static final ArrayList<ArmorSet> MANUALLY_ADDED_SETS;
	static {
		MANUALLY_ADDED_SETS = new ArrayList<ArmorSet>() {{
			add(new ArmorSet(new ItemStack(Blocks.NETHERRACK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.OBSIDIAN, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.REDSTONE_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.SNOW, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.LAPIS_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.END_STONE, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.SLIME_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Items.REEDS, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.PRISMARINE, 1, 2), true));
			add(new ArmorSet(new ItemStack(Blocks.EMERALD_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.BRICK_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.BEDROCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 0), true));
			//add(new ArmorSet(new ItemStack(Blocks.BEACON, 1, 0), true));

			add(new ArmorSet(new ItemStack(Blocks.BROWN_MUSHROOM_BLOCK, 1, 0), false));
			add(new ArmorSet(new ItemStack(Blocks.RED_MUSHROOM_BLOCK, 1, 0), false));
		}};
	}
	/**Reflected value used to find textures*/
	private static ItemModelMesherForge itemModelMesher;
	/**Reflected value used to iterate through all items*/
	private static IdentityHashMap<Item, TIntObjectHashMap<ModelResourceLocation>> locations;
	/**List of current display names of all registered items - to prevent redundant sets*/
	private static ArrayList<String> displayNames;

	public ItemStack stack;
	public Item item;
	public int meta;
	public Block block;
	public ArmorMaterial material;      
	public boolean hasSetEffect;
	public ItemBlockArmor helmet;
	public ItemBlockArmor chestplate;
	public ItemBlockArmor leggings;
	public ItemBlockArmor boots;
	public boolean isTranslucent;

	/**Array of sprites for the block's texture sorted by EntityEquipmentSlot id*/
	private TextureAtlasSprite[] sprites;
	/**Array of fields for TextureAtlasSprite's frameCounter (or null if not animated) sorted by EntityEquipmentSlot id*/
	private Field[] frameFields;

	@SuppressWarnings("deprecation")
	public ArmorSet(ItemStack stack, boolean hasSetEffect) {
		this.stack = stack;
		this.item = stack.getItem();
		this.meta = stack.getMetadata();
		if (item == Items.REEDS)
			this.block = Blocks.REEDS;
		else
			this.block = ((ItemBlock) item).getBlock();
		this.hasSetEffect = hasSetEffect;
		if (hasSetEffect)
			setsWithEffects.put(this, true);
		//calculate values for and set material
		float blockHardness = 0; 
		double durability = 5;
		float toughness = 0;
		int enchantability = 12;

		try {
			blockHardness = this.block.getBlockHardness(this.block.getDefaultState(), null, new BlockPos(0,0,0));
		} catch(Exception e) {
			blockHardness = ReflectionHelper.getPrivateValue(Block.class, this.block, 11); //blockHardness
		}
		if (blockHardness == -1) {
			durability = 0;
			blockHardness = 1000;
		}
		else
			durability = 2 + 8* Math.log(blockHardness + 1);
		if (blockHardness > 10)
			toughness = Math.min(blockHardness / 10F, 10);
		durability = Math.min(30, durability);
		blockHardness = (float) Math.log(blockHardness+1.5D)+1;
		int reductionAmount1 = (int) Math.min(1 + blockHardness, 3);
		int reductionAmount2 = (int) Math.min(1 + 2*blockHardness, 5);
		int reductionAmount3 = (int) Math.min(1 + 2.2D*blockHardness, 6);
		int reductionAmount4 = (int) Math.min(1 + blockHardness, 3);
		int[] reductionAmounts = new int[] {reductionAmount1, reductionAmount2, reductionAmount3, reductionAmount4};
		this.material = EnumHelper.addArmorMaterial(getItemStackDisplayName(stack, null)+" Material", "", 
				(int) durability, reductionAmounts, enchantability, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, toughness);
		//System.out.println(getItemStackDisplayName(stack, null)+": blockHardness = "+blockHardness+", toughness = "+toughness+", durability = "+durability);
	}

	/**Creates ArmorSets for each valid registered item and puts them in allSets*/
	public static void postInit() {
		//initialize reflected fields TODO fix to not use Minecraft.getMinecraft()
		itemModelMesher = ReflectionHelper.getPrivateValue(RenderItem.class, Minecraft.getMinecraft().getRenderItem(), 3);
		locations = ReflectionHelper.getPrivateValue(ItemModelMesherForge.class, itemModelMesher, 0);

		displayNames = new ArrayList<String>();
		for (Item item : locations.keySet())
			for (int meta : locations.get(item).keys())
				if (item != null)
					displayNames.add(new ItemStack(item, 1, meta).getDisplayName());

		allSets = new ArrayList<ArmorSet>();
		allSets.addAll(MANUALLY_ADDED_SETS);
		for (Item item : locations.keySet()) //iterate through all items and meta and create sets for valid ones
			for (int meta : locations.get(item).keys()) {
				ItemStack stack = new ItemStack(item, 1, meta);
				if (isValid(stack) && ArmorSet.getSet(item, meta) == null) 
					if (!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.HEAD)) &&
							!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.CHEST)) &&
							!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.LEGS)) &&
							!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.FEET))) {
						allSets.add(new ArmorSet(stack, false));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.HEAD));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.CHEST));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.LEGS));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.FEET));
					}
			}

		//populate autoGeneratedSets
		for (ArmorSet set : allSets)
			if (!MANUALLY_ADDED_SETS.contains(set))
				autoGeneratedSets.put(set, true);
	}

	/**Returns TextureAtlasSprite corresponding to given ItemModArmor*/
	public static TextureAtlasSprite getSprite(ItemBlockArmor item) {
		ArmorSet set = ArmorSet.getSet(item);
		if (set != null) 
			return set.sprites[item.getEquipmentSlot().getIndex()];
		else
			return null;
	}

	/**Returns TextureAtlasSprite corresponding to given ItemModArmor*/
	public static int getAnimationFrame(ItemBlockArmor item) {
		ArmorSet set = ArmorSet.getSet(item);
		if (set != null) {
			Field field = set.frameFields[item.getEquipmentSlot().getIndex()];
			if (field == null)
				return 0;
			else
				try {
					return field.getInt(ArmorSet.getSprite(item));
				} catch (Exception e) {
					return 0;
				}
		}
		else
			return 0;
	}

	/**Change display name based on the block*/
	public static String getItemStackDisplayName(ItemStack stack, EntityEquipmentSlot slot)
	{
		String name;
		if (stack.getItem() instanceof ItemBlockArmor) {
			ArmorSet set = ArmorSet.getSet((ItemBlockArmor) stack.getItem());
			name = set.stack.getDisplayName();
		}
		else
			name = stack.getDisplayName();

		if (slot != null)
			switch (slot) {
			case HEAD:
				name += " Helmet";
				break;
			case CHEST:
				name += " Chestplate";
				break;
			case LEGS:
				name += " Leggings";
				break;
			case FEET:
				name += " Boots";
				break;
			default:
				break;
			}

		return name.replace("Block of ", "").replace("Block ", "").replace("Gold", "Golden");
	}

	/**Determines if entity is wearing a full set of armor of same material*/
	public static boolean isWearingFullSet(EntityLivingBase entity, ArmorSet set)
	{
		if (entity != null && set != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.FEET) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == set.boots
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == set.leggings
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == set.chestplate
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == set.helmet)
			return true;
		else
			return false;
	}

	/**Returns true if the set has a set effect and is enabled in Config*/
	public static boolean isSetEffectEnabled(ArmorSet set) {
		if (set == null || !set.hasSetEffect || Config.setEffects == 1)
			return false;
		if (setsWithEffects.get(set) || Config.setEffects == 0)
			return true;
		return false;
	}

	/**Returns armor set corresponding to given block and meta, or null if none exists*/
	public static ArmorSet getSet(Block block, int meta) {
		return getSet(Item.getItemFromBlock(block), meta);
	}

	/**Returns armor set corresponding to given item and meta, or null if none exists*/
	public static ArmorSet getSet(Item item, int meta) {
		for (ArmorSet set : allSets)
			if (set.item == item && set.meta == meta)
				return set;
		return null;
	}

	/**Returns armor set containing given ItemModArmor, or null if none exists*/
	public static ArmorSet getSet(ItemBlockArmor item) {
		for (ArmorSet set : allSets)
			if (set.helmet == item || set.chestplate == item || set.leggings == item || set.boots == item)
				return set;
		return null;
	}

	/**Should an armor set be made from this item*/
	@SuppressWarnings("deprecation")
	private static boolean isValid(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemBlock) || stack.getDisplayName().contains("Ore"))
			return false;

		Block block = ((ItemBlock)stack.getItem()).getBlock();
		if (block instanceof BlockLiquid || block instanceof BlockContainer || block.hasTileEntity() || 
				block instanceof BlockOre || block instanceof BlockCrops || block instanceof BlockBush ||
				block == Blocks.BARRIER || block instanceof BlockLeaves || block == Blocks.MONSTER_EGG || 
				block == Blocks.GRASS)
			return false;

		//Check if full block
		ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		try {
			block.addCollisionBoxToList(block.getDefaultState(), null, new BlockPos(0,0,0), Block.FULL_BLOCK_AABB, list, null);
		} catch (Exception e) {
			return false;
		}
		if (list.size() != 1 || !list.get(0).equals(Block.FULL_BLOCK_AABB)) 
			return false;

		return true;
	}

	/**Initialize set's texture variable*/
	@SideOnly(Side.CLIENT)
	public void initTextures() {
		System.out.println("starting init textures");

		this.sprites = new TextureAtlasSprite[EntityEquipmentSlot.values().length];
		this.frameFields = new Field[EntityEquipmentSlot.values().length];

		//Gets textures from item model's BakedQuads (textures for each side)
		IBlockState state = this.block.getDefaultState();
		List<BakedQuad> list = new ArrayList<BakedQuad>();
		list.addAll(itemModelMesher.getItemModel(this.stack).getQuads(state, null, 0));
		for (EnumFacing facing : EnumFacing.VALUES)
			list.addAll(itemModelMesher.getItemModel(this.stack).getQuads(state, facing, 0));
		for (BakedQuad quad : list) { //there's at least one texture per face
			ResourceLocation loc1 = new ResourceLocation(quad.getSprite().getIconName());

			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc1.toString());
			Field field = sprite.getFrameCount() > 1 ? ReflectionHelper.findField(TextureAtlasSprite.class, new String[] {"frameCounter", "field_110973_g"}) : null;

			if (quad.getFace() == EnumFacing.UP) {
				this.sprites[EntityEquipmentSlot.HEAD.getIndex()] = sprite;
				this.frameFields[EntityEquipmentSlot.HEAD.getIndex()] = field;
			}
			else if (quad.getFace() == EnumFacing.NORTH) {
				this.sprites[EntityEquipmentSlot.CHEST.getIndex()] = sprite;
				this.frameFields[EntityEquipmentSlot.CHEST.getIndex()] = field;
			}
			else if (quad.getFace() == EnumFacing.SOUTH) {
				this.sprites[EntityEquipmentSlot.LEGS.getIndex()] = sprite;
				this.frameFields[EntityEquipmentSlot.LEGS.getIndex()] = field;
			}
			else if (quad.getFace() == EnumFacing.DOWN) {
				this.sprites[EntityEquipmentSlot.FEET.getIndex()] = sprite;
				this.frameFields[EntityEquipmentSlot.FEET.getIndex()] = field;
			}
			System.out.println("adding sprite for: "+sprite.getIconName()+", with field: "+field);
			
			if (sprite.getIconName() == TextureMap.LOCATION_MISSING_TEXTURE.toString())
				((ClientProxy)BlockArmor.proxy).remapTextures = true;
		}

		//Check for inventory texture overrides (expects block texture) - location must be registered in ClientProxy TextureStitchEvent.Pre
		try {
			ResourceLocation texture = new ResourceLocation(BlockArmor.MODID+":textures/items/"+stack.getDisplayName().toLowerCase().replace(" ", "_")+".png");
			Minecraft.getMinecraft().getResourceManager().getResource(texture); //does texture exist?
			texture = new ResourceLocation(texture.getResourceDomain(), texture.getResourcePath().replace("textures/", "").replace(".png", ""));
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texture.toString());
			this.sprites[EntityEquipmentSlot.HEAD.getIndex()] = sprite;
			this.frameFields[EntityEquipmentSlot.HEAD.getIndex()] = null;
			this.sprites[EntityEquipmentSlot.CHEST.getIndex()] = sprite;
			this.frameFields[EntityEquipmentSlot.CHEST.getIndex()] = null;
			this.sprites[EntityEquipmentSlot.LEGS.getIndex()] = sprite;
			this.frameFields[EntityEquipmentSlot.LEGS.getIndex()] = null;
			this.sprites[EntityEquipmentSlot.FEET.getIndex()] = sprite;
			this.frameFields[EntityEquipmentSlot.FEET.getIndex()] = null;
			//System.out.println("Texture found at: "+texture.toString());
		} catch (Exception e) {
			//System.out.println("No texture found at: "+texture.toString());
		}

		this.isTranslucent = this.block.getBlockLayer() != BlockRenderLayer.SOLID;
		System.out.println("finished init textures");
	}
}
