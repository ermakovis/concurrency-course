package course.concurrency.exams.refactoring;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Getter
public class MountTableRefresherService {
    private Others.RouterStore routerStore = new Others.RouterStore();
    private long cacheUpdateTimeout;

    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;

    public void serviceInit()  {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new Others.LoadingCache<String, Others.RouterClient>();
        routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

        initClientCacheCleaner(routerClientMaxLiveTime);
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setName("MountTableRefresh_ClientsCacheCleaner");
                t.setDaemon(true);
                return t;
            }
        };

        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and
         * closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh()  {
        var futuresMap = routerStore.getCachedRecords().stream()
                .map(Others.RouterState::getAdminAddress)
                .filter(StringUtils::hasLength)
                //лучше вынести в отдельную функцию
                .map(address -> isLocalAdmin(address) ?
                        new Others.MountTableManager("local") :
                        new Others.MountTableManager(address))
                //можно указать эксекьютор, cachedPoolExecutor был бы наиболее близким к предыдущему функционалу
                .collect(Collectors.toMap(
                        manager -> manager,
                        manager -> CompletableFuture.supplyAsync(manager::refresh)
                                .orTimeout(cacheUpdateTimeout, TimeUnit.MILLISECONDS),
                        (e1, e2) -> e1)
                );

        CompletableFuture.allOf(futuresMap.values().toArray(CompletableFuture[]::new))
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof TimeoutException) {
                        log("Not all router admins updated their cache");
                    }
                    if (ex.getCause() instanceof InterruptedException) {
                        log("Mount table cache refresher was interrupted.");
                    }
                    return null;
                })
                .whenComplete((result, ex) -> {
                    logResult(futuresMap);
                }).join();
    }

    protected MountTableRefresherThread getLocalRefresher(String adminAddress) {
        return new MountTableRefresherThread(new Others.MountTableManager("local"), adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contains("local");
    }

    private void logResult(Map<Others.MountTableManager, CompletableFuture<Boolean>> futures) {
        int successCount = 0;
        int failureCount = 0;
        for (Map.Entry<Others.MountTableManager, CompletableFuture<Boolean>> entry : futures.entrySet()) {
            if (isFutureSuccess(entry.getValue())) {
                successCount++;
            } else {
                failureCount++;
                // remove RouterClient from cache so that new client is created
                removeFromCache(entry.getKey().getAddress());
            }
        }
        log(String.format(
                "Mount table entries cache refresh successCount=%d,failureCount=%d",
                successCount, failureCount));
    }

    private static boolean isFutureSuccess(CompletableFuture<Boolean> booleanFuture) {
        try {
            return booleanFuture.get();
        } catch (Exception e) {
            return false;
        }
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
        this.cacheUpdateTimeout = cacheUpdateTimeout;
    }
    public void setRouterClientsCache(Others.LoadingCache cache) {
        this.routerClientsCache = cache;
    }

    public void setRouterStore(Others.RouterStore routerStore) {
        this.routerStore = routerStore;
    }
}