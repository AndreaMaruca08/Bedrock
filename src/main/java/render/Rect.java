package render;

import scan.BedrockNode;

public record Rect(
        float x,
        float y,
        float w,
        float h,
        BedrockNode node
) {}
