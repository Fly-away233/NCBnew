package net.flyaway.ncbroadblck.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.flyaway.ncbroadblck.block.entity.SlashModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShoulderSlashBakedModel implements BakedModel {
    private final BakedModel base;

    public ShoulderSlashBakedModel(BakedModel base) {
        this.base = base;
    }

    private boolean isPaversTexture(TextureAtlasSprite sprite) {
        // TODO: 如果 asphalt 系列贴图名不同，在这里加 || 条件
        return sprite != null && sprite.contents().name().toString().contains("pavers_square_red");
        // 如果编译报错 sprite.contents().name()，请改成 sprite.getName().toString().contains(...)
    }

    private BakedQuad replaceQuadUV(BakedQuad quad, TextureAtlasSprite belowSprite, int tintIndex) {
        TextureAtlasSprite paverSprite = quad.getSprite();
        if (paverSprite == null) {
            return new BakedQuad(
                    quad.getVertices().clone(),
                    tintIndex,
                    quad.getDirection(),
                    belowSprite,
                    quad.isShade(),
                    quad.hasAmbientOcclusion()
            );
        }

        int[] vertices = quad.getVertices().clone();
        float pu0 = paverSprite.getU0();
        float pu1 = paverSprite.getU1();
        float pv0 = paverSprite.getV0();
        float pv1 = paverSprite.getV1();
        float bu0 = belowSprite.getU0();
        float bu1 = belowSprite.getU1();
        float bv0 = belowSprite.getV0();
        float bv1 = belowSprite.getV1();

        float puRange = pu1 - pu0;
        float pvRange = pv1 - pv0;
        float buRange = bu1 - bu0;
        float bvRange = bv1 - bv0;

        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            float normU = (u - pu0) / puRange;
            float normV = (v - pv0) / pvRange;
            float newU = bu0 + normU * buRange;
            float newV = bv0 + normV * bvRange;
            vertices[offset + 4] = Float.floatToIntBits(newU);
            vertices[offset + 5] = Float.floatToIntBits(newV);
        }

        return new BakedQuad(
                vertices,
                tintIndex,
                quad.getDirection(),
                belowSprite,
                quad.isShade(),
                quad.hasAmbientOcclusion()
        );
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                    ModelData extraData, @Nullable net.minecraft.client.renderer.RenderType renderType) {
        List<BakedQuad> baseQuads = base.getQuads(state, side, rand, extraData, renderType);

        Boolean hasSolid = extraData.get(SlashModelData.HAS_SOLID_BELOW);
        if (!Boolean.TRUE.equals(hasSolid)) {
            return filterPavers(baseQuads);
        }

        BlockState belowState = extraData.get(SlashModelData.BELOW_STATE);
        if (belowState == null || belowState.isAir()) {
            return filterPavers(baseQuads);
        }

        BakedModel belowModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(belowState);
        List<BakedQuad> belowQuads = belowModel.getQuads(belowState, Direction.UP, rand, ModelData.EMPTY, renderType);
        if (belowQuads.isEmpty()) {
            return filterPavers(baseQuads);
        }

        TextureAtlasSprite belowSprite = belowQuads.get(0).getSprite();
        if (belowSprite == null) {
            return filterPavers(baseQuads);
        }
        int belowTintIndex = belowQuads.get(0).getTintIndex();

        List<BakedQuad> result = new ArrayList<>(baseQuads.size());
        for (BakedQuad quad : baseQuads) {
            if (isPaversTexture(quad.getSprite())) {
                result.add(replaceQuadUV(quad, belowSprite, belowTintIndex));
            } else {
                result.add(quad);
            }
        }
        return result;
    }

    private List<BakedQuad> filterPavers(List<BakedQuad> quads) {
        List<BakedQuad> result = new ArrayList<>();
        for (BakedQuad quad : quads) {
            if (!isPaversTexture(quad.getSprite())) {
                result.add(quad);
            }
        }
        return result;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override public boolean useAmbientOcclusion() { return base.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return base.isGui3d(); }
    @Override public boolean usesBlockLight() { return base.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return base.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return base.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return base.getOverrides(); }
}