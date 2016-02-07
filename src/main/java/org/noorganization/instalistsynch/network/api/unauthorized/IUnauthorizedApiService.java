package org.noorganization.instalistsynch.network.api.unauthorized;

import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.network.response.RetrofitAuthToken;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;
import org.noorganization.instalistsynch.model.network.response.RegisterDeviceResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * All Api interactions that does not require any authorization.
 * Created by Desnoo on 06.02.2016.
 */
public interface IUnauthorizedApiService {

    /**
     * Try to register a new group.
     *
     * @return an temporary group-access-id.
     */
    @POST("groups")
    Call<GroupResponse> registerGroup();


    /**
     * Register this device to a specific group.
     *
     * @param _group the temporary group object.
     * @return the registered device id in this group.
     */
    @POST("groups/{id}/devices")
    Call<RegisterDeviceResponse> registerDevice(@Path("id") int _id, @Body Group _group);


    /**
     * Get the token for the group.
     *
     * @param _authorization Authorization-header as defined per RFC 2617. Expects the server defined user-id as user and the client defined secret as password. "Basic " + client:password
     * @return a token for this specific group.
     */
    @GET("groups/{id}/devices/token")
    Call<RetrofitAuthToken> token(@Path("id") int _id, @Header("Authorization") String _authorization);


}
