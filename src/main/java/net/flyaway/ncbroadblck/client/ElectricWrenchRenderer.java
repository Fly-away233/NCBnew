package net.flyaway.ncbroadblck.client;

import net.flyaway.ncbroadblck.item.ElectricWrenchItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ElectricWrenchRenderer extends GeoItemRenderer<ElectricWrenchItem> {

    public ElectricWrenchRenderer() {
        super(new ElectricWrenchModel());
    }

    // 不需要任何 override，GeoItemRenderer 会自动处理所有 3D 视角
}