package com.lunastratos.postofficemvno

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.CookieJar
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


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreference = getSharedPreferences("saveUserInfo", Context.MODE_PRIVATE)

        val getId = sharedPreference.getString("id", "")
        val getPw = sharedPreference.getString("pw", "")

        if(!getId.equals("") && !getPw.equals("")){ // 모든게 로그인 되어있다면
            findViewById<EditText>(R.id.idTxt).setText(getId)
            findViewById<EditText>(R.id.pwTxt).setText(getPw)

            getUserInfo(getId!!, getPw!!) // 로그인후 데이터 가져오기
        }

        findViewById<Button>(R.id.loginBtn).setOnClickListener {
            val id = findViewById<EditText>(R.id.idTxt).text.toString()
            val pw = findViewById<EditText>(R.id.pwTxt).text.toString()

            // 유저가 입력한 id, pw를 쉐어드에 저장한다.
            val editor = sharedPreference.edit()
            editor.putString("id", id)
            editor.putString("pw", pw)
            editor.apply()

            getUserInfo(id, pw) // 로그인후 데이터 가져오기
        }

    }

    fun getUserInfo(id :String, pw: String){

        val client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(CookieManager()))
            .build()
        val retrofit = Retrofit.Builder().baseUrl("https://www.tplusmobile.com")
            .client(client) //OkHttpClient 연결
            .build()
        val service = retrofit.create(RetrofitInterface::class.java);

        service.setLogin(
            id, pw, "true"
        ).enqueue(object : Callback<ResponseBody>{

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful){
                    // 정상적으로 통신이 성공된 경우
                    var result: String? = response.body()!!.string()
                    var code: String? = response.code().toString()
                    val docAfterLogin: Document = Jsoup.parse(result)

//                    Log.d("YMC", "onResponse 성공: " + result?.toString())
//                    Log.d("YMC", "onResponse 성공: " + code?.toString())
//                    Log.d("YMC", "onResponse 성공: " + docAfterLogin.select("li.u2").select("span.atxt").text())

                    //로그인 안됨
                    if(docAfterLogin.select("li.u2").select("span.atxt").text().equals("로그인")){
                        toast("로그인에 실패했습니다.\n아이디와 암호를 확인해주세요")
                    }
                    //로그인 됨
                    if(docAfterLogin.select("li.u21").select("span.atxt").text().equals("로그아웃")){
                        toast("로그인에 성공!\n잠시만 기다려 주세요")

                        service.getPage().enqueue(object : Callback<ResponseBody>{
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

//                                Log.d("YMC", " 결과 " + phoneUserName)
//                                Log.d("YMC", " 결과 " + phoneUserContract)
//                                Log.d("YMC", " 결과 " + phoneUserNumber)

                                findViewById<TextView>(R.id.dataText).setText("${phoneDataLimit} / ${phoneData}")
                                findViewById<TextView>(R.id.mmsText).setText("${phoneMmsLimit} / ${phoneMms}")
                                findViewById<TextView>(R.id.voiceText).setText("${phoneVoiceLimit} / ${phoneVoice}")
                                findViewById<TextView>(R.id.signatureText).setText("${phoneUserContract}")
                                findViewById<TextView>(R.id.userText).setText("${phoneUserName} / ${phoneUserNumber}")

                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                toast("에러가 발생하여 실패하였습니다.")
                            }

                        })
                    }



                }else{
                    // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                    toast("에러가 발생하여 실패하였습니다.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 (인터넷 끊킴, 예외 발생 등 시스템적인 이유)
                toast("에러가 발생하여 실패하였습니다.")
            }
        })
    }

    fun toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
