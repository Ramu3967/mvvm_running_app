package com.example.myapplication.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.db.BitMapTypeConverter
import com.example.myapplication.db.Run
import com.example.myapplication.db.RunDao

@Database(entities = [Run::class], version = 1)
@TypeConverters(BitMapTypeConverter::class)
abstract class RunningDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDao
}