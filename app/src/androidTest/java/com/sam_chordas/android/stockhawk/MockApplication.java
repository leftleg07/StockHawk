package com.sam_chordas.android.stockhawk;


import com.sam_chordas.android.stockhawk.di.ApplicationModule;
import com.sam_chordas.android.stockhawk.di.DaggerMockApplicationComponent;
import com.sam_chordas.android.stockhawk.di.MockApplicationComponent;

/**
 * Mock Application
 */
public class MockApplication extends MyStocksApplication {
    public MockApplicationComponent getMockComponent() {
        return mMockComponent;
    }

    private MockApplicationComponent mMockComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mMockComponent = DaggerMockApplicationComponent.builder().applicationModule(new ApplicationModule(this)).build();
    }
}
