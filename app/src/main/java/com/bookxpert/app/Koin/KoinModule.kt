package com.pxquiz.app.ui.Koin

import android.app.Application
import com.bookxpert.app.ui.repo.CommonRepo
import com.bookxpert.app.ui.viewmodel.CommonViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repoModule = module{
    factory { CommonRepo() }
}

val viewModelModule = module {
    viewModel { (application: Application) -> CommonViewModel(application) }
}