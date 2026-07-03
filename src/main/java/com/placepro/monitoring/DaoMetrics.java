package com.placepro.monitoring;

import io.prometheus.client.Histogram;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Wraps a DAO behind its interface so every call is timed with
 * placepro_db_query_duration_seconds{dao_method="Interface.method"}.
 *
 * This is the single try/finally-with-timer for all DAO calls: applying it
 * once here (via a plain JDK proxy) avoids editing every DAO method by hand.
 * The wrapped DAO behaves identically, including thrown exceptions.
 */
public final class DaoMetrics {

    private DaoMetrics() {
    }

    public static <T> T instrument(Class<T> daoInterface, T target) {
        Object proxy = Proxy.newProxyInstance(
                daoInterface.getClassLoader(),
                new Class<?>[]{daoInterface},
                (unused, method, args) -> {
                    Histogram.Timer timer = MetricsRegistry.get()
                            .startDbTimer(daoInterface.getSimpleName() + "." + method.getName());
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException exception) {
                        throw exception.getCause() == null ? exception : exception.getCause();
                    } finally {
                        timer.observeDuration();
                    }
                });
        return daoInterface.cast(proxy);
    }
}
