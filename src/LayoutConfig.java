import java.awt.Color;

import function.Utility;

public class LayoutConfig {

    public static LayoutConfig createConfig(//
            Color prBackColor, //
            Color prBorderColor, //
            Color cursorMainColor, //
            Color pbBaseLineColor, //
            boolean isVisiblePb, //
            int keyWidth, //
            boolean isVisibleVerticalBorder, //
            boolean isVisibleHorizonBorder, //
            boolean isNotes3D, //
            boolean isVisibleMonitorStr
    ) {
        LayoutConfig instance = new LayoutConfig();
        instance.prBackColor = prBackColor;
        instance.prBorderColor = prBorderColor;
        instance.cursorMainColor = cursorMainColor;
        instance.pbBaseLineColor = pbBaseLineColor;
        instance.isVisiblePb = isVisiblePb;
        instance.keyWidth = keyWidth;
        instance.isVisibleVerticalBorder = isVisibleVerticalBorder;
        instance.isVisibleHorizonBorder = isVisibleHorizonBorder;
        instance.isNotes3D = isNotes3D;
        instance.isVisibleMonitorStr = isVisibleMonitorStr;
        return instance;
    }

    public Color prBackColor = new Color(255, 220, 255);
    public Color prBorderColor = Utility.convertHighLightColor(prBackColor, -100);
    public Color cursorMainColor = Utility.convertHighLightColor(prBackColor, -100);
    public Color pbBaseLineColor = Utility.convertHighLightColor(prBackColor, -140);
    // 表示設定
    public boolean isVisiblePb = true;
    public int keyWidth = 60;
    public boolean isVisibleVerticalBorder = false;
    public boolean isVisibleHorizonBorder = false;
    public boolean isNotes3D = true;
    public boolean isVisibleMonitorStr = true;
    public LayoutConfig() {
    }

}
