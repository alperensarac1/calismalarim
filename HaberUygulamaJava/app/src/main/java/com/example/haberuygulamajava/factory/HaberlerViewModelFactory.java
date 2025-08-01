package com.example.haberuygulamajava.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.haberuygulamajava.dao.HaberDao;
import com.example.haberuygulamajava.viewmodel.HaberlerViewModel;

public class HaberlerViewModelFactory implements ViewModelProvider.Factory {
    private final HaberDao haberDao;

    public HaberlerViewModelFactory(HaberDao haberDao) {
        this.haberDao = haberDao;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HaberlerViewModel(haberDao);
    }
}

