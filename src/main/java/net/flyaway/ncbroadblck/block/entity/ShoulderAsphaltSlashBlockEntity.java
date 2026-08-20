package net.flyaway.ncbroadblck.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ShoulderAsphaltSlashBlockEntity extends BlockEntity {
    private BlockState belowState = null;
    private boolean hasSolidBelow = false;

    public ShoulderAsphaltSlashBlockEntity(BlockPos pos, BlockState state) {
        super(net.flyaway.ncbroadblck.init.ModBlockEntities.SHOULDER_ASPHALT_SLASH.get(), pos, state);
    }

    public ShoulderAsphaltSlashBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ShoulderAsphaltSlashBlockEntity be) {
        be.tick();
    }

    public void tick() {
        if (level == null || !level.isClientSide() || isRemoved()) return;

        BlockState currentState = level.getBlockState(getBlockPos());
        if (currentState.isAir() || !currentState.hasBlockEntity()) return;

        BlockPos belowPos = getBlockPos().below();
        BlockState newBelow = level.getBlockState(belowPos);
        boolean newSolid = !newBelow.isAir() && newBelow.canOcclude();

        if (newBelow != belowState || newSolid != hasSolidBelow) {
            belowState = newBelow;
            hasSolidBelow = newSolid;
            requestModelDataUpdate();
            level.sendBlockUpdated(getBlockPos(), currentState, currentState, 3);
        }
    }

    public BlockState getBelowState() {
        return belowState;
    }

    public boolean hasSolidBelow() {
        return hasSolidBelow;
    }

    @Override
    public ModelData getModelData() {
        ModelData.Builder builder = ModelData.builder()
                .with(SlashModelData.HAS_SOLID_BELOW, hasSolidBelow);
        if (belowState != null) {
            builder.with(SlashModelData.BELOW_STATE, belowState);
        }
        return builder.build();
    }
}