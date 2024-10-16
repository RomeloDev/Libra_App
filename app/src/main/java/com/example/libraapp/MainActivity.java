package com.example.libraapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Button signInBtn;
    private EditText idField;
    private DatabaseReference studentsRef;
    private String getId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        idField = findViewById(R.id.studentId);
        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        signInBtn = findViewById(R.id.signin);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getId = idField.getText().toString();

                if (TextUtils.isEmpty(getId)){
                    Toast.makeText(MainActivity.this, "Please enter your Student ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                verifyStudentId(getId);
            }
        });
    }

    private void verifyStudentId(String studentId) {
        studentsRef.orderByChild("id").equalTo(studentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Student ID found, proceed to the next screen
                            for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                                String name = studentSnapshot.child("name").getValue(String.class);
                                Toast.makeText(MainActivity.this, "Welcome " + name, Toast.LENGTH_SHORT).show();
                            }

                            // Redirect to another activity (e.g., Dashboard)
                            Intent intent = new Intent(MainActivity.this, HomePage.class);
                            intent.putExtra("StudentId", getId);
                            startActivity(intent);
                            finish();  // Close the login activity
                        } else {
                            // Student ID not found
                            Toast.makeText(MainActivity.this, "Invalid student ID", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("LoginActivity", "Database error: " + databaseError.getMessage());
                    }
                });
    }
}