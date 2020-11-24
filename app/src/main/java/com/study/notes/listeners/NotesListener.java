package com.study.notes.listeners;

import com.study.notes.entities.Note;

public interface NotesListener {
    void onNoteClicker(Note note, int position);

}
