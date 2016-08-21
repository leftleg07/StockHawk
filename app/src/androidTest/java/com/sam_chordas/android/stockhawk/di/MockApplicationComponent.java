package com.sam_chordas.android.stockhawk.di;

import com.sam_chordas.android.stockhawk.network.TestApiService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * google dagger component
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface MockApplicationComponent {
    void inject(TestApiService testAPIService);
}
