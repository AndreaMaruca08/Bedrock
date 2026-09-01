package render;

import nv.core.events.NvEvent;
import scan.BedrockNode;

import java.util.List;

public record SearchEvent(
        String query,
        BedrockNode root,
        List<BedrockNode> nodes
) implements NvEvent {}
