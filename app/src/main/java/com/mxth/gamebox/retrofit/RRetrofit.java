package com.mxth.gamebox.retrofit;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Cxp on 2017/1/17.
 */

public class RRetrofit {
    public static String BASE_URL = "http://sw.bos.baidu.com/";//正式服务器域名
    public static <T> T create(final Class<T> cls) {
        Retrofit.Builder builder = new Retrofit.Builder();
        Retrofit retrofit = builder
                .baseUrl(BASE_URL)//注意此处,设置服务器的地址
                .addConverterFactory(GsonConverterFactory.create())//用于Json数据的转换,非必须
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//用于返回Rxjava调用,非必须
                .client(getHttpClient())
                .build();

        return retrofit.create(cls);
    }
    public static <T> T create(final Class<T> cls,String baseUrl) {
        Retrofit.Builder builder = new Retrofit.Builder();
        Retrofit retrofit = builder
                .baseUrl(baseUrl)//注意此处,设置服务器的地址
                .addConverterFactory(GsonConverterFactory.create())//用于Json数据的转换,非必须
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//用于返回Rxjava调用,非必须
                .client(getHttpClient())
                .build();

        return retrofit.create(cls);
    }


    private static OkHttpClient getHttpClient() {
        //设置连接超时时间 ;
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).build();

        try {

            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, null, new SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            client.newBuilder().sslSocketFactory(sslSocketFactory, new X509TrustManager() {


                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }).hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return client;
    }
}
