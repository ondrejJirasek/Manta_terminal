package com.nvsp.manta_terminal.di


import com.nvsp.manta_terminal.BaseApp
import com.nvsp.manta_terminal.Const
import com.nvsp.nvmesapplibrary.database.LibDatabase

import org.koin.dsl.module

val databaseModule = module {
    fun provideDatabase():LibDatabase = LibDatabase.getDatabase(BaseApp.appContext, Const.dbName)
    single {  provideDatabase()} //vola Singleton jen jednu instanci
}