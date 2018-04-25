package ru.whitejoker.testjson;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.whitejoker.testjson.Constant.*;


public class LoginActivity extends AppCompatActivity {

    public static String LOG_TAG = "my_log";

    private UserLoginTask mAuthTask = null;

    // Пользовательский интерфейс
    private EditText mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Настройка формы для входа
        mLoginView = (EditText) findViewById(R.id.login);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Сброс ошибок
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Сохраняем учетные данныые в переменные
        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // проверк пароля на валидность
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Проверка логина на валидность
        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_invalid_email));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // В случае ошибки возвращаем курсор на поле ввода
            focusView.requestFocus();
        } else {
            // Показ инимации и выполнение проверки авторизации
            showProgress(true);
            mAuthTask = new UserLoginTask(login, password);
            Log.d(LOG_TAG, login + password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isLoginValid(String email) {
        //на случай изменения процесса
        return true;//email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    //Показываем выполнение
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private String mLogin;
        private String mPassword;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        UserLoginTask(String login, String password) {
            mLogin = login;
            mPassword = password;
            Log.d(LOG_TAG, password);
        }

        //Соединение с сервером и проверка в отдельном потоке
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Видимость соединения с сервером.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            try {
                URL url = new URL(Constant.IP_ADR + "REST/1.0/ticket/1?user=" + mLogin + "&pass=" + mPassword);
                Log.d(LOG_TAG, url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                if ((line = reader.readLine()) != null) { //читаем первую строку вернувшейся страницы
                    Log.d(LOG_TAG, "вывод" + line);
                }
                reader.close();
                return line.equals("RT/4.4.1 200 Ok");//если строка с полоджительным ответом идем дальше


            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            Log.d(LOG_TAG, "пароль верен? " + success);
            //Если авторизация прошла успешно запускаем активити с заявками пользователя и передаем туда логин и пароль
            if (success) {
                Intent intent = new Intent(LoginActivity.this, UserTicketsActivity.class);
                Log.d(LOG_TAG, mPassword + "млогин и мпароль " + mLogin);
                intent.putExtra("mLogin", mLogin);
                intent.putExtra("mPassword", mPassword);
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));//при неудачной авторизации выводим сообщение
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

