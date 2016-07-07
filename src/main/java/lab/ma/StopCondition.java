package lab.ma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.engine.AsynchronousSteppable;

/**
 * Created on 07/07/16:
 *
 * @author cbadenes
 */
public class StopCondition extends AsynchronousSteppable {

    private static final Logger LOG = LoggerFactory.getLogger(StopCondition.class);

    @Override
    protected void run(boolean resuming, boolean restoringFromCheckpoint) {
        LOG.info("Run => " + resuming + " / " + restoringFromCheckpoint);
    }


    @Override
    protected void halt(boolean pausing) {
        LOG.info("Halt => " + pausing );
    }
}
