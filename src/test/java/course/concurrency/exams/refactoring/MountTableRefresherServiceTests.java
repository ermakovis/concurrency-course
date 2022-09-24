package course.concurrency.exams.refactoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MountTableRefresherServiceTests {

    private MountTableRefresherService service;

    private Others.RouterStore routerStore;
    private Others.MountTableManager manager;
    private Others.LoadingCache routerClientsCache;

    @BeforeEach
    public void setUpStreams() {
        service = new MountTableRefresherService();
        service.setCacheUpdateTimeout(1000);
        routerStore = mock(Others.RouterStore.class);
        manager = mock(Others.MountTableManager.class);
        service.setRouterStore(routerStore);
        routerClientsCache = mock(Others.LoadingCache.class);
        service.setRouterClientsCache(routerClientsCache);
//        service.serviceInit(); // needed for complex class testing, not for now
    }

    @AfterEach
    public void restoreStreams() {
//        service.serviceStop();
    }

    @Test
    @DisplayName("All tasks are completed successfully")
    public void allDone() {
        // given
        try (MockedConstruction<Others.MountTableManager> mocked = Mockito.mockConstruction(Others.MountTableManager.class,
                (mock, context) -> {
                    when(mock.refresh()).thenReturn(true);
                    when(mock.getAddress()).thenReturn((String) context.arguments().get(0));
                })) {

            MountTableRefresherService mockedService = Mockito.spy(service);
            List<String> addresses = List.of("123", "local6", "789", "local");

            List<Others.RouterState> states = addresses.stream()
                    .map(Others.RouterState::new)
                    .collect(toList());

            when(routerStore.getCachedRecords()).thenReturn(states);

            // when
            mockedService.refresh();

            // then
            verify(mockedService).log("Mount table entries cache refresh successCount=4,failureCount=0");
            verify(routerClientsCache, never()).invalidate(anyString());
        }
    }

    @Test
    @DisplayName("All tasks failed")
    public void noSuccessfulTasks() {
        // given
        try (MockedConstruction<Others.MountTableManager> mocked = Mockito.mockConstruction(Others.MountTableManager.class,
                (mock, context) -> {
                    when(mock.refresh()).thenReturn(false);
                    when(mock.getAddress()).thenReturn((String) context.arguments().get(0));
                })) {
            MountTableRefresherService mockedService = Mockito.spy(service);
            List<String> addresses = List.of("123", "local6", "789", "local");

            List<Others.RouterState> states = addresses.stream()
                    .map(Others.RouterState::new)
                    .collect(toList());

            when(routerStore.getCachedRecords()).thenReturn(states);

            // when
            mockedService.refresh();

            // then
            verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=4");
            verify(routerClientsCache, times(4)).invalidate(anyString());
        }
    }

    @Test
    @DisplayName("Some tasks failed")
    public void halfSuccessedTasks() {
        try (MockedConstruction<Others.MountTableManager> mocked = Mockito.mockConstruction(Others.MountTableManager.class,
                (mock, context) -> {
                    when(mock.refresh()).thenReturn(((String) context.arguments().get(0)).startsWith("l"));
                    when(mock.getAddress()).thenReturn((String) context.arguments().get(0));
                })) {
            MountTableRefresherService mockedService = Mockito.spy(service);
            List<String> addresses = List.of("123", "local6", "789", "local");

            List<Others.RouterState> states = addresses.stream()
                    .map(Others.RouterState::new)
                    .collect(toList());

            when(routerStore.getCachedRecords()).thenReturn(states);

            // when
            mockedService.refresh();

            // then
            verify(mockedService).log("Mount table entries cache refresh successCount=2,failureCount=2");
            verify(routerClientsCache, times(2)).invalidate(anyString());
        }
    }

    @Test
    @DisplayName("One task completed with exception")
    public void exceptionInOneTask() {
        try (MockedConstruction<Others.MountTableManager> mocked = Mockito.mockConstruction(Others.MountTableManager.class,
                (mock, context) -> {
                    when(mock.refresh()).thenAnswer(i -> {
                        if (((String) context.arguments().get(0)).startsWith("1")) {
                            throw new InterruptedException();
                        }
                        return true;
                    });
                    when(mock.getAddress()).thenReturn((String) context.arguments().get(0));
                })) {
            MountTableRefresherService mockedService = Mockito.spy(service);
            List<String> addresses = List.of("123", "local6", "789", "local");

            List<Others.RouterState> states = addresses.stream()
                    .map(Others.RouterState::new)
                    .collect(toList());

            when(routerStore.getCachedRecords()).thenReturn(states);

            // when
            mockedService.refresh();

            // then
            verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
            verify(routerClientsCache, times(1)).invalidate(anyString());
        }
    }

    @Test
    @DisplayName("One task exceeds timeout")
    public void oneTaskExceedTimeout() {
        try (MockedConstruction<Others.MountTableManager> mocked = Mockito.mockConstruction(Others.MountTableManager.class,
                (mock, context) -> {
                    when(mock.refresh()).thenAnswer(i -> {
                        if (((String) context.arguments().get(0)).startsWith("7")) {
                            try {
                                Thread.sleep(service.getCacheUpdateTimeout() * 2);
                            } catch (InterruptedException ignored) {}
                        }
                        return true;
                    });
                    when(mock.getAddress()).thenReturn((String) context.arguments().get(0));
                })) {
            MountTableRefresherService mockedService = Mockito.spy(service);
            List<String> addresses = List.of("123", "local6", "local", "789");

            List<Others.RouterState> states = addresses.stream()
                    .map(Others.RouterState::new)
                    .collect(toList());

            when(routerStore.getCachedRecords()).thenReturn(states);

            // when
            mockedService.refresh();

            // then
            verify(mockedService).log("Mount table entries cache refresh successCount=3,failureCount=1");
            verify(routerClientsCache, times(1)).invalidate(anyString());
        }
    }

}
