package render;

import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;

public class TopInfo extends NvComp {
    private String text;
    private int len = 0;
    public TopInfo() {
        super(10,10,300,50);
        setHUD(true);
    }

    public void changeText(String newText){
        this.text = newText;
        this.len = newText.length();
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.setRGB(0, 0, 0);
        g.drawRoundRect(0,0,33*len, 80, 20);
        g.setRGB(1,1,1);
        g.drawRoundRect(8,8,33*len - 8, 80 - 8, 20);
        g.setRGB(0,0,0);
        g.drawText(text, 10,10);
    }

    @Override
    public void update(float dt) {

    }
}
