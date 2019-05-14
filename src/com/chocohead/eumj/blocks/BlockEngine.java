package com.chocohead.eumj.blocks;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.block.VanillaRotationHandlers;
import com.chocohead.eumj.EngineMod;
import com.chocohead.eumj.tileentity.TileEntityAdjustableElectricEngine;
import com.chocohead.eumj.tileentity.TileEntityEngine;
import com.chocohead.eumj.tileentity.TileEntityFastElectricEngine;
import com.chocohead.eumj.tileentity.TileEntityRegularElectricEngine;
import com.chocohead.eumj.tileentity.TileEntitySlowElectricEngine;
import com.chocohead.eumj.util.Registry;
import com.chocohead.eumj.util.VeryOrderedEnumMap;
import ic2.core.IC2;
import ic2.core.block.base.BlockCommonContainer;
import ic2.core.block.base.tile.TileEntityBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockEngine extends BlockCommonContainer {
    public BlockEngine(String name) {
        super(Material.IRON);
        this.setHardness(4.0F);
        this.setResistance(20.0F);
        this.setSoundType(SoundType.METAL);
        this.setCreativeTab(EngineMod.TAB);
        setUnlocalizedName(name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityEngine) {
            TileEntityEngine engine = (TileEntityEngine) tile;
            if (side == engine.getFacing().getOpposite()) {
                return BlockFaceShape.SOLID;
            } else {
                return BlockFaceShape.UNDEFINED;
            }
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityEngine) {
            TileEntityEngine engine = (TileEntityEngine) tile;
            return side == engine.getFacing().getOpposite();
        }
        return false;
    }

    @Override
    public IBlockState getDefaultBlockState() {
        return null;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!IC2.platform.isRendering()) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileEntityEngine) {
                TileEntityEngine block = (TileEntityEngine)tile;
                block.setFacing(getDefaultPlacementFacing(state, placer, block));
                if (stack.hasDisplayName()) {
                    block.setCustomName(stack.getDisplayName());
                }

            }
        }
    }

    public EnumFacing getDefaultPlacementFacing(IBlockState state, EntityLivingBase placer, TileEntityEngine engine) {
        if (placer == null){
            return EnumFacing.UP;
        }
        return engine.isFacingMJ(placer.getHorizontalFacing()) ? EnumFacing.fromAngle((double)placer.rotationYaw) : EnumFacing.UP;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        if (this == Registry.slowElectricEngine){
            return new TileEntitySlowElectricEngine();
        }else if (this == Registry.regularElectricEngine){
            return new TileEntityRegularElectricEngine();
        }else if (this == Registry.fastElectricEngine){
            return new TileEntityFastElectricEngine();
        }else if (this == Registry.adjustableElectricEngine){
            return new TileEntityAdjustableElectricEngine();
        }else {
            return new TileEntityBlock();
        }
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(this), 0,
                new ModelResourceLocation(getRegistryName(), "inventory")
        );
    }
}
