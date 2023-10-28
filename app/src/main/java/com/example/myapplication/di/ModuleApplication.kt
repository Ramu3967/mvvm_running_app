package com.example.myapplication.di

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.room.Room
import com.example.myapplication.db.RunningDatabase
import com.example.myapplication.util.RunConstants
import com.example.myapplication.util.RunConstants.DATABASE_NAME
import com.example.myapplication.util.RunConstants.PREF_MY_APP
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuleApplication {

    @Provides
    @Singleton
    fun provideRunningDb(@ApplicationContext context:Context)=
        Room.databaseBuilder(
            context,
            RunningDatabase::class.java,
            DATABASE_NAME
        ).build()

    @Provides
    @Singleton
    fun provideRunDao(db:RunningDatabase) = db.getRunDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context:Context) : SharedPreferences {
        return context.getSharedPreferences(PREF_MY_APP, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideIsFirstRun(appPref:SharedPreferences) = appPref.getBoolean(RunConstants.PREF_FIRST_TIME,true)

    @Provides
    @Singleton
    fun provideLocationPermissionsArray() = mutableListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)
        .apply {if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)}.toTypedArray()
}