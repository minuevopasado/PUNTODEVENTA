package com.inventarioapp.di

import android.content.Context
import com.inventarioapp.data.database.AppDatabase
import com.inventarioapp.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }
    
    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideTransactionItemDao(database: AppDatabase): TransactionItemDao {
        return database.transactionItemDao()
    }
    
    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }
    
    @Provides
    fun provideAppSettingsDao(database: AppDatabase): AppSettingsDao {
        return database.appSettingsDao()
    }
    
    @Provides
    fun provideProductImageDao(database: AppDatabase): ProductImageDao {
        return database.productImageDao()
    }
    
    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }
}