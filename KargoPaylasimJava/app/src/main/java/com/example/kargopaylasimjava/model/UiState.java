package com.example.kargopaylasimjava.model;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class UiState<T> {

    private UiState() {}

    public static final class Idle<T> extends UiState<T> {
        public Idle() {}
    }

    public static final class Loading<T> extends UiState<T> {
        public Loading() {}
    }

    public static final class Success<T> extends UiState<T> {
        @NonNull public final T data;
        public Success(@NonNull T data) { this.data = data; }
    }

    public static final class Error<T> extends UiState<T> {
        @NonNull public final String message;
        public Error(@NonNull String message) { this.message = message; }
    }

    // küçük yardımcılar
    public static <T> UiState<T> idle() { return new Idle<>(); }
    public static <T> UiState<T> loading() { return new Loading<>(); }
    public static <T> UiState<T> success(@NonNull T data) { return new Success<>(data); }
    public static <T> UiState<T> error(@NonNull String msg) { return new Error<>(msg); }
}
