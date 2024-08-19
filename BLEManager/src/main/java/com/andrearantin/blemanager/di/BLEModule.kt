package com.andrearantin.blemanager.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import androidx.core.content.ContextCompat
import com.andrearantin.blemanager.BLEDataManager
import com.andrearantin.blemanager.BLEService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.annotation.Nullable
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BLEModule {

    @Provides
    @Singleton
    fun provideBluetoothManager(@ApplicationContext context: Context) : BluetoothManager?{
        return ContextCompat.getSystemService(context, BluetoothManager::class.java)
    }

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context) : BluetoothAdapter?{
        return ContextCompat.getSystemService(context, BluetoothManager::class.java)?.adapter
    }

    @Provides
    @Singleton
    fun provideBLEScanner(@Nullable bleAdapter : BluetoothAdapter?) : BluetoothLeScanner?{
        return bleAdapter?.bluetoothLeScanner
    }

    @Provides
    @Singleton
    fun provideBLEService(@Nullable bleAdapter: BluetoothAdapter?,bleDataManager: BLEDataManager) : BLEService {
        return BLEService(bleAdapter,bleDataManager)
    }

    @Provides
    @Singleton
    fun provideBLEDataManager() : BLEDataManager{
        return BLEDataManager()
    }
}