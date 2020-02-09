package com.example.httpstest

import CustomCallback
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.security.KeyStore
import java.security.cert.CertificateFactory

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.cert.X509Certificate
import java.io.*


enum class RequestType {
    REQUEST_TYPE_1,
    REQUEST_TYPE_2
}

class MainActivity : AppCompatActivity(),CustomCallback {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = getResources().openRawResource(
            getResources().getIdentifier("tee",
                "raw", getPackageName()));
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        System.out.println("ca=" + ca.subjectDN)

// Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

// Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

// Create an SSLContext that uses our TrustManager
        val context: SSLContext = SSLContext.getInstance("TLS").apply {
            init(null, tmf.trustManagers, null)
        }





        test_get_button.setOnClickListener() {
            // Tell the URLConnection to use a SocketFactory from our SSLContext
            val baseUrl = "https://${address_field.text}"
            val task = HttpsGetAsyncTask(context)
            val response=task.execute(baseUrl + "/test/get").get()
            test_response_get.text=response
        }

        test_post_button.setOnClickListener(){
            val baseUrl = ("https://${address_field.text}")
            var  postData= HashMap<String,String> ()
            postData["title"]= "Pieris"
            postData["text"]= post_field.text.toString()
            val task = HttpsPostAsyncTask(postData, RequestType.REQUEST_TYPE_2,this)
            task.execute(baseUrl + "/test/post")
        }
    }

    override fun completionHandler(success: Boolean?, type: RequestType, response:Any) {
        when (type){
            RequestType.REQUEST_TYPE_1-> runOnUiThread(Runnable {
                test_response_get.text=response.toString()
            })
            RequestType.REQUEST_TYPE_2-> runOnUiThread(Runnable {
                test_response_post.text=response.toString()
            });
        }
    }
}