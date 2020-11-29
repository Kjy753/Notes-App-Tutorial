package com.study.notes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

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


        getNotes(REQUEST_CODE_SHOW_NOTES,false);
        /*getNotes 메소드는 onCreate 메소드에서 호출이 됩니다.
        * 즉, 응용 프로그램이 방금 시작 되었고 DB의 모든 메모를 표시해야 하며
        * 이것이 REQUEST_CODE_SHOW_NOTES 를 해당 메소드에 전달 해주어야
        * 전체 메모가 한번에 나옵니다.*/

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(noteList.size() != 0){
                    notesAdapter.searchNotes(editable.toString());
                }
            }
        });

        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        findViewById(R.id.imageAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            MainActivity .this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else  {
                    selectImage();
                }
            }
        });
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            } else{
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null,null,null);
        if(cursor == null){
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;

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
    private void getNotes(final int requestcode , final boolean isNoteDeleted){
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

                        if(isNoteDeleted){ /*메모를 삭제한 다음 메모 삭제 여부를 확인 해줍니다.*/
                            notesAdapter.notifyItemRemoved(noteClickedPosition);
                            /*노트가 삭제되면 어댑터에 제거 된 항목에 대하여 알려줍니다.*/
                        }else {
                            noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                            notesAdapter.notifyItemChanged(noteClickedPosition);
                            /*      삭제 되지 않은경우에는 업데이트 해야하므로
                                    제거된 동일한 위체에 새로 업데이트 된 노트를 추가하고,
                                    변경된 항목에 대해 어댑터에 알립니다.*/

                        }
                }
            }
        }
        new GetNoteTask().execute();

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        } else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if(data != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted", false));
                /*이는 데이터베이스에서 이미 사용 가능한 노트를 업데이트하고 있음을 의미하며,
                따라서 매개 변수가 NoteDeleted이므로 CreateNoteActivity에서 값을 전달합니다.
                노트가 삭제되었는지 여부에 관계없이 "isNoteDeleted"키가있는 인 텐트 데이터를 사용하지 않습니다.*/
            }
        }else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try{
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(),CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction",true);
                        intent.putExtra("quickActionType","image");
                        intent.putExtra("imagePath",selectedImagePath);
                        startActivityForResult(intent,REQUEST_CODE_ADD_NOTE);
                    }catch (Exception exception){
                        Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}