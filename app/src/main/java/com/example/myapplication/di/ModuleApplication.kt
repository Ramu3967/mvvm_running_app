package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.db.RunningDatabase
import com.example.myapplication.util.RunConstants.DATABASE_NAME
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
}