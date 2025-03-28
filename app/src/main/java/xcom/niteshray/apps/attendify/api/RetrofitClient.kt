package xcom.niteshray.apps.attendify.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.twilio.com/2010-04-01/"

    fun getTwilioService(): TwilioApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TwilioApi::class.java)
    }
}

