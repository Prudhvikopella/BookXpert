package com.bookxpert.app.ui.interfaces

import com.bookxpert.app.ui.model.AccountDetails
import com.pxquiz.app.constants.ApiConstants
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {

    @GET(ApiConstants.GET_ALL_ACCOUNTS)
    suspend fun hitGetAllAccounts(): Response<String>


}