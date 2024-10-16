package com.example.libraapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomePage extends AppCompatActivity {


    HomeFragment homeFragment = new HomeFragment();
    ScanFragment scanFragment = new ScanFragment();
    InfoFragment infoFragment = new InfoFragment();

    private Spinner purpose;
    private TextView idView, nameView, courseYrView, departmentView;
    private DatabaseReference studentsRef;
    private String selectedPurpose, studentID;
    private List<String> purposeOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_scan) {
                    selectedFragment = new ScanFragment();
                } else if (item.getItemId() == R.id.nav_info) {
                    selectedFragment = new InfoFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
                }

                return true;
            }
        });

        // Set default fragment
        bottomNavigationView.setSelectedItemId(R.id.nav_home);


        idView = findViewById(R.id.idText);
        nameView = findViewById(R.id.nameText);
        courseYrView = findViewById(R.id.courseYrText);
        departmentView = findViewById(R.id.departmentText);

        studentID = getIntent().getStringExtra("StudentId");
        if (studentID == null || studentID.isEmpty()) {
            Toast.makeText(HomePage.this, "Student ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("Students").child(studentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    dataModel model = snapshot.getValue(dataModel.class);
                    if (model != null){
                        idView.setText(model.getId());
                        nameView.setText(model.getName());
                        courseYrView.setText(model.getCourseYr());
                        departmentView.setText(model.getDepartment());
                    }else {
                        Log.e("HomePage", "Model is null");
                    }
                }else {
                    Log.e("HomePage", "Snapshot doesn't exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        purpose = findViewById(R.id.spinner);

        purposeOption = new ArrayList<>();
        purposeOption.add("Purpose of Visit:");
        purposeOption.add("Study");
        purposeOption.add("Research");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, purposeOption);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        purpose.setAdapter(adapter);

        purpose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPurpose = parent.getItemAtPosition(position).toString();
                if (position == 0){
                    Toast.makeText(HomePage.this, "Please Select Purpose of Visit", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(HomePage.this, "Purpose of Visit: "+selectedPurpose, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(HomePage.this, "No purpose of visit selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle BottomNavigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.nav_home) {
                            Toast.makeText(HomePage.this, "Home Selected", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (item.getItemId() == R.id.nav_scan) {
                            initiateQRScan();  // Launch QR scanner
                            return true;
                        } else if (item.getItemId() == R.id.nav_info) {
                            Toast.makeText(HomePage.this, "Info Selected", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });

    }

    // Method to initiate the QR code scan
    private void initiateQRScan() {
        new IntentIntegrator(this).initiateScan();
    }

    // Handle the result of the QR scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            String qrValue = result.getContents();

            if (qrValue.equals("Valid_QR")) {
                submitLogToFirebase();  // Submit log if QR is valid
                Intent intent = new Intent(HomePage.this, Logs.class);  //usba ni e redirect sa lain layout
                startActivity(intent);
                finish();  // Close current activity
            } else {
                Toast.makeText(this, "Invalid QR Code!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No QR Code Found!", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to submit the log to Firebase
    private void submitLogToFirebase() {
        DatabaseReference logsRef = FirebaseDatabase.getInstance().getReference("logs");
        String logID = logsRef.push().getKey();

        // Create a log entry with user ID and timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        LogEntry logEntry = new LogEntry(studentID, timestamp);

        logsRef.child(logID).setValue(logEntry)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Log submitted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit log: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}