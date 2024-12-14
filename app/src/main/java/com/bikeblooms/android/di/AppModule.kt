package com.bikeblooms.android.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.bikeblooms.android.data.FirebaseHelper
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.data.RoomDBHelper
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.util.SharedPrefHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val PREFS_BIKE_BLOOMS = "PREFS_BIKE_BLOOMS"

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val sharedPreferences = context.getSharedPreferences(PREFS_BIKE_BLOOMS, MODE_PRIVATE)
        return sharedPreferences
    }

    @Provides
    fun providesSharedPreferencesHelper(sharedPreferences: SharedPreferences): SharedPrefHelper {
        return SharedPrefHelper(sharedPreferences)
    }

    @Provides
    fun providesRoomDBHelper(): RoomDBHelper {
        return RoomDBHelper()
    }

    @Provides
    fun providesFirebaseHelper(): FirebaseHelper {
        return FirebaseHelper()
    }

    @Singleton
    @Provides
    fun provideRepository(roomDBHelper: RoomDBHelper, firebaseHelper: FirebaseHelper): Repository {
        return Repository(roomDBHelper, firebaseHelper)
    }

    @Singleton
    @Provides
    fun provideVehicleRepository(firebaseHelper: FirebaseHelper): VehiclesRepository {
        return VehiclesRepository(firebaseHelper)
    }
}