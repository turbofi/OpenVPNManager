package com.bvhllc.openvpnmanager;

import com.bvhllc.openvpnmanager.payload.ServiceBrokerGetExitPoints;
import com.bvhllc.openvpnmanager.payload.ServiceBrokerUseExitPoint;
import com.bvhllc.openvpnmanager.response.ExitPointsResponse;
import com.bvhllc.openvpnmanager.response.UseExitPointResponse;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServiceBrokerApi {

    @POST("/")
    Observable<UseExitPointResponse> useExitPoint(@Body ServiceBrokerUseExitPoint resource);

    @POST("/")
    Observable<ExitPointsResponse> getExitPoints(@Body ServiceBrokerGetExitPoints resource);
}