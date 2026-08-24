package de.jupiter1202.test.support;

import static org.mockito.Mockito.mock;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Starts and stops a fresh libGDX headless application for one test class. */
final class HeadlessGdxExtension implements BeforeAllCallback, AfterAllCallback {
    static final String GDX_STATIC_STATE = "libgdx-static-state";
    private static final long TIMEOUT_SECONDS = 5;

    private TestHeadlessApplication application;

    @Override
    public void beforeAll(ExtensionContext context) throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        application = new TestHeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                started.countDown();
            }
        }, configuration);

        if (!started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            application.stopAndWait();
            clearGdxState();
            throw new IllegalStateException("Timed out while starting the libGDX headless backend");
        }

        GL20 gl20 = mock(GL20.class);
        Gdx.gl = gl20;
        Gdx.gl20 = gl20;
    }

    @Override
    public void afterAll(ExtensionContext context) throws InterruptedException {
        try {
            if (application != null) {
                application.stopAndWait();
            }
        } finally {
            application = null;
            clearGdxState();
        }
    }

    private static void clearGdxState() {
        Gdx.app = null;
        Gdx.graphics = null;
        Gdx.audio = null;
        Gdx.input = null;
        Gdx.files = null;
        Gdx.net = null;
        Gdx.gl = null;
        Gdx.gl20 = null;
        Gdx.gl30 = null;
        Gdx.gl31 = null;
        Gdx.gl32 = null;
    }

    private static final class TestHeadlessApplication extends HeadlessApplication {
        private TestHeadlessApplication(
            ApplicationAdapter listener,
            HeadlessApplicationConfiguration configuration
        ) {
            super(listener, configuration);
        }

        private void stopAndWait() throws InterruptedException {
            exit();
            mainLoopThread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            if (mainLoopThread.isAlive()) {
                throw new IllegalStateException("Timed out while stopping the libGDX headless backend");
            }
        }
    }
}
