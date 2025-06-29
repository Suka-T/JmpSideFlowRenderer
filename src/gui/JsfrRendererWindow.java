package gui;

import java.awt.Graphics;

public class JsfrRendererWindow extends RendererWindow {

    public JsfrRendererWindow() {
        super();
        this.setTitle("JMP Side Flow Renderer");
    }

    @Override
    protected void makeKeyboardRsrc() {
        super.makeKeyboardRsrc();
    }

    @Override
    protected String getTopString() {
        return super.getTopString();
    }

    @Override
    protected void copyFromNotesImage(Graphics g) {
        super.copyFromNotesImage(g);
    }

    @Override
    protected int getEffectWidth(int dir) {
        return super.getEffectWidth(dir);
    }
}
