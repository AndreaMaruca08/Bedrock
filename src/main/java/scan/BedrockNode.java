package scan;

import java.util.ArrayList;
import java.util.List;

public class BedrockNode {
    public String name;
    public long ownSize;
    public long totalSize;
    public boolean isDirectory;
    public BedrockNode parent;
    public List<BedrockNode> children;
    public long lastModified;

    BedrockNode(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.children = isDirectory ? new ArrayList<>() : null;
    }
}