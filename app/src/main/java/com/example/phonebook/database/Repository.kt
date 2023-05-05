package com.example.phonebook.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.phonebook.domain.model.ColorModel
import com.example.phonebook.domain.model.NoteModel
import com.example.phonebook.domain.model.TagModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Repository(
    private val noteInterface: NoteInterface,
    private val colorInterface: ColorInterface,
    private val tagInterface: TagInterface,
    private val dbMapper: DbMapper
) {

    // Working Notes
    private val notesNotInTrashLiveData: MutableLiveData<List<NoteModel>> by lazy {
        MutableLiveData<List<NoteModel>>()
    }

    fun getAllNotesNotInTrash(): LiveData<List<NoteModel>> = notesNotInTrashLiveData

    // Deleted Notes
    private val notesInTrashLiveData: MutableLiveData<List<NoteModel>> by lazy {
        MutableLiveData<List<NoteModel>>()
    }

    fun getAllNotesInTrash(): LiveData<List<NoteModel>> = notesInTrashLiveData

    init {
        initDatabase(this::updateNotesLiveData)
    }

    /**
     * Populates database with colors if it is empty.
     */
    private fun initDatabase(postInitAction: () -> Unit) {
        GlobalScope.launch {
            // Prepopulate colors
            val colors = ColorDbModel.DEFAULT_COLORS.toTypedArray()
            val dbColors = colorInterface.getAllSync()
            if (dbColors.isNullOrEmpty()) {
                colorInterface.insertAll(*colors)
            }

            // Prepopulate notes
            val notes = NoteDbModel.DEFAULT_NOTES.toTypedArray()
            val dbNotes = noteInterface.getAllSync()
            if (dbNotes.isNullOrEmpty()) {
                noteInterface.insertAll(*notes)
            }

            //tag
            val tags = TagDbModel.DEFAULT_TAGS.toTypedArray()
            val dbTags = tagInterface.getAllSync()
            if (dbTags.isNullOrEmpty()) {
                tagInterface.insertAll(*tags)
            }

            postInitAction.invoke()
        }
    }

    // get list of working notes or deleted notes
    private fun getAllNotesDependingOnTrashStateSync(inTrash: Boolean): List<NoteModel> {
        val colorDbModels: Map<Long, ColorDbModel> = colorInterface.getAllSync().map { it.id to it }.toMap()
        val tagDbModels: Map<Long, TagDbModel> = tagInterface.getAllSync().map { it.id to it }.toMap()
        //val tagDbModels: Map<Long, TagDbModel> = tagInterface.getAllSync().map { it.id to it }.toMap()
        val dbNotes: List<NoteDbModel> =
            noteInterface.getAllSync().filter { it.isInTrash == inTrash }
        return dbMapper.mapNotes(dbNotes, colorDbModels, tagDbModels)
    }

    fun insertNote(note: NoteModel) {
        noteInterface.insert(dbMapper.mapDbNote(note))
        updateNotesLiveData()
    }

    fun deleteNotes(noteIds: List<Long>) {
        noteInterface.delete(noteIds)
        updateNotesLiveData()
    }

    fun moveNoteToTrash(noteId: Long) {
        val dbNote = noteInterface.findByIdSync(noteId)
        val newDbNote = dbNote.copy(isInTrash = true)
        noteInterface.insert(newDbNote)
        updateNotesLiveData()
    }

    fun restoreNotesFromTrash(noteIds: List<Long>) {
        val dbNotesInTrash = noteInterface.getNotesByIdsSync(noteIds)
        dbNotesInTrash.forEach {
            val newDbNote = it.copy(isInTrash = false)
            noteInterface.insert(newDbNote)
        }
        updateNotesLiveData()
    }

    fun getAllColors(): LiveData<List<ColorModel>> =
        Transformations.map(colorInterface.getAll()) { dbMapper.mapColors(it) }

    fun getAllTags(): LiveData<List<TagModel>> =
        Transformations.map(tagInterface.getAll()) { dbMapper.mapTags(it) }

    private fun updateNotesLiveData() {
        notesNotInTrashLiveData.postValue(getAllNotesDependingOnTrashStateSync(false))
        notesInTrashLiveData.postValue(getAllNotesDependingOnTrashStateSync(true))
    }
}