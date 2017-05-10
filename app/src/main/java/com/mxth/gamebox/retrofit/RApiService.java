package com.mxth.gamebox.retrofit;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;

/**
 * Created by Administrator on 2017/1/17.
 */

public interface RApiService {
    @GET("/sw-search-sp/software/d2622df9c559c/WeChat_2.4.1.67_setup.exe")
    Observable<ResponseBody> getBytes();
}
