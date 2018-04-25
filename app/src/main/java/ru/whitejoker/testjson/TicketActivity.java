package ru.whitejoker.testjson;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static ru.whitejoker.testjson.Constant.*;

public class TicketActivity extends AppCompatActivity {

    public static String LOG_TAG = "my_log";
    ListView ticketListView;
    private ArrayList<HashMap> ticketArrayList;
    int key;
    String ticketNumber;
    String mLogin;
    String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        key = getIntent().getIntExtra("position", 0);
        ticketNumber = getIntent().getStringExtra("ticketNumber");//Принимаем номер заявки
        mLogin = getIntent().getStringExtra("mLogin");//достаем логин
        mPassword = getIntent().getStringExtra("mPassword");//и пароль

        setContentView(R.layout.activity_ticket);
        ticketListView = (ListView)findViewById(R.id.TicketListView);

        Log.d(LOG_TAG, "вывод номера заявки  " + ticketNumber + "   " + key);
        //Проверка из какого списка открыт тикет. Позволяет испольщовть в меню заявки нужную кнопку
        if (getIntent().getStringExtra("prevActivity").equals("nobody")){//если открыто из спика неназначенных заявок
            Button btnResolveTicket = (Button)findViewById(R.id.btn_resolve_ticket);
            btnResolveTicket.setVisibility(View.GONE);//кнопка закрытия заявки скрывается
        }
        else if (getIntent().getStringExtra("prevActivity").equals("user")){//если открыто из спика заявок пользователя
            Button btnChangeOwnerTicket = (Button)findViewById(R.id.btn_change_owner_ticket);
            btnChangeOwnerTicket.setVisibility(View.GONE);//кнопка назначения зявки на себя скрывается
        }

        new TicketActivity.ParseTask().execute(mLogin, mPassword, ticketNumber);
    }

    private class AsyncRequest extends AsyncTask<String, Void, Boolean> {

        //Соединение с сервером и выполненние кода запроса с кнопок изменения владельца и закрытия заявки в отдельном потоке
        @Override
        protected Boolean doInBackground(String... params) {


            HttpURLConnection conn;
            BufferedReader reader;
            String line;
            try {

                URL chOwnUrl = new URL(Constant.IP_ADR + "REST/1.0/ticket/" + ticketNumber + "/edit");
                Log.d(LOG_TAG, "вывод урл для смены владельца  " + chOwnUrl);
                conn = (HttpURLConnection) chOwnUrl.openConnection();
                conn.setRequestMethod("POST");

                String urlParameters = "user=" +params[0]+ "&pass=" +params[1]+ params[2];

                // Посылаем POST запрос с данными пользователя и переменной для изменения хозяина заявки
                conn.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();


                int responseCode = conn.getResponseCode();
                Log.d(LOG_TAG,"Response Code : " + responseCode);
                Log.d(LOG_TAG, "\nSending 'POST' request to URL : " + chOwnUrl);
                Log.d(LOG_TAG, "Post parameters : " + urlParameters);

                //Получаем ответ
                conn.connect();
                InputStream inputStream = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                if ((line = reader.readLine()) != null) {
                    Log.d(LOG_TAG, "вывод" + line);
                }
                reader.close();
                return (line.equals("RT/4.4.1 200 Ok")); //проверяем успешно ли прошло

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }


        }

        //если все прошло успешно выводим сообщение о положительном результате, а если нет об отицательном
        @Override
        protected void onPostExecute(Boolean change) {
            super.onPostExecute(change);
            if (change) {
                Toast toastSuccess = Toast.makeText(TicketActivity.this, R.string.accept_request_from_ticket, Toast.LENGTH_LONG);
                toastSuccess.show();//+
            } else {
                Toast toastError = Toast.makeText(TicketActivity.this, R.string.error_request_from_ticket, Toast.LENGTH_LONG);
                toastError.show();//-
            }
        }

    }


    private class ParseTask extends AsyncTask<String, Void, ArrayList<HashMap>> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //Соединение с сервером и получение полей заявки в отдельном потоке
        @Override
        protected ArrayList<HashMap> doInBackground(String... params) {

            ticketArrayList = new ArrayList<HashMap>();
            try {

                URL url = new URL(Constant.IP_ADR + "REST/1.0/ticket/" +params[2]);
                Log.d(LOG_TAG, url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                String urlParameters = "user=" +params[0]+ "&pass=" +params[1];

                // Посылаем POST запрос с данными пользователя и номером заявки
                urlConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                Log.d(LOG_TAG,"\nSending 'POST' request to URL : " + url);
                Log.d(LOG_TAG,"Post parameters : " + urlParameters);

                //Получаем заявку и форматируем ее
                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                int i = 0;//счетчик для строк(нужен для избавления от пустых и служебных строк)
                while ((line = reader.readLine()) != null) {
                    HashMap temp = new HashMap();
                    Log.d(LOG_TAG, "вывод line " + line);
                    String[] array = line.split(":", 2);//Разделяем полученную строку на заголовок и информационное поле
                    switch (array[0]){//заменяем заголовки на понятные
                        case "id":
                            array[0] = "Номер";
                            break;
                        case "Queue":
                            array[0] = "Очередь";
                            break;
                        case "Owner":
                            array[0] = "Ответственный";
                            break;
                        case "Creator":
                            array[0] = "Создатель";
                            break;
                        case "Subject":
                            array[0] = "Тема";
                            break;
                        case "Status":
                            array[0] = "Статус";
                            break;
                        case "Priority":
                            array[0] = "Приоритет";
                            break;
                        case "InitialPriority":
                            array[0] = "Старт.Приор.";
                            break;
                        case "FinalPriority":
                            array[0] = "Финал.Приор.";
                            break;
                        case "Requestors":
                            array[0] = "Адрес заявителя";
                            break;
                        case "Cc":
                            array[0] = "Служ.поле";
                            break;
                        case "AdminCc":
                            array[0] = "Служ.поле";
                            break;
                        case "Created":
                            array[0] = "Создана";
                            break;
                        case "Starts":
                            array[0] = "Служ.поле";
                            break;
                        case "Started":
                            array[0] = "Начата";
                            break;
                        case "Due":
                            array[0] = "Служ.поле";
                            break;
                        case "Resolved":
                            array[0] = "Решена";
                            break;
                        case "Told":
                            array[0] = "Служ.поле";
                            break;
                        case "LastUpdated":
                            array[0] = "Обновлена";
                            break;
                        case "TimeEstimated":
                            array[0] = "Служ.поле";
                            break;
                        case "TimeWorked":
                            array[0] = "Служ.поле";
                            break;
                        case "TimeLeft":
                            array[0] = "Служ.поле";
                            break;
                        default:
                            break;
                    }
                    if (i > 10) {
                        if (array.length < 2) break;//строк в заявке фиксированное количество.Проверяем конец завки и выходим
                    }
                    Log.d(LOG_TAG, "длина массива " + array.length);//отсеиваем все информационные строки (они разделены ":")
                    if (array.length > 1) {
                        temp.put(ID_COLUMN, array[0]);
                        temp.put(SUBJECT_COLUMN, array[1]);
                        Log.d(LOG_TAG, "вывод temp " + temp);
                        ticketArrayList.add(temp);//заносим информацию в массив
                        i++;
                    }
                }


                reader.close();
                urlConnection.disconnect();

            }
            catch (Exception e){
                e.printStackTrace();
            }
            return ticketArrayList;
        }


        @Override
        protected void onPostExecute(ArrayList<HashMap> array) {
            super.onPostExecute(array);

            ticketArrayList = array;

            UserListViewAdapter adapter = new UserListViewAdapter(TicketActivity.this, ticketArrayList);
            adapter.setUseAdapter("ticket");//Передаем в адаптер информацию об активити скоторого он запущен
            ticketListView.setAdapter(adapter);//заполняем заявку с помощью адаптера
        }

    }
    //обработчик кнопок
    public void onClickButtons (View v) {
        String contentString;//для переменной content
        AsyncTask<String, Void, Boolean> ar = new AsyncRequest();
        String  prevActivity = getIntent().getStringExtra("prevActivity");
        switch (v.getId()) {
            case R.id.btn_call_list_tickets://для кнопки вызова листа заявок из окна заявки
                if (prevActivity.equals("nobody")){//откроет нераспределенные заявки
                    Intent intentNobody = new Intent(TicketActivity.this, NobodyTicketsActivity.class);
                    intentNobody.putExtra("mLogin",mLogin);//ередаем логин
                    intentNobody.putExtra("mPassword", mPassword);//и пароль
                    startActivity(intentNobody);
                }
                else if (prevActivity.equals("user")){//откроет заявки пользователя
                    Intent intentUsers = new Intent(TicketActivity.this, UserTicketsActivity.class);
                    intentUsers.putExtra("mLogin",mLogin);//ередаем логин
                    intentUsers.putExtra("mPassword", mPassword);//и пароль
                    startActivity(intentUsers);
                }
                finish();
                break;
            case R.id.btn_resolve_ticket://для кнопки изменения атрибутов заявки
                contentString = "&content=Status: resolved";//для закрытия заявки закрепленнойц за пользователем
                ar.execute(mLogin, mPassword, contentString);
                break;
            case R.id.btn_change_owner_ticket:
                contentString = "&content=Owner: " + mLogin;//для назначения на себя свободной заявки
                ar.execute(mLogin,mPassword, contentString);
                break;
            default:
                break;
        }
    }
}
