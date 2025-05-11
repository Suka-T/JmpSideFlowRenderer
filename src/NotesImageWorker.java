import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import jlib.midi.LightweightShortMessage;
import jlib.midi.MidiByte;
import jlib.midi.MidiUtility;

class NotesImageWorker extends ImageWorker {
    public static final Color FIX_FOCUS_NOTES_BGCOLOR = Color.WHITE;
    public static final Color FIX_FOCUS_NOTES_BDCOLOR = Color.GREEN;
    
    private AlphaComposite bdAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    private BasicStroke normalStroke = new BasicStroke(1.0f);
    private BasicStroke notesBdStroke = new BasicStroke(1.5f);
    private BasicStroke bdStroke = new BasicStroke(2.0f);
    private BasicStroke pbStroke = new BasicStroke(2.0f);
    private int[] indexCache = null;
    private MidiEvent[][] noteOnEvents = null;
    private List<Integer> pbBufferX = null;
    private List<Integer> pbBufferY = null;
    
    public NotesImageWorker(int width, int height) {
        super(width, height);
        
        noteOnEvents = new MidiEvent[16][];
        for (int i=0; i<16; i++) {
            noteOnEvents[i] = new MidiEvent[128];
        }
        pbBufferX = new ArrayList<Integer>();
        pbBufferY = new ArrayList<Integer>();
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
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        Sequence sequence =  midiUnit.getSequence();
        if (sequence == null) {
            return;
        }
        
        super.run();
    }
    
    @Override
    public void disposeImage() {
        indexCache = null;
        super.disposeImage();
    }
    
    @Override
    public int getImageWidth() {
        return (getWidth() * 3) + LayoutManager.getInstance().getTickBarPosition();
    }

    @Override
    protected void paintImage(Graphics g) {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        Sequence sequence = midiUnit.getSequence();
        if (sequence == null) {
            return;
        }
        
        paintBorder(g);
        paintNotes(g, getLeftMeasTh());
    }
    
    protected void paintBorder(Graphics g) {
    	Graphics2D g2d = (Graphics2D)g;
        JmpSideFlowRendererWindow mainWindow = JmpSideFlowRenderer.MainWindow;
        g.setColor(LayoutManager.getInstance().getBorderColor());
        g2d.setStroke(bdStroke);
        int x = mainWindow.getZeroPosition();
        int y = mainWindow.getMeasCellHeight() * 3;
        if (LayoutManager.getInstance().isVisibleHorizonBorder() == true) {
            while (y <= getImageHeight()) {
                g.drawLine(x, y, x + getImageWidth(), y);
                y += mainWindow.getMeasCellHeight() * 12;
            }
        }
        x = mainWindow.getZeroPosition();
        y = 0;
        while (x <= getImageWidth()) {
            if (LayoutManager.getInstance().isVisibleVerticalBorder() == true) {
                g.drawLine(x, y, x, y + getImageHeight());
            }
            x += mainWindow.getMeasCellWidth();
        }
        g2d.setStroke(normalStroke);
    }
    
    protected boolean isNoteOn(MidiMessage mes) {
        ShortMessage sMes = (ShortMessage)mes;
        int command = sMes.getCommand();
        int data2 = sMes.getData2();
        if ((command == MidiByte.Status.Channel.ChannelVoice.Fst.NOTE_ON) && (data2 > 0)) {
            return true;
        }
        return false;
        
/*
        IMidiToolkit toolkit = JMPCoreAccessor.getSoundManager().getMidiToolkit();
        return toolkit.isNoteOn(mes);
*/
    }
    
    protected boolean isNoteOff(MidiMessage mes) {
        ShortMessage sMes = (ShortMessage)mes;
        int command = sMes.getCommand();
        int data2 = sMes.getData2();
        if ((command == MidiByte.Status.Channel.ChannelVoice.Fst.NOTE_OFF) || (command == MidiByte.Status.Channel.ChannelVoice.Fst.NOTE_ON && data2 <= 0)) {
            return true;
        }
        return false;
        
/*
        IMidiToolkit toolkit = JMPCoreAccessor.getSoundManager().getMidiToolkit();
        return toolkit.isNoteOff(mes);
*/
    }
    
    protected boolean isPitchbend(MidiMessage mes) {
        ShortMessage sMes = (ShortMessage)mes;
        int command = sMes.getCommand();
        if (command == MidiByte.Status.Channel.ChannelVoice.Fst.PITCH_BEND) {
            return true;
        }
        return false;
        
/*
        IMidiToolkit toolkit = JMPCoreAccessor.getSoundManager().getMidiToolkit();
        return toolkit.isPitchBend(mes);
*/
    }

    private final RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float();
    
    protected void paintNotes(Graphics g, int leftMeas) {
        Graphics2D g2d = (Graphics2D)g;
        JmpSideFlowRendererWindow mainWindow = JmpSideFlowRenderer.MainWindow;
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        Sequence sequence = midiUnit.getSequence();
        if (sequence == null) {
            return;
        }
        
        if (LayoutManager.getInstance().getNotesDesign() == LayoutConfig.ENotesDesign.Arc) {
        	// Arc系のNotesはエイリアス必須 
        	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        else {
        	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        
        paintBorder(g);

        // 上部位置の調整
        int offsetCoordX = LayoutManager.getInstance().getTickBarPosition();
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

        if (LayoutManager.getInstance().isVisiblePbLine() == true) {
            g.setColor(LayoutManager.getInstance().getPitchbendColor());
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
        
        int startMeas = 0;
        int startOffset = 0;
        int x = 0;                        
        int y = 0;
        int width = 0;
        int height = 0;
        int channel = 0;
        int data1 = 0;
        //int data2 = 0;
        
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
                
                g2d.setStroke(normalStroke);
                
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    // ShortMessage解析
                    ShortMessage sMes = (ShortMessage) message;
                    channel = sMes.getChannel();
                    data1 = sMes.getData1();
                    //data2 = sMes.getData2();
                    
                    if (isNoteOn(sMes) == true) {
                        // Note ON
                        noteOnEvents[channel][data1] = event;
                    }
                    else if (isNoteOff(sMes) == true) {
                    	int trackIndex = 0;
                        if (message instanceof LightweightShortMessage) {
                        	LightweightShortMessage lwMes = (LightweightShortMessage) message;
                        	trackIndex = (int)lwMes.getTrackIndex();
                        }
                        
                        // Note OFF
                        MidiEvent endEvent = event;
                        MidiEvent startEvent = noteOnEvents[channel][data1];
                        noteOnEvents[channel][data1] = null;
                        
                        if ((startEvent == null) || // 例外 対のNoteONが無いNoteOff 
                            (endEvent.getTick() <= startEvent.getTick()) || // 例外 NoteOnとNoteOffのtick矛盾   
                            (vpStartTick > event.getTick()) ) // ビューポート範囲外は無視  
                        {
                            // 無効データは何もしない 
                        }
                        else {
                            // 描画開始
                            startMeas = (int) ((double) startEvent.getTick() / (double) sequence.getResolution()) + leftMeas;
                            startOffset = (int) ((double) startEvent.getTick() % (double) sequence.getResolution());
                            x = (int) (mainWindow.getMeasCellWidth() * (startMeas + (double) startOffset / sequence.getResolution())) + offsetCoordX;                        
                            y = ((127 - data1) * mainWindow.getMeasCellHeight()) + topOffset;
    
                            width = (int) (mainWindow.getMeasCellWidth()
                                    * (double) (endEvent.getTick() - startEvent.getTick()) / sequence.getResolution());
                            height = mainWindow.getMeasCellHeight();
                            
                            if (width < 2) {
                                width = 2;
                            }
                            
                            if (LayoutManager.getInstance().getColorRule() == LayoutConfig.EColorRule.Channel) {
                            	g2d.setColor(LayoutManager.getInstance().getNotesColor(channel));
                            }
                            else {
                            	g2d.setColor(LayoutManager.getInstance().getNotesColor(trackIndex));
                            }
                            
                            if (LayoutManager.getInstance().getNotesDesign() == LayoutConfig.ENotesDesign.Normal) {
	                            g2d.fill3DRect(x, y, width, height, true);
	                            g2d.setStroke(notesBdStroke);
	                            g2d.setColor(LayoutManager.getInstance().getBackColor());
	                            g2d.setComposite(bdAlpha);
	                            g2d.drawRect(x, y, width, height);
	                            g2d.setComposite(AlphaComposite.SrcOver);
	                            g2d.setStroke(normalStroke);
                            }
                            else if (LayoutManager.getInstance().getNotesDesign() == LayoutConfig.ENotesDesign.Arc) {
	                            roundRect.setRoundRect((float)x, (float)y, (float)width, (float)height, 6.0f, 6.0f);
	                            g2d.fill(roundRect);
                            	g2d.setStroke(notesBdStroke);
	                            g2d.setColor(LayoutManager.getInstance().getBackColor());
	                            g2d.setComposite(bdAlpha);
	                            g2d.draw(roundRect);
	                            g2d.setComposite(AlphaComposite.SrcOver);
	                            g2d.setStroke(normalStroke);
                            }
                            else {
                            	g2d.fillRect(x, y, width, height);
                            }
                        }
                    }
                    else if (isPitchbend(sMes) == true) {
                        if (LayoutManager.getInstance().isVisiblePbLine() == true) {
                            /* ピッチベンド描画 */
                            int pbValue = MidiUtility.convertPitchBendValue(sMes) - 8192;
                            int signed = (pbValue < 0) ? -1 : 1;

                            startMeas = (int) ((double) event.getTick() / (double) sequence.getResolution()) + leftMeas;
                            startOffset = (int) ((double) event.getTick() % (double) sequence.getResolution());
                            x = (int) (mainWindow.getMeasCellWidth() * (startMeas + (double) startOffset / sequence.getResolution())) + offsetCoordX;                        

                            int absPbValue = signed * pbValue;
                            y = pbCenterY - (signed * ((absPbValue * pbMaxHeight) / 8192));

                            // PB描画
                            Color pbColor = LayoutManager.getInstance().getNotesColor(channel);
                            int pastX = 0;
                            int pastY = pbCenterY;
                            if (pbBufferX.isEmpty() == true) {
                                int pastPbValue = notesMonitor.getPitchBend(channel);
                                int pastSigned = (pastPbValue < 0) ? -1 : 1;
                                int pastAbsPbValue = pastSigned * pastPbValue;
                                
                                pastX = x;
                                pastY = pbCenterY - (pastSigned * ((pastAbsPbValue * pbMaxHeight) / 8192));
                            }
                            else {
                                pastX = pbBufferX.get(pbBufferX.size() - 1);
                                pastY = pbBufferY.get(pbBufferY.size() - 1);
                            }

                            Graphics2D g2 = (Graphics2D) g;
                            g2.setStroke(pbStroke);
                            g2.setColor(pbColor);
                            g2.drawLine(pastX, pastY, x, pastY);
                            g2.drawLine(x, pastY, x, y);
                            pbBufferX.add(x);
                            pbBufferY.add(y);
                            g2.setStroke(normalStroke);
                        }
                    }
                }
            } /* Trk End */
        }
    }
}
