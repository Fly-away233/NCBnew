package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RoadsignNoRightTurnBlock extends RoadsignBlock {

    private static final VoxelShape POLE_SHAPE = Block.box(3, 0, 3, 13, 16, 13);
    private static final VoxelShape SMALLPOLE_SHAPE = Block.box(5, 0, 5, 11, 16, 11);

    public RoadsignNoRightTurnBlock() {
        super(BlockBehaviour.Properties.of()
            .sound(SoundType.COPPER)
            .strength(2f, 10f)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .isRedstoneConductor((bs, br, bp) -> false));
    }

    @Override
    protected VoxelShape getPoleShape() {
        return POLE_SHAPE;
    }

    @Override
    protected VoxelShape getSmallpoleShape() {
        return SMALLPOLE_SHAPE;
    }
}