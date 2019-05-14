package com.mind.taxi;

import android.os.AsyncTask;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    MultipartBody.Builder request_body;
    String url;
    int method;
    okhttp3.Callback callback;

    final int HTTP_METHOD_POST = 0;
    final int HTTP_METHOD_GET = 1;
    final int HTTP_METHOD_PUT = 2;
    final int HTTP_METHOD_DELETE = 3;

    HttpUtil(){
        request_body = new MultipartBody.Builder().setType(MultipartBody.FORM);


    }
    HttpUtil setFile(String key, String value, String path) {
        request_body.addFormDataPart(key, value, RequestBody.create(MultipartBody.FORM, new File(path)));
        return this;
    }
    HttpUtil setData(String key, String value) {
        request_body.addFormDataPart(key, value);


        return this;
    }
    HttpUtil setCallback(okhttp3.Callback callback) {
        this.callback = callback;
        return this;
    }
    HttpUtil setUrl(String url) {
        this.url = url;
        return this;
    }
    HttpUtil setMethod(int method) {
        this.method = method;
        return this;
    }
    HttpUtil execute() {
        new Execute().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return this;
    }


    private class Execute extends AsyncTask<Object, Object, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            Request request;

            switch(method){
                case HTTP_METHOD_POST :
                    request = new Request.Builder()
                            .url(url)
                            .post(request_body.build())
                            .build();
                    break;

                case HTTP_METHOD_GET :
                    request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    break;

                case HTTP_METHOD_PUT :
                    request = new Request.Builder()
                            .url(url)
                            .put(request_body.build())
                            .build();
                    break;
                case HTTP_METHOD_DELETE :
                    request = new Request.Builder()
                            .url(url)
                            .delete(request_body.build())
                            .build();
                    break;

                default:
                    request = new Request.Builder()
                            .url(url)
                            .post(request_body.build())
                            .build();
                    break;
            }

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(callback);
            return null;
        }
    }
}

