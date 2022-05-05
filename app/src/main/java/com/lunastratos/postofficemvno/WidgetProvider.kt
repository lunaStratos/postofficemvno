package com.lunastratos.postofficemvno

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
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


class WidgetProvider : AppWidgetProvider() {

    private val UPDATE = "android.appwidget.action.APPWIDGET_UPDATE"
    private val ENABLED = "android.appwidget.action.APPWIDGET_ENABLED"

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val action  = intent?.action
        val appWidgetManager = AppWidgetManager.getInstance(context)
        Log.d("action 이다 : ", action.toString())

        if(action == ENABLED) update(context, appWidgetManager, 0)

    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds!!.forEach {
            update(context, appWidgetManager, it)
        }
    }

    fun update(context: Context?, appWidgetManager: AppWidgetManager?, idx: Int) {
        val views = RemoteViews(context!!.packageName, R.layout.info_widget)

        val sharedPreference = context.getSharedPreferences("saveUserInfo", Context.MODE_PRIVATE)
        val getId = sharedPreference.getString("id", "")!!
        val getPw = sharedPreference.getString("pw", "")!!

        val manager = AppWidgetManager.getInstance(context)

        val client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(CookieManager()))
            .build()
        val retrofit = Retrofit.Builder().baseUrl("https://www.tplusmobile.com")
            .client(client) //OkHttpClient 연결
            .build()
        val service = retrofit.create(RetrofitInterface::class.java);

        service.setLogin(
            getId, getPw, "true"
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

                                val phoneVoice = doc.select("div.voice").select("span.rate").text().split("/")[0]
                                val phoneData =  doc.select("div.data").select("span.rate").text().split("/")[0]
                                val phoneMms = doc.select("div.mms").select("span.rate").text().split("/")[0]

                                Log.d("action 이다 ? ", phoneVoice)

                                views.setTextViewText(R.id.dataWidgetText,phoneData)
                                views.setTextViewText(R.id.mmsWidgetText, phoneMms)
                                views.setTextViewText(R.id.voiceWidgetText,phoneVoice)
                                appWidgetManager!!.updateAppWidget(idx, views)

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

    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }


}