package layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import plg.PropertiesNode;
import plg.PropertiesNode.PropertiesNodeType;

public class LayoutConfig {

    public static final String LC_PLAYER_BGCOLOR = "player.bgcolor";
    public static final String LC_PLAYER_BDCOLOR = "player.bdcolor";
    public static final String LC_PLAYER_COLOR_RULE = "player.colorrule";
    public static final String LC_PLAYER_BORDER_VERTICAL_VISIBLE = "player.vborder.visible";
    public static final String LC_PLAYER_BORDER_HORIZON_VISIBLE = "player.hborder.visible";
    public static final String LC_CURSOR_TYPE = "cursor.type";
    public static final String LC_CURSOR_COLOR = "cursor.color";
    public static final String LC_CURSOR_EFFE_COLOR = "cursor.effect.color";
    public static final String LC_CURSOR_EFFE_VISIBLE = "cursor.effect.visible";
    public static final String LC_CURSOR_POS = "cursor.position";
    public static final String LC_PB_COLOR = "pb.basecolor";
    public static final String LC_PB_VISIBLE = "pb.visible";
    public static final String LC_NOTES_DESIGN = "notes.design";
    public static final String LC_NOTES_COLOR_ASIGN = "notes.colasign";
    public static final String LC_NOTES_COLOR = "notes.color";
    public static final String LC_NOTES_COLOR_BORDER_RGB = "notes.border.colorRGB";
    public static final String LC_INFO_VISIBLE = "info.visible";

    public static enum ENotesDesign {
        Normal, Flat, Arc;

        @Override
        public String toString() {
            switch (this) {
                case Normal:
                    return "normal";
                case Flat:
                    return "flat";
                case Arc:
                    return "arc";
                default:
                    return "";
            }
        }
    }

    private static Object[] ENotesDesignO = { ENotesDesign.Normal, ENotesDesign.Flat, ENotesDesign.Arc };
    private static String[] ENotesDesignS = { ENotesDesign.Normal.toString(), ENotesDesign.Flat.toString(), ENotesDesign.Arc.toString() };

    public static enum ECursorType {
        Keyboard, Line;

        @Override
        public String toString() {
            switch (this) {
                case Keyboard:
                    return "keyboard";
                case Line:
                    return "line";
                default:
                    return "";
            }
        }
    }

    private static Object[] ECursorTypeO = { ECursorType.Keyboard, ECursorType.Line };
    private static String[] ECursorTypeS = { ECursorType.Keyboard.toString(), ECursorType.Line.toString() };

    public static enum EColorRule {
        Channel, Track;

        @Override
        public String toString() {
            switch (this) {
                case Channel:
                    return "channel";
                case Track:
                    return "track";
                default:
                    return "";
            }
        }
    }

    private static Object[] EColorRuleO = { EColorRule.Channel, EColorRule.Track };
    private static String[] EColorRuleS = { EColorRule.Channel.toString(), EColorRule.Track.toString() };

    public static enum EColorAsign {
        Inherit, Asign, None;

        @Override
        public String toString() {
            switch (this) {
                case Inherit:
                    return "inherit";
                case Asign:
                    return "asign";
                case None:
                    return "none";
                default:
                    return "";
            }
        }
    }

    private static Object[] EColorAsignO = { EColorAsign.Inherit, EColorAsign.Asign, EColorAsign.None };
    private static String[] EColorAsignS = { EColorAsign.Inherit.toString(), EColorAsign.Asign.toString(), EColorAsign.None.toString() };

    private static Object[] CursorPosO = { -1 };
    private static String[] CursorPosS = { "top" };

    private List<PropertiesNode> nodes;
    public List<String> notesColorCodes;

    public LayoutConfig() {
        nodes = new ArrayList<>();
        nodes.add(new PropertiesNode(LC_PLAYER_BGCOLOR, PropertiesNodeType.COLOR, "#111111"));
        nodes.add(new PropertiesNode(LC_PLAYER_BDCOLOR, PropertiesNodeType.COLOR, "#202020"));
        nodes.add(new PropertiesNode(LC_PLAYER_COLOR_RULE, PropertiesNodeType.ITEM, EColorRule.Track, EColorRuleS, EColorRuleO));
        nodes.add(new PropertiesNode(LC_PLAYER_BORDER_VERTICAL_VISIBLE, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(LC_PLAYER_BORDER_HORIZON_VISIBLE, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(LC_CURSOR_TYPE, PropertiesNodeType.ITEM, ECursorType.Keyboard, ECursorTypeS, ECursorTypeO));
        nodes.add(new PropertiesNode(LC_CURSOR_COLOR, PropertiesNodeType.COLOR, "#9400d3"));
        nodes.add(new PropertiesNode(LC_CURSOR_EFFE_COLOR, PropertiesNodeType.COLOR, "#FFFFFF"));
        nodes.add(new PropertiesNode(LC_CURSOR_EFFE_VISIBLE, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(LC_CURSOR_POS, PropertiesNodeType.INT, "-1", "", "", CursorPosS, CursorPosO));
        nodes.add(new PropertiesNode(LC_PB_COLOR, PropertiesNodeType.COLOR, "#969696"));
        nodes.add(new PropertiesNode(LC_PB_VISIBLE, PropertiesNodeType.BOOLEAN, "false"));
        nodes.add(new PropertiesNode(LC_NOTES_DESIGN, PropertiesNodeType.ITEM, ENotesDesign.Normal, ENotesDesignS, ENotesDesignO));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR_ASIGN, PropertiesNodeType.ITEM, EColorAsign.Asign, EColorAsignS, EColorAsignO));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR, PropertiesNodeType.COLOR, "#00ff00"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR_BORDER_RGB, PropertiesNodeType.DOUBLE, "1.5", "0.1", "2.0"));
        nodes.add(new PropertiesNode(LC_INFO_VISIBLE, PropertiesNodeType.BOOLEAN, "true"));

        notesColorCodes = new ArrayList<String>();

        // ロードする文字列
        String propertiesString = "";
        Properties props = new Properties();
        try (StringReader reader = new StringReader(propertiesString)) {
            props.load(reader);
            read(props);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    private Object getPropObject(Properties props, String pkey, String nkey) {
        String str = props.getProperty(pkey);
        if (str == null) {
            return null;
        }
        PropertiesNode node = getPropNode(nkey);
        return node.getObject(str);
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
        read(props);
    }

    public void read(Properties props) throws FileNotFoundException, IOException {
        for (PropertiesNode nd : nodes) {
            setPropObject(props, nd.getKey());
        }

        // 以下、ネイティブ変数に分ける
        notesColorCodes.clear();
        StringBuilder sb = new StringBuilder(64); // 初期容量を指定
        for (int i = 1; i <= 512; i++) {
            sb.setLength(0);
            sb.append(LC_NOTES_COLOR).append(i);
            String str = (String) getPropObject(props, sb.toString(), LC_NOTES_COLOR);
            if (str != null) {
                notesColorCodes.add(str);
            }
        }
    }
}
