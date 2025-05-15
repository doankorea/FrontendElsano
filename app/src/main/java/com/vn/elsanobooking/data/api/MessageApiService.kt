package com.vn.elsanobooking.data.api

import com.vn.elsanobooking.data.models.MessageViewModel
import com.vn.elsanobooking.data.models.MessagesUsersListViewModel
import com.vn.elsanobooking.data.models.SignalRChatMessage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessageApiService {
    @GET("api/mobile/messages/conversations/{userId}")
    suspend fun getConversations(@Path("userId") userId: Int): List<MessagesUsersListViewModel>
    
    @GET("api/mobile/messages/{userId}/{contactId}")
    suspend fun getMessages(
        @Path("userId") userId: Int, 
        @Path("contactId") contactId: Int
    ): MessageViewModel
    
    @POST("api/mobile/messages/send")
    suspend fun sendMessage(@Body message: SignalRChatMessage): Response<Unit>
} 