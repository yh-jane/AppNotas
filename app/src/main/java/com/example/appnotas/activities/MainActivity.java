package com.example.appnotas.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.appnotas.R;
import com.example.appnotas.adapters.NotesAdapter;
import com.example.appnotas.database.NotesDatabase;
import com.example.appnotas.entities.Note;
import com.example.appnotas.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {
        public static final int REQUEST_CODE_ADD_NOTE = 1;
        public static final int REQUEST_CODE_UPDATE_NOTE = 2;
        public static final int REQUEST_CODE_SHOW_NOTES = 3;
        private RecyclerView notesRecyclerView;
        private List<Note> noteList;
        private NotesAdapter notesAdapter;

        private int noteClickedPosition = -1;
        private EditText inputSearch; // Mova a declaração do EditText aqui

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        getNotes(REQUEST_CODE_UPDATE_NOTE, false); // Chama o método para atualizar as notas
                    }
                });

        imageAddNoteMain.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            launcher.launch(intent);
        });

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        inputSearch = findViewById(R.id.inputSearch); // Mova a inicialização do EditText aqui
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Este método é chamado antes de o texto ser alterado.

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Este método é chamado quando o texto está sendo alterado.
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Este método é chamado após o texto ter sido alterado.
                if (noteList.size() != 0){
                    notesAdapter.searchNotes(s.toString());
                }

            }
        });
    }
    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        updateNoteLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> updateNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    getNotes(REQUEST_CODE_UPDATE_NOTE,false); // Chama o método para atualizar as notas
                }
            }
    );

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.clear(); // Limpa a lista de notas existente
                    noteList.addAll(notes); // Adiciona as novas notas à lista
                    notesAdapter.notifyDataSetChanged(); // Notifica o adaptador sobre as mudanças
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0)); // Adiciona a nova nota no início da lista
                    notesAdapter.notifyItemInserted(0); // Notifica o adaptador sobre a inserção do item
                    notesRecyclerView.smoothScrollToPosition(0); // Rolagem suave para a nova nota
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    if (noteClickedPosition >= 0 && noteClickedPosition < noteList.size()) {
                        noteList.set(noteClickedPosition, notes.get(0)); // Substitui a nota modificada na lista
                        notesAdapter.notifyItemChanged(noteClickedPosition); // Notifica o adaptador sobre a mudança no item
                    }
                }
            }
        }

        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false); // Chama o método para atualizar as notas
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }
}
