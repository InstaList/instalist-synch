package org.noorganization.instalistsynch;

import org.noorganization.instalistsynch.network.api.IInstantListApiService;

import retrofit2.Retrofit;

/**
 * The main application, entry point of the app.
 * Created by tinos_000 on 29.01.2016.
 */
public class MainApplication extends android.app.Application {

    /**
     * The Base URL of the API.
     */
    public final static String API_ENDPOINT_URL = "https://instantlist.noorganization.org/api/v1";
    /**
     * The Service to query API.
     */
    private IInstantListApiService mInstantListApiService;

    @Override
    public void onCreate() {
        super.onCreate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_ENDPOINT_URL)
                .build();
        mInstantListApiService = retrofit.create(IInstantListApiService.class);

    }

    /**
     * Get the interface to handle api requests and responses with retrofit.
     *
     * @return the service to request the api.
     */
    public IInstantListApiService getApiService() {
        return mInstantListApiService;
    }

}
