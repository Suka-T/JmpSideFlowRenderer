import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.sound.midi.Sequence;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import drawLib.gui.DrawLibFrame;
import function.Utility;
import jlib.core.ISystemManager;
import jlib.core.IWindowManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;

public class JmpSideFlowRendererWindow extends DrawLibFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    
    public static final int WINDOW_FIXED_FPS = 60; //画面の限界FPS値
    
    // 次のページにフリップするpx数
    private static final int NEXT_FLIP_COUNT = 0;

    private JPanel contentPane;

    // 現在のレイアウト設定
    public LayoutConfig layout = CLASSIC_LAYOUT;

    //
    // =##= カスタム(デバッグ用) =##=
    public static final LayoutConfig _DEBUG_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    new Color(0, 255, 0), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    true, // PBの表示
                    60, // KeyStatusの幅
                    true, // 縦線表示
                    true, // 横線表示
                    true, // ノーツを3Dデザイン 
                    false // 情報表示 
            );

    //
    // =##= クラシック =##=
    public static final LayoutConfig CLASSIC_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    new Color(255, 255, 255), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    true, // PBの表示
                    60, // KeyStatusの幅
                    false, // 縦線表示
                    false, // 横線表示
                    true, // ノーツを3Dデザイン
                    true // 情報表示 
            );

    //
    // =##= ライトテーマ =##=
    public static final LayoutConfig LIGHT_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(230, 230, 230), // 背景カラー
                    Utility.convertHighLightColor(new Color(230, 230, 230), 40), // ボーダーカラー
                    new Color(0, 0, 0), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    true, // PBの表示
                    60, // KeyStatusの幅
                    false, // 縦線表示
                    false, // 横線表示
                    true, // ノーツを3Dデザイン
                    true // 情報表示
            );
    //

    private ImagerWorkerManager imageWorkerMgr = null;

    public Color[] notesColor = null;
    public Color[] cursorColor = null;

    private int topMidiNumber = 110;
    private int leftMeas = 0;
    private int zeroPosition = 0;
    private int measCellWidth = 120;
    private int measCellHeight = 5;
    private int dispMeasCount = 0;
    private int orgDispWidth = 1280;
    private int orgDispHeight = 780;
    
    public int getOrgWidth() {
        return  orgDispWidth;
    }
    
    public int getOrgHeight() {
        return  orgDispHeight;
    }
    
    /**
     * Create the frame.
     */
    public JmpSideFlowRendererWindow() {
        this.setTransferHandler(new DropFileHandler());
        this.setTitle("JMP Side Flow Renderer");
        setBounds(10, 10, orgDispWidth, orgDispHeight);
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
        for (int i = 0; i < 16; i++) {
            String key = String.format("ch_color_%d", (i + 1));
            notesColor[i] = Utility.convertCodeToHtmlColor(sm.getCommonRegisterValue(key));
        }
        cursorColor = new Color[] { //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 1.0)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.9)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.8)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.7)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.6)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.5)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.4)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.3)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.2)), //
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.1)), //
        };//
    }

    @Override
    public void repaintAndFlipScreen() {
        super.repaintAndFlipScreen();
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
    }

    public void adjustTickBar() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        Sequence seq = midiUnit.getSequence();
        if (seq == null) {
            return;
        }

        resetPage();
    }

    @Override
    public void paint(Graphics g) {
        // super.paint(g);
        
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        BufferedImage screenImage = new BufferedImage(getOrgWidth(), getOrgHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics screenGraphic = screenImage.createGraphics();
        
        /* ノーツ描画 */
        Sequence sequence = midiUnit.getSequence();
        if (sequence != null) {
            // フリップ
            calcDispMeasCount();
            if (midiUnit.isRunning() == true) {
                flipPage();
            }
        }
        
        paintMain(screenGraphic);
        if (getWidth() == getOrgWidth() && getHeight() == getOrgHeight()) {
            g.drawImage(screenImage, 0, 0, null);
        }
        else {
            g.drawImage(screenImage, 
                    0, 0, getWidth(), getHeight(), 
                    0, 0, screenImage.getWidth(), screenImage.getHeight(), 
                    null);
        }
        
        if (layout.isVisibleMonitorStr == true) {
            int sx = (int)((double)(layout.keyWidth + 10) * ((double)getWidth() / (double)getOrgWidth()));
            int sy = 50;
            int sh = 16;
            int tc = (layout.prBackColor.getRed() + layout.prBackColor.getGreen() + layout.prBackColor.getBlue()) / 3;
            String infoStr = "";
            Color backStrColor = tc >= 128 ? Color.WHITE : Color.BLACK;
            Color topStrColor = tc < 128 ? Color.WHITE : Color.BLACK;
            infoStr = String.format("TIME: %02d:%02d / %02d:%02d", 
                    JMPCoreAccessor.getSoundManager().getPositionSecond() / 60,
                    JMPCoreAccessor.getSoundManager().getPositionSecond() % 60,
                    JMPCoreAccessor.getSoundManager().getLengthSecond() / 60,
                    JMPCoreAccessor.getSoundManager().getLengthSecond() % 60
                    );
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            g.setColor(backStrColor);
            g.drawString(infoStr, sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(infoStr, sx, sy);
            sy += sh;
            infoStr = String.format("BPM: %.2f", midiUnit.getTempoInBPM());
            g.setColor(backStrColor);
            g.drawString(infoStr, sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(infoStr, sx, sy);
            sy += sh;
            infoStr = String.format("NOTES: %d / %d", notesMonitor.getNotesCount(), notesMonitor.getNumOfNotes());
            g.setColor(backStrColor);
            g.drawString(infoStr, sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(infoStr, sx, sy);
            sy += sh;
            infoStr = String.format("NPS: %d", (int)notesMonitor.getNps());
            g.setColor(backStrColor);
            g.drawString(infoStr, sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(infoStr, sx, sy);
            sy += sh;
            infoStr = String.format("POLY: %d", notesMonitor.getPolyphony());
            g.setColor(backStrColor);
            g.drawString(infoStr, sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(infoStr, sx, sy);
            sy += sh;
            infoStr = String.format("FPS: %d", getFPS());
            g.setColor(backStrColor);
            g.drawString(infoStr, sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(infoStr, sx, sy);
        }
    }

    @Override
    public void paintComponents(Graphics g) {
        // super.paintComponents(g);
    }

    private void calcDispMeasCount() {
        int x = getZeroPosition();
        int measLen = 0;
        while (x <= getOrgWidth() * 2) {
            x += getMeasCellWidth();
            measLen++;
        }
        dispMeasCount = measLen;
    }
    
    public int getDispMeasCount() {
        return dispMeasCount;
    }

    private int cnt = 0;
    private void paintMain(Graphics g) {
        g.setColor(layout.prBackColor);
        g.fillRect(0, 0, getOrgWidth(), getOrgHeight());
        
        if (JMPCoreAccessor.getSystemManager().getStatus(ISystemManager.SYSTEM_STATUS_ID_FILE_LOADING) == true) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String str;
            int stringWidth = 0;
            int stringHeight = 0; 

            str = "_(┐「ε:)_";
            stringWidth = fm.stringWidth(str);
            stringHeight = fm.getHeight();
            g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2 - 20);
            str = "Now loading.";
            for (int i=0; i<(cnt / 10); i++) {
                str += "." ;
            }
            
            if (cnt >= 50) {
                cnt = 0;
            }
            cnt++;
            stringWidth = fm.stringWidth(str);
            stringHeight = fm.getHeight();
            g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2 + 20);
            return;
        }
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        /* ノーツ描画 */
        Sequence sequence = midiUnit.getSequence();
        if (sequence != null) {
            if (imageWorkerMgr.getNotesImage() == null) {
                // 描画が追いついていない 
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
                FontMetrics fm = g.getFontMetrics();
                String str = "Rendering in progress.";
                int stringWidth = fm.stringWidth(str);
                int stringHeight = fm.getHeight();
                g.setColor(Color.WHITE);
                g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2);
            }
            else {
                long startMeas = (long) midiUnit.getTickPosition() / sequence.getResolution();
                long offset = (long) midiUnit.getTickPosition() % sequence.getResolution();
                int offsetX = (int) (getMeasCellWidth() * (double) ((double) offset / (double) sequence.getResolution()));
                int tickX = (int) (getZeroPosition() + (startMeas * getMeasCellWidth())) + (getLeftMeas() * getMeasCellWidth());
                g.drawImage(imageWorkerMgr.getNotesImage(), layout.keyWidth - (tickX + offsetX), 0, null);
            }
        }
        else {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
            FontMetrics fm = g.getFontMetrics();
            String str = "Drag and Drop your MIDI or MIDI and AUDIO files here.";
            int stringWidth = fm.stringWidth(str);
            int stringHeight = fm.getHeight();
            g.setColor(Color.WHITE);
            g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2);
        }
        
        // キーステート描画
        if (layout.keyWidth > 0) {
            INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
            g.setColor(layout.prBackColor);
            g.fillRect(0, 0, layout.keyWidth, getOrgHeight());
            int keyHeight = getMeasCellHeight();
            int keyCount = (127 - getTopMidiNumber());
            int topOffset = (keyHeight * keyCount);
            int effWidth = (int)((double)layout.keyWidth * 0.95);
            int effHeight = keyHeight;
            int effMarginCnt = 16;
            int effMargin = 255 / effMarginCnt;
            for (int i = 0; i < 128; i++) {
                int x = 0;
                int y = topOffset + (keyHeight * i);
                int midiNo = 127 - i;
                for (int ch=0; ch<16; ch++) {
                    if (true == notesMonitor.isNoteOn(ch, midiNo)) {
                        for (int j=1; j<=effMarginCnt; j++) {
                            int w = effWidth / 15;
                            g.setColor(new Color(notesColor[ch].getRed(), notesColor[ch].getGreen(), notesColor[ch].getBlue(), 255 - effMargin * j));
                            //g.setColor(new Color(255, 255, 255, 255 - effMargin * j));
                            g.fillRect(x + (layout.keyWidth - j * w), y, w, effHeight);
                        }
                        break;
                    }
                }
            }
        }
        
        /* Tickbar描画 */
        paintTickPosition(g, layout.keyWidth);
    }

    public void resetPage() {
        calcDispMeasCount();
        
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getSequence().getResolution();
        setLeftMeas(-startMeas);
        
        imageWorkerMgr.reset(getLeftMeas(), dispMeasCount, NEXT_FLIP_COUNT);
    }
    private void flipPage() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
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
        if (layout.keyWidth <= 0) {
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
            g.drawLine(x - i, 0, x - i, getOrgHeight());
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
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (JMPCoreAccessor.getSoundManager().isPlay() == true) {
                JMPCoreAccessor.getSoundManager().stop();
            }
            else {
                JMPCoreAccessor.getSoundManager().play();
            }
        }
        else if (e.getButton() == MouseEvent.BUTTON3) {
            if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
                if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
                    JMPCoreAccessor.getWindowManager().getMainWindow().toggleWindowVisible();
                }
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
            if (files.size() >= 2) {
                
                String exMidi = JMPCoreAccessor.getSystemManager().getCommonRegisterValue(ISystemManager.COMMON_REGKEY_NO_EXTENSION_MIDI);
                String path1 = files.get(0).getPath();
                String path2 = files.get(1).getPath();
                if (Utility.checkExtensions(path1, exMidi.split(",")) == true) {
                    JMPCoreAccessor.getFileManager().loadDualFile(path1, path2);
                }
                else if (Utility.checkExtensions(path2, exMidi.split(",")) == true) {
                    JMPCoreAccessor.getFileManager().loadDualFile(path2, path1);
                }
                JMPCoreAccessor.getWindowManager().getWindow(IWindowManager.WINDOW_NAME_MIDI_SETUP).showWindow();
            }
            else {
                String path = files.get(0).getPath();
                if (Utility.checkExtensions(path, JmpSideFlowRenderer.Extensions.split(",")) == true) {
                    JMPCoreAccessor.getFileManager().loadFileToPlay(path);
                }
            }
        }
    }
}
