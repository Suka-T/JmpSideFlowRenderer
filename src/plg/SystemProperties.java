package plg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static final String SYSP_RENDERER_DIMENSION = "renderer.dimension";
    public static final String SYSP_RENDERER_WINSIZE = "renderer.windowSize";

    public static final Map<String, String> SwapKeyName = new HashMap<String, String>() {
        {
            put(SYSP_LAYOUT, "Preload settings file name");
            put(SYSP_DEBUGMODE, "Debug mode enable");
            put(SYSP_RENDERER_WORKNUM, "Rendering thread count [3 - 20]");
            put(SYSP_RENDERER_FPS, "Fixed frame rate");
            put(SYSP_RENDERER_LAYERORDER, "Track rendering order");
            put(SYSP_RENDERER_NOTESSPEED, "Notes Speed [1 - 100 | auto]");
            put(SYSP_RENDERER_NOTESIMAGENUM, "Rendering notes image size [3 - 100]");
            put(SYSP_RENDERER_DIMENSION, "Renderer dimension");
            put(SYSP_RENDERER_WINSIZE, "Window size");
        }
    };

    public static int DIM_CALC_FUNC = 0; // 0:W, 1:H
    public static int DEFAULT_KEY_WIDTH = 50;

    private static int DEFAULT_DIM_W = 1280;
    private static int DEFAULT_DIM_H = 768;

    public static enum SyspLayerOrder {
        ASC, DESC;
    }

    private static Object[] layerOrderItemO = { SyspLayerOrder.ASC, SyspLayerOrder.DESC };
    private static String[] layerOrderItemS = { "asc", "desc" };

    private static Object[] NotesSpeedItemO = { -1 };
    private static String[] NotesSpeedItemS = { "auto" };

    private static Object[] NotesCountItemO = { -1 };
    private static String[] NotesCountItemS = { "auto" };

    private static Object[] WinSizeItemO = { "2560*1440", "1920*1080", "1280*720", "854*480", "640*360" };
    private static Object[] WinSizeItemD = { "2560*1408", "1920*1024", "1280*768", "896*512", "640*384" };
    private static String[] WinSizeItemS = { "1440p", "1080p", "720p", "480p", "360p", };

    private List<PropertiesNode> nodes;
    private boolean notesWidthAuto = true;
    private int notesWidth = 420;
    private int keyWidth = 50;

    private int dimWidth = DEFAULT_DIM_W;
    private int dimHeight = DEFAULT_DIM_H;

    private double dimOffset = 1.0;

    private int windowWidth = 1280;
    private int windowHeight = 720;

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
        nodes.add(new PropertiesNode(SYSP_RENDERER_DIMENSION, PropertiesNodeType.ITEM, "1280×768", WinSizeItemS, WinSizeItemD));
        // nodes.add(new PropertiesNode(SYSP_RENDERER_DIMENSION,
        // PropertiesNodeType.STRING, "1920×1024"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_WINSIZE, PropertiesNodeType.ITEM, "1280×720", WinSizeItemS, WinSizeItemO));
    }

    public static SystemProperties getInstance() {
        return instance;
    }

    public List<PropertiesNode> getNodes() {
        return nodes;
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
    }

    public void iniialize() {

        // 以下、ネイティブ変数に分ける
        int notesSpeed = (int) getData(SYSP_RENDERER_NOTESSPEED);
        boolean notesSpeedIsAuto = false;
        if (notesSpeed == -1) {
            notesSpeedIsAuto = true;
            notesSpeed = 50;
        }
        notesWidthAuto = notesSpeedIsAuto;
        notesWidth = 160 + (int) ((double) (2400 - 160) * ((double) notesSpeed / 100.0));
        if (notesWidth < 160) {
            notesWidth = 160;
        }
        else if (2400 < notesWidth) {
            notesWidth = 2400;
        }

        String sDimSize = (String) getData(SYSP_RENDERER_DIMENSION);
        if (sDimSize != null && sDimSize.isBlank() == false) {
            try {
                String[] parts = sDimSize.split("[x×*,]");
                dimWidth = Integer.parseInt(parts[0].trim()); // 幅
                dimHeight = Integer.parseInt(parts[1].trim()); // 高さ
            }
            catch (Exception e) {
                dimWidth = DEFAULT_DIM_W; // 幅
                dimHeight = DEFAULT_DIM_H; // 高さ
            }

            // 128で割り切れるサイズにする
            dimWidth = dimWidth / 128 * 128;
            dimHeight = dimHeight / 128 * 128;
        }

        String sWinSize = (String) getData(SYSP_RENDERER_WINSIZE);
        if (sWinSize != null && sWinSize.isBlank() == false) {
            try {
                String[] parts = sWinSize.split("[x×*,]");
                windowWidth = Integer.parseInt(parts[0].trim()); // 幅
                windowHeight = Integer.parseInt(parts[1].trim()); // 高さ
            }
            catch (Exception e) {
                windowWidth = 1280; // 幅
                windowHeight = 720; // 高さ
            }
        }

        if (DIM_CALC_FUNC == 0) {
            dimOffset = (double) dimWidth / (double) DEFAULT_DIM_W;
        }
        else if (DIM_CALC_FUNC == 1) {
            dimOffset = (double) dimHeight / (double) DEFAULT_DIM_H;
        }
        keyWidth = (int) ((double) DEFAULT_KEY_WIDTH * dimOffset);
        notesWidth = (int) ((double) notesWidth * dimOffset);
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
        return keyWidth;
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

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getDimWidth() {
        return dimWidth;
    }

    public int getDimHeight() {
        return dimHeight;
    }

    public double getDimOffset() {
        return dimOffset;
    }
}
