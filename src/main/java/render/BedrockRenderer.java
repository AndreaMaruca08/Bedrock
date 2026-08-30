package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import scan.BedrockNode;

public class BedrockRenderer extends NvComp {
    private final TopInfo info;
    private final BedrockNode node;
    public BedrockRenderer(BedrockNode node) {
        var ctx = NvContext.getInstance();
        super(0,0,ctx.getRenderWidth(), ctx.getRenderHeight());
        info = new TopInfo();
        this.node = node;
        info.changeText(node.name);
        addChild(info);
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.drawRect(0,0,getW(),getH(),1,1,1);
    }

    @Override
    public void update(float dt) {

    }
}
