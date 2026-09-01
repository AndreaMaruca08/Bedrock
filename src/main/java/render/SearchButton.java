package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import nv.core.io.Clickable;
import scan.BedrockNode;

public class SearchButton extends NvComp implements Clickable {
    private SearchBar searchBar;
    private BedrockNode root;

    public SearchButton(float x, float y, float w, float h, SearchBar searchBar, BedrockNode root) {
        super(x, y, w, h);
        this.searchBar = searchBar;
        this.root = root;
    }

    public void setRoot(BedrockNode root) {
        this.root = root;
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.drawRect(0,0,getW(),getH(), 1,1,1);
        g.drawText("Search", 0,0);
    }

    @Override
    public void update(float dt) {}

    @Override
    public void onClick(int x, int y) {
        NvContext.getInstance().events().emit(SearchEvent.class, new SearchEvent(searchBar.getText(), root, searchBar.search(root)));
        markDirty();
    }

    @Override
    public void onClickRelease(int x, int y) {}
}
