import nv.core.ContextBuilder;
import nv.core.NvContext;
import nv.utils.camera.NvControlledCamera;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import render.BedrockRenderer;
import render.ChoiceButton;
import render.ChosenDirEvent;
import scan.AsyncScanner;
import scan.BedrockNode;

import java.awt.*;

import static nv.core.errors.NvLogger.logWarn;

void main() {
    // builds the game
    NvContext context = new ContextBuilder("Bedrock", true)
            .setVsync(true)
            .setIdleWhenUnfocused(true)
            .build();

    context.changeFont(new Font("monospaced", Font.PLAIN, 50));
    var page = context.newPage();
    page.setBackgroundColor(0,0,0);

    AsyncScanner scanner = new AsyncScanner(60);
    try {
        new NvControlledCamera(context.getRenderWidth()/2,context.getRenderHeight()/2,2000);

        String path = TinyFileDialogs.tinyfd_selectFolderDialog("Seleziona cartella da scansionare", null);
        if(path == null)return;
        BedrockNode root = scanner.scan(Path.of(path));
        var renderer = new BedrockRenderer(root);
        page.addChild(renderer);

        context.events().on(ChosenDirEvent.class, (event) -> {
            if(event.path() != null) {
                try {
                    var newRoot = scanner.scan(Path.of(event.path()));
                    renderer.reset(newRoot);
                    NvContext.markSceneDirty();
                } catch (Exception e ){
                    logWarn("Error resetting: " + e.getMessage());
                }
            }
        });

        page.addChild(new ChoiceButton(0,-100,550,100));
    }catch (Exception e) {
        scanner.shutdown();
    }

    context.run();
    scanner.shutdown();
}