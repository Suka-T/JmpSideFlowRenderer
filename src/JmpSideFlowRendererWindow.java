import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
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
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import drawLib.gui.DrawLibFrame;
import drawLib.gui.FrameRate;
import function.Utility;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiToolkit;
import jlib.midi.IMidiUnit;
import jlib.midi.MidiUtility;

public class JmpSideFlowRendererWindow extends DrawLibFrame implements MouseListener, MouseMotionListener, MouseWheelListener {
    
    public static final int WINDOW_FIXED_FPS = 60; //画面の限界FPS値

    private JPanel contentPane;

    // 現在のレイアウト設定
    public LayoutConfig layout = CLASSIC_LAYOUT;//NOTESBASE_LAYOUT;

    private boolean useDoubleOffscreen = false;

    private static final Color FIX_FOCUS_NOTES_BGCOLOR = Color.WHITE;
    private static final Color FIX_FOCUS_NOTES_BDCOLOR = Color.GREEN;

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
                    true, // PBの表示
                    true, // tickバーの表示
                    true, // 縦線表示
                    true, // 横線表示
                    true, // tickバーの高精度
                    false, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(0, 0, 0), 150), // ダークアウトカラー
                    true, // フォーカスノーツ色描画
                    true // フォーカスPB色描画
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
                    false, // フォーカス色描画
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
                    false, // フォーカス色描画
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
                    true, // フォーカス色描画
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
                    false, // フォーカス色描画
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
                    false, // フォーカス色描画
                    false // フォーカスPB色描画
            );

    //
    // =##= サクラっぽいGUI =##=
    public static final LayoutConfig SAKURA_LAYOUT = //
            LayoutConfig.createConfig(//
                    new Color(255, 240, 255), // 背景カラー
                    Utility.convertHighLightColor(new Color(255, 220, 255), -100), // ボーダーカラー
                    Utility.convertHighLightColor(new Color(255, 220, 255), -100), // カーソルカラー
                    Utility.convertHighLightColor(new Color(255, 220, 255), -140), // ピッチベンドベースカラー
                    -100, // ノーツのボーダーハイライト
                    200, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    false, // PBの表示
                    true, // tickバーの表示
                    false, // 縦線表示
                    false, // 横線表示
                    true, // tickバーの高精度
                    true, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(255, 240, 255), 180), // ダークアウトカラー
                    false, // フォーカス色描画
                    false // フォーカスPB色描画
            );

    //
    // =##= サクラっぽいGUI =##=
    public static final LayoutConfig SAKURA_LAYOUT2 = //
            LayoutConfig.createConfig(//
                    new Color(255, 200, 227), // 背景カラー
                    Utility.convertHighLightColor(new Color(255, 200, 227), 100), // ボーダーカラー
                    Utility.convertHighLightColor(new Color(255, 200, 227), 100), // カーソルカラー
                    Utility.convertHighLightColor(new Color(255, 200, 227), 140), // ピッチベンドベースカラー
                    100, // ノーツのボーダーハイライト
                    200, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    true, // PBの表示
                    true, // tickバーの表示
                    false, // 縦線表示
                    false, // 横線表示
                    true, // tickバーの高精度
                    true, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(255, 200, 227), 150), // ダークアウトカラー
                    false, // フォーカス色描画
                    true // フォーカスPB色描画
            );
    // =##= サクラっぽいGUI =##=
    public static final LayoutConfig SAKURA_LAYOUT3 = //
            LayoutConfig.createConfig(//
                    new Color(255, 251, 240), // 背景カラー
                    Utility.convertHighLightColor(new Color(255, 251, 240), -20), // ボーダーカラー
                    new Color(255, 168, 211), // カーソルカラー
                    Utility.convertHighLightColor(new Color(255, 251, 240), -80), // ピッチベンドベースカラー
                    200, // ノーツのボーダーハイライト
                    -50, // ノーツのフォーカスハイライト
                    false, // ノーツのフォーカスデザインを固定
                    true, // PBの表示
                    true, // tickバーの表示
                    true, // 縦線表示
                    true, // 横線表示
                    true, // tickバーの高精度
                    true, // ダークアウトモード
                    Utility.convertColorAlpha(new Color(255, 251, 240), 200), // ダークアウトカラー
                    false, // フォーカス色描画
                    false // フォーカスPB色描画
            );

    // 次のページにフリップするpx数
    private static final int NEXT_FLIP_COUNT = 1;

    private IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();

    // private NotesThread notesThread = null;
    private NotesThread firstNotesThread = null;
    private NotesThread secondNotesThread = null;
    private NotesThread currentNotesThread = null;
    private NotesThread nextNotesThread = null;

    private Color[] notesColor = null;
    private Color[] notesBorderColor = null;
    private Color[] notesFocusColor = null;
    private Color[] cursorColor = new Color[] { //
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
    private int measCellWidth = 100;
    private int measCellHeight = 8;
    private int dispMeasCount = 0;

    private class NotesThread extends Thread {
        private int leftMeasTh = 0;
        private int fps = 120;
        private long fixSleep = (1000 << 16) / fps;
        private long errorTime = 0;
        private long sleepTime = 0;
        private long pastTime;
        private long newTime = System.currentTimeMillis() << 16;
        private FrameRate frameRate = null;

        private BufferedImage notesImage = null;
        // private Graphics notesGraphics;
        private BufferedImage offScreenNotesImage;
        private Graphics offScreenNotesGraphic;
        private boolean isRunnable = true;

        public NotesThread() {
            isRunnable = true;
            frameRate = new FrameRate();
        }

        public void disposeNotesImage() {
            notesImage = null;
        }

        public BufferedImage getNotesImage() {
            return notesImage;
        }

        @Override
        public void run() {
            while (isRunnable) {
                try {
                    if (isVisible() == false) {
                        NotesThread.sleep(200);
                        continue;
                    }

                    // 再描画前のタイムを保持
                    fixSleep = (1000 << 16) / 60;
                    pastTime = System.currentTimeMillis() << 16;

                    // if (sequence == null) {
                    // Thread.sleep(20);
                    // continue;
                    // }

                    if (notesImage == null) {
                        // ノーツ画像
                        notesImage = (BufferedImage) createImage(getWidth(), getHeight());
                    }

                    // オフスクリーン
                    offScreenNotesImage = (BufferedImage) createImage(getWidth(), getHeight());
                    offScreenNotesGraphic = offScreenNotesImage.getGraphics();
                    paintBorder(offScreenNotesGraphic);
                    paintNotes(offScreenNotesGraphic, getLeftMeasTh());
                    notesImage.getGraphics().drawImage(offScreenNotesImage, 0, 0, null);

                    frameRate.frameCount();

                    // Thread.sleep(1);

                    newTime = System.currentTimeMillis() << 16;
                    sleepTime = fixSleep - (newTime - pastTime) - errorTime;
                    sleepTime = sleepTime < 0x02 ? 0x02 : sleepTime;
                    pastTime = newTime;

                    // fps固定のためのスリープ処理
                    Utility.threadSleep(sleepTime >> 16);
                    newTime = System.currentTimeMillis() << 16;
                    errorTime = newTime - pastTime - sleepTime;
                }
                catch (Exception e) {

                }
            }
        }

        public int getNFPS() {
            return (int) frameRate.getFrameRate();
        }

        public void exit() {
            isRunnable = false;
        }

        public int getLeftMeasTh() {
            return leftMeasTh;
        }

        public void setLeftMeasTh(int leftMeasTh) {
            this.leftMeasTh = leftMeasTh;
        }
    }

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

        currentNotesThread.disposeNotesImage();
        if (useDoubleOffscreen == true) {

        }
    }

    public void init() {
        firstNotesThread = new NotesThread();
        firstNotesThread.start();

        if (useDoubleOffscreen == true) {
            secondNotesThread = new NotesThread();
            secondNotesThread.start();
        }

        currentNotesThread = firstNotesThread;
        nextNotesThread = secondNotesThread;

        initPane();
        setFixedFPS(WINDOW_FIXED_FPS);
    }

    public void exit() {
        try {
            exitPane();

            if (firstNotesThread != null) {
                firstNotesThread.exit();
                firstNotesThread.join();
            }
            if (secondNotesThread != null) {
                secondNotesThread.exit();
                secondNotesThread.join();
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void adjustTickBar() {
        Sequence seq = midiUnit.getSequence();
        if (seq == null) {
            return;
        }

        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getSequence().getResolution();
        setLeftMeas(-startMeas);
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

        /* ノーツ描画 */
        Image notesImage = currentNotesThread.getNotesImage();
        g.drawImage(notesImage, 0, 0, null);

        /* PBライン描画 */
        int pbMaxHeight = 100;
        int pbCenterY = (pbMaxHeight / 2) + 100;
        if (layout.isVisiblePb == true) {
            if (layout.isDrawFocusPbColor == true) {
                int tickX = getTickbarX();
                g.setColor(FIX_FOCUS_NOTES_BGCOLOR);
                g.drawLine(0, pbCenterY, tickX - 1, pbCenterY);
                g.setColor(FIX_FOCUS_NOTES_BDCOLOR);
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
            currentNotesThread.setLeftMeasTh(getLeftMeas());
            if (useDoubleOffscreen == true) {
                int offsetLeftMeas = getLeftMeas();
                offsetLeftMeas = (offsetLeftMeas < 0) ? -(offsetLeftMeas) : offsetLeftMeas;
                int flipMergin = -(NEXT_FLIP_COUNT);
                int flipLine = (offsetLeftMeas + (dispMeasCount * 2) + flipMergin);
                nextNotesThread.setLeftMeasTh(-(flipLine));
            }
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

    private void flipPage() {
        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getSequence().getResolution();
        int offsetLeftMeas = getLeftMeas();
        offsetLeftMeas = (offsetLeftMeas < 0) ? -(offsetLeftMeas) : offsetLeftMeas;
        int flipMergin = -(NEXT_FLIP_COUNT);
        int flipLine = (offsetLeftMeas + dispMeasCount + flipMergin);
        if (startMeas >= flipLine) {
            if (useDoubleOffscreen == true) {
                NotesThread tmp = currentNotesThread;
                currentNotesThread = nextNotesThread;
                nextNotesThread = tmp;
            }
            setLeftMeas(-(flipLine));
            if (useDoubleOffscreen == true) {
                currentNotesThread.setLeftMeasTh(getLeftMeas());
                if (useDoubleOffscreen == true) {
                    offsetLeftMeas = getLeftMeas();
                    offsetLeftMeas = (offsetLeftMeas < 0) ? -(offsetLeftMeas) : offsetLeftMeas;
                    flipMergin = -(NEXT_FLIP_COUNT);
                    flipLine = (offsetLeftMeas + (dispMeasCount * 2) + flipMergin);
                    System.out.println("" + flipLine);
                    nextNotesThread.setLeftMeasTh(-(flipLine));
                }
            }
        }
    }

    private void paintNotes(Graphics g, int leftMeas) {

        paintBorder(g);

        IMidiToolkit toolkit = JMPCoreAccessor.getSoundManager().getMidiToolkit();
        Sequence sequence = midiUnit.getSequence();
        if (sequence == null) {
            return;
        }

        // 上部位置の調整
        int keyCount = (127 - getTopMidiNumber());
        int topOffset = (getMeasCellHeight() * keyCount);

        long vpLenTick = (dispMeasCount * sequence.getResolution());
        long vpStartTick = -(leftMeas) * sequence.getResolution();
        long vpEndTick = -(leftMeas) * sequence.getResolution() + vpLenTick;

        int pbMaxHeight = 100;
        int pbCenterY = (pbMaxHeight / 2) + 100;

        if (layout.isVisiblePb == true) {
            g.setColor(layout.pbBaseLineColor);
            g.drawLine(0, pbCenterY, getWidth(), pbCenterY);
        }

        Track[] tracks = sequence.getTracks();
        for (int trkCount = tracks.length - 1; trkCount >= 0; trkCount--) {

            List<Integer> pbBufferX = new ArrayList<Integer>();
            List<Integer> pbBufferY = new ArrayList<Integer>();
            for (int i = 0; i < tracks[trkCount].size(); i++) {
                // Midiメッセージを取得
                MidiEvent event = tracks[trkCount].get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    // ShortMessage解析
                    MidiEvent startEvent = event;
                    if (vpEndTick < startEvent.getTick()) {
                        continue;
                    }
                    if (vpStartTick > (startEvent.getTick() + vpLenTick)) {
                        continue;
                    }

                    MidiEvent endEvent = event;
                    ShortMessage sMes = (ShortMessage) message;
                    if (toolkit.isNoteOn(sMes) == true) {
                        /* ノーツ描画 */

                        // Note ON
                        int startPoint = i - 10;
                        startPoint = (startPoint < 0) ? 0 : startPoint;
                        for (int j = startPoint; j < tracks[trkCount].size(); j++) {
                            /* 次のNote Offを探索 */
                            MidiEvent dEvent = tracks[trkCount].get(j);
                            if (dEvent.getTick() <= startEvent.getTick()) {
                                continue;
                            }
                            MidiMessage dMessage = dEvent.getMessage();
                            if (dMessage instanceof ShortMessage) {
                                ShortMessage dMes = (ShortMessage) dMessage;
                                if (toolkit.isNoteOff(dMes) == true) {
                                    if (dMes.getData1() == sMes.getData1()) {
                                        endEvent = dEvent;
                                        break;
                                    }
                                }
                            }
                        }

                        if (vpStartTick > endEvent.getTick()) {
                            continue;
                        }

                        // 描画開始
                        int key = sMes.getData1();
                        int startMeas = (int) ((double) startEvent.getTick() / (double) sequence.getResolution());
                        int startOffset = (int) ((double) startEvent.getTick() % (double) sequence.getResolution());
                        startMeas += leftMeas;
                        int x = (int) (getZeroPosition() + (startMeas * getMeasCellWidth()))
                                + (int) (getMeasCellWidth() * (double) ((double) startOffset / (double) sequence.getResolution()));

                        int y = ((127 - key) * getMeasCellHeight()) + topOffset;

                        long tickLen = endEvent.getTick() - startEvent.getTick();
                        int width = (int) ((double) (getMeasCellWidth()) * (double) ((double) tickLen / (double) sequence.getResolution()));

                        int height = getMeasCellHeight();

                        int colorIndex = sMes.getChannel();

                        // ノーマルカラー
                        Color bgColor = notesColor[colorIndex];
                        Color bdColor = notesBorderColor[colorIndex];
                        if (layout.isDrawFocusNotesColor == true) {
                            if (startEvent.getTick() <= midiUnit.getTickPosition() && midiUnit.getTickPosition() <= endEvent.getTick()
                                    /*|| (sequencer.getTickPosition() >= endEvent.getTick())*/) {
                                // フォーカスカラー
                                if (layout.fixFocusNotesDesign == false) {
                                    bgColor = notesFocusColor[colorIndex];
                                    bdColor = notesColor[colorIndex];
                                }
                                else {
                                    bgColor = FIX_FOCUS_NOTES_BGCOLOR;
                                    bdColor = FIX_FOCUS_NOTES_BDCOLOR;
                                }
                            }
                        }

                        // if (sMes.getChannel() == 0) {
                        if (width > 2) {
                            g.setColor(bgColor);
                            g.fillRect(x, y, width, height + 1);
                            g.setColor(bdColor);
                            g.drawRect(x, y, width, height);
                        }
                        else {
                            g.setColor(bgColor);
                            g.fillRect(x, y, 1, height + 1);
                            g.setColor(bdColor);
                            g.drawLine(x, y, x, y);
                            g.drawLine(x, y + height, x, y + height);
                            // g.drawLine(x, y+height, x+width-1, y+height);
                        }
                        // }
                    }
                    else if (toolkit.isPitchBend(sMes) == true) {
                        if (layout.isVisiblePb == true) {
                            /* ピッチベンド描画 */
                            int pbValue = MidiUtility.convertPitchBendValue(sMes);
                            pbValue -= 8192;

                            int signed = (pbValue < 0) ? -1 : 1;

                            int startMeas = (int) ((double) event.getTick() / (double) sequence.getResolution());
                            int startOffset = (int) ((double) event.getTick() % (double) sequence.getResolution());
                            startMeas += leftMeas;
                            int x = (int) (getZeroPosition() + (startMeas * getMeasCellWidth()))
                                    + (int) (getMeasCellWidth() * (double) ((double) startOffset / (double) sequence.getResolution()));

                            int absPbValue = signed * pbValue;
                            int y = pbCenterY - (signed * ((absPbValue * pbMaxHeight) / 8192));

                            // PB描画
                            Color pbColor = notesColor[sMes.getChannel()];
                            if (layout.isDrawFocusPbColor == true) {
                                if (event.getTick() <= midiUnit.getTickPosition()) {
                                    pbColor = FIX_FOCUS_NOTES_BDCOLOR;
                                }
                            }
                            int pastX = 0;
                            int pastY = pbCenterY;
                            if (pbBufferX.isEmpty() == false) {
                                pastX = pbBufferX.get(pbBufferX.size() - 1);
                                pastY = pbBufferY.get(pbBufferY.size() - 1);
                            }

                            g.setColor(pbColor);
                            g.drawLine(pastX, pastY, x, pastY);
                            g.drawLine(x, pastY, x, y);
                            pbBufferX.add(x);
                            pbBufferY.add(y);
                        }
                    }
                }
            } /* Trk End */
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
    }

    public int getLeftMeas() {
        return leftMeas;
    }

    public void setLeftMeas(int leftMeas) {
        this.leftMeas = leftMeas;
        if (useDoubleOffscreen == false) {
            currentNotesThread.setLeftMeasTh(leftMeas);
        }
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
