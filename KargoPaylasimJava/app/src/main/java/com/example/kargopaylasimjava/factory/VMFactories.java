package com.example.kargopaylasimjava.factory;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.kargopaylasimjava.repo.CargoRepository;
import com.example.kargopaylasimjava.service.TokenStore;
import com.example.kargopaylasimjava.viewmodel.AddressListViewModel;
import com.example.kargopaylasimjava.viewmodel.AddressViewModel;
import com.example.kargopaylasimjava.viewmodel.AuthViewModel;
import com.example.kargopaylasimjava.viewmodel.ShipmentViewModel;

public class VMFactories {

    public static class AuthVmFactory implements ViewModelProvider.Factory {
        private final CargoRepository repo;
        private final TokenStore tokenStore;

        public AuthVmFactory(CargoRepository repo, TokenStore tokenStore) {
            this.repo = repo;
            this.tokenStore = tokenStore;
        }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new AuthViewModel(repo, tokenStore);
        }
    }

    public static class ShipmentVmFactory implements ViewModelProvider.Factory {
        private final CargoRepository repo;
        public ShipmentVmFactory(CargoRepository repo) { this.repo = repo; }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ShipmentViewModel(repo);
        }
    }

    public static class AddressVmFactory implements ViewModelProvider.Factory {
        private final CargoRepository repo;
        public AddressVmFactory(CargoRepository repo) { this.repo = repo; }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new AddressViewModel(repo);
        }
    }

    public static class AddressListVmFactory implements ViewModelProvider.Factory {
        private final CargoRepository repo;
        public AddressListVmFactory(CargoRepository repo) { this.repo = repo; }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AddressListViewModel.class)) {
                return (T) new AddressListViewModel(repo);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

