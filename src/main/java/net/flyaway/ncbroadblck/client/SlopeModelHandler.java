package net.flyaway.ncbroadblck.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.flyaway.ncbroadblck.block.entity.ShoulderAsphaltSlashBlockEntity;
import net.flyaway.ncbroadblck.block.entity.ShoulderAsphaltSlashSlabBlockEntity;
import net.flyaway.ncbroadblck.block.entity.ShoulderMarbleSlashBlockEntity;
import net.flyaway.ncbroadblck.block.entity.ShoulderMarbleSlashSlabBlockEntity;
import net.flyaway.ncbroadblck.init.NcbRoadblckModBlocks;

import java.util.Map;

@EventBusSubscriber(modid = "ncb_roadblck", value = Dist.CLIENT)
public class SlopeModelHandler {

    private static final String MOD_ID = "ncb_roadblck";

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColor paverColor = (state, level, pos, tintIndex) -> {
            if (level == null || pos == null) return -1;
            BlockEntity be = level.getBlockEntity(pos);
            BlockState belowState = null;
            if (be instanceof ShoulderMarbleSlashBlockEntity ssbe) {
                belowState = ssbe.getBelowState();
            } else if (be instanceof ShoulderMarbleSlashSlabBlockEntity ssbe) {
                belowState = ssbe.getBelowState();
            } else if (be instanceof ShoulderAsphaltSlashBlockEntity ssbe) {
                belowState = ssbe.getBelowState();
            } else if (be instanceof ShoulderAsphaltSlashSlabBlockEntity ssbe) {
                belowState = ssbe.getBelowState();
            }
            if (belowState != null && !belowState.isAir()) {
                return Minecraft.getInstance().getBlockColors().getColor(belowState, level, pos.below(), tintIndex);
            }
            return -1;
        };

        event.register(paverColor,
                NcbRoadblckModBlocks.SHOULDER_MARBLE_SLASH.get(),
                NcbRoadblckModBlocks.SHOULDER_MARBLE_SLASH_SLAB.get(),
                NcbRoadblckModBlocks.SHOULDER_ASPHALT_SLASH.get(),
                NcbRoadblckModBlocks.SHOULDER_ASPHALT_SLASH_SLAB.get()
        );
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        // ===================== 第一轮：稳定收集杆模型（显式指定变体） =====================
        BakedModel poleModel = null;
        BakedModel smallpoleModel = null;

        for (Map.Entry<ModelResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            ModelResourceLocation key = entry.getKey();
            if (!MOD_ID.equals(key.id().getNamespace())) continue;

            String path = key.id().getPath();
            String variant = key.variant();
            if (path.startsWith("block/")) {
                path = path.substring(6);
            }

            // 大杆：优先使用中间段模型（connection=both = pole_1），这是最通用的
            if (poleModel == null && path.equals("traffic_signal_pole")) {
                if (variant.equals("connection=both,low=false")) {
                    poleModel = entry.getValue();
                }
            }
            // 小杆：优先使用堆叠模型（pole_type=stacked = smallpole_1）
            if (smallpoleModel == null && path.equals("traffic_signal_smallpole")) {
                if (variant.equals("pole_type=stacked")) {
                    smallpoleModel = entry.getValue();
                }
            }
        }

        // 备用收集：如果优先变体未找到，fallback 到其他变体
        if (poleModel == null || smallpoleModel == null) {
            for (Map.Entry<ModelResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
                ModelResourceLocation key = entry.getKey();
                if (!MOD_ID.equals(key.id().getNamespace())) continue;

                String path = key.id().getPath();
                String variant = key.variant();
                if (path.startsWith("block/")) {
                    path = path.substring(6);
                }

                if (poleModel == null && path.equals("traffic_signal_pole")) {
                    // fallback 顺序：down(底段) -> none(基础) -> 任意
                    if (variant.equals("connection=down,low=false") 
                        || variant.equals("connection=none,low=false")
                        || variant.isEmpty()) {
                        poleModel = entry.getValue();
                    }
                }
                if (smallpoleModel == null && path.equals("traffic_signal_smallpole")) {
                    // fallback 顺序：single(单根) -> low(低位) -> 任意
                    if (variant.equals("pole_type=single") 
                        || variant.equals("pole_type=low")
                        || variant.isEmpty()) {
                        smallpoleModel = entry.getValue();
                    }
                }
            }
        }

        // ===================== 第二轮：处理所有模型（其他逻辑保持现状） =====================
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            ModelResourceLocation key = entry.getKey();
            if (!MOD_ID.equals(key.id().getNamespace())) {
                continue;
            }

            String path = key.id().getPath();
            if (path.startsWith("block/")) {
                path = path.substring(6);
            }

            BakedModel original = entry.getValue();
            String variant = key.variant();

            // ========== Shoulder Slash 统一处理（保持现状） ==========
            if (path.startsWith("shoulder_") && path.contains("_slash")) {
                BakedModel wrapped = new ShoulderSlashBakedModel(original);
                entry.setValue(new SlopeBakedModel(wrapped, SlopeBakedModel.Axis.Y, -45f, 0.5f, 0.5f, 0.5f));
                continue;
            }

            // ========== Slope 后缀匹配（顺序不可变，保持现状） ==========
            if (path.endsWith("_slope_pro_4")) {
                applySlopePro(entry, variant, 0.75f);
            } else if (path.endsWith("_slope_pro_3")) {
                applySlopePro(entry, variant, 0.5f);
            } else if (path.endsWith("_slope_pro_2")) {
                applySlopePro(entry, variant, 0.25f);
            } else if (path.endsWith("_slope_pro")) {
                applySlopePro(entry, variant, 0f);
            } else if (path.endsWith("_slope_2")) {
                applySlope(entry, variant, 0.5f);
            } else if (path.endsWith("_slope")) {
                applySlope(entry, variant, 0f);
            }

            // ========== ROADSIGN：像 slope 一样直接替换（核心修改） ==========
            // blockstate 已直接引用 roadsign_XXX_1 / _2，entry.getValue() 就是路牌模型
            if (path.startsWith("roadsign_") && !path.endsWith("_1") && !path.endsWith("_2")) {
                if (variant.contains("base=pole") && poleModel != null) {
                    entry.setValue(new RoadsignBakedModel(original, poleModel));
                    continue;
                } else if (variant.contains("base=smallpole") && smallpoleModel != null) {
                    entry.setValue(new RoadsignBakedModel(original, smallpoleModel));
                    continue;
                }
            }
        }
    }

    private static void applySlope(Map.Entry<ModelResourceLocation, BakedModel> entry, String variant, float offsetY) {
        applySlopeInternal(entry, variant, offsetY, 26.56f);
    }

    private static void applySlopePro(Map.Entry<ModelResourceLocation, BakedModel> entry, String variant, float offsetY) {
        applySlopeInternal(entry, variant, offsetY, 14.037f);
    }

    private static void applySlopeInternal(Map.Entry<ModelResourceLocation, BakedModel> entry, String variant,
                                           float offsetY, float angle) {
        SlopeBakedModel.Axis axis;
        float signedAngle;

        if (variant.contains("facing=north")) {
            axis = SlopeBakedModel.Axis.X;
            signedAngle = -angle;
        } else if (variant.contains("facing=south")) {
            axis = SlopeBakedModel.Axis.X;
            signedAngle = angle;
        } else if (variant.contains("facing=east")) {
            axis = SlopeBakedModel.Axis.Z;
            signedAngle = -angle;
        } else if (variant.contains("facing=west")) {
            axis = SlopeBakedModel.Axis.Z;
            signedAngle = angle;
        } else {
            axis = SlopeBakedModel.Axis.X;
            signedAngle = 0f;
        }

        entry.setValue(new SlopeBakedModel(entry.getValue(), axis, signedAngle,
                0.5f, 0.0f, 0.5f, 0f, offsetY, 0f));
    }
}