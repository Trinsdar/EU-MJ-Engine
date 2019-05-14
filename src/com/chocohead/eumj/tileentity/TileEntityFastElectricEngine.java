package com.chocohead.eumj.tileentity;

import buildcraft.api.mj.MjAPI;
import ic2.core.inventory.container.ContainerIC2;
import net.minecraft.entity.player.EntityPlayer;

public class TileEntityFastElectricEngine extends TileEntityElectricEngine {
    public TileEntityFastElectricEngine() {
        super(3, 512);
    }

    @Override
    protected double getPistonSpeed() {
        return 0.07;
    }


    @Override
    protected long getOutput() {
        return 10 * MjAPI.MJ;
    }
}
