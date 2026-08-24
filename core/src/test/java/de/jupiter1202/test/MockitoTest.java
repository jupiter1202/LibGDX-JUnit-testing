package de.jupiter1202.test;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockitoTest {
    @Mock
    private Runnable platformPort;

    @Test
    void injectsMocksForPlatformPorts() {
        platformPort.run();

        verify(platformPort).run();
    }
}
