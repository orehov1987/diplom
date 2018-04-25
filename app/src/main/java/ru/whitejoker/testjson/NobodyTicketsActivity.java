package ru.whitejoker.testjson;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

public class NobodyTicketsActivity extends AppCompatActivity {

    public static String LOG_TAG = "my_log";
    ListView nobodyTicketsListView;
    private ArrayList<HashMap> nobodyTicketsArrayList;//Список неназначенных заявок
    Button btnCallUserTickets;//Кнопка вызова списка заявок пользователя
    Button btnExit;//Выход
    String mLogin;
    String mPassword;
    Constant constant;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogin = getIntent().getStringExtra("mLogin");//достаем логин
        mPassword = getIntent().getStringExtra("mPassword");//и пароль
        setContentView(R.layout.activity_nobody_tickets);
        nobodyTicketsListView = (ListView)findViewById(R.id.NobodyTicketsListView);
        //Заголовок такой же что и у заявок пользователя
        View header = getLayoutInflater().inflate(R.layout.header_user_tickets, null);
        nobodyTicketsListView.addHeaderView(header);

        btnCallUserTickets = (Button) findViewById(R.id.btn_call_user_tickets);
        btnExit = (Button) findViewById(R.id.btn_exit_nobody_tickets);
        btnCallUserTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_call_user_tickets://вызываем активити для заявок пользователя
                        Intent intent = new Intent(NobodyTicketsActivity.this, UserTicketsActivity.class);
                        intent.putExtra("mLogin", mLogin);//перенос логина
                        intent.putExtra("mPassword", mPassword);//и пароля на след. Активити
                        startActivity(intent);
                        constant.clearTicketNumberArrayList();//Очистка массива звявок для использования в след. Активити
                        finish();//закрываем эту активити
                        break;
                    case R.id.btn_exit_nobody_tickets:
                        break;
                    default:
                        break;
                }
            }
        });

        //Обработчик нажатия на пункт списка заявок
        nobodyTicketsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), TicketActivity.class);
                position = position - 1;
                intent.putExtra("position", position);//передаем позицию в списке
                Log.d(LOG_TAG, "вывод position setonitemclicklistener " + position);

                String prevActivity = "nobody";//переменная для обозначения перехода с этого активити
                intent.putExtra("prevActivity", prevActivity);

                //String ticketNumber = ticketNumberArrayList.get(position);
                Log.d(LOG_TAG, "вывод ticketnumber setonitemclicklistener " + getTicketNumber(position));
                intent.putExtra("ticketNumber", getTicketNumber(position));//передаем номер заявки в активити заявки

                intent.putExtra("mLogin",mLogin);//ередаем логин
                intent.putExtra("mPassword", mPassword);//и пароль

                startActivity(intent);//не закрываем это активити для возможности возврата
            }
        });

        new NobodyTicketsActivity.ParseTask().execute();
    }

    private class ParseTask extends AsyncTask<Object, Object, ArrayList<HashMap>> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //Соединение с сервером и получение списка нераспределеных заявок в отдельном потоке
        @Override
        protected ArrayList<HashMap> doInBackground(Object... params) {

            nobodyTicketsArrayList = new ArrayList<HashMap>();
            try {
                URL url = new URL(Constant.IP_ADR + "REST/1.0/search/ticket");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                String urlParameters = "user=" +mLogin+ "&pass=" +mPassword+ "&query=Owner='Nobody'AND(Status='new'ORStatus='open')";

                // Посылаем POST запрос с данными пользователя(для авторизации запроса) и критериями отбора заявок
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
                Constant constant = new Constant();
                while ((line = reader.readLine()) != null) {
                    HashMap temp = new HashMap();
                    Log.d(LOG_TAG, "вывод line " + line);
                    String[] array = line.split(":", 2);//Разделяем полученную строку на номер заявки и тему
                    if (i > 1) {//не закончится на 2 пустой строчке
                        if (array.length < 2) break;//но закончится на первой пустой после перечисления заявок
                    }
                    Log.d(LOG_TAG, "длина массива " + array.length);//отсеиваем все строки с заявками(они разделены ":")
                    if (array.length > 1) {
                        temp.put(ID_COLUMN, array[0]);
                        temp.put(SUBJECT_COLUMN, array[1]);
                        Log.d(LOG_TAG, "вывод temp " + temp);
                        nobodyTicketsArrayList.add(temp);//заносим информацию в массив
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
            return nobodyTicketsArrayList;
        }


        @Override
        protected void onPostExecute(ArrayList<HashMap> array) {
            super.onPostExecute(array);
            nobodyTicketsArrayList = array;

            UserListViewAdapter adapter = new UserListViewAdapter(NobodyTicketsActivity.this, nobodyTicketsArrayList);
            adapter.setUseAdapter("nobodyTickets");//Передаем в адаптер информацию об активити скоторого он запущен
            nobodyTicketsListView.setAdapter(adapter);//заполняем список с помощью адаптера
        }



    }
}



