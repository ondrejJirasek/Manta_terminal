package com.nvsp.manta_terminal.di



import com.nvsp.nvmesapplibrary.database.LibDao
import com.nvsp.nvmesapplibrary.database.LibRepository
import org.koin.dsl.module

val repositoryModule = module {
    fun provideLocalRepository(dao: LibDao):LibRepository{
        return LibRepository(dao)
    }

    single {
        provideLocalRepository(get())
    }

}