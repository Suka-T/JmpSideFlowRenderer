package gui;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
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
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.Sequence;
import javax.swing.JFrame;
import javax.swing.TransferHandler;

import function.Utility;
import image.ImagerWorkerManager;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import layout.LayoutConfig;
import layout.LayoutManager;
import plg.JmpSideFlowRenderer;
import plg.SystemProperties;

public class RendererWindow extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener, Runnable {
    
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    public static final int DEFAULT_WINDOW_HEIGHT = 768;
    
    private long delayNano = 0;
    
    // 次のページにフリップするpx数
    private static final int NEXT_FLIP_COUNT = 0;
    
    private Canvas canvas;
    private BufferStrategy strategy;
    
    private int frameCount = 0;
    private int fps = 0;

    private ImagerWorkerManager imageWorkerMgr = null;
    
    private int orgDispWidth = DEFAULT_WINDOW_WIDTH;
    private int orgDispHeight = DEFAULT_WINDOW_HEIGHT;

    private int leftMeas = 0;
    private int zeroPosition = 0;
    private int measCellWidth = LayoutManager.DEFAULT_1MEAS_WIDTH;
    private int measCellHeight = orgDispHeight / 128;//5;
    private int dispMeasCount = 0;
    
    //private int topMidiNumber = 128 - ((orgDispHeight - (measCellHeight * 128)) / measCellHeight) / 2;
    private int topMidiNumber = 127;
    
    private int[] hitEffectPosY = null;
    
    private volatile boolean running = false;
    private Thread renderThread;
    
    class KeyInfo {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        int midiNo = 0;
    }
    
    private KeyInfo[] aHakken = null;
    private KeyInfo[] aKokken = null;
    
    public int getOrgWidth() {
        return  orgDispWidth;
    }
    
    public int getOrgHeight() {
        return  orgDispHeight;
    }
    
    /**
     * Create the frame.
     */
    public RendererWindow() {
        this.setTransferHandler(new DropFileHandler());
        this.setTitle("JMP Side Flow Renderer");
        setLocation(10, 10);
        getContentPane().setPreferredSize(new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
        pack();
        
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
        
        LayoutManager.getInstance().initialize(canvas);
        delayNano = 1_000_000_000 / SystemProperties.getInstance().getFixedFps();
        
        hitEffectPosY = new int[128];
        int keyHeight = getMeasCellHeight();
        int keyCount = (127 - getTopMidiNumber());
        int topOffset = (keyHeight * keyCount);
        for (int i = 0; i < 128; i++) {
            hitEffectPosY[i] = topOffset + (keyHeight * i);
        }
        
        aHakken = new KeyInfo[75];
        aKokken = new KeyInfo[53];
        
        int kkCnt = 0;
        int hkCnt = 0;
        int hkWidth = 50;
        int kkWidth = (int)(hkWidth * 0.7);
        int hakkenHeight = (128 * keyHeight) / 75;
        for (int i = 0; i < 128; i++) {
            int midiNo = 127 - i;
            int key = midiNo % 12;
            switch (key) {
                case 0:
                case 5:
                    aHakken[hkCnt] = new KeyInfo();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight;
                    aHakken[hkCnt].y -= (keyHeight / 2);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 7:
                case 9:
                case 2:
                    aHakken[hkCnt] = new KeyInfo();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight + keyHeight / 2;
                    aHakken[hkCnt].y -= (keyHeight / 2);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 4:
                case 11:
                    aHakken[hkCnt] = new KeyInfo();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 1:
                case 3:
                case 6:
                case 8:
                case 10:
                    aKokken[kkCnt] = new KeyInfo();
                    aKokken[kkCnt].x = LayoutManager.getInstance().getTickBarPosition() - kkWidth;
                    aKokken[kkCnt].y = hitEffectPosY[i];
                    aKokken[kkCnt].width = kkWidth;
                    aKokken[kkCnt].height = keyHeight;
                    aKokken[kkCnt].midiNo = midiNo;
                    kkCnt++;
                    break;
                default:
                    break;
            }
        }
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        
        if (b) {
            canvas.requestFocusInWindow();
            canvas.createBufferStrategy(2); // ダブルバッファリング
            strategy = canvas.getBufferStrategy();
            
            running = true;
            renderThread = new Thread(this::run, "RenderThread");
            renderThread.start();
            
            imageWorkerMgr.start();
        }
        else {
            running = false;
            try {
                if (renderThread != null) {
                    renderThread.join();
                }
            } catch (InterruptedException e) {
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
        long startTime = System.nanoTime();
        long nextFrameTime = startTime;
        frameCount = 0;

        while (running) {
            long now = System.nanoTime();
            
            if (now >= nextFrameTime) {
                render();
                frameCount++;

                long elapsed = now - startTime;
                if (elapsed >= TimeUnit.SECONDS.toNanos(1)) {
                    fps = frameCount;
                    frameCount = 0;
                    startTime = now;
                }

                nextFrameTime += delayNano;
            } else {
                // 次のフレームまで余裕があれば軽く寝る
                long sleepTimeMillis = (nextFrameTime - now) / 1_000_000;
                if (sleepTimeMillis > 0) {
                    try {
                        Thread.sleep(sleepTimeMillis);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
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
        imageWorkerMgr = new ImagerWorkerManager(getOrgWidth(), getOrgHeight());
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
    
    private StringBuilder sb = new StringBuilder(64); // 初期容量を指定
    private Font infoFont = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private volatile VolatileImage orgScreenImage = null;
    private volatile Graphics orgScreenGraphic = null;
    public void paintDisplay(Graphics g) {
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        /* ノーツ描画 */
        Sequence sequence = midiUnit.getSequence();
        
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (orgScreenImage == null || orgScreenImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            orgScreenImage = LayoutManager.getInstance().createLayerImage(getOrgWidth(), getOrgHeight());
            orgScreenGraphic = orgScreenImage.createGraphics();
        }
        
        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();
        
        if (JMPCoreAccessor.getSystemManager().getStatus(ISystemManager.SYSTEM_STATUS_ID_FILE_LOADING) == true) {
            g.setColor(LayoutManager.getInstance().getBackColor());
            g.fillRect(0, 0, paneWidth, paneHeight);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int stringWidth = 0;
            int stringHeight = 0; 

            sb.setLength(0);
            sb.append("＼＿ヘ(Д｀*)");
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            g.drawString(sb.toString(), (paneWidth - stringWidth) / 2, (paneHeight - stringHeight) / 2 - 20);
            sb.setLength(0);
            sb.append("Now loading");
            stringWidth = fm.stringWidth(sb.toString());
            for (int i=0; i<(cnt / 10); i++) {
                sb.append(".");
            }
            
            if (cnt >= 30) {
                cnt = 0;
            }
            cnt++;
            stringHeight = fm.getHeight();
            g.drawString(sb.toString(), (paneWidth - stringWidth) / 2, (paneHeight - stringHeight) / 2 + 20);
        }
        else if (sequence == null) {
            g.setColor(LayoutManager.getInstance().getBackColor());
            g.fillRect(0, 0, paneWidth, paneHeight);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Color.WHITE);
            sb.setLength(0);
            sb.append("_(┐「ε:)_");
            int stringWidth = fm.stringWidth(sb.toString());
            int stringHeight = fm.getHeight();
            g.drawString(sb.toString(), (paneWidth - stringWidth) / 2, (paneHeight - stringHeight) / 2 - 20);
            sb.setLength(0);
            sb.append("Drag and Drop your MIDI or MIDI and AUDIO files here.");
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            g.drawString(sb.toString(), (paneWidth - stringWidth) / 2, (paneHeight - stringHeight) / 2 + 20);
        }
        else if (imageWorkerMgr.getNotesImage() == null) {
            // 描画が追いついていない
            g.setColor(LayoutManager.getInstance().getBackColor());
            g.fillRect(0, 0, paneWidth, paneHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
            FontMetrics fm = g.getFontMetrics();
            sb.setLength(0);
            sb.append("...φ(｡_｡*)");
            int stringWidth = fm.stringWidth(sb.toString());
            int stringHeight = fm.getHeight();
            g.drawString(sb.toString(), (paneWidth - stringWidth) / 2, (paneHeight - stringHeight) / 2 - 20);
            sb.setLength(0);
            sb.append("Rendering now");
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            g.drawString(sb.toString(), (paneWidth - stringWidth) / 2, (paneHeight - stringHeight) / 2 + 20);
        }
        else {
            paintContents(orgScreenGraphic);
            
            Graphics2D g2 = (Graphics2D) g;
            
            // 補間方法を設定 
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); //バイリニア補間 
            //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); //高速 
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            
            Dimension dim = this.getContentPane().getSize();
            g.drawImage(orgScreenImage, 
                    0, 0, (int)dim.getWidth(), (int)dim.getHeight(), 
                    0, 0, orgScreenImage.getWidth(), orgScreenImage.getHeight(), 
                    null);
        }
        

        if (LayoutManager.getInstance().isVisibleInfoStr() == true) {
            int sx = 10;
            int sy = 20;
            int sh = 18;
            Color bgColor = LayoutManager.getInstance().getBackColor();
            int tc = (bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3;
            Color backStrColor = tc >= 128 ? Color.WHITE : Color.BLACK;
            Color topStrColor = tc < 128 ? Color.WHITE : Color.BLACK;
            g.setFont(infoFont);
            
            sb.setLength(0);
            sb.append("TIME: ");
            long val1 = JMPCoreAccessor.getSoundManager().getPositionSecond() / 60;
            if (val1 < 10) sb.append('0');
            sb.append(val1);
            sb.append(":");
            long val2 = JMPCoreAccessor.getSoundManager().getPositionSecond() % 60;
            if (val2 < 10) sb.append('0');
            sb.append(val2);
            sb.append(" / ");
            val1 = JMPCoreAccessor.getSoundManager().getLengthSecond() / 60;
            if (val1 < 10) sb.append('0');
            sb.append(val1);
            sb.append(":");
            val2 = JMPCoreAccessor.getSoundManager().getLengthSecond() % 60;
            if (val2 < 10) sb.append('0');
            sb.append(val2);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
            
            sb.setLength(0);
            sb.append("TICK: ");
            val1 = JMPCoreAccessor.getSoundManager().getMidiUnit().getTickPosition();
            sb.append(val1);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
            
            sb.setLength(0);
            sb.append("NOTES: ");
            val1 = notesMonitor.getNotesCount();
            val2 = notesMonitor.getNumOfNotes();
            sb.append(val1).append(" / ").append(val2);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
            
            sb.setLength(0);
            val1 = (long)notesMonitor.getNps();
            sb.append("NPS: ").append(val1);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
            
            sb.setLength(0);
            val1 = (long)notesMonitor.getPolyphony();
            sb.append("POLY: ").append(val1);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
            
            sb.setLength(0);
            val1 = (int)midiUnit.getTempoInBPM();
            val2 = (int)((midiUnit.getTempoInBPM() - val1) * 100);
            sb.append("BPM: ").append(val1).append(".").append(val2);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
            
            sb.setLength(0);
            val1 = getFPS();
            sb.append("FPS: ").append(val1);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
            
            if (SystemProperties.getInstance().isDebugMode() == true) {
	            for (int i=0; i<imageWorkerMgr.getNumOfWorker(); i++) {
	                int dbx = sx + (i * 15);
	                if (imageWorkerMgr.getWorker(i).isExec() == false) {
	                    g.setColor(Color.GREEN);
	                }
	                else {
	                    g.setColor(Color.RED);
	                }
	                g.fillRect(dbx, sy + 5, 10, 10);
	            }
            }
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
    private void paintContents(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g.setColor(LayoutManager.getInstance().getBackColor());
        g.fillRect(0, 0, getOrgWidth(), getOrgHeight());
        
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        /* ノーツ描画 */
        Sequence sequence = midiUnit.getSequence();

        // フリップ
        calcDispMeasCount();
        if (midiUnit.isRunning() == true) {
            flipPage();
        }
        
        // 現在の画面に表示する相対tick位置を求める 
        long tickPos = midiUnit.getTickPosition();
        long relPosTick =  tickPos + sequence.getResolution() * getLeftMeas();
        // 相対tick位置を座標に変換(TICK × COORD / RESOLUTION)
        int tickX = (int) ((double)relPosTick * (double)getMeasCellWidth() / (double)sequence.getResolution()); 
        g.drawImage(imageWorkerMgr.getNotesImage(), /*layout.keyWidth*/ - tickX, 0, null);
        
        int tickBarPosition = LayoutManager.getInstance().getTickBarPosition();
        if (tickBarPosition > 0) {
            int effOrgX = tickBarPosition;
            if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
                /*  Keyboard */
                int keyFocus = 0;
                Color keyBgColor;
                for (int i=0; i<aHakken.length; i++) {
                	if (LayoutManager.getInstance().getColorRule() == LayoutConfig.EColorRule.Channel) {
                		keyFocus = notesMonitor.getTopNoteOnChannel(aHakken[i].midiNo);
                	}
                	else if (LayoutManager.getInstance().getColorRule() == LayoutConfig.EColorRule.Track) {
                		keyFocus = notesMonitor.getTopNoteOnTrack(aHakken[i].midiNo);
                	}
                	
                    if (keyFocus != -1) {
                        keyBgColor = LayoutManager.getInstance().getNotesColor(keyFocus);
                    }
                    else {
                        keyBgColor = Color.WHITE;
                    }
                    
                    g.setColor(keyBgColor);
                    g.fill3DRect(aHakken[i].x, aHakken[i].y, aHakken[i].width, aHakken[i].height, true);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(aHakken[i].x, aHakken[i].y, aHakken[i].width, aHakken[i].height);
                }
                for (int i=0; i<aKokken.length; i++) {
                	if (LayoutManager.getInstance().getColorRule() == LayoutConfig.EColorRule.Channel) {
                		keyFocus = notesMonitor.getTopNoteOnChannel(aKokken[i].midiNo);
                	}
                	else if (LayoutManager.getInstance().getColorRule() == LayoutConfig.EColorRule.Track) {
                		keyFocus = notesMonitor.getTopNoteOnTrack(aKokken[i].midiNo);
                	}
                	
                    if (keyFocus != -1) {
                        keyBgColor = LayoutManager.getInstance().getNotesColor(keyFocus);
                    }
                    else {
                        keyBgColor = Color.BLACK;
                    }
                    
                    g.setColor(keyBgColor);
                    g.fill3DRect(aKokken[i].x, aKokken[i].y, aKokken[i].width, aKokken[i].height, true);
                }
            }
            
            /* 衝突エフェクト描画 */
            Color hitEffectColor = LayoutManager.getInstance().getCursorEffectColor();
            g.setColor(LayoutManager.getInstance().getBackColor());
            int keyHeight = getMeasCellHeight();
            int effWidth = 4;
            int effx = 0;
            g.setColor(hitEffectColor);
            for (int i = 0; i < 128; i++) {
                int midiNo = 127 - i;
                boolean isFocus = false;
                if (LayoutManager.getInstance().getColorRule() == LayoutConfig.EColorRule.Channel) {
                	isFocus = (notesMonitor.getTopNoteOnChannel(midiNo) != -1) ? true : false;
                }
                else {
                	isFocus = (notesMonitor.getTopNoteOnTrack(midiNo) != -1) ? true : false;
                }
                
                if (isFocus == true) {
                    effx = effOrgX;
                    for (int j = 0; j < 10; j++) {
                        float alpha = 1.0f - j * 0.1f;
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g2d.fillRect(effx, hitEffectPosY[i], effWidth, keyHeight);
                        effx += effWidth;
                    }
                    g2d.setComposite(AlphaComposite.SrcOver);
                }
            }
            
            /* Tickbar描画 */
            Color csrColor = LayoutManager.getInstance().getCursorColor();
            g2d.setColor(csrColor);
            g2d.drawLine(tickBarPosition - 1, 0, tickBarPosition - 1, getOrgHeight());
            g2d.drawLine(tickBarPosition, 0, tickBarPosition, getOrgHeight());
            g2d.drawLine(tickBarPosition + 1, 0, tickBarPosition + 1, getOrgHeight());
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
                JMPCoreAccessor.getSoundManager().initPosition();
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
            }
            else {
                String path = files.get(0).getPath();
                if (Utility.checkExtensions(path, JmpSideFlowRenderer.Extensions.split(",")) == true) {
                    JMPCoreAccessor.getFileManager().loadFile(path);
                }
            }
        }
    }
}
