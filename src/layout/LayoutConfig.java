package layout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LayoutConfig {
    
    public static final String LC_PLAYER_BGCOLOR = "player.bgcolor";
    public static final String LC_PLAYER_BDCOLOR = "player.bdcolor";
    public static final String LC_PLAYER_COLOR_RULE = "player.colorrule";
    public static final String LC_PLAYER_BORDER_VERTICAL_VISIBLE = "player.vborder.visible";
    public static final String LC_PLAYER_BORDER_HORIZON_VISIBLE = "player.hborder.visible";
    public static final String LC_CURSOR_TYPE = "cursor.type";
    public static final String LC_CURSOR_COLOR = "cursor.color";
    public static final String LC_CURSOR_EFFE_COLOR = "cursor.effect.color";
    public static final String LC_CURSOR_POS = "cursor.position";
    public static final String LC_PB_COLOR = "pb.basecolor";
    public static final String LC_PB_VISIBLE = "pb.visible";
    public static final String LC_NOTES_DESIGN = "notes.design";
    public static final String LC_NOTES_COLOR_ASIGN = "notes.colasign";
    public static final String LC_NOTES_COLOR = "notes.color";
    public static final String LC_INFO_VISIBLE = "info.visible";
    
    public static enum ENotesDesign {
        Normal,
        Flat,
        Arc;
        
        @Override
        public String toString() {
            switch (this) {
                case Normal: return "normal";
                case Flat: return "flat";
                case Arc: return "arc";
                default: return "";
            }
        }
    }
    
    public static enum ECursorType {
        Keyboard,
        Line;
        
        @Override
        public String toString() {
            switch (this) {
                case Keyboard: return "keyboard";
                case Line: return "line";
                default: return "";
            }
        }
    }
    
    public static enum EColorRule {
        Channel,
        Track;
        
        @Override
        public String toString() {
            switch (this) {
                case Channel: return "channel";
                case Track: return "track";
                default: return "";
            }
        }
    }
    
    public static enum EColorAsign {
        Inherit,
        Asign,
    	None;
        
        @Override
        public String toString() {
            switch (this) {
                case Inherit: return "inherit";
                case Asign: return "asign";
                case None: return "none";
                default: return "";
            }
        }
    }

    public String prBackColor = "#111111";
    public String prBorderColor = "#222222";
    public String cursorMainColor = "#DDDD00";
    public String cursorEffeColor = "#FFFFFF";
    public String pbBaseLineColor = "#969696";
    // 表示設定
    public boolean isVisiblePb = false;
    public int tickBarPosition = 420;
    public boolean isVisibleVerticalBorder = true;
    public boolean isVisibleHorizonBorder = true;
    public boolean isVisibleMonitorStr = true;
    public ENotesDesign notessDesign = ENotesDesign.Normal;
    public ECursorType cursorType = ECursorType.Keyboard;
    public EColorRule colorRule = EColorRule.Track;
    public EColorAsign colorAsign = EColorAsign.None;
    public List<String> notesColorCodes;
    
    public LayoutConfig() {
    	notesColorCodes = new ArrayList<String>();
    }

    public void read(File file) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        
        String str = null;
        str = props.getProperty(LC_PLAYER_BGCOLOR);
        if (str != null) prBackColor = str2ColorCode(str);
        str = props.getProperty(LC_PLAYER_BDCOLOR);
        if (str != null) prBorderColor = str2ColorCode(str);
        str = props.getProperty(LC_PLAYER_BORDER_VERTICAL_VISIBLE);
        if (str != null) isVisibleVerticalBorder = Boolean.parseBoolean(str);
        str = props.getProperty(LC_PLAYER_BORDER_HORIZON_VISIBLE);
        if (str != null) isVisibleHorizonBorder = Boolean.parseBoolean(str);
        str = props.getProperty(LC_PLAYER_COLOR_RULE);
        if (str == null) {
        }
        else if (str.equalsIgnoreCase("channel")) {
        	colorRule = EColorRule.Channel;
        }
        else if (str.equalsIgnoreCase("track")) {
        	colorRule = EColorRule.Track;
        }
        str = props.getProperty(LC_CURSOR_TYPE);
        if (str == null) {
        }
        else if (str.equalsIgnoreCase("keyboard")) {
            cursorType = ECursorType.Keyboard;
        }
        else if (str.equalsIgnoreCase("line")) {
            cursorType = ECursorType.Line;
        }
        str = props.getProperty(LC_CURSOR_COLOR);
        if (str != null) cursorMainColor = str2ColorCode(str);
        str = props.getProperty(LC_CURSOR_EFFE_COLOR);
        if (str != null) cursorEffeColor = str2ColorCode(str);
        str = props.getProperty(LC_CURSOR_POS);
        if (str != null) tickBarPosition = Integer.parseInt(str);
        str = props.getProperty(LC_PB_COLOR);
        if (str != null) pbBaseLineColor = str2ColorCode(str);
        str = props.getProperty(LC_PB_VISIBLE);
        if (str != null) isVisiblePb = Boolean.parseBoolean(str);
        str = props.getProperty(LC_INFO_VISIBLE);
        if (str != null) isVisibleMonitorStr = Boolean.parseBoolean(str);
        
        str = props.getProperty(LC_NOTES_DESIGN);
        if (str == null) {
        }
        else if (str.equalsIgnoreCase(ENotesDesign.Normal.toString())) {
        	notessDesign = ENotesDesign.Normal;
        }
        else if (str.equalsIgnoreCase(ENotesDesign.Flat.toString())) {
        	notessDesign = ENotesDesign.Flat;
        }
        else if (str.equalsIgnoreCase(ENotesDesign.Arc.toString())) {
        	notessDesign = ENotesDesign.Arc;
        }
        str = props.getProperty(LC_NOTES_COLOR_ASIGN);
        if (str == null) {
        }
        else if (str.equalsIgnoreCase(EColorAsign.Asign.toString())) {
        	colorAsign = EColorAsign.Asign;
        }
        else if (str.equalsIgnoreCase(EColorAsign.Inherit.toString())) {
        	colorAsign = EColorAsign.Inherit;
        }
        else if (str.equalsIgnoreCase(EColorAsign.None.toString())) {
        	colorAsign = EColorAsign.None;
        }
        
        notesColorCodes.clear();
        StringBuilder sb = new StringBuilder(64); // 初期容量を指定
        for (int i=1; i<=512; i++) {
            sb.setLength(0);
            sb.append(LC_NOTES_COLOR).append(i);
            str = props.getProperty(sb.toString());
            if (str != null) {
            	notesColorCodes.add(str2ColorCode(str));
            }
        }
    }
    
    public void write(File file) throws IOException {
        Properties props = new Properties();
        props.setProperty(LC_PLAYER_BGCOLOR, colorCode2str2(prBackColor));
        props.setProperty(LC_PLAYER_BDCOLOR, colorCode2str2(prBorderColor));
        props.setProperty(LC_PLAYER_BORDER_VERTICAL_VISIBLE, String.valueOf(isVisibleVerticalBorder));
        props.setProperty(LC_PLAYER_BORDER_HORIZON_VISIBLE, String.valueOf(isVisibleHorizonBorder));
        props.setProperty(LC_PLAYER_COLOR_RULE, colorRule.toString());
        props.setProperty(LC_CURSOR_TYPE, cursorType.toString());
        props.setProperty(LC_CURSOR_COLOR, colorCode2str2(cursorMainColor));
        props.setProperty(LC_CURSOR_EFFE_COLOR, colorCode2str2(cursorEffeColor));
        props.setProperty(LC_CURSOR_POS, String.valueOf(tickBarPosition));
        props.setProperty(LC_PB_COLOR, colorCode2str2(pbBaseLineColor));
        props.setProperty(LC_PB_VISIBLE, String.valueOf(isVisiblePb));
        props.setProperty(LC_INFO_VISIBLE, String.valueOf(isVisibleMonitorStr));
        props.setProperty(LC_NOTES_DESIGN, notessDesign.toString());
        
        props.setProperty(LC_NOTES_COLOR_ASIGN, colorAsign.toString());
        StringBuilder sb = new StringBuilder(64); // 初期容量を指定
        for (int i=0; i<notesColorCodes.size(); i++) {
            sb.setLength(0);
            sb.append(LC_NOTES_COLOR).append(i + 1);
            props.setProperty(sb.toString(), colorCode2str2(notesColorCodes.get(i)));
        }
        
        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "Layout");
        }
    }
    
    private String colorCode2str2(String code) {
        if (code.startsWith("#")) {
            code = code.substring(1);
        }
        return code;
    }
    
    private String str2ColorCode(String str) {
        return "#" + str;
    }
}
