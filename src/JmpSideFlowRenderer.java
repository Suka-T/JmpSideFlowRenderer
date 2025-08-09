

import plg.AbstractRenderPlugin;

public class JmpSideFlowRenderer extends AbstractRenderPlugin {

    public static void main(String[] args) {
        System.out.println("JmpSideFlowRenderer");
    }

    public JmpSideFlowRenderer() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();

        AbstractRenderPlugin.MainWindow.setTitle("JSFR");
    }
}
