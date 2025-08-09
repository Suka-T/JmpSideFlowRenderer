package layout;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import function.Utility;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import layout.parts.ArcNotesPainter;
import layout.parts.FlatNotesPainter;
import layout.parts.NormalNotesPainter;
import layout.parts.NotesPainter;

public class LayoutManager {
    public static final int DEFAULT_TICK_MEAS = 1;

    private List<Color> notesColor = null;
    private List<Color> notesBorderColor = null;
    private Color cursorColor = null;
    private Color cursorEffeColor = null;

    private Color bgColor = null;
    private Color bdColor = null;
    private Color pbColor = null;

    private Canvas rootCanvas = null;

    private NotesPainter notesPainter = null;

    // 現在のレイアウト設定
    private LayoutConfig layout = new LayoutConfig();

    private static LayoutManager instance = new LayoutManager();

    private LayoutManager() {
    }

    public static LayoutManager getInstance() {
        return instance;
    }

    public VolatileImage createLayerImage(int width, int height) {
        GraphicsConfiguration gc = rootCanvas.getGraphicsConfiguration();
        return gc.createCompatibleVolatileImage(width, height, Transparency.OPAQUE);
    }

    public BufferedImage createBufferdImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void initialize(Canvas canvas) {
        rootCanvas = canvas;

        notesColor = new ArrayList<Color>();
        notesBorderColor = new ArrayList<Color>();
        ISystemManager sm = JMPCoreAccessor.getSystemManager();

        if (layout.notesColorCodes.isEmpty() == true) {
            if (layout.notesColorCodes.isEmpty() == true) {
                layout.notesColorCodes.add("#00FF00");
                layout.notesColorCodes.add("#FFFF00");
                layout.notesColorCodes.add("#00FFFF");
                layout.notesColorCodes.add("#FF00FF");
                layout.notesColorCodes.add("#FFA500");
                layout.notesColorCodes.add("#87CEEB");
                layout.notesColorCodes.add("#FF69B4");
                layout.notesColorCodes.add("#FFF700");
                // layout.notesColorCodes.add("#50FA7B");
                layout.notesColorCodes.add("#ffe4e1");
                layout.notesColorCodes.add("#FF0000");
            }
        }

        if (layout.colorAsign == LayoutConfig.EColorAsign.Inherit || layout.colorAsign == LayoutConfig.EColorAsign.None) {
            for (int i = 0; i < 16; i++) {
                String key = String.format("ch_color_%d", (i + 1));
                notesColor.add(Utility.convertCodeToHtmlColor(sm.getCommonRegisterValue(key)));
            }
        }
        if (layout.colorAsign == LayoutConfig.EColorAsign.Inherit || layout.colorAsign == LayoutConfig.EColorAsign.Asign) {
            for (String s : layout.notesColorCodes) {
                notesColor.add(Utility.convertCodeToHtmlColor(s));
            }
        }

        double borderOffset = layout.notesColorBorderRgb;
        for (Color nc : notesColor) {
            int r = nc.getRed();
            int g = nc.getGreen();
            int b = nc.getBlue();
            int a = nc.getAlpha();
            r = (int) ((double) r * borderOffset);
            g = (int) ((double) g * borderOffset);
            b = (int) ((double) b * borderOffset);
            notesBorderColor.add(new Color(r > 255 ? 255 : r, g > 255 ? 255 : g, b > 255 ? 255 : b, a));
        }

        cursorColor = Utility.convertCodeToHtmlColor(layout.cursorMainColor);
        cursorEffeColor = Utility.convertCodeToHtmlColor(layout.cursorEffeColor);

        bgColor = Utility.convertCodeToHtmlColor(layout.prBackColor);
        bdColor = Utility.convertCodeToHtmlColor(layout.prBorderColor);
        pbColor = Utility.convertCodeToHtmlColor(layout.pbBaseLineColor);

        if (layout.notessDesign == LayoutConfig.ENotesDesign.Normal) {
            notesPainter = new NormalNotesPainter();
        }
        else if (layout.notessDesign == LayoutConfig.ENotesDesign.Arc) {
            notesPainter = new ArcNotesPainter();
        }
        else {
            notesPainter = new FlatNotesPainter();
        }
    }

    public void write(File f) throws IOException {
        layout.write(f);
    }

    public void read(File f) throws IOException {
        if (f.exists() == true) {
            layout.read(f);
        }
    }

    public Color getNotesColor(int index) {
        return notesColor.get(index % notesColor.size());
    }

    public Color getNotesBorderColor(int index) {
        return notesBorderColor.get(index % notesBorderColor.size());
    }

    public List<Color> getNotesBorderColors() {
        return notesBorderColor;
    }

    public LayoutConfig.ECursorType getCursorType() {
        return layout.cursorType;
    }

    public LayoutConfig.EColorRule getColorRule() {
        return layout.colorRule;
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public Color getCursorEffectColor() {
        return cursorEffeColor;
    }

    public Color getBackColor() {
        return bgColor;
    }

    public Color getBorderColor() {
        return bdColor;
    }

    public Color getPitchbendColor() {
        return pbColor;
    }

    public boolean isVisibleInfoStr() {
        return layout.isVisibleMonitorStr;
    }

    public boolean isVisibleHorizonBorder() {
        return layout.isVisibleHorizonBorder;
    }

    public boolean isVisibleVerticalBorder() {
        return layout.isVisibleVerticalBorder;
    }

    public boolean isVisiblePbLine() {
        return layout.isVisiblePb;
    }
    
    public boolean isVisibleCursorEffect() {
        return layout.isVisibleCursorEffect;
    }

    public LayoutConfig.ENotesDesign getNotesDesign() {
        return layout.notessDesign;
    }

    public int getTickBarPosition() {
        return layout.tickBarPosition;
    }

    public NotesPainter getNotesPainter() {
        return notesPainter;
    }

}
