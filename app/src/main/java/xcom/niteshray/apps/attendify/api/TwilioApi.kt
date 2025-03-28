package xcom.niteshray.apps.attendify.api

import retrofit2.Call
import retrofit2.http.*

interface TwilioApi {
    @FormUrlEncoded
    @POST("Accounts/{AccountSid}/Messages.json")
    fun sendWhatsAppMessage(
        @Path("AccountSid") accountSid: String,
        @Header("Authorization") authHeader: String,
        @Field("To") to: String,
        @Field("From") from: String,
        @Field("Body") body: String
    ): Call<Void>
}


