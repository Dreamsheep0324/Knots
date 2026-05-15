package com.tang.prm.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AnniversaryWithContact(
    @Embedded val anniversary: AnniversaryEntity,
    @Relation(
        parentColumn = "contactId",
        entityColumn = "id"
    )
    val contact: ContactEntity?
)
