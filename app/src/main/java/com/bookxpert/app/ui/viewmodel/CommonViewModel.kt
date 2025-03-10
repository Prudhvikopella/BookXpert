package com.bookxpert.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bookxpert.app.ui.model.AccountDetails
import com.bookxpert.app.ui.repo.CommonRepo
import com.bookxpert.app.utils.Account
import com.bookxpert.app.utils.AppDatabase
import com.bookxpert.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CommonViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val commonRepo: CommonRepo by inject()
    private val db = AppDatabase.getDatabase(application)  // ✅ Initialize DB once
    private val accountDao = db.accountDao()

    fun getAllAccounts() {
        commonRepo.hitGetAllAccounts()
    }

    fun allAccountsResponse(): MutableLiveData<Resource<List<AccountDetails>>> {
        return commonRepo.accountsResponse
    }

    fun insertAllAccounts(accounts: List<Account>, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            accountDao.insertAll(accounts)  // ✅ Insert accounts
            withContext(Dispatchers.Main) { onComplete() }  // ✅ Notify completion
        }
    }

    fun deleteAccountById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            accountDao.deleteById(id)
        }
    }

    fun updateAccountDetails(id: Int, altName: String, profile: String) {
        viewModelScope.launch(Dispatchers.IO) {
            accountDao.updateAltNameAndProfile(id, altName, profile)
        }
    }

    fun getAllAccountsFromDb(): LiveData<List<Account>> {
        val accountsLiveData = MutableLiveData<List<Account>>()
        viewModelScope.launch(Dispatchers.IO) {
            accountsLiveData.postValue(accountDao.getAllAccounts())
        }
        return accountsLiveData
    }

}

