package com.chocohead.eumj.container;

import com.chocohead.eumj.tileentity.TileEntityElectricEngine;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.container.ContainerTileComponent;
import ic2.core.inventory.gui.components.base.MachineChargeComp;
import ic2.core.inventory.slots.SlotDischarge;
import ic2.core.platform.registry.Ic2GuiComp;
import ic2.core.util.math.Box2D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class ContainerElectricEngine extends ContainerTileComponent<TileEntityElectricEngine> {
    public static Box2D machineChargeBox = new Box2D(81, 24, 14, 14);
    public ContainerElectricEngine(InventoryPlayer player, TileEntityElectricEngine tile) {
        super(tile);
        this.addSlotToContainer(new SlotDischarge(tile, 0, 80, 41));
        this.addComponent(new MachineChargeComp(tile, machineChargeBox, Ic2GuiComp.machineChargePos));
        this.addPlayerInventory(player);
    }

    @Override
    public ResourceLocation getTexture() {
        return this.getGuiHolder().getGuiTexture();
    }

    @Override
    public int guiInventorySize() {
        return this.getGuiHolder().slotCount;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer)
    {
        return this.getGuiHolder().canInteractWith(entityPlayer);
    }
}
