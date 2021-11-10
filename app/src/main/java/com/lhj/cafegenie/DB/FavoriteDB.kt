package com.lhj.cafegenie.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteData::class], version = 1)
abstract class FavoriteDB: RoomDatabase() {

    abstract fun favDao(): FavoriteDao

    companion object {
        private var INSTANCE: FavoriteDB? = null

        fun getInstance(context: Context): FavoriteDB? {
            if (INSTANCE == null) {
                synchronized(FavoriteDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        FavoriteDB::class.java, "favorite")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }

}