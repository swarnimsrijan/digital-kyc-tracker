package in.zeta;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainTest {

    @Test
    void main_ShouldStartSpringApplication() {
        // Given
        String[] args = {"--spring.profiles.active=test"};
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

        // When
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(Main.class, args))
                    .thenReturn(mockContext);

            Main.main(args);

            // Then
            mockedSpringApplication.verify(() -> SpringApplication.run(Main.class, args));
        }
    }

    @Test
    void main_ShouldStartSpringApplicationWithEmptyArgs() {
        // Given
        String[] args = {};
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

        // When
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(Main.class, args))
                    .thenReturn(mockContext);

            Main.main(args);

            // Then
            mockedSpringApplication.verify(() -> SpringApplication.run(Main.class, args));
        }
    }

    @Test
    void main_ShouldStartSpringApplicationWithNullArgs() {
        // Given
        String[] args = null;
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

        // When
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(Main.class, (String[]) null))
                    .thenReturn(mockContext);

            Main.main(args);

            // Then
            mockedSpringApplication.verify(() -> SpringApplication.run(Main.class, (String[]) null));
        }
    }
}