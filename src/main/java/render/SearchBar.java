package render;

import nv.utils.NvTextField;
import scan.BedrockNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SearchBar extends NvTextField {
    public SearchBar(int x, int y, int w, int h) {
        super(x, y, w, h,  new Color(255,255,255), new Color(0,0,0));
    }
    public List<BedrockNode> search(BedrockNode root){
        List<BedrockNode> result = new ArrayList<>();
        var txt = getText().toLowerCase();
        if(txt.isBlank()) return result;
        InfoDisplay.forEachFile(root, node -> {
            if(node.name.isBlank() || !node.name.toLowerCase().contains(txt)) return;
            result.add(node);
        });
        return result;
    }

}
