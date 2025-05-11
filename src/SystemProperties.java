import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SystemProperties {
	
	public static final String SYSP_IMAGEWORKER_COUNT = "renderer.worker.count";
	public static final String SYSP_LAYOUT_FILE = "layout.file";
	
	private int workerNum = 3;
	private String layoutFile = "";

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
}
