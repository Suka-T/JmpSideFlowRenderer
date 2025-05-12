import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.SwingUtilities;

import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.player.IPlayerListener;
import jlib.plugin.ISupportExtensionConstraints;
import jlib.plugin.JMidiPlugin;

public class AbstractRenderPlugin extends JMidiPlugin implements IPlayerListener, ISupportExtensionConstraints {

    public static String Extensions = "";
    public static RendererWindow MainWindow = null;

    public AbstractRenderPlugin() {
    }

    @Override
    public void initialize() {
        createExtensions();
        
        Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_DATA_DIR, this));
        Path fullPath = folder.resolve("renderer.properties");
        try {
        	SystemProperties.getInstance().read(new File(fullPath.toString()));
        	
        	String layoutFilename = SystemProperties.getInstance().getLayoutFile();
            if (!layoutFilename.contains(".")) {
            	layoutFilename += ".layout";
            }
        	fullPath = folder.resolve(layoutFilename);
            LayoutManager.getInstance().read(new File(fullPath.toString()));
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        
        if (SwingUtilities.isEventDispatchThread()) {
            MainWindow = new RendererWindow();
            MainWindow.init();
        }
        else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
    
                    @Override
                    public void run() {
                        MainWindow = new RendererWindow();
                        MainWindow.init();
                    }
                });
            }
            catch (InvocationTargetException | InterruptedException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            }
        }
    }

    private void createExtensions() {
        String exMidi = JMPCoreAccessor.getSystemManager().getCommonRegisterValue("extension_midi");
        String exMXML = JMPCoreAccessor.getSystemManager().getCommonRegisterValue("extension_musicxml");
        Extensions = exMidi + "," + exMXML;
    }

    @Override
    public boolean isEnable() {
        return super.isEnable();
    }

    @Override
    public void exit() {
    }

    @Override
    public void open() {
        MainWindow.setVisible(true);
        MainWindow.adjustTickBar();
    }

    @Override
    public void close() {
        MainWindow.setVisible(false);
    }

    @Override
    public boolean isOpen() {
        return MainWindow.isVisible();
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    protected void noteOn(int channel, int midiNumber, int velocity, long timeStamp, short senderType) {
    }

    @Override
    protected void noteOff(int channel, int midiNumber, long timeStamp, short senderType) {
    }

    @Override
    protected void programChange(int channel, int programNumber, long timeStamp, short senderType) {
    }

    @Override
    protected void pitchBend(int channel, int pbValue, long timeStamp, short senderType) {
    }

    @Override
    public void loadFile(File file) {
        super.loadFile(file);
        MainWindow.loadFile();
    }

    @Override
    public void startSequencer() {
        //MainWindow.adjustTickBar();
    }

    @Override
    public void stopSequencer() {
    }

    @Override
    public void updateTickPosition(long before, long after) {
        if (before != after) {
            MainWindow.adjustTickBar();
        }
    }

    @Override
    public String allowedExtensions() {
        return Extensions;
    }

}
