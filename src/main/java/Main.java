import nv.core.ContextBuilder;
import nv.core.NvContext;
import nv.utils.camera.NvControlledCamera;
import render.BedrockRenderer;
import scan.AsyncScanner;
import scan.BedrockNode;

import java.awt.*;

import static nv.core.errors.NvLogger.logInfo;

void main() {
    // builds the game
    NvContext context = new ContextBuilder("START", true)
            .setVsync(true)
            .setIdleWhenUnfocused(true)
            .build();

    context.changeFont(new Font("monospaced", Font.PLAIN, 50));
    var page = context.newPage();
    page.setBackgroundColor(0,0,0);

    AsyncScanner scanner = new AsyncScanner(32);
    try {
        BedrockNode node = scanner.scan(Path.of("/Users/andreamaruca/Desktop/progetti/java/Bedrock"));
        var renderer = new BedrockRenderer(node);
        page.addChild(renderer);
        new NvControlledCamera(context.getRenderWidth()/2,context.getRenderHeight()/2,2000);
    }catch (Exception e) {
        scanner.shutdown();
    }

    context.run();
    scanner.shutdown();
}