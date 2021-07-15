package vo.aliabyev.measurements;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import static vo.aliabyev.measurements.DatabaseActions.*;


//активность списка вывода всех проведенных экспериментов
public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Подключение к бд, получение списка экспериментов
        SQLiteDatabase db;
        int dbEmpty;

        try {
            db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);
            dbEmpty = isExperimentTableEmpty(db);
            if(dbEmpty == -1){
                Toast.makeText(this, "Ошибка создания таблиц/доступа к бд.", Toast.LENGTH_SHORT).show();
                db.close();
                return;
            }
        } catch (SQLiteException e) {
            Toast.makeText(this, "Ошибка подключения к бд.", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView message = findViewById(R.id.ExperimentList_message);
        if (dbEmpty==1) {
            //Установить соотвествующий текст
            message.setText("В базе данных пока что нет ни одного измерения.");
        } else {
            message.setText("Список всех измерений:");
            //Если не пусто, то чтение и вывод

            ArrayList<ItemExperiment> itemExperiments = getExperimentList(db);

            if(itemExperiments.size()==0){
                Toast.makeText(this, "Ошибка считывания экспериментов.", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> expListElements = new ArrayList<>();
            String buf;

            for (ItemExperiment item:
                 itemExperiments) {
                buf = "Измерение " + item.getID() + "\n" +
                        "Подобласть: " + item.getSubArea() + "\n" +
                        "Физический процесс:" + item.getProcess() +"\n"+
                        "Оборудование/установка: " + item.getEquipment() +"\n"+
                        "Параметры: " + item.getParam1();
                if(!item.getParam2().equals("")){
                    buf = buf.concat("; " + item.getParam2());
                    if(!item.getParam3().equals("")){
                        buf = buf.concat("; " + item.getParam3());
                        if(item.isMore()){
                            buf = buf.concat("...");
                        }
                    }
                }
                expListElements.add(buf);
            }
            //вывод

            ListView listView = findViewById(R.id.ExperimentList_listview);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, expListElements);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                {
                    //по клику на элемент - переход на другую активность с передачей ID эксперимента
                    int idToTransmit = itemExperiments.get(position).getID();
                    Intent intent = new Intent(ListActivity.this, ExperimentActivity.class);
                    intent.putExtra("ID", idToTransmit);
                    startActivity(intent);
                    ListActivity.this.finish();
                }
            });

        }
        db.close();
    }

}
