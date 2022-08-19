package course.concurrency.m3_shared.collections;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RestaurantService {
    private ConcurrentHashMap<String, Integer> stat = new ConcurrentHashMap<>();
    private Restaurant mockRestaurant = new Restaurant("A");

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return mockRestaurant;
    }

    public void addToStat(String restaurantName) {
        stat.compute(restaurantName, (k,v) -> v == null ? 1 : v + 1);
    }

    public Set<String> printStat() {
        return stat.entrySet().stream()
                .map(entry -> String.format("%s - %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }
}
