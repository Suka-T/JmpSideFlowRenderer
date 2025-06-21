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
	public static final String SYSP_LAYER_ORDER = "renderer.layer.order";
	public static final String SYSP_LAYOUT_FILE = "layout.file";
	public static final String SYSP_KEYWIDTH = "renderer.key.width";
	public static final String SYSP_NOTES_SPEED = "renderer.notes.speed";
	
	public static int DEFAULT_KEY_WIDTH = 50;
	
	public static enum SyspLayerOrder {
		ASC,
		DESC;
		
        @Override
        public String toString() {
            switch (this) {
                case ASC: return "asc";
                case DESC: return "desc";
                default: return "";
            }
        }
	}
	
	private int workerNum = 3;
	private int fixedFps = 60;
	private String layoutFile = "";
	private SyspLayerOrder layerOrder = SyspLayerOrder.DESC;
	private int keyWidth = DEFAULT_KEY_WIDTH;
	private int notesWidth = 420;
	private boolean notesWidthAuto = false;
	
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
        		workerNum = 10;
			}
        	
            if (workerNum < 3) {
            	workerNum = 3;
            }
            else if (20 < workerNum) {
            	workerNum = 20;
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
        
        str = props.getProperty(SYSP_LAYER_ORDER);
        if (str != null) {
        	if (str.equals(SyspLayerOrder.ASC.toString())) {
        		layerOrder = SyspLayerOrder.ASC;
        	}
        	else {
        		layerOrder = SyspLayerOrder.DESC;
        	}
        }
        
        boolean notesSpeedIsAuto = true;
        double notesSpeed = 50.0;
        str = props.getProperty(SYSP_NOTES_SPEED);
        if (str == null) {
        }
        else if (str.equalsIgnoreCase("auto") == true) {
        	notesSpeedIsAuto = true;
        }
        else {
	        try {
	        	notesSpeedIsAuto = false;
	        	notesSpeed = Double.parseDouble(str);
	        	if (notesSpeed < 1.0) {
	        		notesSpeed = 1.0;
	        	}
	        	else if (notesSpeed > 100.0) {
	        		notesSpeed = 100.0;
	        	}
	        }catch (Exception e) {
	        	notesSpeedIsAuto = true;
	        	notesSpeed = 50.0;
			}
        }
        
        notesWidthAuto = notesSpeedIsAuto;
        notesWidth = 160 + (int) ((double)(1200 - 160) * (notesSpeed / 100.0));
        if (notesWidth < 160) {
        	notesWidth = 160;
        }
        else if (1200 < notesWidth) {
        	notesWidth = 1200;
        }
        
        keyWidth = DEFAULT_KEY_WIDTH;
        
        str = props.getProperty(SYSP_KEYWIDTH);
        if (str != null) {
        	try {
        		setKeyWidth(Integer.parseInt(str));
        	} catch (Exception e) {
        		setKeyWidth(DEFAULT_KEY_WIDTH);
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
	public SyspLayerOrder getLayerOrder() {
		return layerOrder;
	}
	public int getKeyWidth() {
		return keyWidth;
	}
	public void setKeyWidth(int keyWidth) {
		this.keyWidth = keyWidth;
	}
	public int getNotesWidth() {
		return notesWidth;
	}
	public boolean isNotesWidthAuto() {
		return notesWidthAuto;
	}
}
