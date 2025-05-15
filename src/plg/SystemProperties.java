package plg;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SystemProperties {
	
	public static final String SYSP_IMAGEWORKER_COUNT = "renderer.worker.count";
	public static final String SYSP_DEBUGMODE = "renderer.debugmode";
	public static final String SYSP_FPS = "renderer.fps";
	public static final String SYSP_LAYOUT_FILE = "layout.file";
	
	private int workerNum = 3;
	private int fixedFps = 60;
	private String layoutFile = "";
	private boolean debugMode = false;

    private static SystemProperties instance = new SystemProperties();
    private SystemProperties() {}
    public static SystemProperties getInstance() {
        return instance;
    }
	
    public void read(File file) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        
        String str = null;
        str = props.getProperty(SYSP_IMAGEWORKER_COUNT);
        if (str != null) {
        	try {
        		workerNum = Integer.parseInt(str);
        	} catch (Exception e) {
        		workerNum = 3;
			}
        }
        
        str = props.getProperty(SYSP_DEBUGMODE);
        if (str != null) {
        	try {
        		debugMode = Boolean.parseBoolean(str);
        	} catch (Exception e) {
        		debugMode = false;
			}
        }
        
        str = props.getProperty(SYSP_FPS);
        if (str != null) {
        	try {
        		fixedFps = Integer.parseInt(str);
        	} catch (Exception e) {
        		fixedFps = 60;
			}
        	
        	// 最小20まで 
        	if (fixedFps < 20) {
        		fixedFps = 20;
        	}
        }
        
        str = props.getProperty(SYSP_LAYOUT_FILE);
        if (str != null) {
        	try {
        		layoutFile = str;
        	} catch (Exception e) {
        		layoutFile = "";
			}
        }
	}

	public int getWorkerNum() {
		return workerNum;
	}

	public String getLayoutFile() {
		return layoutFile;
	}

	public boolean isDebugMode() {
		return debugMode;
	}
	
	public int getFixedFps() {
		return fixedFps;
	}
}
