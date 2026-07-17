package com.tang.prm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "recipes",
    indices = [Index("createdAt"), Index("title")]
)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val cuisine: String?,
    val difficulty: String,
    val cookingTime: Int?,
    val servings: Int?,
    val ingredients: String,
    val steps: String,
    val photos: List<String> = emptyList(),
    @ColumnInfo(name = "photos_count", defaultValue = "0")
    val photosCount: Int = 0,
    val notes: String?,
    val rating: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recipe_tags")
data class RecipeTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recipe_contact_cross_ref",
    primaryKeys = ["recipeId", "contactId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId")]
)
data class RecipeContactCrossRef(
    val recipeId: Long,
    val contactId: Long
)

@Entity(
    tableName = "recipe_tag_cross_ref",
    primaryKeys = ["recipeId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecipeTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagId")]
)
data class RecipeTagCrossRef(
    val recipeId: Long,
    val tagId: Long
)

data class RecipeWithContactsAndTags(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeContactCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "contactId"
        )
    )
    val contacts: List<ContactEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeTagCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "tagId"
        )
    )
    val tags: List<RecipeTagEntity>
)
