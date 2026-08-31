package render;

import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import scan.BedrockNode;

import java.util.ArrayList;
import java.util.List;

public class InfoDisplay extends NvComp {
    private BedrockNode root;
    private String title;
    private int maxFileNameLength;
    private float rectW;

    private BedrockNode largestFile;
    private BedrockNode smallestFile;

    private List<BedrockNode> sortedChildren;

    public InfoDisplay(float x, float y, float w, float h, BedrockNode root) {
        super(x, y, w, h);
        reset(root);
    }

    public void reset(BedrockNode newRoot) {
        this.root = newRoot;
        title = "Dir info: " + root.name;
        var max = title.length();
        for (BedrockNode child : root.children) {
            if (child.name.length() > max) max = child.name.length();
        }

        sortedChildren = new ArrayList<>(root.children);
        sortedChildren.sort((a, b) -> Long.compare(b.totalSize, a.totalSize));
        maxFileNameLength = max;
        rectW = max * 35;

        largestFile = null;
        smallestFile = null;
        findExtreme(root);
    }

    private void findExtreme(BedrockNode node) {
        for (BedrockNode child : node.children) {
            if (child.isDirectory) {
                findExtreme(child);
            } else {
                if (largestFile == null || child.ownSize > largestFile.ownSize) largestFile = child;
                if (smallestFile == null || child.ownSize < smallestFile.ownSize) smallestFile = child;
            }
        }
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.setRGB(1,1,1);
        g.drawText(title, 10, 10);
        g.drawLine(0, 70, rectW, 70, 5,1,1,1);
        float y = 50;
        for(BedrockNode child : sortedChildren) {
            g.drawText(child.name + " ".repeat(maxFileNameLength + 2 - child.name.length()) +BedrockRenderer.formatByte(child.totalSize), 10, y += 70);
        }
        y+=100;
        g.drawText("Largest file: " + largestFile.name + " " + BedrockRenderer.formatByte(largestFile.totalSize), 10, y += 70);
        y += 70;
        g.drawText("Smallest file: " + smallestFile.name + " " + BedrockRenderer.formatByte(smallestFile.totalSize), 10, y);
    }

    @Override
    public void update(float dt) {

    }
}
