package com.sam_chordas.android.stockhawk.di;

import com.sam_chordas.android.stockhawk.service.StockTaskService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by heim on 8/20/16.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(StockTaskService stockTaskService);
}
