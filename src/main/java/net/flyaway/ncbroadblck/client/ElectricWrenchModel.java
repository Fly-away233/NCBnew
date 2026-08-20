package net.flyaway.ncbroadblck.client;

import net.flyaway.ncbroadblck.item.ElectricWrenchItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ElectricWrenchModel extends GeoModel<ElectricWrenchItem> {

    @Override
    public ResourceLocation getModelResource(ElectricWrenchItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "geo/item/electric_wrench.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ElectricWrenchItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "textures/item/electric_wrench.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ElectricWrenchItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("ncb_roadblck", "animations/item/electric_wrench.animation.json");
    }
}