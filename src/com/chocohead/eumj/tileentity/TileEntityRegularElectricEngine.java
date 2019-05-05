package com.chocohead.eumj.tileentity;

import buildcraft.api.mj.MjAPI;
import ic2.core.inventory.container.ContainerIC2;
import net.minecraft.entity.player.EntityPlayer;

public class TileEntityRegularElectricEngine extends TileEntityElectricEngine {
    public TileEntityRegularElectricEngine() {
        super(3, 128);
    }

    @Override
    protected double getPistonSpeed() {
        return 0.05;
    }


    @Override
    protected long getOutput() {
        return 5 * MjAPI.MJ;
    }

    @Override
    public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer) {
        return null;
    }
}
