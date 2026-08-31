package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import nv.core.io.Clickable;
import scan.BedrockNode;

import java.awt.*;
import java.util.List;

import static nv.core.errors.NvLogger.logInfo;

public class BedrockRenderer extends NvComp implements Clickable {
    private final TopInfo info;
    private final TopInfo clickedInfo;
    private List<Rect> rects;
    private double viewPortW;
    private double viewPortH;

    private static final double MIN_RECT_SIZE_PX = 2;

    public BedrockRenderer(BedrockNode node) {
        var ctx = NvContext.getInstance();
        super(0,0,ctx.getRenderWidth(), ctx.getRenderHeight());
        info = new TopInfo(10,10);
        clickedInfo = new TopInfo(10,100);
        clickedInfo.setTextScale(0.5f);
        clickedInfo.changeText(formatNode(node));
        info.changeText("Currently in: "+node.name);
        addChild(clickedInfo);
        addChild(info);
        rects = new TreemapLayout().layout(node, getX(), getY(), getW(), getH());
        logInfo(rects.size());
        viewPortW = ctx.getRenderWidth();
        viewPortH = ctx.getRenderHeight();
    }

    public void reset(BedrockNode node){
        var ctx = NvContext.getInstance();
        info.changeText(node.name);
        rects = new TreemapLayout().layout(node, getX(), getY(), getW(), getH());
        viewPortW = ctx.getRenderWidth();
        viewPortH = ctx.getRenderHeight();
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.drawRect(0,0,getW(),getH(),0,0,0);

        for(Rect rect : rects) {
            if(isInRendering(rect, viewPortW, viewPortH)){
                int hue = getOrComputeHue(rect.node());
                Color c = Color.getHSBColor(hue / 360f, 0.6f, 0.65f);
                float r = c.getRed() / 255f;
                float gr = c.getGreen() / 255f;
                float b = c.getBlue() / 255f;
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

        double camRight  = camX + viewW;
        double camBottom = camY + viewH;

        double rectLeft   = rect.x();
        double rectRight  = rect.x() + rect.w();
        double rectTop    = rect.y();
        double rectBottom = rect.y() + rect.h();

        return rectLeft < camRight && rectRight > camX &&
                rectTop < camBottom && rectBottom > camY;
    }

    private String formatNode(BedrockNode node){
        return "|"+(node.isDirectory ? "DIR" : "FILE")+"|Name: " + node.name + " | ByteSize: " + node.totalSize +
                (node.children == null ? "" : " | Inner files/dir: " + node.children.size());
    }

    private int getOrComputeHue(BedrockNode node) {
        if (node.cachedHue >= 0) return node.cachedHue;
        int parentHue = node.parent != null ? getOrComputeHue(node.parent) : 0;
        int combined = 31 * parentHue + node.name.hashCode();
        node.cachedHue = Math.abs(combined) % 360;
        return node.cachedHue;
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void onClick(int x, int y) {
        var transX = NvGraphic.camera.x + x / NvGraphic.camera.zoom;
        var transY = NvGraphic.camera.y + y / NvGraphic.camera.zoom;
        boolean found = false;
        for(Rect r : rects){
            if(transX >= r.x() && transX <= r.x() + r.w() &&
               transY >= r.y() && transY <= r.y() + r.h()){
                clickedInfo.changeText(formatNode(r.node()));
                NvContext.markSceneDirty();

            }
        }
    }

    @Override
    public void onClickRelease(int x, int y) {

    }
}
