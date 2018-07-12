package assra.bahria.fyp.Common.Utils;

import android.util.Log;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RxOkhttpWrapper {

    private static final String TAG = "RxOkhttpWrapper";
    public static Observable<Response> getResponse(final HttpUrl url) {

        final OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .build();

        Log.d(TAG,request.url().toString());

        return  Observable.fromCallable(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return client.newCall(request).execute();
            }
        });


    }


    public static Observable<Response> getResponse(final String  url, final RequestBody requestBody) {

        final OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Log.d(TAG,request.url().toString());

        return  Observable.fromCallable(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return client.newCall(request).execute();
            }
        });


    }
}
