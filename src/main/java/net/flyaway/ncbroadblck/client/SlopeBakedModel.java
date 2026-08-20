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
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlopeBakedModel implements BakedModel {
    private final BakedModel original;
    private final Axis axis;
    private final float angle;
    private final float centerX, centerY, centerZ;
    private final float offsetX, offsetY, offsetZ;

    public enum Axis { X, Y, Z }

    public SlopeBakedModel(BakedModel original, Axis axis, float angle,
                           float centerX, float centerY, float centerZ,
                           float offsetX, float offsetY, float offsetZ) {
        this.original = original;
        this.axis = axis;
        this.angle = angle;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public SlopeBakedModel(BakedModel original, Axis axis, float angle,
                           float centerX, float centerY, float centerZ) {
        this(original, axis, angle, centerX, centerY, centerZ, 0f, 0f, 0f);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        List<BakedQuad> originalQuads = original.getQuads(state, direction, random);
        if (originalQuads.isEmpty()) return originalQuads;

        List<BakedQuad> transformed = new ArrayList<>(originalQuads.size());
        for (BakedQuad quad : originalQuads) {
            transformed.add(transformQuad(quad));
        }
        return transformed;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction,
                                    RandomSource random, ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> originalQuads = original.getQuads(state, direction, random, extraData, renderType);
        if (originalQuads.isEmpty()) return originalQuads;

        List<BakedQuad> transformed = new ArrayList<>(originalQuads.size());
        for (BakedQuad quad : originalQuads) {
            transformed.add(transformQuad(quad));
        }
        return transformed;
    }

    private BakedQuad transformQuad(BakedQuad quad) {
        int[] vertexData = quad.getVertices().clone();
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        for (int i = 0; i < 4; i++) {
            int idx = i * 8;
            float x = Float.intBitsToFloat(vertexData[idx]);
            float y = Float.intBitsToFloat(vertexData[idx + 1]);
            float z = Float.intBitsToFloat(vertexData[idx + 2]);

            x -= centerX;
            y -= centerY;
            z -= centerZ;

            float nx = x, ny = y, nz = z;
            switch (axis) {
                case X -> {
                    ny = y * cos - z * sin;
                    nz = y * sin + z * cos;
                }
                case Y -> {
                    nx = x * cos - z * sin;
                    nz = x * sin + z * cos;
                }
                case Z -> {
                    nx = x * cos - y * sin;
                    ny = x * sin + y * cos;
                }
            }
            x = nx; y = ny; z = nz;

            x += centerX + offsetX;
            y += centerY + offsetY;
            z += centerZ + offsetZ;

            vertexData[idx] = Float.floatToRawIntBits(x);
            vertexData[idx + 1] = Float.floatToRawIntBits(y);
            vertexData[idx + 2] = Float.floatToRawIntBits(z);

            int normal = vertexData[idx + 7];
            if (normal != 0) {
                float nxf = ((byte) (normal & 0xFF)) / 127.0f;
                float nyf = ((byte) ((normal >> 8) & 0xFF)) / 127.0f;
                float nzf = ((byte) ((normal >> 16) & 0xFF)) / 127.0f;

                float nnx = nxf, nny = nyf, nnz = nzf;
                switch (axis) {
                    case X -> {
                        nny = nyf * cos - nzf * sin;
                        nnz = nyf * sin + nzf * cos;
                    }
                    case Y -> {
                        nnx = nxf * cos - nzf * sin;
                        nnz = nxf * sin + nzf * cos;
                    }
                    case Z -> {
                        nnx = nxf * cos - nyf * sin;
                        nny = nxf * sin + nyf * cos;
                    }
                }

                int newNormal = ((byte) (nnx * 127) & 0xFF)
                        | (((byte) (nny * 127) & 0xFF) << 8)
                        | (((byte) (nnz * 127) & 0xFF) << 16);
                vertexData[idx + 7] = newNormal;
            }
        }

        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(),
                quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());
    }

    @Override public boolean useAmbientOcclusion() { return original.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return original.isGui3d(); }
    @Override public boolean usesBlockLight() { return original.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return original.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return original.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return original.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return original.getOverrides(); }
}