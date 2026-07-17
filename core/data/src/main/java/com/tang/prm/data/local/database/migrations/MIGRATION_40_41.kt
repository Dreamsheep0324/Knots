package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v40 → v41：新增菜谱功能，创建 4 张表。
 * - recipes: 菜谱主表
 * - recipe_tags: 标签表
 * - recipe_contact_cross_ref: 菜谱↔人物关联表
 * - recipe_tag_cross_ref: 菜谱↔标签关联表
 */
val MIGRATION_40_41 = object : Migration(40, 41) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recipes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT,
                `cuisine` TEXT,
                `difficulty` TEXT NOT NULL,
                `cookingTime` INTEGER,
                `servings` INTEGER,
                `ingredients` TEXT NOT NULL DEFAULT '[]',
                `steps` TEXT NOT NULL DEFAULT '[]',
                `photos` TEXT NOT NULL DEFAULT '[]',
                `photos_count` INTEGER NOT NULL DEFAULT 0,
                `notes` TEXT,
                `rating` INTEGER NOT NULL DEFAULT 0,
                `isFavorite` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_createdAt` ON `recipes` (`createdAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipes_title` ON `recipes` (`title`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recipe_tags` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `color` TEXT,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recipe_contact_cross_ref` (
                `recipeId` INTEGER NOT NULL,
                `contactId` INTEGER NOT NULL,
                PRIMARY KEY(`recipeId`, `contactId`),
                FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_recipe_contact_cross_ref_contactId` ON `recipe_contact_cross_ref` (`contactId`)"
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recipe_tag_cross_ref` (
                `recipeId` INTEGER NOT NULL,
                `tagId` INTEGER NOT NULL,
                PRIMARY KEY(`recipeId`, `tagId`),
                FOREIGN KEY(`recipeId`) REFERENCES `recipes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`tagId`) REFERENCES `recipe_tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_recipe_tag_cross_ref_tagId` ON `recipe_tag_cross_ref` (`tagId`)"
        )
    }
}
