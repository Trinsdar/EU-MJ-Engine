package com.chocohead.eumj.tileentity;

import buildcraft.api.mj.MjAPI;
import com.chocohead.eumj.EngineMod;
import com.chocohead.eumj.container.ContainerAdjustableElectricEngine;
import ic2.core.IC2;
import ic2.core.inventory.container.ContainerIC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class TileEntityAdjustableElectricEngine extends TileEntityElectricEngine {

    protected long output = 10;
    protected double pistonSpeed = 0.07;

    public TileEntityAdjustableElectricEngine() {
        super(3, 2048);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setLong("output", output);
        nbt.setDouble("piston", pistonSpeed);

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        output = nbt.getLong("output");
        pistonSpeed = nbt.getDouble("piston");
    }

    @Override
    public List<String> getNetworkedFields() {
        List<String> out = super.getNetworkedFields();

        out.add("output");
        out.add("pistonSpeed");

        return out;
    }

    @Override
    protected double getPistonSpeed() {
        return pistonSpeed;
    }

    @Override
    protected long getOutput() {
        return output * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 3000 * MjAPI.MJ; //TODO: Configurable
    }


    //@Override
    public void onNetworkEvent(EntityPlayer player, int event) {
        switch (event / 10) {
            case 0:
                switch (event % 10) {
                    case 0:
                        changeProduction(-100);
                        break;
                    case 1:
                        changeProduction(-10);
                        break;
                    case 2:
                        changeProduction(-1);
                        break;
                    case 3:
                        changeProduction(1);
                        break;
                    case 4:
                        changeProduction(10);
                        break;
                    case 5:
                        changeProduction(100);
                        break;
                }
                break;
        }
    }

    protected void changeProduction(int value) {
        output = Math.max(output + value, 1);
        pistonSpeed = Math.max(0.1F, 1.0F / (3000 / output - 2.0F));

        IC2.network.get(true).updateTileEntityField(this, "pistonSpeed");
    }

    @Override
    public ContainerIC2 getGuiContainer(EntityPlayer player) {
        return new ContainerAdjustableElectricEngine(player.inventory, this);
    }

    @Override
    public ResourceLocation getGuiTexture(){
        return new ResourceLocation(EngineMod.MODID, "textures/gui/gui_adjustable_electric_engine.png");
    }

//    @SideOnly(Side.CLIENT)
//    public String getCurrentOutput() {
//        return Localization.translate("eu-mj_engine.engines.adjustable_electric_engine.info", EngineMod.Conversion.MJtoEU(getOutput()), output);
//    }
}
