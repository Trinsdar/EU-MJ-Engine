package com.chocohead.eumj.item;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.transport.pipe.flow.PipeFlowPower.Section;
import com.chocohead.eumj.EngineMod;
import ic2.core.item.tool.electric.ItemElectricReader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.Map;

public class ItemReaderMJ extends Item {
	private static final String NAME = "mj_reader";

	public ItemReaderMJ() {
		super();

		setUnlocalizedName(NAME);
		maxStackSize = 1;
		setMaxDamage(0);
		setCreativeTab(EngineMod.TAB);
	}


	@SideOnly(Side.CLIENT)
	public void registerModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(EngineMod.MODID+':'+NAME, "inventory"));
	}

	@Override
	public String getUnlocalizedName() {
		return EngineMod.MODID + super.getUnlocalizedName().substring(3);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (world.isRemote) return EnumActionResult.PASS;

		TileEntity te = world.getTileEntity(pos);
		if (te == null || hand == EnumHand.OFF_HAND) return EnumActionResult.FAIL;

		Map<EnumFacing, Section> caps = new EnumMap<>(EnumFacing.class);
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (te.hasCapability(MjAPI.CAP_CONNECTOR, facing)) {
				IMjConnector cap = te.getCapability(MjAPI.CAP_CONNECTOR, facing);

				if (cap instanceof Section) {
					//MJ really doesn't make it easy to work out average power flow
					//But the internal BuildCraft logic renders using averages, so we can just steal it
					//Ideally there should be a better (and more generic) way to do this though
					caps.put(facing, (Section) cap);
				}
			}
		}
		if (caps.isEmpty()) return EnumActionResult.FAIL;


		return EnumActionResult.PASS;
	}


	public boolean canBeStoredInToolbox(ItemStack stack) {
		return true;
	}
}