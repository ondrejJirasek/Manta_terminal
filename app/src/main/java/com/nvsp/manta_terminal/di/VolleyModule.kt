package com.nvsp.manta_terminal.di


import com.nvsp.nvmesapplibrary.communication.volley.VolleySingleton
import org.koin.dsl.module

val volleyModule = module(override = true) {
    fun provideVolleySingleton()=VolleySingleton.getInstance()
    single {  provideVolleySingleton()}
}
