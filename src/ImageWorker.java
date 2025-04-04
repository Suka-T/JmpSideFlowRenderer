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
import javax.sound.midi.Track;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiToolkit;
import jlib.midi.IMidiUnit;
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
        isRunnable = true;
    }
    
    public boolean isWait() {
        return isWait;
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
        return JmpSideFlowRenderer.MainWindow.getWidth();
    }
    public int getHeight() {
        return JmpSideFlowRenderer.MainWindow.getHeight();
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
                    offScreenNotesImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    offScreenNotesGraphic = offScreenNotesImage.createGraphics();
                    doClear = true;
                    isWait = false;
                }
                
                if (isWait == true) {
                    Thread.sleep(100);
                    continue;
                }

                if (doClear == true) {
                    Graphics2D g2d = offScreenNotesImage.createGraphics();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                    doClear = false;
                }

                // オフスクリーン
                //paintBorder(offScreenNotesGraphic);
                paintNotes(offScreenNotesGraphic, getLeftMeasTh());
                isWait = true;
            }
            catch (Exception e) {

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
        Sequence sequence = midiUnit.getSequence();
        if (sequence == null) {
            return;
        }

        // 上部位置の調整
        int keyCount = (127 - mainWindow.getTopMidiNumber());
        int topOffset = (mainWindow.getMeasCellHeight() * keyCount);

        long vpLenTick = (mainWindow.getDispMeasCount() * sequence.getResolution());
        long vpStartTick = -(leftMeas) * sequence.getResolution();
        long vpEndTick = -(leftMeas) * sequence.getResolution() + vpLenTick;

        int pbMaxHeight = 100;
        int pbCenterY = (pbMaxHeight / 2) + 100;

        if (mainWindow.layout.isVisiblePb == true) {
            g.setColor(mainWindow.layout.pbBaseLineColor);
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
                        int x = (int) (mainWindow.getZeroPosition() + (startMeas * mainWindow.getMeasCellWidth()))
                                + (int) (mainWindow.getMeasCellWidth() * (double) ((double) startOffset / (double) sequence.getResolution()));

                        int y = ((127 - key) * mainWindow.getMeasCellHeight()) + topOffset;

                        long tickLen = endEvent.getTick() - startEvent.getTick();
                        int width = (int) ((double) (mainWindow.getMeasCellWidth()) * (double) ((double) tickLen / (double) sequence.getResolution()));

                        int height = mainWindow.getMeasCellHeight();

                        int colorIndex = sMes.getChannel();

                        // ノーマルカラー
                        Color bgColor = mainWindow.notesColor[colorIndex];
                        Color bdColor = mainWindow.notesBorderColor[colorIndex];
                        if (mainWindow.layout.isDrawFocusNotesColor == true) {
                            if (startEvent.getTick() <= midiUnit.getTickPosition() && midiUnit.getTickPosition() <= endEvent.getTick()
                                    /*|| (sequencer.getTickPosition() >= endEvent.getTick())*/) {
                                // フォーカスカラー
                                if (mainWindow.layout.fixFocusNotesDesign == false) {
                                    bgColor = mainWindow.notesFocusColor[colorIndex];
                                    bdColor = mainWindow.notesColor[colorIndex];
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
