import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiToolkit;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import jlib.midi.MidiUtility;

class ImageWorker implements Runnable {
    public static final Color FIX_FOCUS_NOTES_BGCOLOR = Color.WHITE;
    public static final Color FIX_FOCUS_NOTES_BDCOLOR = Color.GREEN;
    
    private int leftMeasTh = 0;
    private BufferedImage offScreenNotesImage;
    private Graphics2D offScreenNotesGraphic;
    private boolean isWait = true;
    private boolean doClear = true;
    
    private int[] indexCache = null;
    private MidiEvent[][] noteOnEvents = null;
    private List<Integer> pbBufferX = null;
    private List<Integer> pbBufferY = null;
    
    ScheduledExecutorService scheduler = null;

    public ImageWorker() {
        super();
        
        noteOnEvents = new MidiEvent[16][];
        for (int i=0; i<16; i++) {
            noteOnEvents[i] = new MidiEvent[128];
        }
        pbBufferX = new ArrayList<Integer>();
        pbBufferY = new ArrayList<Integer>();
    }
    
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this, 0, 200, TimeUnit.MILLISECONDS);
    }
    
    public void stop() {
        scheduler.shutdown();
    }
    
    public boolean isWait() {
        return isWait;
    }
    public synchronized void setWait(boolean wait) {
        isWait = wait;
    }

    public void clearImage() {
        //notesImage = null;
        doClear = true;
    }
    
    public void disposeImage() {
        offScreenNotesImage = null;
    }

    public BufferedImage getNotesImage() {
        return offScreenNotesImage;
    }
    
    public int getWidth() {
        return JmpSideFlowRenderer.MainWindow.getOrgWidth();
    }
    public int getHeight() {
        return JmpSideFlowRenderer.MainWindow.getOrgHeight();
    }
    
    public int getImageWidth() {
        return (getWidth() + JmpSideFlowRenderer.MainWindow.layout.tickBarPosition) * 3;
    }
    public int getImageHeight() {
        return getHeight();
    }
    
    public final int[] getTrackCache() {
        return indexCache;
    }
    
    public void copyTrackCacheFrom(int[] src) {
        if (indexCache != null) {
            for (int i=0; i<src.length; i++) {
                indexCache[i] = src[i];
            }
        }
    }

    @Override
    public void run() {
        boolean isLoading  = JMPCoreAccessor.getSystemManager().getStatus(ISystemManager.SYSTEM_STATUS_ID_FILE_LOADING);
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        Sequence sequence =  midiUnit.getSequence();
        if (JmpSideFlowRenderer.MainWindow.isVisible() == false) {
            offScreenNotesImage = null; //イメージオブジェクトのメモリを解放 
            return;
        }
        else if (sequence == null || isWait == true || isLoading == true) {
            return;
        }
        
        if (offScreenNotesImage == null) {
            // ノーツ画像
            offScreenNotesImage = new BufferedImage(getImageWidth(), getImageHeight(), BufferedImage.TYPE_INT_ARGB);
            offScreenNotesGraphic = offScreenNotesImage.createGraphics();
            indexCache = null;
            doClear = true;
            setWait(false);
        }

        if (doClear == true) {
            Graphics2D g2d = offScreenNotesImage.createGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            g2d.fillRect(0, 0, getImageWidth(), getImageHeight());
            g2d.dispose();
            doClear = false;
        }

        // オフスクリーン
        //paintBorder(offScreenNotesGraphic);
        paintNotes(offScreenNotesGraphic, getLeftMeasTh());
        isWait = true;
    }

    public int getLeftMeasTh() {
        return leftMeasTh;
    }

    public void setLeftMeasTh(int leftMeasTh) {
        this.leftMeasTh = leftMeasTh;
    }
    
    public void makeImage() {
        isWait = false;
    }
    
    protected void paintBorder(Graphics g) {
        JmpSideFlowRendererWindow mainWindow = JmpSideFlowRenderer.MainWindow;
        g.setColor(mainWindow.layout.prBorderColor);
        int x = mainWindow.getZeroPosition();
        int y = 0;
        if (mainWindow.layout.isVisibleHorizonBorder == true) {
            while (y <= getImageHeight()) {
                g.drawLine(x, y, x + getImageWidth(), y);
                y += mainWindow.getMeasCellHeight();
            }
        }
        x = mainWindow.getZeroPosition();
        y = 0;
        while (x <= getImageWidth()) {
            if (mainWindow.layout.isVisibleVerticalBorder == true) {
                g.drawLine(x, y, x, y + getImageHeight());
            }
            x += mainWindow.getMeasCellWidth();
        }
    }

    protected void paintNotes(Graphics g, int leftMeas) {
        JmpSideFlowRendererWindow mainWindow = JmpSideFlowRenderer.MainWindow;
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        IMidiToolkit toolkit = JMPCoreAccessor.getSoundManager().getMidiToolkit();
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        Sequence sequence = midiUnit.getSequence();
        if (sequence == null) {
            return;
        }
        
        paintBorder(g);

        // 上部位置の調整
        int offsetCoordX = mainWindow.layout.tickBarPosition;
        int offsetCoordXtoMeas = offsetCoordX / mainWindow.getMeasCellWidth();
        int offsetCoordXtoTick = offsetCoordXtoMeas * sequence.getResolution();
        int totalMeasCount = (int)((double)mainWindow.getDispMeasCount() * 2.0);
        int keyCount = (127 - mainWindow.getTopMidiNumber());
        int topOffset = (mainWindow.getMeasCellHeight() * keyCount);

        long absLeftMeas = -(leftMeas);
        long vpLenTick = (totalMeasCount * sequence.getResolution());
        long vpStartTick = absLeftMeas * sequence.getResolution() - offsetCoordXtoTick;
        long vpEndTick = vpStartTick + vpLenTick + (offsetCoordXtoTick * 2);

        int pbMaxHeight = 100;
        int pbCenterY = (pbMaxHeight / 2) + 100;

        if (mainWindow.layout.isVisiblePb == true) {
            g.setColor(mainWindow.layout.pbBaseLineColor);
            g.drawLine(0, pbCenterY, getImageWidth(), pbCenterY);
        }
        
        if (notesMonitor.getNumOfTrack() <= 0) {
            return;
        }
        
        if (indexCache == null) {
            indexCache = new int[notesMonitor.getNumOfTrack()];
            for (int i=0; i<indexCache.length; i++) {
                indexCache[i] = 0;
            }
        }
        
        //for (int trkIndex = notesMonitor.getNumOfTrack() - 1; trkIndex >= 0; trkIndex--) {
        for (int trkIndex = 0; trkIndex < notesMonitor.getNumOfTrack(); trkIndex++) {
            for (int i=0; i<16; i++) {
                for (int j=0; j<128; j++) {
                    noteOnEvents[i][j] = null;
                }
            }
            
            pbBufferX.clear();
            pbBufferY.clear();
            
            boolean notCache = true;
            /*
            if (notesMonitor.getNumOfNotes() >= 1000000) {
                // TODO ノーツ100万以上はキャッシュを使用することで高速化する。
                //       ただし、バイナリ構成によってバグるため要検討
                notCache = false;
            }
            */
            int startIndex = indexCache[trkIndex];
            if (vpEndTick > midiUnit.getTickLength() - (totalMeasCount * sequence.getResolution())) {
                // 終端付近は取り逃さないようにする 
                notCache = true;
            }
                    
            for (int i = startIndex; i <  notesMonitor.getNumOfTrackEvent(trkIndex); i++) {
                // Midiメッセージを取得
                MidiEvent event = notesMonitor.getTrackEvent(trkIndex, i);
                if (event == null) {
                    continue;
                }
                
                if (vpEndTick < event.getTick()) {
                    // 描き残しがあるNoteOnが無いようにする 
                    boolean isExestsNoteOn = false;
                    for (int ch = 0; ch < 16; ch++) {
                        for (int midiNoIndex = 0; midiNoIndex < 128; midiNoIndex++) {
                            if (noteOnEvents[ch][midiNoIndex] != null) {
                                isExestsNoteOn = true;
                                break;
                            }
                        }
                    }
                    
                    if (isExestsNoteOn == false) {
                        if (notCache == false) {
                            indexCache[trkIndex] = i;
                        }
                        break;
                    }
                }
                
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    // ShortMessage解析
                    ShortMessage sMes = (ShortMessage) message;
                    int channel = sMes.getChannel();
                    int data1 = sMes.getData1();
                    //int data2 = sMes.getData2();
                    
                    if (toolkit.isNoteOn(sMes) == true) {
                        // Note ON
                        noteOnEvents[channel][data1] = event;
                    }
                    else if (toolkit.isNoteOff(sMes) == true) {
                        // Note OFF
                        MidiEvent endEvent = event;
                        MidiEvent startEvent = noteOnEvents[channel][data1];
                        noteOnEvents[channel][data1] = null;
                        if (startEvent == null) {
                            // 例外 対のNoteONが無いNoteOff  
                            continue;
                        }
                        else if (endEvent.getTick() <= startEvent.getTick()) {
                            // 例外 NoteOnとNoteOffのtick矛盾   
                            continue;
                        }
                        else if (vpStartTick > event.getTick()) {
                            // ビューポート範囲外は無視 
                            continue;
                        }

                        // 描画開始
                        int startMeas = (int) ((double) startEvent.getTick() / (double) sequence.getResolution()) + leftMeas;
                        int startOffset = (int) ((double) startEvent.getTick() % (double) sequence.getResolution());
                        int x = (int) (mainWindow.getMeasCellWidth() * (startMeas + (double) startOffset / sequence.getResolution())) + offsetCoordX;                        
                        int y = ((127 - data1) * mainWindow.getMeasCellHeight()) + topOffset;

                        int width = (int) (mainWindow.getMeasCellWidth()
                                * (double) (endEvent.getTick() - startEvent.getTick()) / sequence.getResolution());
                        int height = mainWindow.getMeasCellHeight();
                        
                        // ノーマルカラー
                        if (width > 2) {
                            g.setColor(mainWindow.notesColor[channel]);
                            g.fill3DRect(x, y, width, height, mainWindow.layout.isNotes3D);
                        }
                        else {
                            g.setColor(mainWindow.notesColor[channel]);
                            g.fill3DRect(x, y, 2, height, mainWindow.layout.isNotes3D);
                        }
                    }
                    else if (toolkit.isPitchBend(sMes) == true) {
                        if (mainWindow.layout.isVisiblePb == true) {
                            /* ピッチベンド描画 */
                            int pbValue = MidiUtility.convertPitchBendValue(sMes);
                            pbValue -= 8192;

                            int signed = (pbValue < 0) ? -1 : 1;

                            int startMeas = (int) ((double) event.getTick() / (double) sequence.getResolution()) + leftMeas;
                            int startOffset = (int) ((double) event.getTick() % (double) sequence.getResolution());
                            int x = (int) (mainWindow.getMeasCellWidth() * (startMeas + (double) startOffset / sequence.getResolution())) + offsetCoordX;                        

                            int absPbValue = signed * pbValue;
                            int y = pbCenterY - (signed * ((absPbValue * pbMaxHeight) / 8192));

                            // PB描画
                            Color pbColor = mainWindow.notesColor[channel];
                            int pastX = 0;
                            int pastY = pbCenterY;
                            if (pbBufferX.isEmpty() == false) {
                                pastX = pbBufferX.get(pbBufferX.size() - 1);
                                pastY = pbBufferY.get(pbBufferY.size() - 1);
                            }

                            float lineWidth = 2.0f;
                            Graphics2D g2 = (Graphics2D) g;
                            BasicStroke stroke = new BasicStroke(lineWidth);
                            g2.setStroke(stroke);
                            g2.setColor(pbColor);
                            g2.drawLine(pastX, pastY, x, pastY);
                            g2.drawLine(x, pastY, x, y);
                            pbBufferX.add(x);
                            pbBufferY.add(y);
                            g2.setStroke(new BasicStroke(1.0f));
                        }
                    }
                }
            } /* Trk End */
        }
    }
}
