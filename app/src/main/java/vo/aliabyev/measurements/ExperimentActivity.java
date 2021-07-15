package vo.aliabyev.measurements;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import static vo.aliabyev.measurements.DatabaseActions.*;

//Страница с информацией о выбранном эксперименте
public class ExperimentActivity extends AppCompatActivity{

    private int experimentID;
    private Experiment experimentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //получение айди эксперимента, который нужно вывести
        Bundle arguments = getIntent().getExtras();
        experimentID = arguments.getInt("ID");

        setContentView(R.layout.activity_experimentinfo);
        TextView title = findViewById(R.id.experiment_title);
        TextView info = findViewById(R.id.experiment_info);
        ListView listView = findViewById(R.id.experiment_parameters);

        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);
        ArrayList<Parameter> parameters = GetParametersList(db, experimentID);
        experimentInfo = GetExperimentInfo(db, experimentID);
        db.close();

        String t = "Измерение " + experimentID + "\n" +
                "Дата внесения: " + parameters.get(0).getDate();
        title.setText(t);

        String inf = "Область: " + experimentInfo.getArea() + "\n";

        if(!(experimentInfo.getParentSubArea().equals("")||experimentInfo.getParentSubArea().equals("root"))){
            inf = inf.concat("Родительская подобласть: " + experimentInfo.getParentSubArea() + "\n");
        }
        inf = inf.concat("Подобласть: " + experimentInfo.getSubArea() + "\n" +
                        "Физический процесс: " + experimentInfo.getPhysicalProcess() + "\n" +
                        "Оборудование/установка: " + experimentInfo.getPowerEquipment() + "\n" +
                        "Тип данных: " + experimentInfo.getDataType());

        LinearLayout imgbuttons = findViewById(R.id.new_fromtemplate_linear);
        imgbuttons.setVisibility(View.INVISIBLE);

        if(!(experimentInfo.getMainPicture().equals("")&&experimentInfo.getGeomPicture().equals("")&&
                experimentInfo.getTeplPicture().equals("")&&experimentInfo.getRegPicture().equals(""))){
            inf = inf.concat("\nИзображения:");
            imgbuttons.setVisibility(View.VISIBLE);

            Button mainimg = findViewById(R.id.exp_img_main);
            Button geomimg = findViewById(R.id.exp_img_geom);
            Button regimg = findViewById(R.id.exp_img_reg);
            Button teplimg = findViewById(R.id.exp_img_tepl);

            if(experimentInfo.getMainPicture().equals("")){
                mainimg.setEnabled(false);
            }
            if(experimentInfo.getRegPicture().equals("")){
                regimg.setEnabled(false);
            }
            if(experimentInfo.getTeplPicture().equals("")){
                teplimg.setEnabled(false);
            }
            if(experimentInfo.getGeomPicture().equals("")){
                geomimg.setEnabled(false);
            }
        }
        info.setText(inf);

        //список и получение данных из бд
        ArrayList<String> paramElements = new ArrayList<>();
        String buf;

        for (Parameter item:
                parameters) {
            buf = "Имя: " + item.getName() + "\n" +
                    "Тип: " + item.getStringType() + "\n";
            if (item.getType() == 0) {
                buf = buf.concat(item.getShortName()) + " = " + item.getFloatValue();
            } else if (item.getType() == 1) {
                buf = buf.concat(item.getShortName()) + " = " + item.getStringValue();
            } else if (item.getType() == 2) {
                buf = buf.concat(item.getShortName() + " от " + item.getRangeValue1() + " до " + item.getRangeValue2());
            } else if (item.getType() == 3) {
                buf = buf.concat("Нажмите, чтобы открыть изображение.");
            }
            paramElements.add(buf);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, paramElements);
            listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                if(parameters.get(position).getType()==3){
                    GoToImg(parameters.get(position).getStringValue(), parameters.get(position).getName());
                }
            }
        });
    }

    public void DeleteExperimentFromDB(View view){

        new AlertDialog.Builder(ExperimentActivity.this)
                .setTitle("Удалить")
                .setMessage("Данные будут утеряны. Вы уверены, что хотите удалить данный эксперимент?")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);
                    DeleteExperiment(db, experimentID);
                    db.close();
                    Intent intent = new Intent(ExperimentActivity.this, ListActivity.class);
                    startActivity(intent);
                    ExperimentActivity.this.finish();
                })
                .show();
    }


    public void exp_showImageMain(View view){

        GoToImg(experimentInfo.getMainPicture(), "Основное изображение");
    }

    public void exp_showImageGeom(View view){

        GoToImg(experimentInfo.getGeomPicture(), "Геометрическое изображение");
    }

    public void exp_showImageReg(View view){

        GoToImg(experimentInfo.getRegPicture(), "Регулярное изображение");
    }

    public void exp_showImageTepl(View view){

        GoToImg(experimentInfo.getTeplPicture(), "Тепловое изображение");
    }

    private void GoToImg(String imglink, String description){
        if(URLUtil.isValidUrl(imglink)){
            Intent intent = new Intent(ExperimentActivity.this, ImageActivity.class);
            intent.putExtra("link", imglink);
            intent.putExtra("desc", description);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Введена неверная ссылка.", Toast.LENGTH_SHORT).show();
        }

    }


}
