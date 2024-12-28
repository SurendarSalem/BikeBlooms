package com.bikeblooms.android.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.location.Geocoder
import com.bikeblooms.android.data.FirebaseHelper
import com.bikeblooms.android.data.LocationRepository
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.data.RoomDBHelper
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.data.VendorRepository
import com.bikeblooms.android.util.LocationClient
import com.bikeblooms.android.util.LocationHelper
import com.bikeblooms.android.util.SharedPrefHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
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

    @Singleton
    @Provides
    fun providesFirebaseHelper(): FirebaseHelper {
        return FirebaseHelper()
    }

    @Singleton
    @Provides
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context, Locale.getDefault())
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

    @Singleton
    @Provides
    fun provideVendorRepository(firebaseHelper: FirebaseHelper): VendorRepository {
        return VendorRepository(firebaseHelper)
    }

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Singleton
    @Provides
    fun provideLocationRepository(locationClient: LocationClient): LocationRepository {
        return LocationRepository(locationClient)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface YourClassModule {
    @Singleton
    @Binds
    abstract fun bindLocationClient(concretion: LocationHelper): LocationClient
}