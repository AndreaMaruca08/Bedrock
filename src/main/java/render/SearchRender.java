package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import scan.BedrockNode;

import static nv.core.errors.NvLogger.logInfo;
import static render.BedrockRenderer.formatNode;

public class SearchRender extends NvComp {
    private SearchEvent searchEvent;
    public SearchRender(float x, float y, float w, float h) {
        super(x, y, w, h);
        NvContext.getInstance().events().on(SearchEvent.class, (ev) -> {
            searchEvent = ev;
        });
    }

    @Override
    public void drawIntern(NvGraphic g) {
        if(searchEvent != null){
            float y = 49700;
            var root = searchEvent.root();
            for(BedrockNode node : searchEvent.nodes()){
                g.setRGB(1,1,1);
                g.drawText(formatNode(node, root), 0, y-=50);
            }
        }
    }

    @Override
    public void update(float dt) {

    }
}
