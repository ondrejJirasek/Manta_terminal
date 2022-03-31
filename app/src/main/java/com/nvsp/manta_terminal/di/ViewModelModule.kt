package com.nvsp.manta_terminal.di

import androidx.lifecycle.SavedStateHandle
import com.nvsp.manta_terminal.viewmodels.MainFragmentViewModel
import com.nvsp.manta_terminal.viewmodels.MainViewModel
import com.nvsp.manta_terminal.viewmodels.SplashViewModel


import com.nvsp.nvmesapplibrary.architecture.CommunicationViewModel
import com.nvsp.nvmesapplibrary.login.LoginActivityViewModel
import com.nvsp.nvmesapplibrary.login.LoginByIDViewModel
import com.nvsp.nvmesapplibrary.login.LoginByNameViewModel
import com.nvsp.nvmesapplibrary.queue.GenericQueueViewModel
import com.nvsp.nvmesapplibrary.queue.work_queue.WorkQueueViewModel
import com.nvsp.nvmesapplibrary.queue.work_queue.WorkQueueViewModel2
import com.nvsp.nvmesapplibrary.settings.SettingsActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel


import org.koin.dsl.module

val viewModelModule = module {

    viewModel { GenericQueueViewModel(get(), get()) }
    viewModel { WorkQueueViewModel(get(), get()) }
    viewModel { WorkQueueViewModel2(get(), get()) }
   // viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { LoginActivityViewModel(get(), get())}
    viewModel { SettingsActivityViewModel(get()) }
    viewModel { LoginByNameViewModel(get(),get()) }
    viewModel { LoginByIDViewModel(get(),get()) }
    viewModel { CommunicationViewModel(get(), get()) }

   // viewModel { MainMenuViewModel(get(),get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { MainFragmentViewModel(get(), get()) }
    //viewModel { AddTaskViewModel(get(), get()) }
fun provideSavedStateHandle():SavedStateHandle{
    return SavedStateHandle()
}
    factory {provideSavedStateHandle()  } // neposila singleton
}