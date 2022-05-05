package com.lunastratos.postofficemvno

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitInterface {

    // 최초 로그인 
    @FormUrlEncoded
    @POST("view/mytplus/loginAction.do")
    fun setLogin(
        @Field("mberId") mberId: String,
        @Field("password") password: String,
        @Field("idSaveCheck") idSaveCheck : String,
    ): Call<ResponseBody>

    //데이터 있는 부분
    @GET("view/mytplus/getPrductrecomend.do")
    fun getPage(): Call<ResponseBody>
}