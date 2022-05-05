package com.lunastratos.postofficemvno

import android.content.Context
import android.widget.TextView
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.net.CookieManager

class commonUtil {

    fun getUserInfo(id :String, pw: String) : Map<String, String>{

        val map = mutableMapOf<String, String>()

        val client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(CookieManager()))
            .build()
        val retrofit = Retrofit.Builder().baseUrl("https://www.tplusmobile.com")
            .client(client) //OkHttpClient 연결
            .build()
        val service = retrofit.create(RetrofitInterface::class.java);

        service.setLogin(
            id, pw, "true"
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful){
                    // 정상적으로 통신이 성공된 경우
                    var result: String? = response.body()!!.string()
                    var code: String? = response.code().toString()
                    val docAfterLogin: Document = Jsoup.parse(result)

                    //로그인 안됨
                    if(docAfterLogin.select("li.u2").select("span.atxt").text().equals("로그인")){
                    }
                    //로그인 됨
                    if(docAfterLogin.select("li.u21").select("span.atxt").text().equals("로그아웃")){

                        service.getPage().enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call2: Call<ResponseBody>,
                                response2: Response<ResponseBody>
                            ) {
                                var html: String? = response2.body()!!.string()
                                val doc: Document = Jsoup.parse(html)

                                //Log.d("YMC", " 결과? " + doc.select("div.voice"))

                                val phoneVoiceLimit = doc.select("div.voice").select("span.rate").text().split("/")[1]
                                val phoneDataLimit =  doc.select("div.data").select("span.rate").text().split("/")[1]
                                val phoneMmsLimit = doc.select("div.mms").select("span.rate").text().split("/")[1]

                                val phoneVoice = doc.select("div.voice").select("span.rate").text().split("/")[0]
                                val phoneData =  doc.select("div.data").select("span.rate").text().split("/")[0]
                                val phoneMms = doc.select("div.mms").select("span.rate").text().split("/")[0]

                                val phoneUserName = doc.select("div.box.box1").select("strong").text() // 사용자이름
                                val phoneUserContract = doc.select("div.box.box2").select("em").text() // 요금제
                                val phoneUserNumber = doc.select("p.mt10").text() // 번호

                                map.put("phoneDataLimit", phoneDataLimit)
                                map.put("phoneMmsLimit", phoneMmsLimit)
                                map.put("phoneVoiceLimit", phoneVoiceLimit)

                                map.put("phoneVoice", phoneVoice)
                                map.put("phoneData", phoneData)
                                map.put("phoneMms", phoneMms)

                                map.put("phoneUserName", phoneUserName)
                                map.put("phoneUserContract", phoneUserContract)
                                map.put("phoneUserNumber", phoneUserNumber)

                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                TODO("Not yet implemented")
                            }

                        })
                    }

                }else{
                    // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 (인터넷 끊킴, 예외 발생 등 시스템적인 이유)
            }
        })

        return map
    }
}