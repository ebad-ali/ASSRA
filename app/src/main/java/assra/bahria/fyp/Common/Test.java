package assra.bahria.fyp.Common;

import org.reactivestreams.Subscriber;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Test<T> {

    final OkHttpClient client = new OkHttpClient();

    private  Observable<T> fetch(T t, final Request request , RequestBody requestBody){

        return  Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {

            }
        });

    }

}
