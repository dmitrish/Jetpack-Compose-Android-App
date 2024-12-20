package com.coroutines.thisdayinhistory.di

import com.coroutines.api.translation.TranslationApiService
import com.coroutines.api.wiki.WikiMediaApiService
import com.coroutines.api.interceptors.CacheInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTranslationApiService(): TranslationApiService {
        return Retrofit.Builder()
            .baseUrl(TranslationApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(TranslationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWikiMediaApiService(): WikiMediaApiService {
        val cacheInterceptor = CacheInterceptor()
       // val interceptor = HttpLoggingInterceptor()
       // interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(cacheInterceptor)
          //  .addInterceptor(interceptor)
           // .addNetworkInterceptor(interceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl(WikiMediaApiService.BASE_WIKI_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build().create(WikiMediaApiService::class.java)
    }
}

