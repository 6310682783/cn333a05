package com.example.phonebook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phonebook.domain.model.NoteModel
import com.example.phonebook.util.fromHex

@ExperimentalMaterialApi
@Composable
fun Note(
    modifier: Modifier = Modifier,
    note: NoteModel,
    onNoteClick: (NoteModel) -> Unit = {},
    onNoteCheckedChange: (NoteModel) -> Unit = {},
    isSelected: Boolean
) {
    val background = if (isSelected)
        Color.LightGray
    else
        MaterialTheme.colors.surface

    Card(
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        backgroundColor = background
    ) {
        ListItem(
            text = { Text(text = note.firstname + " "+ note.lastname, maxLines = 1)


                   },
            secondaryText = {
                Text(text = note.phone, maxLines = 1)
            },

            modifier = Modifier.clickable {
                onNoteClick.invoke(note)
            }
        )
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun NotePreview() {
    Note(note = NoteModel(1, "0123456798","f","l","Note 1", "Content 1", false), isSelected = true)
}