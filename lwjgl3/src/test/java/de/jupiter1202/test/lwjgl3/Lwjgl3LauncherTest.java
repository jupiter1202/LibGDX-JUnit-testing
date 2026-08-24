package de.jupiter1202.test.lwjgl3;

import static org.mockito.Mockito.verify;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Lwjgl3LauncherTest {
    @Mock
    private Lwjgl3ApplicationConfiguration configuration;

    @Test
    void appliesDesktopWindowDefaultsWithoutOpeningAWindow() {
        Lwjgl3Launcher.configure(configuration, 144);

        verify(configuration).setTitle("test");
        verify(configuration).useVsync(true);
        verify(configuration).setForegroundFPS(145);
        verify(configuration).setWindowedMode(640, 480);
        verify(configuration).setWindowIcon(
            "libgdx128.png",
            "libgdx64.png",
            "libgdx32.png",
            "libgdx16.png"
        );
    }
}
