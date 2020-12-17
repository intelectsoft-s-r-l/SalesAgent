package md.intelectsoft.salesagent.BrokerServiceUtils;

import md.intelectsoft.salesagent.BrokerServiceUtils.Body.InformationData;
import md.intelectsoft.salesagent.BrokerServiceUtils.Body.SendGetURI;
import md.intelectsoft.salesagent.BrokerServiceUtils.Body.SendRegisterApplication;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.ErrorMessage;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.GetNews;
import md.intelectsoft.salesagent.BrokerServiceUtils.Results.RegisterApplication;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BrokerServiceAPI {
    @GET("/ISConnectionBrokerService/json/Ping")
    Call<Boolean> ping ();

    @POST("/ISConnectionBrokerService/json/RegisterApplication")
    Call<RegisterApplication> registerApplicationCall(@Body SendRegisterApplication bodyRegisterApp);

    @POST("/ISConnectionBrokerService/json/GetURI")
    Call<RegisterApplication> getURICall(@Body SendGetURI sendGetURI);

    @POST("/ISConnectionBrokerService/json/UpdateDiagnosticInformation")
    Call<ErrorMessage> updateDiagnosticInfo (@Body InformationData informationData);

    @GET("/ISConnectionBrokerService/json/GetNews")
    Call<GetNews> getNews (@Query("ID") int id, @Query("ProductType") int productType);
}
