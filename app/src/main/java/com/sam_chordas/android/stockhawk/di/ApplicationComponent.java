package com.sam_chordas.android.stockhawk.di;

import com.sam_chordas.android.stockhawk.service.StockTaskService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * google dagger component
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(StockTaskService stockTaskService);
}
