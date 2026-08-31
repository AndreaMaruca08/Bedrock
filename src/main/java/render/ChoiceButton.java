package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;
import nv.core.io.Clickable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.swing.*;

public class ChoiceButton extends NvComp implements Clickable {
    public ChoiceButton(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.drawRect(0,0,getW(),getH(), 1,1,1);
        g.setRGB(0,0,0);
        g.drawText("Choose a directory to scan", 0,0);
    }

    @Override
    public void update(float dt) {}

    @Override
    public void onClick(int x, int y) {
        String path = TinyFileDialogs.tinyfd_selectFolderDialog("Seleziona cartella", null);
        NvContext.getInstance().events().emit(ChosenDirEvent.class, new ChosenDirEvent(path));
    }

    @Override
    public void onClickRelease(int x, int y) {

    }
}
