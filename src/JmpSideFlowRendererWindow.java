import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.List;

import javax.sound.midi.Sequence;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import drawLib.gui.DrawLibFrame;
import function.Utility;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;

public class JmpSideFlowRendererWindow extends DrawLibFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    
    public static final int WINDOW_FIXED_FPS = 60; //画面の限界FPS値
    
    // 次のページにフリップするpx数
    private static final int NEXT_FLIP_COUNT = 1;

    private JPanel contentPane;

    // 現在のレイアウト設定
    public LayoutConfig layout = _DEBUG_LAYOUT;//NOTESBASE_LAYOUT;

    //
    // =##= カスタム(デバッグ用) =##=
    public static final LayoutConfig _DEBUG_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    new Color(0, 255, 0), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    100, // ノーツのボーダーハイライト
                    200, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    false, // PBの表示
                    true, // tickバーの表示
                    true, // 縦線表示
                    true, // 横線表示
                    true, // tickバーの高精度
                    false, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(0, 0, 0), 150), // ダークアウトカラー
                    false // フォーカスPB色描画
            );

    //
    // =##= クラシック =##=
    public static final LayoutConfig CLASSIC_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    new Color(255, 255, 255), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    100, // ノーツのボーダーハイライト
                    200, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    true, // PBの表示
                    true, // tickバーの表示
                    false, // 縦線表示
                    false, // 横線表示
                    true, // tickバーの高精度
                    true, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(0, 0, 0), 150), // ダークアウトカラー
                    true // フォーカスPB色描画
            );

    //
    // =##= クラシック =##=
    public static final LayoutConfig CLASSIC_LAYOUT2 = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    new Color(255, 255, 255), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    100, // ノーツのボーダーハイライト
                    200, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    false, // PBの表示
                    true, // tickバーの表示
                    false, // 縦線表示
                    false, // 横線表示
                    true, // tickバーの高精度
                    false, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(0, 0, 0), 150), // ダークアウトカラー
                    false // フォーカスPB色描画
            );

    //
    // =##= Notes base =##=
    public static final LayoutConfig NOTESBASE_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    new Color(0, 255, 0), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    100, // ノーツのボーダーハイライト
                    255, // ノーツのフォーカスハイライト
                    true, // ノーツのフォーカスデザインを固定
                    true, // PBの表示
                    false, // tickバーの表示
                    false, // 縦線表示
                    false, // 横線表示
                    true, // tickバーの高精度
                    false, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(0, 0, 0), 150), // ダークアウトカラー
                    true // フォーカスPB色描画
            );

    //
    // =##= 軽量 =##=
    public static final LayoutConfig LIGHT_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    new Color(0, 255, 0), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    100, // ノーツのボーダーハイライト
                    200, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    false, // PBの表示
                    true, // tickバーの表示
                    false, // 縦線表示
                    false, // 横線表示
                    false, // tickバーの高精度
                    false, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(0, 0, 0), 150), // ダークアウトカラー
                    false // フォーカスPB色描画
            );

    //
    // =##= 黒っぽい =##=
    public static final LayoutConfig BLACK_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 100), // ボーダーカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 255), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    100, // ノーツのボーダーハイライト
                    200, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    true, // PBの表示
                    true, // tickバーの表示
                    false, // 縦線表示
                    false, // 横線表示
                    true, // tickバーの高精度
                    true, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(0, 0, 0), 150), // ダークアウトカラー
                    false // フォーカスPB色描画
            );

    //

    private ImagerWorkerManager imageWorkerMgr = null;
    private IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();

    public Color[] notesColor = null;
    public Color[] notesBorderColor = null;
    public Color[] notesFocusColor = null;
    public Color[] cursorColor = new Color[] { //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 1.0)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.9)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.3)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.3)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.3)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.2)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.2)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.2)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.1)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.1)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.1)), //
            Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.1)), //
    };//

    private int topMidiNumber = 120;// 127;
    private int leftMeas = 0;
    private int zeroPosition = 10;
    private int measCellWidth = 80;
    private int measCellHeight = 8;
    private int dispMeasCount = 0;

    /**
     * Create the frame.
     */
    public JmpSideFlowRendererWindow() {
        this.setTransferHandler(new DropFileHandler());
        this.setTitle("JMP SideFlowRenderer");
        setBounds(10, 10, 1600, 1024);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        addMouseListener(this);
        addMouseWheelListener(this);
        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    repaintAndFlipScreen();
                }
                catch (Exception ex) {
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        ISystemManager sm = JMPCoreAccessor.getSystemManager();
        notesColor = new Color[16];
        notesBorderColor = new Color[16];
        notesFocusColor = new Color[16];
        for (int i = 0; i < 16; i++) {
            String key = String.format("ch_color_%d", (i + 1));
            notesColor[i] = Utility.convertCodeToHtmlColor(sm.getCommonRegisterValue(key));
            notesBorderColor[i] = Utility.convertHighLightColor(notesColor[i], layout.borderHighLight);
            notesFocusColor[i] = Utility.convertHighLightColor(notesColor[i], layout.focusHighLight);
        }
    }

    @Override
    public void repaintAndFlipScreen() {
        super.repaintAndFlipScreen();

        resetPage();
    }

    public void init() {
        imageWorkerMgr = new ImagerWorkerManager();
        imageWorkerMgr.init();

        initPane();
        setFixedFPS(WINDOW_FIXED_FPS);
    }

    public void exit() {
        try {
            exitPane();

            imageWorkerMgr.exit();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    public void loadFile() {
        setLeftMeas(0);
        calcDispMeasCount();
        resetPage();
        
        while(true) {
            if (imageWorkerMgr.getWorker(0).isWait() == true) {
                break;
            }
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void adjustTickBar() {
        Sequence seq = midiUnit.getSequence();
        if (seq == null) {
            return;
        }

        resetPage();
    }

    @Override
    public void paint(Graphics g) {
        // super.paint(g);
        paintMain(g);
    }

    @Override
    public void paintComponents(Graphics g) {
        // super.paintComponents(g);
    }

    private void calcDispMeasCount() {
        int x = getZeroPosition();
        int measLen = 0;
        while (x <= getWidth()) {
            x += getMeasCellWidth();
            measLen++;
        }
        dispMeasCount = measLen;
    }
    
    public int getDispMeasCount() {
        return dispMeasCount;
    }

    private void paintBorder(Graphics g) {
        g.setColor(layout.prBackColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(layout.prBorderColor);
        int x = getZeroPosition();
        int y = 0;
        if (layout.isVisibleHorizonBorder == true) {
            while (y <= getHeight()) {
                g.drawLine(x, y, x + getWidth(), y);
                y += getMeasCellHeight();
            }
        }
        x = getZeroPosition();
        y = 0;
        while (x <= getWidth()) {
            if (layout.isVisibleVerticalBorder == true) {
                g.drawLine(x, y, x, y + getHeight());
            }
            x += getMeasCellWidth();
        }
    }

    private int getTickbarX() {
        /* Tickbar描画 */
        Sequence sequence = midiUnit.getSequence();
        if (sequence == null) {
            return 0;
        }
        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getSequence().getResolution();
        int tickX = 0;
        if (layout.tickHighPrecision == true) {
            long offset = (long) midiUnit.getTickPosition() % sequence.getResolution();
            int offsetX = (int) (getMeasCellWidth() * (double) ((double) offset / (double) sequence.getResolution()));
            tickX = (int) (getZeroPosition() + (startMeas * getMeasCellWidth())) + (getLeftMeas() * getMeasCellWidth());
            tickX += offsetX;
        }
        else {
            tickX = (int) (getZeroPosition() + (startMeas * getMeasCellWidth())) + (getLeftMeas() * getMeasCellWidth());
        }
        return tickX;
    }

    private void paintMain(Graphics g) {
        
        paintBorder(g);

        /* ノーツ描画 */
        imageWorkerMgr.copyNotesImage(g);

        /* PBライン描画 */
        int pbMaxHeight = 100;
        int pbCenterY = (pbMaxHeight / 2) + 100;
        if (layout.isVisiblePb == true) {
            if (layout.isDrawFocusPbColor == true) {
                int tickX = getTickbarX();
                g.setColor(ImageWorker.FIX_FOCUS_NOTES_BGCOLOR);
                g.drawLine(0, pbCenterY, tickX - 1, pbCenterY);
                g.setColor(ImageWorker.FIX_FOCUS_NOTES_BDCOLOR);
                g.drawLine(0, pbCenterY-1, tickX, pbCenterY-1);
                g.drawLine(0, pbCenterY+1, tickX, pbCenterY+1);
            }
        }

        /* Tickbar描画 */
        Sequence sequence = midiUnit.getSequence();
        if (sequence != null) {
            int tickX = getTickbarX();
            if (layout.isDarkout == true) {
                g.setColor(layout.darkoutColor);
                g.fillRect(0, 0, tickX, getHeight());
            }
            paintTickPosition(g, tickX);

            // フリップ
            calcDispMeasCount();
            if (midiUnit.isRunning() == true) {
                flipPage();
            }
        }

        /* パフォーマンス表示 */
//        if (useDoubleOffscreen == false) {
//            int perX = 15;
//            int perY = 60;
//            int perWidth = 40;
//            int perHeight = 15;
//            int width = (perWidth * firstNotesThread.getNFPS()) / 60;
//            for (int i = 0; i < width; i++) {
//                int height = (i * perHeight) / perWidth;
//                if (i < (perWidth * 0.2)) {
//                    g.setColor(Color.RED);
//                }
//                else if (i < (perWidth * 0.5)) {
//                    g.setColor(Color.YELLOW);
//                }
//                else {
//                    g.setColor(Color.GREEN);
//                }
//                g.drawLine(perX + (i * 2), perY - height, perX + (i * 2), perY);
//            }
//        }
    }

    public void resetPage() {
        calcDispMeasCount();
        
        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getSequence().getResolution();
        setLeftMeas(-startMeas);
        
        imageWorkerMgr.reset(getLeftMeas(), dispMeasCount, NEXT_FLIP_COUNT);
    }
    private void flipPage() {
        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getSequence().getResolution();
        int offsetLeftMeas = getLeftMeas();
        offsetLeftMeas = (offsetLeftMeas < 0) ? -(offsetLeftMeas) : offsetLeftMeas;
        int flipMergin = -(NEXT_FLIP_COUNT);
        int flipLine = (offsetLeftMeas + dispMeasCount + flipMergin);
        if (startMeas >= flipLine) {
            setLeftMeas(-(flipLine));
            offsetLeftMeas = getLeftMeas();
            imageWorkerMgr.flipPage(offsetLeftMeas, dispMeasCount, NEXT_FLIP_COUNT);
        }
    }

    public void paintTickPosition(Graphics g, int x) {
        if (layout.isVisibleTickbar == false) {
            return;
        }

        if (g == null) {
            // nullPointer防止
            return;
        }

        int len = cursorColor.length;
        if (JMPCoreAccessor.getSoundManager().isPlay() == false) {
            len = 1;
        }

        for (int i = 0; i < len; i++) {
            g.setColor(cursorColor[i]);
            g.drawLine(x - i, 0, x - i, getHeight());
        }
    }

    public int getZeroPosition() {
        return zeroPosition;
    }

    public void setZeroPosition(int zeroPosition) {
        this.zeroPosition = zeroPosition;
    }

    public int getMeasCellWidth() {
        return measCellWidth;
    }

    public void setMeasCellWidth(int measCellWidth) {
        this.measCellWidth = measCellWidth;
    }

    public int getMeasCellHeight() {
        return measCellHeight;
    }

    public void setMeasCellHeight(int measCellHeight) {
        this.measCellHeight = measCellHeight;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
            JMPCoreAccessor.getWindowManager().getMainWindow().toggleWindowVisible();
        }
        else {
            if (JMPCoreAccessor.getSoundManager().isPlay() == true) {
                JMPCoreAccessor.getSoundManager().stop();
            }
            else {
                JMPCoreAccessor.getSoundManager().play();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public int getTopMidiNumber() {
        return topMidiNumber;
    }

    public void setTopMidiNumber(int topMidiNumber) {
        this.topMidiNumber = topMidiNumber;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int amount;
        if (e.isAltDown()) {
            amount = 1;
            // スクロール方向
            amount *= (e.getWheelRotation() < 0) ? 1 : -1;

            int before = getLeftMeas();
            setLeftMeas(before + amount);
        }
        else if (e.isControlDown()) {
            amount = 1;
            // スクロール方向
            amount *= (e.getWheelRotation() < 0) ? 1 : -1;

            int before = measCellWidth;
            before += amount;
            if (before >= 300) {
                before = 300;
            }
            else if (before < 0) {
                before = 0;
            }
            measCellWidth = before;
        }
        else {
            amount = 1;
            // スクロール方向
            amount *= (e.getWheelRotation() < 0) ? -1 : 1;

            int before = getTopMidiNumber();
            setTopMidiNumber(before + amount);
        }
        
        resetPage();
    }

    public int getLeftMeas() {
        return leftMeas;
    }

    public void setLeftMeas(int leftMeas) {
        this.leftMeas = leftMeas;
    }

    /**
     *
     * ドラッグ＆ドロップハンドラー
     *
     */
    public class DropFileHandler extends TransferHandler {
        /**
         * ドロップされたものを受け取るか判断 (アイテムのときだけ受け取る)
         */
        @Override
        public boolean canImport(TransferSupport support) {
            if (support.isDrop() == false) {
                // ドロップ操作でない場合は受け取らない
                return false;
            }

            if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == false) {
                // ファイルでない場合は受け取らない
                return false;
            }

            return true;
        }

        /**
         * ドロップされたアイテムを受け取る
         */
        @Override
        public boolean importData(TransferSupport support) {
            // ドロップアイテム受理の確認
            if (canImport(support) == false) {
                return false;
            }

            // ドロップ処理
            Transferable t = support.getTransferable();
            try {
                // ドロップアイテム取得
                catchLoadItem(t.getTransferData(DataFlavor.javaFileListFlavor));
                return true;
            }
            catch (Exception e) {
                /* 受け取らない */
            }
            return false;
        }
    }

    public void catchLoadItem(Object item) {
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>) item;

        // 一番先頭のファイルを取得
        if ((files != null) && (files.size() > 0)) {
            String path = files.get(0).getPath();
            if (Utility.checkExtensions(path, JmpSideFlowRenderer.Extensions.split(",")) == true) {
                JMPCoreAccessor.getFileManager().loadFileToPlay(path);
            }
        }
    }
}
