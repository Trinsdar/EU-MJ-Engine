package com.chocohead.eumj.util;

import buildcraft.api.BCModules;
import com.chocohead.eumj.blocks.BlockEngine;
import com.chocohead.eumj.item.ItemBlockEngine;
import com.chocohead.eumj.item.ItemReaderMJ;
import ic2.core.IC2;

public class Registry {
    public static BlockEngine slowElectricEngine = new BlockEngine("slow_electric_engine");
    public static BlockEngine regularElectricEngine = new BlockEngine("regular_electric_engine");
    public static BlockEngine fastElectricEngine = new BlockEngine("fast_electric_engine");
    public static BlockEngine adjustableElectricEngine = new BlockEngine("adjustable_electric_engine");
    public static ItemReaderMJ mjReader = new ItemReaderMJ();
    public static void init(){
        if (BCModules.TRANSPORT.isLoaded()) {
            IC2.getInstance().createItem(mjReader);
        }
        IC2.getInstance().createBlock(adjustableElectricEngine, ItemBlockEngine.class);
        IC2.getInstance().createBlock(fastElectricEngine, ItemBlockEngine.class);
        IC2.getInstance().createBlock(regularElectricEngine, ItemBlockEngine.class);
        IC2.getInstance().createBlock(slowElectricEngine, ItemBlockEngine.class);
    }

    public static void initModels(){
        slowElectricEngine.initModel();
        regularElectricEngine.initModel();
        fastElectricEngine.initModel();
        adjustableElectricEngine.initModel();
        mjReader.registerModel();
    }
}
