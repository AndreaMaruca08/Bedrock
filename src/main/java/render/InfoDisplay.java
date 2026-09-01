package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import scan.BedrockNode;
import scan.FileCategory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InfoDisplay extends NvComp {
    private String title;
    private int maxFileNameLength;
    private float rectW;

    private BedrockNode largestFile;
    private BedrockNode smallestFile;

    private float W;
    private float H;

    private final Map<String, FileCategory> fileCategories = new HashMap<>(20);
    private List<FileCategory> sortedCategories;

    private List<BedrockNode> sortedChildren;

    public InfoDisplay(float x, float y, float w, float h, BedrockNode root) {
        super(x, y, w, h+10000);
        reset(root);
        var ctx = NvContext.getInstance();
        W = ctx.getRenderWidth()*0.015f;
        H = ctx.getRenderHeight()*0.03f;
    }

    public void reset(BedrockNode newRoot) {
        fileCategories.clear();
        title = "Dir info: " + newRoot.name;
        var max = title.length();
        for (BedrockNode child : newRoot.children) {
            if (child.name.length() > max) max = child.name.length();
        }

        sortedChildren = new ArrayList<>(newRoot.children);
        sortedChildren.sort((a, b) -> Long.compare(b.totalSize, a.totalSize));
        maxFileNameLength = max;
        rectW = max * NvContext.getInstance().getRenderWidth()*0.015f;

        largestFile = null;
        smallestFile = null;
        forEachFile(newRoot,
        (child) -> {
            if (largestFile == null || child.ownSize > largestFile.ownSize) largestFile = child;
            if (smallestFile == null || child.ownSize < smallestFile.ownSize) smallestFile = child;
        },
        (child) -> {
            var extension = child.name.substring(child.name.lastIndexOf('.') + 1);
            fileCategories.computeIfAbsent(extension, (ext) -> new FileCategory(ext, 0, 0));
            var cat = fileCategories.get(extension);
            cat.count++;
            cat.totalSize += child.ownSize;
        });
        sortedCategories = new ArrayList<>(fileCategories.values());
        sortedCategories.sort((a, b) -> Long.compare(b.totalSize, a.totalSize));
    }

    @SafeVarargs
    public static void forEachFile(BedrockNode node, Consumer<BedrockNode>... consumer) {
        for (BedrockNode child : node.children) {
            if (child.isDirectory) {
                forEachFile(child, consumer);
            } else {
                for(Consumer<BedrockNode> c : consumer) c.accept(child);
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
            g.drawText(child.name + " ".repeat(maxFileNameLength + 2 - child.name.length()) +BedrockRenderer.formatByte(child.totalSize), 10, y += H);
        }
        if(largestFile == null || smallestFile == null)
            return;
        y+=100;
        g.drawText("Largest file: " + largestFile.name + " " + BedrockRenderer.formatByte(largestFile.totalSize), 10, y += 70);
        y += 70;
        g.drawText("Smallest file: " + smallestFile.name + " " + BedrockRenderer.formatByte(smallestFile.totalSize), 10, y);

        y = 0;
        g.drawText("File categories by byte size: ", 10, y += H);
        for(int i = 0; i < sortedCategories.size() && i < 200; i++) {
            var sizeCategory = sortedCategories.get(i);
            int combined = 31 * sizeCategory.extension.hashCode();

            Color c = getColor(combined);
            float rC = c.getRed() / 255f;
            float grC = c.getGreen() / 255f;
            float bC = c.getBlue() / 255f;

            g.setRGB(rC, grC, bC);
            g.drawText(
                    sizeCategory.extension + " ".repeat(Math.max(5,maxFileNameLength + 2 - sizeCategory.extension.length())) + BedrockRenderer.formatByte(sizeCategory.totalSize) + " (" + sizeCategory.count + " files)",
                    rectW +300,
                    y += H
            );
        }
    }

    public Color getColor(int combined) {
        return Color.getHSBColor(combined / 360f, 0.6f, 0.65f);
    }

    @Override
    public void update(float dt) {}
}
