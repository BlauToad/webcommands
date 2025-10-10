package de.blautoad.webcommands;

// ResultManager.java

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class ResultManager {
    private static final BlockingQueue<Result> results = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Predicate<Result>> searched_filters = new LinkedBlockingQueue<>();

    public static BlockingQueue<Predicate<Result>> getSearched_filters() {
        return searched_filters;
    }

    public static void addResult(Result result) {
        results.add(result);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            results.remove(result);
        });
        thread.start();
    }

    public static Result waitForResult(Predicate<Result> filter, long timeout, TimeUnit unit) throws InterruptedException {
        searched_filters.add(filter);
        long startTime = System.currentTimeMillis();
        while (true) {
            for (Result result : results) {
                if (filter.test(result)) {
                    searched_filters.remove(filter);
                    results.remove(result); // remove the result from the queue
                    result.debug = results.size();
                    return result;
                }
            }
            if (unit.toMillis(timeout) < System.currentTimeMillis() - startTime) {
                searched_filters.remove(filter);
                throw new RuntimeException("Timeout waiting for result");
            }
            TimeUnit.MILLISECONDS.sleep(100); // adjust the sleep time as needed
        }
    }

    public static Result waitForResult_TextFilter(String filterResult, long timeout, TimeUnit unit) throws InterruptedException {
        Predicate<Result> filter = result -> result.getCommandResultText().toLowerCase().contains(filterResult.toLowerCase());
        return waitForResult(filter, timeout, unit);
    }

    public static Result waitForResult_ObjFilter(String filterResult, long timeout, TimeUnit unit) throws InterruptedException {
        Predicate<Result> filter = result -> result.getCommandResult().toLowerCase().contains(filterResult.toLowerCase());
        return waitForResult(filter, timeout, unit);
    }
}