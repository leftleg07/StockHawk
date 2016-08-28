package com.sam_chordas.android.stockhawk;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.sam_chordas.android.stockhawk.di.ApplicationComponent;
import com.sam_chordas.android.stockhawk.di.ApplicationModule;
import com.sam_chordas.android.stockhawk.di.DaggerApplicationComponent;

/**
 * Created by heim on 8/20/16.
 */

public class StockApplication extends Application {
    public ApplicationComponent getComponent() {
        return mComponent;
    }

    private ApplicationComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        mComponent = DaggerApplicationComponent.builder().applicationModule(new ApplicationModule(this)).build();
    }
}
