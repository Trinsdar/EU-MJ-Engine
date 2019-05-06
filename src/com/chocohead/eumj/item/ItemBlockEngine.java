package com.chocohead.eumj.item;

import com.chocohead.eumj.util.Registry;
import ic2.core.item.block.ItemBlockRare;
import ic2.core.platform.lang.storage.Ic2InfoLang;
import ic2.core.platform.player.PlayerHandler;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemBlockEngine extends ItemBlockRare {
    public ItemBlockEngine(Block block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        PlayerHandler handler = PlayerHandler.getClientPlayerHandler();
        if (handler.hasEUReader()) {
            tooltip.add(Ic2InfoLang.euReaderSinkInfo.getLocalizedFormatted(getMaxInput()));
        }
    }

    public int getMaxInput(){
        if (this.getBlock() == Registry.adjustableElectricEngine){
            return 2048;
        }else if (this.getBlock() == Registry.fastElectricEngine){
            return 512;
        }else if (this.getBlock() == Registry.regularElectricEngine){
            return 128;
        }else {
            return 32;
        }
    }
}
