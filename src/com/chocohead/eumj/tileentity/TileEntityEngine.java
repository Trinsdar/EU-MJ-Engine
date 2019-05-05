package com.chocohead.eumj.tileentity;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.data.ModelVariableData;
import com.chocohead.eumj.util.IEngine;
import com.chocohead.eumj.util.VeryOrderedEnumMap;
import ic2.api.classic.item.IMachineUpgradeItem;
import ic2.api.classic.tile.IMachine;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.core.IC2;
import ic2.core.block.base.tile.TileEntityElecMachine;
import ic2.core.inventory.base.IHasGui;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TileEntityEngine extends TileEntityElecMachine implements IHasGui, IEngine, ITickable, IMachine{
    public enum Progress {
        START, IN, OUT;

        public boolean isMoving() {
            return this != START;
        }

        static final com.chocohead.eumj.tileentity.TileEntityEngine.Progress[] VALUES = values();
    }

    private static final Map<EnumFacing, List<AxisAlignedBB>> AABBs = makeAABBmap();

    protected final IMjConnector mjConnector = createConnector();
    protected final MjCapabilityHelper mjCaps = new MjCapabilityHelper(mjConnector);

    //@SideOnly(Side.CLIENT) //Servers get sad with this due to assigning it during init
    protected final ModelVariableData modelData = new ModelVariableData();


    protected double heat = TileEngineBase_BC8.MIN_HEAT;
    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;

    public boolean redstoneInverted;

    protected long power;
    protected com.chocohead.eumj.tileentity.TileEntityEngine.Progress movement = com.chocohead.eumj.tileentity.TileEntityEngine.Progress.START;
    protected float progress;
    @SideOnly(Side.CLIENT)
    protected float lastProgress;
    public TileEntityEngine(int slots, int maxinput) {
        super(slots, maxinput);
        addNetworkFields("soundLevel", "redstoneInverted", "redstoneSensitive");
    }

    private static Map<EnumFacing, List<AxisAlignedBB>> makeAABBmap() {
        Map<EnumFacing, List<AxisAlignedBB>> out = new EnumMap<>(EnumFacing.class);

        out.put(EnumFacing.DOWN,  Arrays.asList(new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0), new AxisAlignedBB(0.25, 0.0,  0.25, 0.75, 0.5,  0.75)));
        out.put(EnumFacing.UP,    Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), new AxisAlignedBB(0.25, 0.5,  0.25, 0.75, 1.0,  0.75)));
        out.put(EnumFacing.NORTH, Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.5, 1.0, 1.0, 1.0), new AxisAlignedBB(0.25, 0.25, 0.0,  0.75, 0.75, 0.5)));
        out.put(EnumFacing.SOUTH, Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), new AxisAlignedBB(0.25, 0.25, 0.5,  0.75, 0.75, 1.0)));
        out.put(EnumFacing.WEST,  Arrays.asList(new AxisAlignedBB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0), new AxisAlignedBB(0.0,  0.25, 0.25, 0.5,  0.75, 0.75)));
        out.put(EnumFacing.EAST,  Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), new AxisAlignedBB(0.5,  0.25, 0.25, 1.0,  0.75, 0.75)));

        return out;
    }

    protected abstract IMjConnector createConnector();

    @Override
    public void update() {
        if (this.getWorld().isRemote) {
            this.updateEntityClient();
        } else {
            this.updateEntityServer();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setDouble("heat", heat);
        nbt.setLong("power", power);
        nbt.setFloat("progress", progress);
        nbt.setByte("movement", (byte) movement.ordinal());

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        heat = nbt.getDouble("heat");
        power = nbt.getLong("power");
        progress = nbt.getFloat("progress");
        movement = com.chocohead.eumj.tileentity.TileEntityEngine.Progress.VALUES[nbt.getByte("movement") % com.chocohead.eumj.tileentity.TileEntityEngine.Progress.VALUES.length];
    }

    @Override
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
        return side == getFacing().getOpposite();
    }

    @Override
    public boolean isRedstonePowered() {
        return this.redstoneInverted != super.isRedstonePowered();
    }

    public boolean isFacingMJ(EnumFacing dir) {
        TileEntity neighbour = world.getTileEntity(pos.offset(dir));
        if (neighbour == null) return false;

        IMjConnector other = neighbour.getCapability(MjAPI.CAP_CONNECTOR, dir.getOpposite());
        if (other == null) return false;

        return mjConnector.canConnect(other) && other.canConnect(mjConnector);
    }

    protected EnumFacing spin(EnumFacing start) {
        assert start != null; //Don't pass null, it's not cool.

        for (EnumFacing facing : VeryOrderedEnumMap.loopFrom(VanillaRotationHandlers.ROTATE_FACING, start)) {
            if (isFacingMJ(facing)) return facing;
        }

        return null;
    }

    public boolean trySpin(EnumFacing start) {
        EnumFacing spun = spin(start);

        if (spun != null && getFacing() != spun) {
            setFacing(spun);

            return true;
        } else
            return false;
    }


    public EnumFacing getPlacementFacing(EntityLivingBase placer) {
		/*EnumFacing natural = super.getPlacementFacing(placer, placerFacing).getOpposite();
		if (isFacingMJ(natural)) return natural;

		for (EnumFacing facing : getSupportedFacings().stream().filter(facing -> facing != natural).collect(Collectors.toSet())) {
			if (isFacingMJ(natural)) return facing;
		}*/

        EnumFacing spun = spin(super.getFacing().getOpposite());
        return spun != null ? spun : EnumFacing.UP;
    }



    @Override
    public List<String> getNetworkedFields() {
        List<String> out = super.getNetworkedFields();

        out.add("progress");
        out.add("powerStage");

        return out;
    }
    // << NBT + Loading

    // Engine Logic >>
    // Power >>>
    protected double getPowerLevel() {
        return power / (double) getMaxPower();
    }

    protected abstract long getMaxPower();
    // <<< Power
    // Heat >>>
    protected double getHeatLevel() {
        return (heat - TileEngineBase_BC8.MIN_HEAT) / (TileEngineBase_BC8.MAX_HEAT - TileEngineBase_BC8.MIN_HEAT);
    }

    protected void updateHeatLevel() {
        heat = (TileEngineBase_BC8.MAX_HEAT - TileEngineBase_BC8.MIN_HEAT) * getPowerLevel() + TileEngineBase_BC8.MIN_HEAT;
    }
    // <<< Heat
    // Power Stage + Speed >>>
    protected EnumPowerStage computePowerStage() {
        double heatLevel = getHeatLevel();

        if (heatLevel < 0.25F) return EnumPowerStage.BLUE;
        else if (heatLevel < 0.5F) return EnumPowerStage.GREEN;
        else if (heatLevel < 0.75F) return EnumPowerStage.YELLOW;
        else if (heatLevel < 0.85F) return EnumPowerStage.RED;
        else return EnumPowerStage.OVERHEAT;
    }

    @Override
    public EnumPowerStage getPowerStage() {
        if (!world.isRemote) {
            EnumPowerStage newStage = computePowerStage();

            if (powerStage != newStage) {
                powerStage = newStage;
                IC2.network.get(true).updateTileEntityField(this, "powerStage");
            }
        }

        return powerStage;
    }

    protected double getPistonSpeed() {
        if (!world.isRemote)
            return Math.max(0.16 * getHeatLevel(), 0.01);
        else {
            switch (getPowerStage()) {
                case BLUE:
                    return 0.02;
                case GREEN:
                    return 0.04;
                case YELLOW:
                    return 0.08;
                case RED:
                    return 0.16;
                default:
                    return 0;
            }
        }
    }
    // <<< Power Stage + Speed
    // Engine Activity >>>
    protected void engineUpdate() {
        if (!isRedstonePowered() && power > 0) {
            power = Math.max(power - 1, 0);
        }
    }

    protected boolean canMove() {
        return isRedstonePowered();
    }
    // <<< Engine Activity
    // Energy >>>
    public abstract long maxPowerExtracted();

    public IMjReceiver getReceiverToPower(TileEntity tile, EnumFacing side) {
        if (tile == null) return null;

        IMjReceiver rec = tile.getCapability(MjAPI.CAP_RECEIVER, side.getOpposite());
        return rec != null && rec.canConnect(mjConnector) ? rec : null;
    }

    public void addPower(long microJoules) {
        power = Math.min(power + microJoules, getMaxPower());

        if (getPowerStage() == EnumPowerStage.OVERHEAT) {
            // TODO: turn engine off
        }
    }

    public long extractPower(long min, long max, boolean doExtract) {
        if (power < min)
            return 0;

        long actualMax = Math.min(maxPowerExtracted(), max);
        if (actualMax < min)
            return 0;

        long extracted;
        if (power >= actualMax) {
            extracted = actualMax;

            if (doExtract) {
                power -= actualMax;
            }
        } else {
            extracted = power;

            if (doExtract) {
                power = 0;
            }
        }

        return extracted;
    }

    protected long getPowerToExtract(boolean doExtract) {
        TileEntity tile = world.getTileEntity(pos.offset(getFacing()));
        if (tile == null) return 0;

        if (tile.getClass() == getClass()) {
            com.chocohead.eumj.tileentity.TileEntityEngine other = (com.chocohead.eumj.tileentity.TileEntityEngine) tile;
            return other.getMaxPower() - power;
        } else {
            IMjReceiver receiver = getReceiverToPower(tile, getFacing());
            if (receiver == null)
                return 0;

            return extractPower(0, receiver.getPowerRequested(), doExtract);
            //return extractPower(receiver.getMinPowerReceived(), receiver.getMaxPowerReceived(), false); //TODO: This one
        }
    }

    protected void sendPower() {
        TileEntity tile = world.getTileEntity(pos.offset(getFacing()));
        if (tile == null)
            return;

        if (getClass() == tile.getClass()) {
            com.chocohead.eumj.tileentity.TileEntityEngine other = (com.chocohead.eumj.tileentity.TileEntityEngine) tile;

            if (getFacing() == other.getFacing()) {
                other.power += extractPower(0, power, true);
            }
        } else {
            IMjReceiver receiver = getReceiverToPower(tile, getFacing());

            if (receiver != null) {
                long extracted = getPowerToExtract(true);

                if (extracted > 0) {
                    long excess = receiver.receivePower(extracted, false);
                    extractPower(extracted - excess, extracted - excess, true);
                }
            }
        }
    }

    // <<< Energy
    // Updating >>>
    protected void updateEntityServer() {

        if (!isRedstonePowered() && power > 0) {
            power = Math.max(power - MjAPI.MJ, 0);
        }

        updateHeatLevel();
        boolean overheat = getPowerStage() == EnumPowerStage.OVERHEAT;
        engineUpdate();

        if (movement.isMoving()) {
            progress += getPistonSpeed();

            if (progress > 0.5 && movement == com.chocohead.eumj.tileentity.TileEntityEngine.Progress.IN) {
                movement = com.chocohead.eumj.tileentity.TileEntityEngine.Progress.OUT;
                sendPower();
            } else if (progress >= 1) {
                progress = 0;
                movement = com.chocohead.eumj.tileentity.TileEntityEngine.Progress.START;
            }
        } else if (canMove() && getPowerToExtract(false) > 0) {
            movement = com.chocohead.eumj.tileentity.TileEntityEngine.Progress.IN;
            setActive(true);
        } else {
            setActive(false);
        }

        if (!overheat) {
            burn();
        }
    }

    protected abstract void burn();
    // <<< Updating
    // << Engine Logic

    // Capabilities >>
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (facing == getFacing()) return mjCaps.hasCapability(capability, facing) ||
                capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && super.hasCapability(capability, facing);
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (facing == getFacing()) {
            T out = mjCaps.getCapability(capability, facing);
            if (out != null) return out;
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return null;
        }

        return super.getCapability(capability, facing);
    }
    // << Capabilities

    // Client >>
    @Override
    @SideOnly(Side.CLIENT)
    public ModelVariableData getModelData() {
        return modelData;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long getPower() {
        return power;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getHeat() {
        return heat;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getProgressClient(float partialTicks) {
        float last = lastProgress;
        float now = progress;

        if (last > 0.5 && now < 0.5) {
            now += 1;
        }

        return (last * (1 - partialTicks) + now * partialTicks) % 1;
    }

    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {

        lastProgress = progress;

        if (getActive()) {
            progress += getPistonSpeed();

            if (progress >= 1) {
                progress = 0;
            }
        } else if (progress > 0) {
            progress -= 0.01f;
        }

        modelData.tick();
    }

    //Not actually only client side, but affects rendering
    protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
        return AABBs.get(getFacing());
    }



    @Override
    public boolean canRenderBreaking() {
        return true; //Without this, trying to break an engine will cause a rendering crash
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public boolean supportsNotify() {
        return true;
    }

    @Override
    public long getActiveOutput() {
        return 0;
    }

    @Override
    public void onLoaded() {
        super.onLoaded();
        if (isSimulating()) {
            setOverclockRates();
        }
    }

    public void setOverclockRates() {
        int extraTier = 0;
        boolean redstonePowered = false;
        for (int i = 0; i < 2; i++) {
            ItemStack item = inventory.get(i + inventory.size() - 4);
            if (item.getItem() instanceof IMachineUpgradeItem) {
                IMachineUpgradeItem upgrade = (IMachineUpgradeItem) item.getItem();
                upgrade.onInstalling(item, this);
                extraTier += upgrade.getExtraTier(item, this) * item.getCount();
                if (upgrade.useRedstoneInverter(item, this)) {
                    redstonePowered = true;
                }
            }
        }
        redstoneInverted = redstonePowered;
        tier = baseTier + extraTier;
        if (tier > 13) {
            tier = 13;
        }
        maxInput = (int) EnergyNet.instance.getPowerFromTier(tier);
        if (energy > maxEnergy) {
            energy = maxEnergy;
        }
        getNetwork().updateTileEntityField(this, "redstoneInverted");
        getNetwork().updateTileGuiField(this, "maxInput");
        getNetwork().updateTileGuiField(this, "energy");
    }

    @Override
    public void onGuiClosed(EntityPlayer entityPlayer) {

    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public boolean hasGui(EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public double getWrenchDropRate() {
        return 1.0D;
    }
}
