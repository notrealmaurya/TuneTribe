package com.maurya.dtxloopplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.database.tuneTribeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Database(
    entities = [MusicDataClass::class, FolderDataClass::class, PlayListDataClass::class],
    version = 1
)
abstract class tuneTribeDatabase : RoomDatabase() {

    abstract fun tuneTribeDao(): tuneTribeDao

}
