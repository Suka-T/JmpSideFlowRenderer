import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiToolkit;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import jlib.midi.MidiUtility;

class ImageWorker extends Thread {
    public static final Color FIX_FOCUS_NOTES_BGCOLOR = Color.WHITE;
    public static final Color FIX_FOCUS_NOTES_BDCOLOR = Color.GREEN;
    
    private int leftMeasTh = 0;
    private BufferedImage offScreenNotesImage;
    private Graphics2D offScreenNotesGraphic;
    private boolean isRunnable = true;
    private boolean isWait = true;
    private boolean doClear = true;

    public ImageWorker() {
        super();
        this.setPriority(MAX_PRIORITY);
        isRunnable = true;
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
        return getWidth() * 3;
    }
    public int getImageHeight() {
        return getHeight();
    }

    @Override
    public void run() {
        while (isRunnable) {
            try {
                if (JmpSideFlowRenderer.MainWindow.isVisible() == false) {
                    Thread.sleep(200);
                    continue;
                }

                IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
                Sequence sequence =  midiUnit.getSequence();
                if (sequence == null) {
                    Thread.sleep(100);
                    continue;
                }
                
                if (offScreenNotesImage == null) {
                    // ノーツ画像
                    offScreenNotesImage = new BufferedImage(getImageWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    offScreenNotesGraphic = offScreenNotesImage.createGraphics();
                    doClear = true;
                    setWait(false);
                }
                
                if (isWait == true) {
                    Thread.sleep(100);
                    continue;
                }

                if (doClear == true) {
                    Graphics2D g2d = offScreenNotesImage.createGraphics();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                    g2d.fillRect(0, 0, getImageWidth(), getHeight());
                    g2d.dispose();
                    doClear = false;
                }

                // オフスクリーン
                //paintBorder(offScreenNotesGraphic);
                paintNotes(offScreenNotesGraphic, getLeftMeasTh());
                isWait = true;
            }
            catch (InterruptedException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            }
        }
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
    
    public void makeImage() {
        isWait = false;
    }

    private void paintNotes(Graphics g, int leftMeas) {
        JmpSideFlowRendererWindow mainWindow = JmpSideFlowRenderer.MainWindow;
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        IMidiToolkit toolkit = JMPCoreAccessor.getSoundManager().getMidiToolkit();
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        Sequence sequence = midiUnit.getSequence();
        if (sequence == null) {
            return;
        }

        // 上部位置の調整
        int totalMeasCount = mainWindow.getDispMeasCount() * 2;
        int keyCount = (127 - mainWindow.getTopMidiNumber());
        int topOffset = (mainWindow.getMeasCellHeight() * keyCount);

        long vpLenTick = (totalMeasCount * sequence.getResolution());
        long vpStartTick = -(leftMeas) * sequence.getResolution();
        long vpEndTick = -(leftMeas) * sequence.getResolution() + vpLenTick;

        int pbMaxHeight = 100;
        int pbCenterY = (pbMaxHeight / 2) + 100;

        if (mainWindow.layout.isVisiblePb == true) {
            g.setColor(mainWindow.layout.pbBaseLineColor);
            g.drawLine(0, pbCenterY, getImageWidth(), pbCenterY);
        }
        
        if (notesMonitor.getNumOfTrack() <= 0) {
            return;
        }
        
        for (int trkIndex = notesMonitor.getNumOfTrack() - 1; trkIndex >= 0; trkIndex--) {
            MidiEvent[][] noteOnEvents = new MidiEvent[16][];
            for (int i=0; i<16; i++) {
                noteOnEvents[i] = new MidiEvent[128];
            }
            
            List<Integer> pbBufferX = new ArrayList<Integer>();
            List<Integer> pbBufferY = new ArrayList<Integer>();
            for (int i = 0; i <  notesMonitor.getNumOfTrackEvent(trkIndex); i++) {
                // Midiメッセージを取得
                MidiEvent event = notesMonitor.getTrackEvent(trkIndex, i);
                if (event == null) {
                    continue;
                }
                
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    // ShortMessage解析
                    ShortMessage sMes = (ShortMessage) message;
                    if (toolkit.isNoteOn(sMes) == true) {
                        // Note ON
                        noteOnEvents[sMes.getChannel()][sMes.getData1()] = event;
                    }
                    else if (toolkit.isNoteOff(sMes) == true) {
                        // Note OFF
                        MidiEvent endEvent = event;
                        MidiEvent startEvent = noteOnEvents[sMes.getChannel()][sMes.getData1()];
                        noteOnEvents[sMes.getChannel()][sMes.getData1()] = null;
                        if (startEvent == null) {
                            continue;
                        }
                        else if (vpEndTick < startEvent.getTick()) {
                            continue;
                        }
                        else if (endEvent.getTick() <= startEvent.getTick()) {
                            continue;
                        }
                        else if (vpStartTick > endEvent.getTick()) {
                            continue;
                        }

                        // 描画開始
                        int key = sMes.getData1();
                        int startMeas = (int) ((double) startEvent.getTick() / (double) sequence.getResolution());
                        int startOffset = (int) ((double) startEvent.getTick() % (double) sequence.getResolution());
                        startMeas += leftMeas;
                        int x = (int) (mainWindow.getZeroPosition() + (startMeas * mainWindow.getMeasCellWidth()))
                                + (int) (mainWindow.getMeasCellWidth() * (double) ((double) startOffset / (double) sequence.getResolution()));

                        int y = ((127 - key) * mainWindow.getMeasCellHeight()) + topOffset;

                        long tickLen = endEvent.getTick() - startEvent.getTick();
                        int width = (int) ((double) (mainWindow.getMeasCellWidth()) * (double) ((double) tickLen / (double) sequence.getResolution()));

                        int height = mainWindow.getMeasCellHeight();

                        int colorIndex = sMes.getChannel();
                        
                        // ノーマルカラー
                        if (width > 2) {
                            g.setColor(mainWindow.notesColor[colorIndex]);
                            g.fill3DRect(x, y, width, height, mainWindow.layout.isNotes3D);
                        }
                        else {
                            g.setColor(mainWindow.notesColor[colorIndex]);
                            g.fill3DRect(x, y, 2, height, mainWindow.layout.isNotes3D);
                        }
                    }
                    else if (toolkit.isPitchBend(sMes) == true) {
                        if (mainWindow.layout.isVisiblePb == true) {
                            /* ピッチベンド描画 */
                            int pbValue = MidiUtility.convertPitchBendValue(sMes);
                            pbValue -= 8192;

                            int signed = (pbValue < 0) ? -1 : 1;

                            int startMeas = (int) ((double) event.getTick() / (double) sequence.getResolution());
                            int startOffset = (int) ((double) event.getTick() % (double) sequence.getResolution());
                            startMeas += leftMeas;
                            int x = (int) (mainWindow.getZeroPosition() + (startMeas * mainWindow.getMeasCellWidth()))
                                    + (int) (mainWindow.getMeasCellWidth() * (double) ((double) startOffset / (double) sequence.getResolution()));

                            int absPbValue = signed * pbValue;
                            int y = pbCenterY - (signed * ((absPbValue * pbMaxHeight) / 8192));

                            // PB描画
                            Color pbColor = mainWindow.notesColor[sMes.getChannel()];
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
}
