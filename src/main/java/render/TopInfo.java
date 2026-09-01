package render;

import nv.core.NvContext;
import nv.core.components.NvComp;
import nv.core.graphic.NvGraphic;

public class TopInfo extends NvComp {
    private String text;
    private float textScale = 1;
    private float multiplier;
    private int len = 0;
    public TopInfo(float x, float y) {
        super(x,y,300,50);
        setHUD(true);
        multiplier = NvContext.getInstance().getRenderWidth()*0.01f;
    }

    public void changeText(String newText){
        this.text = newText;
        this.len = newText.length();
    }

    public void setTextScale(float scale){
        this.textScale = scale;
    }

    @Override
    public void drawIntern(NvGraphic g) {
        g.setRGB(1,1,1);
        g.drawRoundRect(8,8,textScale*multiplier*len - 8, textScale*multiplier*3 - 8, 20);
        g.setRGB(0,0,0);
        g.drawText(text, multiplier, multiplier/2, textScale);
    }

    @Override
    public void update(float dt) {

    }
}
