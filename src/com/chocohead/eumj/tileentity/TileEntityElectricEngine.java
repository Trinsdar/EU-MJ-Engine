package com.chocohead.eumj.tileentity;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.engine.EngineConnector;
import com.chocohead.eumj.EngineMod;
import com.chocohead.eumj.container.ContainerAdjustableElectricEngine;
import com.chocohead.eumj.container.ContainerElectricEngine;
import ic2.api.classic.item.IMachineUpgradeItem;
import ic2.core.inventory.container.ContainerIC2;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class TileEntityElectricEngine extends TileEntityEngine{
    protected boolean engineActive = false;
    public TileEntityElectricEngine(int slots, int maxinput) {
        super(slots, maxinput);
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    protected EnumPowerStage computePowerStage() {
        return EnumPowerStage.GREEN;
    }

    @Override
    protected abstract double getPistonSpeed();
    // <<< Engine State

    // Power >>>
    @Override
    protected void burn() {
        long output = getOutput();
        double input = EngineMod.Conversion.MJtoEU(output);

        if (useEnergy(input, true) && isRedstonePowered()) {
            engineActive = true;

            addPower(output);
            useEnergy(input, false);
        } else {
            engineActive = false;
        }
    }

    /**
     * @return The engine output in microMJ
     */
    protected abstract long getOutput();

    @Override
    protected long getMaxPower() {
        return 10000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public boolean useEnergy(double amount, boolean simulate) {
        if (energy < amount) {
            return false;
        }
        if (!simulate) {
            useEnergy((int) amount);
        }
        return true;
    }

    @Override
    public void setRedstoneSensitive(boolean b) {

    }

    @Override
    public boolean isRedstoneSensitive() {
        return true;
    }

    @Override
    public boolean isProcessing() {
        return false;
    }

    @Override
    public boolean isValidInput(ItemStack itemStack) {
        return false;
    }

    @Override
    public Set<IMachineUpgradeItem.UpgradeType> getSupportedTypes() {
        return new LinkedHashSet(Arrays.asList(IMachineUpgradeItem.UpgradeType.RedstoneControl, IMachineUpgradeItem.UpgradeType.MachineModifierB));
    }

    @Override
    public World getMachineWorld() {
        return this.getWorld();
    }

    @Override
    public BlockPos getMachinePos() {
        return this.getPos();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long getActiveOutput() {
        return engineActive ? getOutput() : 0;
    }


    @Override
    public ContainerIC2 getGuiContainer(EntityPlayer player) {
        return new ContainerElectricEngine(player.inventory, this);
    }

    @Override
    public Class<? extends GuiScreen> getGuiClass(EntityPlayer entityPlayer) {
        return null;
    }

    public ResourceLocation getGuiTexture(){
        return new ResourceLocation(EngineMod.MODID, "textures/gui/gui_electric_engine.png");
    }
}
