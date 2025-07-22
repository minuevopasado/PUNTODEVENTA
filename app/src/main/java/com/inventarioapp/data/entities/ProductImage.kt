package com.inventarioapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_images",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class ProductImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val imagePath: String,
    val imageName: String,
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)