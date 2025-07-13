package com.inventarioapp.data.database

import androidx.room.TypeConverter
import com.inventarioapp.data.entities.*

class Converters {
    
    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }
    
    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }
    
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory {
        return ExpenseCategory.valueOf(value)
    }
    
    @TypeConverter
    fun fromDocumentType(value: DocumentType): String {
        return value.name
    }
    
    @TypeConverter
    fun toDocumentType(value: String): DocumentType {
        return DocumentType.valueOf(value)
    }
}