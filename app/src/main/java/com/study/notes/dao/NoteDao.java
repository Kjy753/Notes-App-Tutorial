package com.study.notes.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.study.notes.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();      /*List<??> =
                                     "note"라고하는 2 개 이상의 항목을 볼 수 있습니다.
                                    "entities"패키지 안에 만든 항목을 선택했는지 확인하고
                                    선택할 때마다 기억하세요.*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserNote(Note note);

    @Delete
    void deleteNote(Note note);

}
