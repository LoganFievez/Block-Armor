package twopiradians.blockArmor.common.seteffect;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectFeeder extends SetEffect
{

	protected SetEffectFeeder() {
		this.color = TextFormatting.RED;
		this.description = "Automatically feeds the wearer";
	}

	/**Only called when player wearing full, enabled set*/
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);//TODO add particles (of block)

		if (!player.getCooldownTracker().hasCooldown(stack.getItem()) && 
				!world.isRemote && ArmorSet.getFirstSetItem(player, this) == stack)
			if (player.canEat(false)) {
				int foodLevel = player.getFoodStats().getFoodLevel();
				int foodToFeed = Math.min(20-foodLevel, 4);
				player.getFoodStats().addStats(foodToFeed,0.1f);

				if (foodLevel < 16)
					for (int i=0; i<2; ++i)
						world.playSound((EntityPlayer)null, player.getPosition(), SoundEvents.ENTITY_GENERIC_EAT, 
								SoundCategory.PLAYERS, 0.3F, world.rand.nextFloat()*0.2f + 1.0f);
				else
					world.playSound((EntityPlayer)null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, 
							SoundCategory.PLAYERS, 0.3F, world.rand.nextFloat()*0.2f + 1.0f);
				
				this.setCooldown(player, 80);
				this.damageArmor(player, foodToFeed, true);
			}
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block, int meta) {	
		if (SetEffect.registryNameContains(block, meta, new String[] {"pumpkin", "melon", "food", "berry", "feed", "pork"}))
			return true;		
		return false;
	}	
}