import java.awt.Graphics;
import java.awt.Image;

class ImagerWorkerManager {
    
    private ImageWorker[] workers = null;
    private int currentWorkerIndex = 0;
    
    public ImagerWorkerManager() {
        workers = new ImageWorker[5];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new ImageWorker();
        }
    }
    
    public int getNumOfWorker() {
        return workers.length;
    }
    
    public ImageWorker getWorker(int index) {
        return workers[index];
    }

    public void init() {
        for (ImageWorker w : workers) {
            w.start();
        }
        currentWorkerIndex = 0;
    }
    
    public void exit() throws InterruptedException {
        for (ImageWorker w : workers) {
            w.exit();
            w.join();
        }
    }
    
    public void copyNotesImage(Graphics g) {
        if (workers[currentWorkerIndex].isWait() == true) {
            Image notesImage = workers[currentWorkerIndex].getNotesImage();
            if (notesImage != null) {
                g.drawImage(notesImage, 0, 0, null);
            }
        }
    }
    
    public void reset(int leftMeas, int dispMeas, int flipCount) {
        for (int i = 0; i < workers.length; i++) {
            int offsetLeftMeas = leftMeas;
            offsetLeftMeas = (offsetLeftMeas < 0) ? -(offsetLeftMeas) : offsetLeftMeas;
            int flipMergin = -(flipCount);
            int flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * i);
            workers[i].setLeftMeasTh(-(flipLine));
            workers[i].disposeImage();
            workers[i].makeImage();
        }
        currentWorkerIndex = 0;
    }
    
    public void flipPage(int newLeftMeas, int dispMeas, int flipCount) {
        workers[currentWorkerIndex].clearImage();
        ImageWorker nextNotesThread = workers[currentWorkerIndex]; 
        
        currentWorkerIndex = (currentWorkerIndex + 1 >= workers.length) ? 0 : currentWorkerIndex + 1;
        int offsetLeftMeas = (newLeftMeas < 0) ? -(newLeftMeas) : newLeftMeas;
        int flipMergin = -(flipCount);
        int flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * (workers.length - 1));
        nextNotesThread.setLeftMeasTh(-(flipLine));
        nextNotesThread.makeImage();
    }
}
