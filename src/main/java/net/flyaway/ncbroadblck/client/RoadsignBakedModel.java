package net.flyaway.ncbroadblck.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.flyaway.ncbroadblck.block.RoadsignBlock;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoadsignBakedModel implements BakedModel {

    private final BakedModel delegate;   // 路牌模型（roadsign_XXX_1 / _2）
    private final BakedModel poleModel;  // 杆模型（来自 blockstate 烘焙）

    public RoadsignBakedModel(BakedModel delegate, BakedModel poleModel) {
        this.delegate = delegate;
        this.poleModel = poleModel;
    }

    private static BakedQuad rotateY(BakedQuad quad, float cos, float sin) {
        int[] vertexData = quad.getVertices().clone();
        float cx = 0.5f, cz = 0.5f;

        for (int i = 0; i < 4; i++) {
            int idx = i * 8;
            float x = Float.intBitsToFloat(vertexData[idx]);
            float y = Float.intBitsToFloat(vertexData[idx + 1]);
            float z = Float.intBitsToFloat(vertexData[idx + 2]);

            x -= cx;
            z -= cz;

            float nx = x * cos - z * sin;
            float nz = x * sin + z * cos;
            x = nx + cx;
            z = nz + cz;

            vertexData[idx] = Float.floatToRawIntBits(x);
            vertexData[idx + 2] = Float.floatToRawIntBits(z);

            int normal = vertexData[idx + 7];
            if (normal != 0) {
                float nxf = ((byte) (normal & 0xFF)) / 127.0f;
                float nyf = ((byte) ((normal >> 8) & 0xFF)) / 127.0f;
                float nzf = ((byte) ((normal >> 16) & 0xFF)) / 127.0f;

                float nnx = nxf * cos - nzf * sin;
                float nnz = nxf * sin + nzf * cos;

                int newNormal = ((byte) (nnx * 127) & 0xFF)
                        | (((byte) (nyf * 127) & 0xFF) << 8)
                        | (((byte) (nnz * 127) & 0xFF) << 16);
                vertexData[idx + 7] = newNormal;
            }
        }

        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(),
                quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();

        // 1. 先渲染杆模型（传 state 而不是 null，避免某些模型内部 NPE）
        if (poleModel != null) {
            quads.addAll(poleModel.getQuads(state, side, rand, ModelData.EMPTY, renderType));
        }

        // 2. 再渲染路牌模型（根据 ROTATION 做 16 向 Y 轴旋转）
        List<BakedQuad> signQuads = delegate.getQuads(state, side, rand, extraData, renderType);
        if (!signQuads.isEmpty() && state != null && state.hasProperty(RoadsignBlock.ROTATION)) {
            int rotation = state.getValue(RoadsignBlock.ROTATION);
            if (rotation != 0) {
                float angle = -rotation * 22.5f;
                float rad = (float) Math.toRadians(angle);
                float cos = (float) Math.cos(rad);
                float sin = (float) Math.sin(rad);
                for (BakedQuad quad : signQuads) {
                    quads.add(rotateY(quad, cos, sin));
                }
                return quads;
            }
        }
        quads.addAll(signQuads);
        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        Set<RenderType> set = new HashSet<>();
        for (RenderType rt : delegate.getRenderTypes(state, rand, data)) set.add(rt);
        if (poleModel != null) {
            for (RenderType rt : poleModel.getRenderTypes(state, rand, ModelData.EMPTY)) set.add(rt);
        }
        RenderType[] arr = set.toArray(new RenderType[0]);
        if (arr.length == 0) return ChunkRenderTypeSet.none();
        return ChunkRenderTypeSet.of(arr);
    }

    @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return delegate.isGui3d(); }
    @Override public boolean usesBlockLight() { return delegate.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return delegate.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return delegate.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return delegate.getOverrides(); }
}