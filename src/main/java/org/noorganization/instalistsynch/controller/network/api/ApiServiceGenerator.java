/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.network.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * The ServiceGenerator to get a apiservice to inject the auth header.
 * Created by Desnoo on 06.02.2016.
 */
public class ApiServiceGenerator {
    /**
     * The Base URL of the API.
     */
    public final static String API_ENDPOINT_URL = "http://instalist.noorganization.org/v1/";

    private static OkHttpClient.Builder sHttpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder sBuilder = new Retrofit.Builder()
            .baseUrl(API_ENDPOINT_URL)
            .addConverterFactory(JacksonConverterFactory.create());

    /**
     * Call this to generate a apiService with no injected authorization header.
     *
     * @param _serviceClass the api interface.
     * @param <S>           the service class type.
     * @return the gnerated service class.
     */
    public static <S> S createService(Class<S> _serviceClass) {
        return createService(_serviceClass, null);
    }

    /**
     * Create a service that supports a token.
     *
     * @param _serviceClass the class with the services.
     * @param _token        the token for the desired request.
     * @param <S>           the apiservice interface
     * @return the new request
     */
    public static <S> S createService(Class<S> _serviceClass, final String _token) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (_token != null) {
            sHttpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain _chain) throws IOException {
                    Request request = _chain.request();
                    Request.Builder reqBuilder = request.newBuilder();
                    reqBuilder
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "X-Token ".concat(_token))
                            .method(request.method(), request.body());
                    Request requestRet = reqBuilder.build();
                    return _chain.proceed(requestRet);
                }
            });
        }
        OkHttpClient client = sHttpClient.addInterceptor(interceptor).build();

        Retrofit retrofit = sBuilder.client(client).build();
        return retrofit.create(_serviceClass);
    }
}
