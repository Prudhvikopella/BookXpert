package com.bookxpert.app.network

import com.bookxpert.app.BuildConfig
import com.bookxpert.app.ui.interfaces.ApiInterface
import com.google.gson.GsonBuilder
import com.pxquiz.app.constants.ApiConstants.Companion.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitUtil {

    fun createBaseApiService(
        baseUrl: String = BASE_URL,
        connectTimeoutInSec: Long = 70,
        readTimeoutInSec: Long = 120,
        writeTimeoutInSec: Long = 120
    ): ApiInterface {

        return getRetrofitClient(
            getOkhttpClientBuilder(
                connectTimeoutInSec,
                readTimeoutInSec,
                writeTimeoutInSec
            ), baseUrl
        ).create(ApiInterface::class.java)
    }

    private fun getRetrofitClient(okHttpClientBuilder: OkHttpClient.Builder, baseUrl: String) =
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(okHttpClientBuilder.build())
            .baseUrl(baseUrl)
            .build()

    private fun getOkhttpClientBuilder(
        connectTimeoutInSec: Long,
        readTimeoutInSec: Long,
        writeTimeoutInSec: Long
    ): OkHttpClient.Builder {

        val okHttpClientBuilder = OkHttpClient.Builder().apply {
            connectTimeout(connectTimeoutInSec, TimeUnit.SECONDS)
            readTimeout(readTimeoutInSec, TimeUnit.SECONDS)
            writeTimeout(writeTimeoutInSec, TimeUnit.SECONDS)
            //addInterceptor(AuthInterceptor())
            if(BuildConfig.DEBUG){
                val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                addInterceptor(logging)
            }
        }
        return okHttpClientBuilder

    }

}