public class LayoutConfig {

    public static LayoutConfig createConfig(//
            String prBackColor, //
            String prBorderColor, //
            String cursorMainColor, //
            String pbBaseLineColor, //
            boolean isVisiblePb, //
            int tickBarPosition, //
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
        instance.tickBarPosition = tickBarPosition;
        instance.isVisibleVerticalBorder = isVisibleVerticalBorder;
        instance.isVisibleHorizonBorder = isVisibleHorizonBorder;
        instance.isNotes3D = isNotes3D;
        instance.isVisibleMonitorStr = isVisibleMonitorStr;
        return instance;
    }

    public String prBackColor = "#000000";
    public String prBorderColor = "#969696";
    public String cursorMainColor = "#FFFFFF";
    public String pbBaseLineColor = "#969696";
    // 表示設定
    public boolean isVisiblePb = true;
    public int tickBarPosition = 60;
    public boolean isVisibleVerticalBorder = false;
    public boolean isVisibleHorizonBorder = false;
    public boolean isNotes3D = true;
    public boolean isVisibleMonitorStr = true;
    public LayoutConfig() {
    }

}
