package com.chocohead.eumj.tileentity;

import buildcraft.api.mj.MjAPI;
import ic2.core.inventory.container.ContainerIC2;
import net.minecraft.entity.player.EntityPlayer;

public class TileEntitySlowElectricEngine extends TileEntityElectricEngine {
    public TileEntitySlowElectricEngine() {
        super(3, 32);
    }

    @Override
    protected double getPistonSpeed() {
        return 0.02;
    }


    @Override
    protected long getOutput() {
        return 1 * MjAPI.MJ;
    }
}
