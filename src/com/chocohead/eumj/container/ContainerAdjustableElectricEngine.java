package com.chocohead.eumj.container;

import com.chocohead.eumj.tileentity.TileEntityAdjustableElectricEngine;
import com.chocohead.eumj.tileentity.TileEntityElectricEngine;
import ic2.core.inventory.container.ContainerTileComponent;
import ic2.core.inventory.gui.GuiIC2;
import ic2.core.inventory.gui.components.base.MachineChargeComp;
import ic2.core.inventory.slots.SlotDischarge;
import ic2.core.platform.registry.Ic2GuiComp;
import ic2.core.util.math.Box2D;
import ic2.core.util.math.Vec2i;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerAdjustableElectricEngine extends ContainerTileComponent<TileEntityAdjustableElectricEngine> {
    public static Box2D machineChargeBox = new Box2D(95, 62, 14, 14);
    public static Vec2i machineChargePos = new Vec2i(205, 0);
    public ContainerAdjustableElectricEngine(InventoryPlayer player, TileEntityAdjustableElectricEngine tile) {
        super(tile);
        this.addSlotToContainer(new SlotDischarge(tile, 0, 94, 79));
        this.addComponent(new MachineChargeComp(tile, machineChargeBox, machineChargePos));
        this.addPlayerInventory(player, 14, 38);
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

    @Override
    @SideOnly(Side.CLIENT)
    public void onGuiLoaded(GuiIC2 gui) {
        gui.setMaxGuiXY(205, 204);
        gui.dissableInvName();
    }
}
