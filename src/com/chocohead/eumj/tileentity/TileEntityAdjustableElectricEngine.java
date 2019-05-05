package com.chocohead.eumj.tileentity;

import buildcraft.api.mj.MjAPI;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import com.chocohead.eumj.EngineMod;
import com.chocohead.eumj.gui.DynamicBridgeGUI;
import com.chocohead.eumj.gui.TransparentDynamicBridgeGUI;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IC2;
import ic2.core.inventory.container.ContainerIC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

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
    public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer) {
        return null;
    }

//    @SideOnly(Side.CLIENT)
//    public String getCurrentOutput() {
//        return Localization.translate("eu-mj_engine.engines.adjustable_electric_engine.info", EngineMod.Conversion.MJtoEU(getOutput()), output);
//    }
}
