package twopiradians.blockArmor.common.tileentity;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTileEntities
{
	public static void preInit() 
	{
		GameRegistry.registerTileEntity(TileEntityMovingLightSource.class, "light_tile_entity");
	}
}
