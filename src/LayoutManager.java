import java.awt.Color;

import function.Utility;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;

public class LayoutManager {
    
    public static final int DEFAULT_1MEAS_WIDTH = 320;//128 * 3;
    public static final int DEFAULT_TICK_MEAS = 1;
    
    private Color[] notesColor = null;
    private Color cursorColor = null;
    private Color cursorEffeColor = null;
    private Color[] hitEffectColor = null;
    
    private Color bgColor = null;
    private Color bdColor = null;
    private Color pbColor = null;

    // 現在のレイアウト設定
    private LayoutConfig layout = CLASSIC_LAYOUT;

    //
    // =##= クラシック =##=
    public static final LayoutConfig CLASSIC_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    new Color(210, 210, 210),
                    Utility.convertCodeToHtmlColor("#FFFFFF"), // カーソルカラー
                    new Color(210, 210, 210),
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
                    new Color(240, 240, 240), // 背景カラー
                    new Color(40, 40, 40),
                    Utility.convertCodeToHtmlColor("#000000"), // カーソルカラー
                    new Color(40, 40, 40),
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
    
    public void initialize() {
        ISystemManager sm = JMPCoreAccessor.getSystemManager();
        notesColor = new Color[16];
        for (int i = 0; i < 16; i++) {
            String key = String.format("ch_color_%d", (i + 1));
            notesColor[i] = Utility.convertCodeToHtmlColor(sm.getCommonRegisterValue(key));
        }
        hitEffectColor = new Color[8];
        for (int i=0; i<hitEffectColor.length; i++) {
            hitEffectColor[i] = new Color(
                    layout.cursorMainColor.getRed(), 
                    layout.cursorMainColor.getGreen(), 
                    layout.cursorMainColor.getBlue(), 
                    255 - (255 / hitEffectColor.length) * i
                    );
        }
        cursorColor = layout.cursorMainColor;
        cursorEffeColor = new Color(cursorColor.getRed(), cursorColor.getBlue(), cursorColor.getGreen(), 180);
        
        bgColor = layout.prBackColor;
        bdColor = layout.prBorderColor;
        pbColor = layout.pbBaseLineColor;
    }
    
    public Color[] getNotesColors() {
        return notesColor;
    }
    
    public Color getNotesColor(int index) {
        return notesColor[index];
    }
    
    public Color[] getHitEffectColors() {
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
