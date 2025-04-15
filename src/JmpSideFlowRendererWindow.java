import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.Sequence;
import javax.swing.JFrame;
import javax.swing.TransferHandler;

import function.Utility;
import jlib.core.IDataManager;
import jlib.core.ISystemManager;
import jlib.core.IWindowManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;

public class JmpSideFlowRendererWindow extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener, Runnable {
    
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    public static final int DEFAULT_WINDOW_HEIGHT = 780;
    public static final int WINDOW_FIXED_FPS = 120; //画面の限界FPS値
    public static final long DELAY_NANO = 1000000000L / WINDOW_FIXED_FPS;
    
    public static final int DEFAULT_1MEAS_WIDTH = 280;//128 * 3;
    public static final int DEFAULT_TICK_MEAS = 1;
    
    // 次のページにフリップするpx数
    private static final int NEXT_FLIP_COUNT = 0;
    
    private Canvas canvas;
    private BufferStrategy strategy;
    
    private int frameCount = 0;
    private long startTime = System.nanoTime();
    private int fps = 0;

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
                    DEFAULT_1MEAS_WIDTH * DEFAULT_TICK_MEAS, // TickBar位置
                    false, // 縦線表示
                    false, // 横線表示
                    true, // ノーツを3Dデザイン 
                    false // 情報表示 
            );

    //
    // =##= クラシック =##=
    public static final LayoutConfig CLASSIC_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(0, 0, 0), // 背景カラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 40), // ボーダーカラー
                    Utility.convertCodeToHtmlColor("#FFFFFF"), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    true, // PBの表示
                    DEFAULT_1MEAS_WIDTH * DEFAULT_TICK_MEAS, // // TickBar位置
                    true, // 縦線表示
                    false, // 横線表示
                    true, // ノーツを3Dデザイン
                    true // 情報表示 
            );

    //
    // =##= ライトテーマ =##=
    public static final LayoutConfig LIGHT_LAYOUT = //
            LayoutConfig.createConfig(//
                    Utility.convertCodeToHtmlColor("#c0c0c0"), // 背景カラー
                    Utility.convertHighLightColor(Utility.convertCodeToHtmlColor("#c0c0c0"), 40), // ボーダーカラー
                    Utility.convertCodeToHtmlColor("#000000"), // カーソルカラー
                    Utility.convertHighLightColor(new Color(0, 0, 0), 140), // ピッチベンドベースカラー
                    false, // PBの表示
                    DEFAULT_1MEAS_WIDTH * DEFAULT_TICK_MEAS, // // TickBar位置
                    true, // 縦線表示
                    false, // 横線表示
                    true, // ノーツを3Dデザイン
                    true // 情報表示
            );
    //

    private ImagerWorkerManager imageWorkerMgr = null;

    public Color[] notesColor = null;
    public Color[] cursorColor = null;
    public Color[] hitEffectColor = null;

    private int topMidiNumber = 110;
    private int leftMeas = 0;
    private int zeroPosition = 0;
    private int measCellWidth = DEFAULT_1MEAS_WIDTH;
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
        setBounds(10, 10, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        setLayout(new BorderLayout(0, 0));
        
        canvas = new Canvas();
        canvas.setBackground(Color.BLACK);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        contentPane.add(canvas, BorderLayout.CENTER);

        canvas.addMouseListener(this);
        canvas.addMouseWheelListener(this);
        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
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
                Utility.convertColorAlpha(layout.cursorMainColor, (int) (255 * 0.7)), //
        };//
        hitEffectColor = new Color[8];
        for (int i=0; i<hitEffectColor.length; i++) {
            hitEffectColor[i] = new Color(
                    layout.cursorMainColor.getRed(), 
                    layout.cursorMainColor.getGreen(), 
                    layout.cursorMainColor.getBlue(), 
                    255 - (255 / hitEffectColor.length) * i
                    );
        }
    }
    
    ScheduledExecutorService scheduler = null;
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        
        if (b) {
            canvas.requestFocusInWindow();
            canvas.createBufferStrategy(2); // ダブルバッファリング
            strategy = canvas.getBufferStrategy();
            
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this, 1000, DELAY_NANO, TimeUnit.NANOSECONDS);
            
            imageWorkerMgr.start();
        }
        else {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            }
            
            imageWorkerMgr.stop();
        }
    }
    
    public int getFPS() {
        return fps;
    }
    
    @Override
    public void run() {
        render();
        
        frameCount++;
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - startTime;
        
        if (elapsedTime >= TimeUnit.SECONDS.toNanos(1)) {
            fps = frameCount;
            frameCount = 0;
            startTime = currentTime;
        }
        
    }
    
    protected void render() {
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        try {
            paintDisplay(g);
        } finally {
            // Graphics オブジェクトの解放
            g.dispose();
        }
        
        strategy.show();
    }

    public void init() {
        imageWorkerMgr = new ImagerWorkerManager();
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
    
    private BufferedImage orgScreenImage = null;
    private Graphics orgScreenGraphic = null;
    public void paintDisplay(Graphics g) {
        
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        if (orgScreenImage == null) {
            orgScreenImage = new BufferedImage(getOrgWidth(), getOrgHeight(), BufferedImage.TYPE_INT_ARGB);
            orgScreenGraphic = orgScreenImage.createGraphics();
        }
        
        /* ノーツ描画 */
        Sequence sequence = midiUnit.getSequence();
        if (sequence != null) {
            // フリップ
            calcDispMeasCount();
            if (midiUnit.isRunning() == true) {
                flipPage();
            }
        }
        
        paintMain(orgScreenGraphic);
        if (getWidth() == getOrgWidth() && getHeight() == getOrgHeight()) {
            g.drawImage(orgScreenImage, 0, 0, null);
        }
        else {
            g.drawImage(orgScreenImage, 
                    0, 0, getWidth(), getHeight(), 
                    0, 0, orgScreenImage.getWidth(), orgScreenImage.getHeight(), 
                    null);
        }
        
        
        if (layout.isVisibleMonitorStr == true) {
            int sx = 15;
            int sy = 15;
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
//            infoStr = String.format("TICK: %d", midiUnit.getTickPosition());
//            g.setColor(backStrColor);
//            g.drawString(infoStr, sx + 1, sy + 1);
//            g.setColor(topStrColor);
//            g.drawString(infoStr, sx, sy);
//            sy += sh;
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
            infoStr = String.format("BPM: %.2f", midiUnit.getTempoInBPM());
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
            sy += sh;
/*
            for (int i=0; i<imageWorkerMgr.getNumOfWorker(); i++) {
                int dbx = sx + (i * 15);
                if (imageWorkerMgr.getWorker(i).isWait() == true) {
                    g.setColor(Color.GREEN);
                }
                else {
                    g.setColor(Color.RED);
                }
                g.fillRect(dbx, sy + 5, 10, 10);
            }
*/
        }
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

            str = "＼＿ヘ(Д｀*)";
            stringWidth = fm.stringWidth(str);
            stringHeight = fm.getHeight();
            g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2 - 20);
            str = "Now loading";
            stringWidth = fm.stringWidth(str);
            for (int i=0; i<(cnt / 10); i++) {
                str += "." ;
            }
            
            if (cnt >= 30) {
                cnt = 0;
            }
            cnt++;
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
                g.setColor(Color.WHITE);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
                FontMetrics fm = g.getFontMetrics();
                String str = "...φ(｡_｡*)";
                int stringWidth = fm.stringWidth(str);
                int stringHeight = fm.getHeight();
                g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2 - 20);
                str = "Rendering now";
                stringWidth = fm.stringWidth(str);
                stringHeight = fm.getHeight();
                g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2 + 20);
            }
            else {
                // 現在の画面に表示する相対tick位置を求める 
                long relPosTick =  midiUnit.getTickPosition() + sequence.getResolution() * getLeftMeas();
                // 相対tick位置を座標に変換(TICK × COORD / RESOLUTION)
                int tickX = (int) ((double)relPosTick * (double)getMeasCellWidth() / (double)sequence.getResolution()); 
                g.drawImage(imageWorkerMgr.getNotesImage(), /*layout.keyWidth*/ - tickX, 0, null);
                
                // 衝突エフェクト描画
                /*
                if (layout.tickBarPosition > 0) {
                    INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
                    g.setColor(layout.prBackColor);
                    int keyHeight = getMeasCellHeight();
                    int keyCount = (127 - getTopMidiNumber());
                    int topOffset = (keyHeight * keyCount);
                    int effWidth = 64;
                    int effHeight = keyHeight;
                    int w = effWidth / hitEffectColor.length;
                    for (int i = 0; i < 128; i++) {
                        int y = topOffset + (keyHeight * i);
                        int midiNo = 127 - i;
                        for (int ch=0; ch<16; ch++) {
                            if (true == notesMonitor.isNoteOn(ch, midiNo)) {
                                for (int j=0; j<hitEffectColor.length; j++) {
                                    g.setColor(hitEffectColor[j]);
                                    g.fillRect(layout.tickBarPosition + (j * w), y, w, effHeight);
                                }
                                break;
                            }
                        }
                    }
                }
                */
                
                /* Tickbar描画 */
                if (sequence != null) {
                    paintTickPosition(g, layout.tickBarPosition);
                }
            }
        }
        else {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Color.WHITE);
            String str = "_(┐「ε:)_";
            int stringWidth = fm.stringWidth(str);
            int stringHeight = fm.getHeight();
            g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2 - 20);
            str = "Drag and Drop your MIDI or MIDI and AUDIO files here.";
            stringWidth = fm.stringWidth(str);
            stringHeight = fm.getHeight();
            g.drawString(str, (getOrgWidth() - stringWidth) / 2, (getOrgHeight() - stringHeight) / 2 + 20);
        }

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
        if (layout.tickBarPosition <= 0) {
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
            g.drawLine(x + i, 0, x + i, getOrgHeight());
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
/*
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
*/
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
                
                String midiOutName = JMPCoreAccessor.getDataManager().getConfigParam(IDataManager.CFG_KEY_MIDIOUT);
                System.out.println(midiOutName);
                if (midiOutName.equalsIgnoreCase("NULL") == false) {
                    // 同時再生時にmidiOutを指定する用途は少ない？ 
                    JMPCoreAccessor.getWindowManager().getWindow(IWindowManager.WINDOW_NAME_MIDI_SETUP).showWindow();
                }
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
