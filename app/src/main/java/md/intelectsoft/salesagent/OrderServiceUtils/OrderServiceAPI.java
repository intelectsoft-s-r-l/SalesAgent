package md.intelectsoft.salesagent.OrderServiceUtils;

import md.intelectsoft.salesagent.OrderServiceUtils.Results.AssortmentList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.AuthorizeUser;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientPriceLists;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.ClientPrices;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.GetPrintRequest;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.RequestList;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.SaveRequestResult;
import md.intelectsoft.salesagent.OrderServiceUtils.Results.assortmentDescription.AssortmentDescription;
import md.intelectsoft.salesagent.OrderServiceUtils.body.ClientPricesBody;
import md.intelectsoft.salesagent.OrderServiceUtils.body.saveRequest.SaveRequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrderServiceAPI {
    @GET("json/AuthorizeUser")
    Call<AuthorizeUser> authorizeUser(@Query("authUid") String authId, @Query("userName") String userName, @Query("userPassword") String userPass);

    @GET("json/GetRequestList")
    Call<RequestList> getRequestList(@Query("tokenUid") String token, @Query("startDate") String startDate, @Query("endDate") String endDate , @Query("clientUid") String clientUid);

    @GET("json/GetAssortimentList")
    Call<AssortmentList> getAssortmentList(@Query("tokenUid") String token);

    @GET("json/GetContragentsList")
    Call<ClientList> getClients(@Query("tokenUid") String token);

    @GET("json/GetClientPriceLists")   //get price lists
    Call<ClientPriceLists> getClientPriceLists (@Query("tokenUid") String token);
    /**
     * Get client discount for products,
     * you need send priceListUid product
     * @param listPriceBody {@link ClientPricesBody}
     * and receive assortment uid and price
     * @return {@link ClientPrices}
     */
    @POST("json/GetClientPrices")
    Call<ClientPrices> getClientPriceDiscount (@Body ClientPricesBody listPriceBody);

    @GET("json/GetContragent")
    Call<ClientList> getClientInfo (@Query("tokenUid") String tokenUid, @Query("clientUid") String clientUid);

    @GET("json/DeleteRequest")
    Call<RequestList> deleteRequest (@Query("tokenUid") String token, @Query("invoiceUid") String invoiceId);

    @GET("json/GetAssortimentDescription")
    Call<AssortmentDescription> getAssortmentDescription (@Query("tokenUid") String token, @Query("assortimentUid") String assortmentUid);

    @POST("json/SaveRequest")
    Call<SaveRequestResult> saveRequest(@Body SaveRequestBody orderBody, @Query("tokenUid") String token);

    @GET("json/GetPrintRequest")
    Call<GetPrintRequest> getPrintRequest (@Query("tokenUid") String token, @Query("requestUid") String requestUid);
}
