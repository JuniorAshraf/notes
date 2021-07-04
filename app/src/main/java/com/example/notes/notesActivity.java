package com.example.notes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class notesActivity extends AppCompatActivity {
    FloatingActionButton mcreatenotefab;

    private FirebaseAuth firebaseauth;

    RecyclerView mrecyclerview;
    StaggeredGridLayoutManager mstaggerdgridlayoutmanager;

    FirebaseUser firebaseuser;
    FirebaseFirestore firebasefirestore;
    FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder> noteadpter;

    AlertDialog.Builder builder;

    GridLayoutManager mgridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mcreatenotefab = findViewById(R.id.createnotefab);
        firebaseauth = FirebaseAuth.getInstance();


        firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        firebasefirestore = FirebaseFirestore.getInstance();

        builder = new AlertDialog.Builder(this);



        getSupportActionBar().setTitle("Your Notes");

        mcreatenotefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(notesActivity.this, createnote.class));
            }
        });


        Query query = firebasefirestore.collection("notes").document(firebaseuser.getUid()).collection("myNotes").orderBy("title",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<firebasemodel> allusernotes = new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();

        noteadpter = new FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder>(allusernotes){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteviewholder, int i, @NonNull firebasemodel  firebasemodel) {


                ImageView popupbutton = noteviewholder.itemView.findViewById(R.id.menupopupbutton);


                int colourcode = getRandomColor();
                noteviewholder.mnote.setBackgroundColor(noteviewholder.itemView.getResources().getColor(colourcode,null));


                noteviewholder.notetitles.setText(firebasemodel.getTitle());
                noteviewholder.notecontents.setText(firebasemodel.getContent());

                String docId = noteadpter.getSnapshots().getSnapshot(i).getId();

                noteviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //we have to open note detail activity

                        Intent intent = new Intent(v.getContext(),notedetails.class);

                        intent.putExtra("title", firebasemodel.getTitle());
                        intent.putExtra("content", firebasemodel.getContent());
                        intent.putExtra("noteId", docId);

                        v.getContext().startActivity(intent);


                       // Toast.makeText(getApplicationContext(), "this is clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                popupbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(),v);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                Intent intent = new Intent(v.getContext(),editnoteactivity.class);
                                intent.putExtra("title", firebasemodel.getTitle());
                                intent.putExtra("content", firebasemodel.getContent());
                                intent.putExtra("noteId", docId);

                                v.getContext().startActivity(intent);
                                return false;
                            }
                        });

                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                builder.setMessage("Do you want to delete?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                DocumentReference documentReference = firebasefirestore.collection("notes").document(firebaseuser.getUid()).collection("myNotes").document(docId);
                                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(v.getContext(), "This note is deleted", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                                //return false;

                                                //finish();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.setTitle("Delete");
                                alertDialog.show();

                               // Toast.makeText(v.getContext(), "This note is deleted", Toast.LENGTH_SHORT).show();
                               // DocumentReference documentReference = firebasefirestore.collection("notes").document(firebaseuser.getUid()).collection("myNotes").document(docId);
                                //documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                  //  @Override
                                    //public void onSuccess(Void unused) {
                                      //  Toast.makeText(v.getContext(), "This note is deleted", Toast.LENGTH_SHORT).show();
                                    //}
                                //}).addOnFailureListener(new OnFailureListener() {
                                  //  @Override
                                    //public void onFailure(@NonNull Exception e) {
                                      //  Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                                    //}
                                //});

                                return false;
                            }
                        });

                        popupMenu.show();
                    }
                });


            }
            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.noteslayout,parent,false);
                GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
                lp.height = parent.getMeasuredHeight() / 4;
                view.setLayoutParams(lp);
                return  new NoteViewHolder(view);
            }
        };

        mrecyclerview = findViewById(R.id.recyclerview);
        mrecyclerview.setHasFixedSize(true);
        //mstaggerdgridlayoutmanager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        mgridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        mrecyclerview.setLayoutManager(mgridLayoutManager);

       // mrecyclerview.setLayoutManager(mstaggerdgridlayoutmanager);
        mrecyclerview.setAdapter(noteadpter);

    }


    public class NoteViewHolder extends RecyclerView.ViewHolder{
        private TextView notetitles;
        private TextView notecontents;
        LinearLayout mnote;

        public  NoteViewHolder(@NonNull View itemView){
            super(itemView);
            notetitles = itemView.findViewById(R.id.notetitle);
            notecontents = itemView.findViewById(R.id.notecontent);
            mnote = itemView.findViewById(R.id.note);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                firebaseauth.signOut();
                finish();
                startActivity(new Intent(notesActivity.this,MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteadpter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteadpter!=null){
            noteadpter.stopListening();
        }
    }

     private int getRandomColor()
    {

        List<Integer> colorcode = new ArrayList<>();
        colorcode.add(R.color.gray);
        colorcode.add(R.color.pink);
        colorcode.add(R.color.lightgreen);
        colorcode.add(R.color.skyblue);
        colorcode.add(R.color.green);
        colorcode.add(R.color.color1);
        colorcode.add(R.color.color2);
        colorcode.add(R.color.color3);
        colorcode.add(R.color.color4);
        colorcode.add(R.color.color5);

        Random random = new Random();
        int number = random.nextInt(colorcode.size());
        return colorcode.get(number);

    }

}