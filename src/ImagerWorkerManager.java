import java.awt.Image;

class ImagerWorkerManager {
    
    private volatile ImageWorker[] workers = null;
    private int currentWorkerIndex = 0;
    
    public ImagerWorkerManager(int width, int height) {
        workers = new NotesImageWorker[SystemProperties.getInstance().getWorkerNum()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new NotesImageWorker(width, height);
        }
    }
    
    public int getNumOfWorker() {
        return workers.length;
    }
    
    public ImageWorker getWorker(int index) {
        return workers[index];
    }

    public void start() {
        for (ImageWorker w : workers) {
            w.start();
        }
        currentWorkerIndex = 0;
    }
    
    public void stop() {
        for (ImageWorker w : workers) {
            w.stop();
        }
    }
    
    public Image getNotesImage() {
        if (workers[currentWorkerIndex].isExec() == false) {
            return workers[currentWorkerIndex].getImage();
        }
        return null;
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
        ImageWorker nextNotesThread = workers[currentWorkerIndex]; 
        //int[] nextCache = workers[currentWorkerIndex].getTrackCache();
        
        currentWorkerIndex = (currentWorkerIndex + 1 >= workers.length) ? 0 : currentWorkerIndex + 1;
        int offsetLeftMeas = (newLeftMeas < 0) ? -(newLeftMeas) : newLeftMeas;
        int flipMergin = -(flipCount);
        int flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * (workers.length - 1));
        nextNotesThread.setLeftMeasTh(-(flipLine));
        
        // TODO ここでキャッシュ情報を渡したいが高頻度でバグるのでやらない 要検討 
        //nextNotesThread.copyTrackCacheFrom(nextCache);
        nextNotesThread.makeImage();
    }
}
