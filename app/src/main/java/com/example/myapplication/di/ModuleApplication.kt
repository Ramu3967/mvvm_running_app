package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.db.RunningDatabase
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
            "database_running"
        ).build()

    @Provides
    @Singleton
    fun provideRunDao(db:RunningDatabase) = db.getRunDao()
}