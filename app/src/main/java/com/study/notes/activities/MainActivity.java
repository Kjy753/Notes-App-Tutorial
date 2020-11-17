package com.study.notes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.study.notes.R;
import com.study.notes.adapters.NotesAdapter;
import com.study.notes.database.NotesDatabase;
import com.study.notes.entities.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE = 1;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                        /*createNoteActivity 에서 노트를 새로 작성해 추가한 후
                         onActivityResult 메서드에서 결과를 처리하여 노트 목록을 업데이트 해야합니다.*/
                );
            }
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList);
        notesRecyclerView.setAdapter(notesAdapter);


        getNotes();
    }

    // 데이터 베이스에서 노트를 가져오는 데도 비동기 작업이 필요합니다.
    private void getNotes(){

        @SuppressLint("StaticFieldLeak")
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void... voids) {

                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if(noteList.size() == 0){
                    /* 화면의 노트 목록이 비어있다면*/
                    noteList.addAll(notes);
                    /*notes 의 모든 목록을 noteList에 모두 추가합니다*/
                    notesAdapter.notifyDataSetChanged();
                    /*notesAdapter DataSet 이 변함을 알려줍니다.*/
                } else {
                    /*noteList 가 비어있지 않다면 데이터 베이스에서 노트가 이미 로드 되어있음을 의미합니다.*/
                    noteList.add(0,notes.get(0));
                    /*noteList 에 최신 목록만 추가*/
                    notesAdapter.notifyItemInserted(0);
                    /*어댑터에 새 노트가 삽입되었음을 알림*/
                }
                notesRecyclerView.smoothScrollToPosition(0);
                /*RecyclerVIew 의 스크롤 위치를 맨 위로 스크롤*/
            }
        }
        new GetNoteTask().execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes();
        }
    }
}