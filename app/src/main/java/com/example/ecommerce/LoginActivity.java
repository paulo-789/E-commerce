package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Model.Users;
import Prevalent.Prevalent;
import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    private EditText InputPhoneNumber,InputPassword;
    private Button  LoginButton;
    private ProgressDialog loadingBar;
    private TextView AdminLink,NotAdminLink;

    private String parentDbName = "Users";
    private CheckBox ChkBoxRememberMe;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton= (Button) findViewById(R.id.login_btn);
        InputPhoneNumber = (EditText)findViewById(R.id.login_phone_number_input);
        InputPassword = (EditText)findViewById(R.id.login_password_input);
        AdminLink = (TextView) findViewById(R.id.admin_panel_link);
        NotAdminLink = (TextView)findViewById(R.id.not_admin_panel_link);

        loadingBar= new ProgressDialog(this);

        ChkBoxRememberMe = (CheckBox)findViewById(R.id.remember_me_Chkb);
        Paper.init(this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginButton.setText("Login Admin");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";
            }
        });
        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";
            }
        });

    }

    private void loginUser() {
        String phone = InputPhoneNumber.getText().toString();
        String passWord = InputPassword.getText().toString();

         if ( TextUtils.isEmpty(phone)){
            Toast.makeText(this,"please put your phone number",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(passWord)){
            Toast.makeText(this,"please write your password",Toast.LENGTH_SHORT).show();
        }
         else {
             loadingBar.setTitle("Login Account");
             loadingBar.setMessage("verifying your Details..");
             loadingBar.setCanceledOnTouchOutside(false);
             loadingBar.show();

             AllowAccessToAccount(phone ,passWord);
         }

         }

    private void AllowAccessToAccount(final String phone, final String passWord) {

       if (ChkBoxRememberMe.isChecked()){
           Paper.book().write(Prevalent.UserPhoneKey,phone);
           Paper.book().write(Prevalent.UserPasswordKey,passWord);
       }

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(parentDbName).child(phone).exists()){
                    Users usersData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);
                    if (usersData.getPhone().equals(phone)){
                        if (usersData.getPassword().equals(passWord)) {
                            if (parentDbName.equals("Admins")) {
                                Toast.makeText(LoginActivity.this, "You are now logged in admin", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                startActivity(intent);
                            } else if (parentDbName.equals("Users")) {
                                Toast.makeText(LoginActivity.this, "You are now logged in", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, homeActivity.class);
                                startActivity(intent);
                            }
                        }
                        else {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this,"incorrect password",Toast.LENGTH_SHORT).show();
                        }

                    }

                }
                else{
                    Toast.makeText(LoginActivity.this,"Invalid Credentials",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}