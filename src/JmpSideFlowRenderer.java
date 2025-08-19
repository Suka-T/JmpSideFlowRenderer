

import gui.JsfrRendererWindow;
import plg.AbstractRenderPlugin;
import plg.SystemProperties;

public class JmpSideFlowRenderer extends AbstractRenderPlugin {

    public static void main(String[] args) {
        System.out.println("JmpSideFlowRenderer");
    }

    public JmpSideFlowRenderer() {
        super();
    }
    
    @Override
    protected void createMainWindow() {
        MainWindow = new JsfrRendererWindow(
                SystemProperties.getInstance().getWindowWidth(), 
                SystemProperties.getInstance().getWindowHeight());
        MainWindow.init();
    }

    @Override
    public void initialize() {
        super.initialize();

        AbstractRenderPlugin.MainWindow.setTitle("JSFR");
    }
}
