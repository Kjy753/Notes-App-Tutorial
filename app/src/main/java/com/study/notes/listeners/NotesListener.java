package com.study.notes.listeners;

import com.study.notes.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);

}
