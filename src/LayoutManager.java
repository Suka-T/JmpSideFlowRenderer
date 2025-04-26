import java.awt.Canvas;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.VolatileImage;

import function.Utility;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;

public class LayoutManager {
    
    public static final int DEFAULT_1MEAS_WIDTH = 420;//320;
    public static final int DEFAULT_TICK_MEAS = 1;
    
    private Color[] notesColor = null;
    private Color cursorColor = null;
    private Color cursorEffeColor = null;
    private Color hitEffectColor = null;
    
    private Color bgColor = null;
    private Color bdColor = null;
    private Color pbColor = null;
    
    private Canvas rootCanvas = null;

    // 現在のレイアウト設定
    private LayoutConfig layout = CLASSIC_LAYOUT;

    //
    // =##= クラシック =##=
    public static final LayoutConfig CLASSIC_LAYOUT = //
            LayoutConfig.createConfig(//
                    "#000000", // 背景カラー
                    "#969696", // 枠線カラー 
                    "#ffffff", // カーソルカラー
                    "#969696", // PBベースカラー 
                    false, // PBの表示
                    DEFAULT_1MEAS_WIDTH * DEFAULT_TICK_MEAS, // TickBar位置
                    true, // 縦線表示
                    false, // 横線表示
                    true, // ノーツを3Dデザイン
                    true // 情報表示 
            );

    //
    // =##= ライトテーマ =##=
    public static final LayoutConfig LIGHT_LAYOUT = //
            LayoutConfig.createConfig(//
                    "#dcdcdc", // 背景カラー
                    "#969696", // 枠線カラー 
                    "#5f9ea0", // カーソルカラー
                    "#969696", // PBベースカラー 
                    false, // PBの表示
                    DEFAULT_1MEAS_WIDTH * DEFAULT_TICK_MEAS, // // TickBar位置
                    true, // 縦線表示
                    false, // 横線表示
                    true, // ノーツを3Dデザイン
                    true // 情報表示
            );
    //
    
    private static LayoutManager instance = new LayoutManager();
    private LayoutManager() {}
    
    public static LayoutManager getInstance() {
        return instance;
    }
    
    public VolatileImage createLayerImage(int width, int height) {
        GraphicsConfiguration gc = rootCanvas.getGraphicsConfiguration();
        return gc.createCompatibleVolatileImage(width, height, Transparency.OPAQUE);
    }
    
    public void initialize(Canvas canvas) {
        rootCanvas = canvas;
        ISystemManager sm = JMPCoreAccessor.getSystemManager();
        notesColor = new Color[16];
        for (int i = 0; i < 16; i++) {
            String key = String.format("ch_color_%d", (i + 1));
            notesColor[i] = Utility.convertCodeToHtmlColor(sm.getCommonRegisterValue(key));
        }
        
        cursorColor = Utility.convertCodeToHtmlColor(layout.cursorMainColor);
        
        hitEffectColor = Color.WHITE;
        cursorEffeColor = new Color(cursorColor.getRed(), cursorColor.getBlue(), cursorColor.getGreen(), 180);
        
        bgColor = Utility.convertCodeToHtmlColor(layout.prBackColor);
        bdColor = Utility.convertCodeToHtmlColor(layout.prBorderColor);
        pbColor = Utility.convertCodeToHtmlColor(layout.pbBaseLineColor);
    }
    
    public Color[] getNotesColors() {
        return notesColor;
    }
    
    public Color getNotesColor(int index) {
        return notesColor[index];
    }
    
    public Color getHitEffectColor() {
        return hitEffectColor;
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
    
    public boolean isNotes3D() {
        return layout.isNotes3D;
    }
    
    public int getTickBarPosition() {
        return layout.tickBarPosition;
    }

}
