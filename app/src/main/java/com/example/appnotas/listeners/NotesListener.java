package com.example.appnotas.listeners;

import com.example.appnotas.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
