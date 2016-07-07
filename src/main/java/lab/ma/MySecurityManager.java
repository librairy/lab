package lab.ma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 07/07/16:
 *
 * @author cbadenes
 */
public class MySecurityManager extends SecurityManager {

    private static final Logger LOG = LoggerFactory.getLogger(MySecurityManager.class);

    @Override
    public void checkExit(int status) {
        LOG.warn("Internal call to system.exit("+status+")");
//        Thread.currentThread().stop();
        throw new SecurityException();
    }
}
