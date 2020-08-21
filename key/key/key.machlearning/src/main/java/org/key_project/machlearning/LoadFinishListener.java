package org.key_project.machlearning;

import de.uka.ilkd.key.prover.ProverTaskListener;
import de.uka.ilkd.key.prover.TaskFinishedInfo;
import de.uka.ilkd.key.prover.TaskStartedInfo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class LoadFinishListener implements ProverTaskListener {

    private final CountDownLatch indicator = new CountDownLatch(1);

    @Override
    public void taskStarted(TaskStartedInfo info) {
        
    }

    @Override
    public void taskProgress(int position) {

    }

    @Override
    public void taskFinished(TaskFinishedInfo info) {
        indicator.countDown();
    }

    public void waitFinished() throws InterruptedException {
        indicator.await();
    }
}
