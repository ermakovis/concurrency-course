package course.concurrency.m3_shared.collections;

import java.util.HashSet;
import java.util.Set;

public class RestaurantService {

    private Object stat;
    private Restaurant mockRestaurant = new Restaurant("A");

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return mockRestaurant;
    }

    public void addToStat(String restaurantName) {
        // Ð²Ð°Ñˆ ÐºÐ¾Ð´
    }

    public Set<String> printStat() {
        // Ð²Ð°Ñˆ ÐºÐ¾Ð´
        return new HashSet<>();
    }
}
