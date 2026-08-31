package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import scan.BedrockNode;

import java.util.List;

import static nv.core.errors.NvLogger.logInfo;

public class BedrockRenderer extends NvComp {
    private final TopInfo info;
    private final List<Rect> rects;
    private double viewPortW;
    private double viewPortH;

    private static final double MIN_RECT_SIZE_PX = 3;

    public BedrockRenderer(BedrockNode node) {
        var ctx = NvContext.getInstance();
        super(0,0,ctx.getRenderWidth(), ctx.getRenderHeight());
        info = new TopInfo();
        info.changeText(node.name);
        addChild(info);
        rects = new TreemapLayout().layout(node, getX(), getY(), getW(), getH());
        logInfo(rects.size());
        viewPortW = ctx.getRenderWidth();
        viewPortH = ctx.getRenderHeight();
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.drawRect(0,0,getW(),getH(),0,0,0);

        for(Rect rect : rects) {
            if(isInRendering(rect, viewPortW, viewPortH)){
                int hue = Math.abs(rect.node().name.hashCode()) % 360;
                float r = hue / 360.0f;
                float gr = hue/360f + 0.2f;
                float b = 0.4f;
                g.drawRect(rect.x(), rect.y(), rect.w(), rect.h(), r, gr, b);
            }
        }
    }

    private boolean isInRendering(Rect rect, double viewportWidth, double viewportHeight) {
        double camX = NvGraphic.camera.x;
        double camY = NvGraphic.camera.y;
        double zoom = NvGraphic.camera.zoom;

        double screenW = rect.w() * zoom;
        double screenH = rect.h() * zoom;
        if (screenW < MIN_RECT_SIZE_PX && screenH < MIN_RECT_SIZE_PX) {
            return false;
        }

        double viewW = viewportWidth / zoom;
        double viewH = viewportHeight / zoom;

        double camLeft   = camX;
        double camRight  = camX + viewW;
        double camTop    = camY;
        double camBottom = camY + viewH;

        double rectLeft   = rect.x();
        double rectRight  = rect.x() + rect.w();
        double rectTop    = rect.y();
        double rectBottom = rect.y() + rect.h();

        return rectLeft < camRight && rectRight > camLeft &&
                rectTop < camBottom && rectBottom > camTop;
    }
    @Override
    public void update(float dt) {

    }
}
