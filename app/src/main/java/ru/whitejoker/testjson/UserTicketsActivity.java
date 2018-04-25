package ru.whitejoker.testjson;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import static ru.whitejoker.testjson.Constant.*;

public class UserTicketsActivity extends AppCompatActivity {

    public static String LOG_TAG = "my_log";
    ListView userTicketsListView;
    private ArrayList<HashMap> userTicketsArrayList;//список заявок пользователя
    Button btnCallNobodyTickets;//кнопка перехода к нераспределенным заявкам
    Button btnExit;//Выйти
    String mLogin;
    String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogin = getIntent().getStringExtra("mLogin");//достаем логин
        mPassword = getIntent().getStringExtra("mPassword");//и пароль
        Log.d(LOG_TAG,"mLogin : " + mLogin);
        Log.d(LOG_TAG,"mLogin : " + getIntent().getStringExtra("mLogin"));

        setContentView(R.layout.activity_user_tickets);
        userTicketsListView = (ListView)findViewById(R.id.UserTicketsListView);

        View header = getLayoutInflater().inflate(R.layout.header_user_tickets, null);
        userTicketsListView.addHeaderView(header);//заголовок списка заявок

        btnCallNobodyTickets = (Button) findViewById(R.id.btn_call_nodoby_tickets);
        btnExit = (Button) findViewById(R.id.btn_exit_user_tickets);
        btnCallNobodyTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_call_nodoby_tickets://вызываем активити для нераспределенных заявок
                        Intent intent = new Intent(UserTicketsActivity.this, NobodyTicketsActivity.class);
                        intent.putExtra("mLogin", mLogin);//перенос логина
                        intent.putExtra("mPassword", mPassword);//и пароля на след. Активити
                        clearTicketNumberArrayList();//Очистка массива звявок для использования в след. Активити
                        startActivity(intent);
                        finish();//закрываем эту активити
                        break;
                    case R.id.btn_exit_user_tickets:
                        UserTicketsActivity.this.finish();
                        break;

                    default:
                        break;
                }
            }
        });
        //Обработчик нажатия на пункт списка заявок
        userTicketsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), TicketActivity.class);
                position = position - 1;
                intent.putExtra("position", position);//передаем позицию в списке
                Log.d(LOG_TAG, "вывод position setonitemclicklistener " + position);

                String prevActivity = "user";//переменная для обозначения перехода с этого активити
                intent.putExtra("prevActivity", prevActivity);

                String ticketNumber = getTicketNumber(position);
                Log.d(LOG_TAG, "вывод ticketnumber setonitemclicklistener " + ticketNumber);
                intent.putExtra("ticketNumber", ticketNumber);//передаем номер заявки в активити заявки

                intent.putExtra("mLogin",mLogin);//ередаем логин
                intent.putExtra("mPassword", mPassword);//и пароль

                startActivity(intent);//не закрываем это активити для возможности возврата
                //finish();
            }
        });



        new ParseTask().execute(mLogin,mPassword);
    }

    private class ParseTask extends AsyncTask <String, Void, ArrayList<HashMap>> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //Соединение с сервером и получение заявок пользователя в отдельном потоке
        @Override
        protected ArrayList<HashMap> doInBackground(String... params) {

            userTicketsArrayList = new ArrayList<HashMap>();
            try {
                URL url = new URL(Constant.IP_ADR + "REST/1.0/search/ticket");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                String urlParameters = "user=" +params[0]+ "&pass=" +params[1]+ "&query=Owner='" +params[0]+ "'AND(Status='new'ORStatus='open'ORStatus='stalled')";

                // Посылаем POST запрос с данными пользователя и критериями отбора заявок
                urlConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                Log.d(LOG_TAG,"\nSending 'POST' request to URL : " + url);
                Log.d(LOG_TAG,"Post parameters : " + urlParameters);

                //Получаем заявки пользователя и форматируем их
                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                int i = 0;//счетчик для строк(нужен для избавления от пустых и служебных строк)
                while ((line = reader.readLine()) != null) {
                    HashMap temp = new HashMap();
                    Log.d(LOG_TAG, "вывод line " + line);
                    String[] array = line.split(":", 2);//Разделяем полученную строку на номер заявки и тему
                    if (i > 1) { //не закончится на 2 пустой строчке
                        if (array.length < 2) break;//но закончится на первой пустой после перечисления заявок
                    }
                    Log.d(LOG_TAG, "длина массива " + array.length);//отсеиваем все строки с заявками(они разделены ":")
                    if (array.length > 1) {
                        temp.put(ID_COLUMN, array[0]);
                        temp.put(SUBJECT_COLUMN, array[1]);
                        Log.d(LOG_TAG, "вывод temp " + temp);
                        userTicketsArrayList.add(temp);//заносим информацию в массив
                        i++;
                        //Первая строка со служебной информацией исключается из массива т.к. там нет ":"
                        //и из нее получается массив размером 1
                        setAddTicketNumber(array[0]);//добавляем номер заявки в массив с номерами заявок
                        Log.d(LOG_TAG, "содержимое adapterArrayList" + getTicketNumberArrayList());
                    }
                }

                reader.close();

            }
            catch (Exception e){
                e.printStackTrace();
            }
            return userTicketsArrayList;
        }


        @Override
        protected void onPostExecute(ArrayList<HashMap> array) {
            super.onPostExecute(array);
            userTicketsArrayList = array;

            UserListViewAdapter adapter = new UserListViewAdapter(UserTicketsActivity.this, userTicketsArrayList);
            adapter.setUseAdapter("userTickets");//Передаем в адаптер информацию об активити скоторого он запущен
            userTicketsListView.setAdapter(adapter);//заполняем список с помощью адаптера
        }



    }
}
