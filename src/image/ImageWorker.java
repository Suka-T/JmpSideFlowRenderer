package image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import layout.LayoutManager;
import plg.JmpSideFlowRenderer;

public class ImageWorker implements Runnable {
    private int leftMeasTh = 0;
    protected volatile VolatileImage offScreenImage;
    protected Graphics2D offScreenGraphic;
    private boolean isExec = false;
    private int width = 0;
    private int height = 0;
    private ExecutorService service = null;

    public ImageWorker(int width, int height) {
        super();
        
        this.width = width;
        this.height = height;
    }
    
    public void start() {
        service = Executors.newSingleThreadExecutor();
    }
    
    public void stop() {
        service.shutdown();
    }
    
    public void makeImage() {
        if (isExec == false) {
            isExec = true;
            service.execute(this);
        }
    }
    
    public boolean isExec() {
        return isExec;
    }
    
    public void disposeImage() {
        if (offScreenImage != null) {
            offScreenImage.flush();
            offScreenImage = null;
        }
    }

    public Image getImage() {
        return offScreenImage;
    }
    
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    
    public int getImageWidth() {
        return getWidth();
    }
    public int getImageHeight() {
        return getHeight();
    }

    @Override
    public void run() {
        if (JmpSideFlowRenderer.MainWindow.isVisible() == false) {
            if (offScreenImage != null) {
                //イメージオブジェクトのメモリを解放
                disposeImage();
            }
            return;
        }
        
        GraphicsConfiguration gc = JmpSideFlowRenderer.MainWindow.getGraphicsConfiguration();
        if (offScreenImage == null || offScreenImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            // ノーツ画像
            offScreenImage = LayoutManager.getInstance().createLayerImage(getImageWidth(), getImageHeight());
            offScreenGraphic = offScreenImage.createGraphics();
        }

        Graphics2D g2d = offScreenImage.createGraphics();
        g2d.setColor(LayoutManager.getInstance().getBackColor());
        g2d.fillRect(0, 0, getImageWidth(), getImageHeight());
        //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        //g2d.fillRect(0, 0, getImageWidth(), getImageHeight());
        g2d.dispose();

        // オフスクリーン描画 
        paintImage(offScreenGraphic);
        
        isExec = false;
    }

    public int getLeftMeasTh() {
        return leftMeasTh;
    }

    public void setLeftMeasTh(int leftMeasTh) {
        this.leftMeasTh = leftMeasTh;
    }
    
    protected void paintImage(Graphics g) {
        /* 継承先で処理を記述 */
    }
    
    public void reset() {
    	/* 継承先で処理を記述 */
    }
    
}
