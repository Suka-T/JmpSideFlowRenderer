import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ImageWorker implements Runnable {
    private int leftMeasTh = 0;
    private BufferedImage offScreenImage;
    private Graphics2D offScreenGraphic;
    private boolean isWait = true;
    private boolean doClear = true;
    
    private ScheduledExecutorService scheduler = null;

    public ImageWorker() {
        super();
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
        offScreenImage = null;
    }

    public BufferedImage getNotesImage() {
        return offScreenImage;
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

    @Override
    public void run() {
        if (JmpSideFlowRenderer.MainWindow.isVisible() == false) {
            if (offScreenImage != null) {
                offScreenImage = null; //イメージオブジェクトのメモリを解放
            }
            return;
        }
        else if (isWait == true) {
            return;
        }
        
        if (offScreenImage == null) {
            // ノーツ画像
            offScreenImage = new BufferedImage(getImageWidth(), getImageHeight(), BufferedImage.TYPE_INT_ARGB);
            offScreenGraphic = offScreenImage.createGraphics();
            doClear = true;
            setWait(false);
        }

        if (doClear == true) {
            Graphics2D g2d = offScreenImage.createGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            g2d.fillRect(0, 0, getImageWidth(), getImageHeight());
            g2d.dispose();
            doClear = false;
        }

        // オフスクリーン描画 
        paintImage(offScreenGraphic);
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
    
    protected void paintImage(Graphics g) {
        /* 継承先で処理を記述 */
    }
    
}
