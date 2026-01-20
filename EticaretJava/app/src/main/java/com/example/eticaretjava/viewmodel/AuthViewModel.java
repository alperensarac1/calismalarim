package com.example.eticaretjava.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eticaretjava.model.User.UserDto;
import com.example.eticaretjava.repo.RepositoriesImpl;
import com.example.eticaretjava.repo.ResultCallback;

public class AuthViewModel extends ViewModel {

    public static class AuthState {
        public boolean inFlight = false;
        public String error = null;
        public boolean loggedIn = false;
        public boolean registered = false;
        public UserDto me = null;
    }

    private final MutableLiveData<AuthState> state = new MutableLiveData<>(new AuthState());
    private final RepositoriesImpl.AuthRepositoryImpl repo;

    public AuthViewModel(RepositoriesImpl.AuthRepositoryImpl repo) {
        this.repo = repo;
    }

    public LiveData<AuthState> getState() {
        return state;
    }

    private AuthState copyState() {
        AuthState cur = state.getValue() != null ? state.getValue() : new AuthState();
        AuthState n = new AuthState();
        n.inFlight = cur.inFlight;
        n.error = cur.error;
        n.loggedIn = cur.loggedIn;
        n.registered = cur.registered;
        n.me = cur.me;
        return n;
    }

    public void login(String email, String password) {
        AuthState s = copyState();
        s.inFlight = true;
        s.error = null;
        s.loggedIn = false;
        state.setValue(s);

        repo.login(email, password, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                AuthState ns = copyState();
                ns.inFlight = false;
                ns.loggedIn = true;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                AuthState ns = copyState();
                ns.inFlight = false;
                ns.error = message != null ? message : "Login failed";
                state.postValue(ns);
            }
        });
    }

    public void register(String name, String email, String password) {
        AuthState s = copyState();
        s.inFlight = true;
        s.error = null;
        s.registered = false;
        state.setValue(s);

        repo.register(name, email, password, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                AuthState ns = copyState();
                ns.inFlight = false;
                ns.registered = true;
                state.postValue(ns);
            }

            @Override
            public void onError(String message) {
                AuthState ns = copyState();
                ns.inFlight = false;
                ns.error = message != null ? message : "Register failed";
                state.postValue(ns);
            }
        });
    }

    public void clearError() {
        AuthState ns = copyState();
        ns.error = null;
        state.setValue(ns);
    }

    public void clearRegistered() {
        AuthState ns = copyState();
        ns.registered = false;
        state.setValue(ns);
    }
}

