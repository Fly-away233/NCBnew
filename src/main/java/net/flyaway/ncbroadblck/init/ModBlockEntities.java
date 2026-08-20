package net.flyaway.ncbroadblck.init;

import net.flyaway.ncbroadblck.NcbRoadblckMod;
import net.flyaway.ncbroadblck.block.entity.ShoulderAsphaltSlashBlockEntity;
import net.flyaway.ncbroadblck.block.entity.ShoulderAsphaltSlashSlabBlockEntity;
import net.flyaway.ncbroadblck.block.entity.ShoulderMarbleSlashBlockEntity;
import net.flyaway.ncbroadblck.block.entity.ShoulderMarbleSlashSlabBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, NcbRoadblckMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShoulderMarbleSlashBlockEntity>> SHOULDER_MARBLE_SLASH =
            BLOCK_ENTITIES.register("shoulder_marble_slash", () ->
                    BlockEntityType.Builder.of(ShoulderMarbleSlashBlockEntity::new,
                            NcbRoadblckModBlocks.SHOULDER_MARBLE_SLASH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShoulderMarbleSlashSlabBlockEntity>> SHOULDER_MARBLE_SLASH_SLAB =
            BLOCK_ENTITIES.register("shoulder_marble_slash_slab", () ->
                    BlockEntityType.Builder.of(ShoulderMarbleSlashSlabBlockEntity::new,
                            NcbRoadblckModBlocks.SHOULDER_MARBLE_SLASH_SLAB.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShoulderAsphaltSlashBlockEntity>> SHOULDER_ASPHALT_SLASH =
            BLOCK_ENTITIES.register("shoulder_asphalt_slash", () ->
                    BlockEntityType.Builder.of(ShoulderAsphaltSlashBlockEntity::new,
                            NcbRoadblckModBlocks.SHOULDER_ASPHALT_SLASH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShoulderAsphaltSlashSlabBlockEntity>> SHOULDER_ASPHALT_SLASH_SLAB =
            BLOCK_ENTITIES.register("shoulder_asphalt_slash_slab", () ->
                    BlockEntityType.Builder.of(ShoulderAsphaltSlashSlabBlockEntity::new,
                            NcbRoadblckModBlocks.SHOULDER_ASPHALT_SLASH_SLAB.get()).build(null));
}