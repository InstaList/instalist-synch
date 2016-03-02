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

package org.noorganization.instalistsynch.utils;

import org.noorganization.instalistsynch.controller.network.api.ApiServiceGenerator;
import org.noorganization.instalistsynch.controller.network.api.unauthorized.IUnauthorizedApiService;

/**
 * Utils to get the api interface for the interaction.
 * Created by Desnoo on 06.02.2016.
 */
public class ApiUtils {
    /**
     * The unauthorized listApiService.
     */
    private IUnauthorizedApiService mUnauthorizedInstantListApiService;
    /**
     * The instance of this class.
     */
    private static ApiUtils sInstance;

    /**
     * Get the instance of this singleton.
     *
     * @return the instance.
     */
    public final static ApiUtils getInstance() {
        if (sInstance == null) {
            sInstance = new ApiUtils();
        }
        return sInstance;
    }

    private ApiUtils() {
        mUnauthorizedInstantListApiService = ApiServiceGenerator.createService(IUnauthorizedApiService.class);
    }


    /**
     * Get the interface to handle api requests  that are unauthorized.
     * the service to request the api.
     */
    public IUnauthorizedApiService getUnauthorizedInstantListApiService() {
        return mUnauthorizedInstantListApiService;
    }

    /**
     * Get the specified Api service with auth integration.
     *
     * @param _clazz the Api interface to get.
     * @param _token the token it should send in the Authorization header.
     * @param <S>    the interface of an item in {@link org.noorganization.instalistsynch.controller.network.api.authorized}.
     * @return the built api interaction class.
     */
    public <S> S getAuthorizedApiService(Class<S> _clazz, String _token) {
        return ApiServiceGenerator.createService(_clazz, _token);
    }

}
