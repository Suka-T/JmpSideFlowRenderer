import java.awt.Color;

import function.Utility;

public class LayoutConfig {

    public static LayoutConfig createConfig(//
            Color prBackColor, //
            Color prBorderColor, //
            Color cursorMainColor, //
            Color pbBaseLineColor, //
            int borderHighLight, //
            int focusHighLight, //
            boolean fixFocusNotesDesign, //
            boolean isVisiblePb, //
            boolean isVisibleTickbar, //
            boolean isVisibleVerticalBorder, //
            boolean isVisibleHorizonBorder, //
            boolean tickHighPrecision, //
            boolean isDarkout, //
            Color darkoutColor, //
            boolean isDrawFocusPbColor//
    ) {
        LayoutConfig instance = new LayoutConfig();
        instance.prBackColor = prBackColor;
        instance.prBorderColor = prBorderColor;
        instance.cursorMainColor = cursorMainColor;
        instance.pbBaseLineColor = pbBaseLineColor;
        instance.borderHighLight = borderHighLight;
        instance.focusHighLight = focusHighLight;
        instance.fixFocusNotesDesign = fixFocusNotesDesign;
        instance.isVisiblePb = isVisiblePb;
        instance.isVisibleTickbar = isVisibleTickbar;
        instance.isVisibleVerticalBorder = isVisibleVerticalBorder;
        instance.isVisibleHorizonBorder = isVisibleHorizonBorder;
        instance.tickHighPrecision = tickHighPrecision;
        instance.isDarkout = isDarkout;
        instance.darkoutColor = darkoutColor;
        instance.isDrawFocusPbColor = isDrawFocusPbColor;
        return instance;
    }

    public Color prBackColor = new Color(255, 220, 255);
    public Color prBorderColor = Utility.convertHighLightColor(prBackColor, -100);
    public Color cursorMainColor = Utility.convertHighLightColor(prBackColor, -100);
    public Color pbBaseLineColor = Utility.convertHighLightColor(prBackColor, -140);
    public int borderHighLight = -100;
    public int focusHighLight = 200;
    public boolean fixFocusNotesDesign = false;
    // 表示設定
    public boolean isVisiblePb = true;
    public boolean isVisibleTickbar = true;
    public boolean isVisibleVerticalBorder = false;
    public boolean isVisibleHorizonBorder = false;
    // tickバーの高精度移動
    public boolean tickHighPrecision = true;
    // ダークアウトモード
    public boolean isDarkout = true;
    public Color darkoutColor = Utility.convertColorAlpha(prBackColor, 150);
    // Notesフォーカス色描画
    public boolean isDrawFocusNotesColor = false;
    // PBフォーカス色描画
    public boolean isDrawFocusPbColor = false;

    public LayoutConfig() {
    }

}
