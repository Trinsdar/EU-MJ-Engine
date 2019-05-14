package com.chocohead.eumj;

import static com.chocohead.eumj.EngineMod.MODID;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import com.chocohead.eumj.tileentity.TileEntityEngine;
import com.chocohead.eumj.util.Registry;
import ic2.api.classic.recipe.ClassicRecipes;
import ic2.core.platform.registry.Ic2Items;
import ic2.core.util.misc.StackUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.BCBlocks.Core;
import buildcraft.api.BCItems;
import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.mj.MjAPI;
import buildcraft.transport.BCTransportItems;

import ic2.api.item.IC2Items;

@Mod(modid=MODID, name="EU-MJ Engine", dependencies="required-after:ic2;required-after:buildcraftenergy@[7.99.22, 7.99.24.1];after:buildcrafttransport", version="@VERSION@")
public final class EngineMod {
	public static final String MODID = "eu-mj_engine";

	public static final CreativeTabs TAB = new CreativeTabs("EU-MJ Engine") {
		private ItemStack[] items;
		private int ticker;

		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getIconItemStack() {
			if (++ticker >= 500) {
				ticker = 0;
			}

			if (items == null) {
				items = new ItemStack[4];

				items[0] = new ItemStack(Registry.slowElectricEngine);
				items[1] = new ItemStack(Registry.regularElectricEngine);
				items[2] = new ItemStack(Registry.fastElectricEngine);
				items[3] = new ItemStack(Registry.adjustableElectricEngine);
			}

			assert ticker / 100 < items.length;
			return items[ticker / 100];
		}

		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getTabIconItem() {
			return null; //Only normally called from getIconItemStack
		}

		@Override
		@SideOnly(Side.CLIENT)
		public String getTranslatedTabLabel() {
			return MODID + ".creative_tab";
		}
	};


	@EventHandler
	public void construction(FMLConstructionEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}


	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		loadConfig(event.getSuggestedConfigurationFile());
		event.getModLog().info("Running with "+Conversion.MJperEU / MjAPI.MJ+" MJ per EU or "+MjAPI.MJ / Conversion.MJperEU+" EU per MJ");
		Registry.init();


		if (event.getSide().isClient()) {
			Registry.initModels();
		}
	}

	private void loadConfig(File file) {
		Configuration config = new Configuration(file);

		try {
			config.load();

			Conversion.MJperEU = MjAPI.MJ * config.getFloat("MJperEU", "balance", 2F / 5F, 1F / 100, 100F, "The number of MJ per EU");
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception loading config!", e);
		} finally {
			if (config.hasChanged()) {
				config.save();
			}
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

		if (Core.ENGINE != null) {
			ClassicRecipes.advCrafting.addRecipe(new ItemStack(Registry.slowElectricEngine),
					"B", "E", "C",
					'B', anyCharge(Ic2Items.battery),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.STONE.ordinal()),
					'C', Ic2Items.electricCircuit);

			ClassicRecipes.advCrafting.addRecipe(new ItemStack(Registry.regularElectricEngine),
					"BBB", "EPE", "CPC",
					'B', anyCharge(Ic2Items.battery),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.IRON.ordinal()),
					'P', IC2Items.getItem("crafting", "alloy"),
					'C', IC2Items.getItem("crafting", "circuit"));

			ClassicRecipes.advCrafting.addRecipe(new ItemStack(Registry.fastElectricEngine),
					"BPB", "EEE", "CPC",
					'B', anyCharge(IC2Items.getItem("energy_crystal")),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.IRON.ordinal()),
					'P', IC2Items.getItem("crafting", "alloy"),
					'C', IC2Items.getItem("crafting", "advanced_circuit"));

			ClassicRecipes.advCrafting.addRecipe(new ItemStack(Registry.adjustableElectricEngine),
					"BCB", "EEE", "MTM",
					'B', anyCharge(IC2Items.getItem("lapotron_crystal")),
					'E', new ItemStack(Core.ENGINE, 1, EnumEngineType.IRON.ordinal()),
					'C', IC2Items.getItem("crafting", "advanced_circuit"),
					'M', IC2Items.getItem("resource", "advanced_machine"),
					'T', IC2Items.getItem("te", "hv_transformer"));
		}

		if (Registry.mjReader != null && BCItems.Core.GEAR_GOLD != null && BCTransportItems.pipePowerWood != null) {
			Collection<ItemStack> pipes = new HashSet<>();

			for (Item pipe : new Item[] {BCTransportItems.pipePowerCobble, BCTransportItems.pipePowerStone,
					BCTransportItems.pipePowerQuartz, BCTransportItems.pipePowerGold, BCTransportItems.pipePowerSandstone}) {
				if (pipe != null) {
					pipes.add(new ItemStack(pipe));
				}
			}

			if (!pipes.isEmpty()) {
				ClassicRecipes.advCrafting.addRecipe(new ItemStack(Registry.mjReader),
						" D ", "PGP", "p p",
						'D', Items.GLOWSTONE_DUST,
						'G', BCItems.Core.GEAR_GOLD,
						'P', pipes,
						'p', BCTransportItems.pipePowerWood);
			}
		}

		if (event.getSide().isClient()) {
			//BuildCraft Lib loads pages in post-init, but it also loads first, so we do this here
			GuideThings.addLoaders();
			GuideThings.addTags();
		}
	}
	
	private static ItemStack anyCharge(ItemStack stack) {
		return StackUtil.copyWithWildCard(stack);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
//		CustomRotationHelper.INSTANCE.registerHandler(engine, (world, pos, state, side) -> {
//			TileEntity te = world.getTileEntity(pos);
//
//			return te instanceof TileEntityEngine && ((TileEntityEngine) te).trySpin(side.getOpposite()) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
//		});
	}


	public static class Conversion {
		static double MJperEU = MjAPI.MJ * 2 / 5;

		public static double MJtoEU(long microjoules) {
			return microjoules / MJperEU;
		}

		public static long EUtoMJ(double EU) {
			return (long) (EU * MJperEU);
		}
	}
}