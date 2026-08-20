package net.flyaway.ncbroadblck.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;

public class Asphalt3ColorLineSlopeProBlock extends AsphaltRoadSlopeProBlock {
    public Asphalt3ColorLineSlopeProBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .sound(SoundType.BASALT)
                .strength(1.5f, 10f)
                .requiresCorrectToolForDrops());
    }
}