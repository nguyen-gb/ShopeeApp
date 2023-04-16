package com.example.shopee.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopee.R;
import com.example.shopee.retrofit.ApiShopee;
import com.example.shopee.retrofit.RetrofitClient;
import com.example.shopee.utils.Utils;

import io.paperdb.Paper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText input_username, input_password;
    TextView link_signin, btn_login, forgotpwd_link;
    ApiShopee apiShopee;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Mapping();
        ActionToolBar();
        initControll();
    }

    private void ActionToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initControll() {
        link_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signinIntent = new Intent(getApplicationContext(), SigninActivity.class);
                startActivity(signinIntent);
            }
        });

        forgotpwd_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forgotPassIntent = new Intent(getApplicationContext(), ForgotPassActivity.class);
                startActivity(forgotPassIntent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = input_username.getText().toString().trim();
                String pass = input_password.getText().toString().trim();

                if(TextUtils.isEmpty(username)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập tên đăng nhập", Toast.LENGTH_LONG).show();
                }
                else if(TextUtils.isEmpty(pass)) {
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập mật khẩu", Toast.LENGTH_LONG).show();
                }
                else {
                    //save data
                    Paper.book().write("username", username);
                    Paper.book().write("pass", pass);

                    Login(username, pass);
                }
            }
        });
    }

    private void Mapping() {
        Paper.init(this);
        apiShopee = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiShopee.class);

        toolbar = findViewById(R.id.tbar_login);
        link_signin = findViewById(R.id.link_signin);
        input_username = findViewById(R.id.input_username);
        input_password = findViewById(R.id.input_password);
        btn_login = findViewById(R.id.btn_login);
        forgotpwd_link = findViewById(R.id.forgotpwd_link);

        //read data
        if(Paper.book().read("username") != null && Paper.book().read("pass") != null) {
            input_username.setText(Paper.book().read("username"));
            input_password.setText(Paper.book().read("pass"));

            /*if(Paper.book().read("isLogin") != null) {
                boolean flag = Paper.book().read("isLogin");
                if (flag) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Login(Paper.book().read("username"), Paper.book().read("pass"));
                        }
                    }, 1000);
                }
            }*/
        }
    }

    private void Login(String username, String pass) {
        compositeDisposable.add(apiShopee.login(username, pass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            if(userModel.isSuccess()) {

                                isLogin = true;
                                Paper.book().write("isLogin", isLogin);
                                Utils.UserCurent = userModel.getResult().get(0);
                                //Luu thong tin
                                Paper.book().write("user", userModel.getResult().get(0));
                                Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intentMain);
                                finish();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Tài khoản hoặc mật khẩu không chính xác", Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.UserCurent.getEmail() != null && Utils.UserCurent.getUsername() != null && Utils.UserCurent.getPass() != null) {
            input_username.setText(Utils.UserCurent.getEmail());
            input_password.setText(Utils.UserCurent.getPass());
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}