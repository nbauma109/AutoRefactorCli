package org.autorefactor.cli;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.autorefactor.environment.Environment;
import org.autorefactor.environment.EventLoop;
import org.autorefactor.environment.Logger;
import org.autorefactor.util.UnhandledException;

/** AutoRefactor environment related utilities. */
public class EnvUtil extends AutoRefactor {
    /** Copied from test package. */
    static class CurrentThreadEvenLoop implements EventLoop {
        /** @Override */
        public <E extends Exception> void syncExec(Callable<E> callable) throws E {
            try {
                callable.call();
            } catch (ExecutionException e) {
                throw new UnhandledException(null, e.getCause());
            } catch (Exception e) {
                throw new UnhandledException(null, e);
            }
        }
    }

    /** Copied from test package. */
    static class ThrowingLogger implements Logger {
        @Override
        public void error(String message) {
            throw new RuntimeException(message);
        }

        @Override
        public void error(String message, Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new UnhandledException(null, message, e);
        }

        @Override
        public void warn(String message) {
            throw new RuntimeException(message);
        }
    }

    /**
     * A minimal command line compatible environment (copied from test package).
     */
    public static final Environment SIMPLE_ENVIRONMENT = new Environment(new CurrentThreadEvenLoop(), null,
            new ThrowingLogger(), null);
}
