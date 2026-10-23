package com.centralchain.app.network;

import com.centralchain.app.models.CreateOrderRequest;
import com.centralchain.app.models.Delivery;
import com.centralchain.app.models.InventoryItem;
import com.centralchain.app.models.InventoryRequest;
import com.centralchain.app.models.LoginRequest;
import com.centralchain.app.models.LoginResponse;
import com.centralchain.app.models.Message;
import com.centralchain.app.models.ProofRequest;
import com.centralchain.app.models.PurchaseOrder;
import com.centralchain.app.models.RegisterRequest;
import com.centralchain.app.models.SendMessageRequest;
import com.centralchain.app.models.UpdateOrderRequest;
import com.centralchain.app.models.User;
import com.centralchain.app.models.UserRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

// spring boot api endpoints
public interface ApiService {

    // auth
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<Void> register(@Body RegisterRequest request);

    // users
    @GET("api/users")
    Call<List<User>> getUsers();

    @GET("api/users/email/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    @GET("api/users/{id}")
    Call<User> getUser(@Path("id") long id);

    // admin only
    @POST("api/users")
    Call<User> addUser(@Body UserRequest request);

    @PUT("api/users/{id}")
    Call<User> updateUser(@Path("id") long id, @Body UserRequest request);

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") long id);

    // inventory
    @GET("api/inventory")
    Call<List<InventoryItem>> getInventory();

    @GET("api/inventory/low-stock")
    Call<List<InventoryItem>> getLowStock();

    @GET("api/inventory/{id}")
    Call<InventoryItem> getInventoryItem(@Path("id") long id);

    @POST("api/inventory")
    Call<InventoryItem> addInventoryItem(@Body InventoryRequest request);

    @PUT("api/inventory/{id}")
    Call<InventoryItem> updateInventoryItem(@Path("id") long id, @Body InventoryRequest request);

    @PATCH("api/inventory/{id}/adjust")
    Call<InventoryItem> adjustStock(@Path("id") long id, @Query("delta") int amount);

    @DELETE("api/inventory/{id}")
    Call<Void> deleteInventoryItem(@Path("id") long id);

    // purchase orders
    @GET("api/purchase-orders")
    Call<List<PurchaseOrder>> getOrders();

    @GET("api/purchase-orders/{id}")
    Call<PurchaseOrder> getOrder(@Path("id") long id);

    @POST("api/purchase-orders")
    Call<PurchaseOrder> createOrder(@Body CreateOrderRequest request);

    @PUT("api/purchase-orders/{id}")
    Call<PurchaseOrder> updateOrder(@Path("id") long id, @Body UpdateOrderRequest request);

    @DELETE("api/purchase-orders/{id}")
    Call<Void> deleteOrder(@Path("id") long id);

    @PATCH("api/purchase-orders/{id}/approve")
    Call<PurchaseOrder> approveOrder(@Path("id") long id);

    @PATCH("api/purchase-orders/{id}/dispatch")
    Call<PurchaseOrder> dispatchOrder(@Path("id") long id, @Query("driverId") long driverId);

    @PATCH("api/purchase-orders/{id}/cancel")
    Call<PurchaseOrder> cancelOrder(@Path("id") long id);

    // deliveries
    @GET("api/deliveries")
    Call<List<Delivery>> getDeliveries();

    @GET("api/deliveries/{id}")
    Call<Delivery> getDelivery(@Path("id") long id);

    @GET("api/deliveries/po/{poId}")
    Call<Delivery> getDeliveryForOrder(@Path("poId") long poId);

    @GET("api/deliveries/driver/{driverId}")
    Call<List<Delivery>> getDriverDeliveries(@Path("driverId") long driverId);

    @PATCH("api/deliveries/{id}/in-transit")
    Call<Delivery> markInTransit(@Path("id") long id);

    @PATCH("api/deliveries/{id}/delivered")
    Call<Delivery> markDelivered(@Path("id") long id);

    @PATCH("api/deliveries/{id}/failed")
    Call<Delivery> markFailed(@Path("id") long id);

    // only works after the delivery is delivered
    @PATCH("api/deliveries/{id}/proof")
    Call<Delivery> addProof(@Path("id") long id, @Body ProofRequest request);

    // messages
    @GET("api/messages/po/{poId}")
    Call<List<Message>> getMessages(@Path("poId") long poId);

    @POST("api/messages")
    Call<Message> sendMessage(@Body SendMessageRequest request);
}
