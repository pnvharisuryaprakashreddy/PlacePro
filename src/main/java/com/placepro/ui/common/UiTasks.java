package com.placepro.ui.common;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

public final class UiTasks {

    private UiTasks() {
    }

    public static <T> void run(Supplier<T> backgroundTask, Consumer<T> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return backgroundTask.get();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Exception exception) {
                    onError.accept(exception);
                }
            }
        }.execute();
    }
}
