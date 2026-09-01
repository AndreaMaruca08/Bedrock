package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import nv.core.io.Clickable;
import scan.BedrockNode;

import java.awt.*;
import java.util.List;

public class BedrockRenderer extends NvComp implements Clickable {
    private final TopInfo info;
    private final TopInfo clickedInfo;
    private final SearchBar searchBar;
    private final SearchButton searchButton;
    private List<Rect> rects;
    private double viewPortW;
    private double viewPortH;

    private BedrockNode root;
    private static final double MIN_RECT_SIZE_PX = 2;

    public BedrockRenderer(BedrockNode node) {
        var ctx = NvContext.getInstance();
        super(0,0,ctx.getRenderWidth(), ctx.getRenderHeight());
        info = new TopInfo(10,10);
        root = node;
        var halfW = ctx.getRenderWidth()*0.5f;
        clickedInfo = new TopInfo(10,ctx.getRenderHeight()*0.06f);
        clickedInfo.setTextScale(0.6f);
        clickedInfo.changeText(formatNode(node, root));
        info.changeText("Currently in: "+node.name + " | " + formatByte(node.totalSize));
        searchBar = new SearchBar((int) (halfW/1.5f), -120, (int)(halfW/2), 70);
        searchButton = new SearchButton(searchBar.getW()+searchBar.getX()+50, -120, halfW/6, 70, searchBar, root);
        var researchRender = new SearchRender(0, -50000,4000, 49700);
        addChild(searchBar);
        addChild(searchButton);
        addChild(researchRender);
        addChild(clickedInfo);
        addChild(info);
        rects = new TreemapLayout().layout(node, getX(), getY(), getW(), getH());
        viewPortW = ctx.getRenderWidth();
        viewPortH = ctx.getRenderHeight();
    }

    public void reset(BedrockNode node){
        var ctx = NvContext.getInstance();
        info.changeText("Currently in: "+node.name + " | " + formatByte(node.totalSize));
        rects = new TreemapLayout().layout(node, getX(), getY(), getW(), getH());
        viewPortW = ctx.getRenderWidth();
        viewPortH = ctx.getRenderHeight();
        root = node;
        searchButton.setRoot(node);
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

    public static String formatNode(BedrockNode node, BedrockNode root){
        return "|"+(node.isDirectory ? "DIR" : "FILE")+"|Name: " + node.name + " | ByteSize: " + formatByte(node.totalSize) +
                ((node.children == null ? "" : " | Inner files/dir: " + node.children.size())
                        + String.format(" | Ratio to %s directory: %f%%", root.name,((float)node.totalSize / (float)root.totalSize)*100f));
    }
    public static String formatByte(long byteSize) {
        if (byteSize < 1024) {
            return byteSize + " B";
        }

        String[] units = {"KB", "MB", "GB", "TB", "PB"};
        double size = byteSize;
        int unitIndex = -1;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    public static int getOrComputeHue(BedrockNode node) {
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
        for(Rect r : rects){
            if(transX >= r.x() && transX <= r.x() + r.w() &&
               transY >= r.y() && transY <= r.y() + r.h()){
                clickedInfo.changeText(formatNode(r.node(), root));
                NvContext.markSceneDirty();
            }
        }
    }

    @Override
    public void onClickRelease(int x, int y) {

    }
}
