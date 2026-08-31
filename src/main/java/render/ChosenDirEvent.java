package render;

import nv.core.events.NvEvent;

public record ChosenDirEvent(
        String path
) implements NvEvent {}
