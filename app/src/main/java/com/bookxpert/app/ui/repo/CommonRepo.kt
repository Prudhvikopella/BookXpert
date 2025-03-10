package com.bookxpert.app.ui.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bookxpert.app.network.RetrofitUtil
import com.bookxpert.app.ui.model.AccountDetails
import com.bookxpert.app.utils.Coroutines
import com.bookxpert.app.utils.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CommonRepo {

    val accountsResponse: MutableLiveData<Resource<List<AccountDetails>>> = MutableLiveData()

        fun hitGetAllAccounts(){
            Coroutines.main {
                try {
                    accountsResponse.postValue(Resource.loading(null))
                    val response = RetrofitUtil.createBaseApiService().hitGetAllAccounts()
                    if (response.isSuccessful) {
                        Log.d("CHECKINGDATARECIEVED", "raw result = ${response.body()}")

                        val jsonString = response.body() // Get raw string
                        val gson = Gson()
                        val accountList: List<AccountDetails> = gson.fromJson(jsonString, object : TypeToken<List<AccountDetails>>() {}.type)
                        Log.d("CHECKINGDATARECIEVED", "Parsed result = $accountList")
                        accountsResponse.postValue(Resource.success(accountList))
                    } else {
                        Log.e("CHECKINGDATARECIEVED", "API Error: ${response.code()} - ${response.message()}")
                        accountsResponse.postValue(Resource.error(Exception("API Error: ${response.code()} - ${response.message()}"), null))
                    }
                }catch (ex : Exception){
                    Log.e("CHECKINGDATARECIEVED","error = ${ex}")
                    accountsResponse.postValue(Resource.error(ex, null))
                }

            }
        }
}