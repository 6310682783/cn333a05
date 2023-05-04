package com.example.phonebook.domain.model

import android.nfc.Tag
import com.example.phonebook.database.ColorDbModel
import com.example.phonebook.database.TagDbModel

data class TagModel(
    val id: Long,
    val name: String,
    val hex: String
) {
    companion object {
        val DEFAULT = with(TagDbModel.DEFAULT_TAG) { TagModel(id, name, hex) }
    }
}
