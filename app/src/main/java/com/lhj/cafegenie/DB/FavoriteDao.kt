package com.lhj.cafegenie.DB

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite")
    fun getAllFav() : LiveData<List<FavoriteData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFav(favData : FavoriteData)

    @Delete
    fun deleteFav(favData : FavoriteData)

}