package plg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import plg.PropertiesNode.PropertiesNodeType;

public class SystemProperties {
    public static final String SYSP_LAYOUT = "layout";
    public static final String SYSP_DEBUGMODE = "debugMode";
    public static final String SYSP_RENDERER_WORKNUM = "renderer.workerNum";
    public static final String SYSP_RENDERER_FPS = "renderer.fps";
    public static final String SYSP_RENDERER_LAYERORDER = "renderer.layerOrder";
    public static final String SYSP_RENDERER_KEYWIDTH = "renderer.keyWidth";
    public static final String SYSP_RENDERER_NOTESSPEED = "renderer.notesSpeed";
    public static final String SYSP_RENDERER_NOTESIMAGENUM = "renderer.notesImageNum";

    public static int DEFAULT_KEY_WIDTH = 50;

    public static enum SyspLayerOrder {
        ASC, DESC;

        @Override
        public String toString() {
            switch (this) {
                case ASC:
                    return "asc";
                case DESC:
                    return "desc";
                default:
                    return "";
            }
        }
    }

    private static Object[] layerOrderItemO = { SyspLayerOrder.ASC, SyspLayerOrder.DESC };
    private static String[] layerOrderItemS = { SyspLayerOrder.ASC.toString(), SyspLayerOrder.DESC.toString() };

    private static Object[] NotesSpeedItemO = { -1 };
    private static String[] NotesSpeedItemS = { "auto" };

    private static Object[] NotesCountItemO = { -1 };
    private static String[] NotesCountItemS = { "auto" };

    private List<PropertiesNode> nodes;
    private boolean notesWidthAuto = true;
    private int notesWidth = 420;

    private static SystemProperties instance = new SystemProperties();

    private SystemProperties() {
        nodes = new ArrayList<>();

        nodes.add(new PropertiesNode(SYSP_LAYOUT, PropertiesNodeType.STRING, ""));
        nodes.add(new PropertiesNode(SYSP_DEBUGMODE, PropertiesNodeType.BOOLEAN, "false"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_WORKNUM, PropertiesNodeType.INT, "3", "3", "20"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_FPS, PropertiesNodeType.INT, "60", "20", ""));
        nodes.add(new PropertiesNode(SYSP_RENDERER_LAYERORDER, PropertiesNodeType.ITEM, SyspLayerOrder.DESC, layerOrderItemS, layerOrderItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_KEYWIDTH, PropertiesNodeType.INT, "420", "", ""));
        nodes.add(new PropertiesNode(SYSP_RENDERER_NOTESSPEED, PropertiesNodeType.INT, "-1", "1", "100", NotesSpeedItemS, NotesSpeedItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_NOTESIMAGENUM, PropertiesNodeType.INT, "60", "3", "100", NotesCountItemS, NotesCountItemO));
    }

    public static SystemProperties getInstance() {
        return instance;
    }

    private PropertiesNode getPropNode(String key) {
        for (PropertiesNode nd : nodes) {
            if (nd.getKey().equalsIgnoreCase(key)) {
                return nd;
            }
        }
        return null;
    }

    private void setPropObject(Properties props, String key) {
        String str = props.getProperty(key);
        PropertiesNode node = getPropNode(key);
        node.setObject(str);
    }
    
    public Object getData(String key) {
        PropertiesNode node = getPropNode(key);
        if (node == null) {
            return null;
        }
        return node.getData();
    }

    public void read(File file) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        for (PropertiesNode nd : nodes) {
            setPropObject(props, nd.getKey());
        }
        
        // 以下、ネイティブ変数に分ける 
        int notesSpeed = (int) getData(SYSP_RENDERER_NOTESSPEED);
        boolean notesSpeedIsAuto = true;
        if (notesSpeed == -1) {
            notesSpeedIsAuto = true;
            notesSpeed = 50;
        }
        notesWidthAuto = notesSpeedIsAuto;
        notesWidth = 160 + (int) ((double) (1200 - 160) * ((double) notesSpeed / 100.0));
        if (notesWidth < 160) {
            notesWidth = 160;
        }
        else if (1200 < notesWidth) {
            notesWidth = 1200;
        }
    }

    public int getWorkerNum() {
        return (int) getPropNode(SYSP_RENDERER_WORKNUM).getData();
    }

    public String getLayoutFile() {
        return (String) getPropNode(SYSP_LAYOUT).getData();
    }

    public boolean isDebugMode() {
        return (boolean) getPropNode(SYSP_DEBUGMODE).getData();
    }

    public int getFixedFps() {
        return (int) getPropNode(SYSP_RENDERER_FPS).getData();
    }

    public SyspLayerOrder getLayerOrder() {
        return (SyspLayerOrder) getPropNode(SYSP_RENDERER_LAYERORDER).getData();
    }

    public int getKeyWidth() {
        return SystemProperties.DEFAULT_KEY_WIDTH;
    }

    public int getNotesWidth() {
        return notesWidth;
    }

    public boolean isNotesWidthAuto() {
        return notesWidthAuto;
    }

    public int getNotesImageCount() {
        return (int) getPropNode(SYSP_RENDERER_NOTESIMAGENUM).getData();
    }
}
