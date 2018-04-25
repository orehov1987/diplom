package ru.whitejoker.testjson;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static ru.whitejoker.testjson.Constant.*;

public class UserListViewAdapter extends BaseAdapter
{
    public ArrayList<HashMap> userAdapterTicketsArrayList;//Список заявок в адаптере
    public static String LOG_TAG = "my_log";
    Activity userActivity;
    String useAdapter;//Для определения откуда запускется адаптер

    //Присваем переменой значение активити откуда вызван адаптер
    public  void setUseAdapter(String act) {
        useAdapter = act;
    }


    public UserListViewAdapter(Activity userActivity, ArrayList<HashMap> userAdapterTicketsArrayList) {
        super();
        this.userActivity = userActivity;
        this.userAdapterTicketsArrayList = userAdapterTicketsArrayList;

    }

    //Размер листа заявок
    @Override
    public int getCount() {
        return userAdapterTicketsArrayList.size();
    }

    //Позиция которая заполняется
    @Override
    public Object getItem(int position) {
        return userAdapterTicketsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private  class ViewHolder {
        TextView txtId;
        TextView txtSubject;
    }

    //Заполняем поле списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(LOG_TAG, "p: " + position);
        ViewHolder holder;
        LayoutInflater inflater = userActivity.getLayoutInflater();

        if (convertView == null)//Проверяем пусто ли
        {
            holder = new ViewHolder();
            //Разные варианты заполнения в зависимости от активити
            switch (useAdapter) {
                case "ticket"://для окна заявки
                    convertView = inflater.inflate(R.layout.listview_row_ticket, null);
                    holder.txtId = (TextView) convertView.findViewById(R.id.static_field_ticket);
                    holder.txtSubject = (TextView) convertView.findViewById(R.id.variable_field_ticket);
                    convertView.setTag(holder);
                    break;
                case "userTickets"://для окна заявок пользователя
                    convertView = inflater.inflate(R.layout.listview_row_user, null);
                    holder.txtId = (TextView) convertView.findViewById(R.id.id_ticket);
                    holder.txtSubject = (TextView) convertView.findViewById(R.id.subject_ticket);
                    convertView.setTag(holder);
                    break;
                case "nobodyTickets"://для окна свободных заявок
                    convertView = inflater.inflate(R.layout.listview_row_user, null);
                    holder.txtId = (TextView) convertView.findViewById(R.id.id_ticket);
                    holder.txtSubject = (TextView) convertView.findViewById(R.id.subject_ticket);
                    convertView.setTag(holder);
                    break;
                default:
                    break;
            }
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        //переносим в интерфейс заполненне поле
        HashMap map = userAdapterTicketsArrayList.get(position);
        holder.txtId.setText((String)map.get(ID_COLUMN));
        holder.txtSubject.setText((String)map.get(SUBJECT_COLUMN));
        Log.d(LOG_TAG, (String) holder.txtId.getText());
        Log.d(LOG_TAG, position + "");


    return convertView;

    }

}
