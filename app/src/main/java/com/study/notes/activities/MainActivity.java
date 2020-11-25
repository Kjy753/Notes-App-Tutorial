package com.study.notes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.study.notes.R;
import com.study.notes.adapters.NotesAdapter;
import com.study.notes.database.NotesDatabase;
import com.study.notes.entities.Note;
import com.study.notes.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;   /*메모를 업데이트 하는데 사용 되는 REQUEST_CODE*/
    public static final int REQUEST_CODE_SHOW_NOTES = 3;    /*모든 코드를 보여줄떄 사용하는 코드*/

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

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
        notesAdapter = new NotesAdapter(noteList,this );
        notesRecyclerView.setAdapter(notesAdapter);


        getNotes(REQUEST_CODE_SHOW_NOTES);
        /*getNotes 메소드는 onCreate 메소드에서 호출이 됩니다.
        * 즉, 응용 프로그램이 방금 시작 되었고 DB의 모든 메모를 표시해야 하며
        * 이것이 REQUEST_CODE_SHOW_NOTES 를 해당 메소드에 전달 해주어야
        * 전체 메모가 한번에 나옵니다.*/
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    // 데이터 베이스에서 노트를 가져오는 데도 비동기 작업이 필요합니다.
    private void getNotes(final int requestcode){
            /*요청 코드를 getNotes 매개변수로 넣기.*/
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
                if(requestcode == REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    //전체 노트 보여주기
                    notesAdapter.notifyDataSetChanged();
                }else if(requestcode == REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    //노트 추가
                    notesAdapter.notifyItemInserted(0);
                    //데이터 베이스에 노트 추가
                    notesRecyclerView.smoothScrollToPosition(0);
                    // 노트 목록 최상단으로 옮기기
                }else if(requestcode == REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(noteClickedPosition);
                    // 현재 포지션 지우기
                    noteList.add(noteClickedPosition,notes.get(noteClickedPosition));
                    //노트 목록에 새로운 노트 포지션을 맨위로 추가
                    notesAdapter.notifyItemChanged(noteClickedPosition);
                    // 반영.
                }
            }
        }
        new GetNoteTask().execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE);
        } else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if(data != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE);
            }
        }
    }
}