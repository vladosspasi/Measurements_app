package vo.aliabyev.measurements;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.webkit.URLUtil;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import android.widget.AdapterView;
import org.json.*;
import static vo.aliabyev.measurements.DatabaseActions.AddFullExperiment;

//TODO элементы списка занимают не всю ширину

//Активность заполнения существующего шаблона измерения и добавление данных в бд
public class CreateFromExistedActivity extends AppCompatActivity implements OpenFileDialog.OpenFileDialogListener,
        DialogEnterValue.DialogEnterValueListener, DialogChooseParam.DialogChooseParamListener{

    private String FILE_NAME = null;    //имя шаблона
    private Experiment experimentInfo;

    private ArrayList<TemplateParam> paramsInfo;
    private int selectedParamTemp = -1;

    private ArrayList<Parameter> Parameters;
    private int selectedParameter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createfromexisted);

        //Список параметров
        Parameters = new ArrayList<>();
        showList();
    }

    //----ОТКРЫТИЕ ФАЙЛА-------//

    public void showOpenFileDialog() {
        DialogFragment dialog = new OpenFileDialog();
        dialog.show(getSupportFragmentManager(), "OpenFileDialogFragment");
    }

    @Override
    public void onFileDialogPositiveClick(DialogFragment dialog, int selected, String[] files) {
        FILE_NAME = files[selected];
        TextView TempName = findViewById(R.id.new_fromtemplate_tempname);
        TempName.setText("Выбранный шаблон: " + FILE_NAME);
    }

    @Override
    public void onFileDialogNothingSelected(DialogFragment dialog){
        Toast.makeText(this, "Выберите шаблон!", Toast.LENGTH_SHORT).show();
    }

    //--Выбор файла в диалоге--//
    public void OnOpenFileClick(View view){
        /*String[] files = fileList();*/

        String[] filesArray = fileList();
        ArrayList<String> filesList = new ArrayList<>();
        for (String item:
             filesArray) {
            if(item.matches(".+\\.json")){
                filesList.add(item);
            }
        }
        filesArray = new String[filesList.size()];
        for(int i=0; i<filesArray.length;i++){
            filesArray[i]=filesList.get(i);
        }



        if(filesArray.length == 0){
            Toast.makeText(this, "Нет ни одного шаблона.", Toast.LENGTH_SHORT).show();
        }else{
            //Диалог для выбора шаблона
            showOpenFileDialog();
        }
    }

    private void JSONtoString() {
        //Открытие файла и извлечение JSON строки
        FileInputStream fin = null;
        String jsonStringTemp = null;
        //чтение файла
        try {
            fin = openFileInput(FILE_NAME);
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            jsonStringTemp = new String(bytes);
        } catch (IOException ex) {
            Toast.makeText(this, "Ошибка открытия/чтения файла!", Toast.LENGTH_SHORT).show();
            return;
        } finally {
            try {
                if (fin != null)
                    fin.close();
            } catch (IOException ex) {
                Toast.makeText(this, "Ошибка закрытия файла!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        //чтение JSON-объекта

        experimentInfo = new Experiment();
        paramsInfo = new ArrayList<>();
        JSONObject jsonTemp;
        JSONObject object;

        try {
            jsonTemp = new JSONObject(jsonStringTemp);
            //считывание основной информации
            object = jsonTemp.getJSONObject("experiment_info");
            experimentInfo.setArea(object.getString("area"));
            experimentInfo.setSubArea(object.getString("subarea"));
            experimentInfo.setPhysicalProcess(object.getString("phprocess"));
            experimentInfo.setPowerEquipment(object.getString("powequ"));
            experimentInfo.setDataType(object.getString("datatype"));
            TextView TempInfo = findViewById(R.id.new_fromtemplate_tempInfo);
            TempInfo.setText(object.toString());
        } catch (JSONException ex) {
            Toast.makeText(this, "Ошибка парсинга! " + ex.getMessage(), Toast.LENGTH_LONG).show();
            experimentInfo = new Experiment();
            return;
        }

        try {
            experimentInfo.setParentSubArea(object.getString("parentsubarea"));
        } catch (JSONException ignored) {
        }
        try {
            experimentInfo.setMainPicture(object.getString("mainpic"));
        } catch (JSONException ignored) {
        }
        try {
            experimentInfo.setGeomPicture(object.getString("geompic"));
        } catch (JSONException ignored) {
        }
        try {
            experimentInfo.setRegPicture(object.getString("regpic"));
        } catch (JSONException ignored) {
        }
        try {
            experimentInfo.setTeplPicture(object.getString("teplpic"));
        } catch (JSONException ignored) {
        }

        //Считывание массива параметров
        JSONArray array;
        try {
            array = jsonTemp.getJSONArray("experiment_params");
        } catch (JSONException ex) {
            Toast.makeText(this, "Ошибка парсинга! " + ex.getMessage(), Toast.LENGTH_LONG).show();
            experimentInfo = new Experiment();
            return;
        }

        TemplateParam param;
        for (int i = 0; i < array.length(); i++) {
            param = new TemplateParam();
            try {
                object = array.getJSONObject(i);
                param.setName(object.getString("name"));
                param.setShortName(object.getString("shortname"));
                param.setType(object.getInt("type"));
            } catch (JSONException ex) {
                Toast.makeText(this, "Ошибка парсинга! " + ex.getMessage(), Toast.LENGTH_LONG).show();
                experimentInfo = new Experiment();
                return;
            }
            try {
                param.setUnit(object.getString("unit"));
            } catch (JSONException ignored) {
            }
            paramsInfo.add(param);
        }

    }

    //--Установка шаблона для последующего заполнения--//
    public void ApplyTemplate(View view){
        if(FILE_NAME==null){
            Toast.makeText(this, "Сначала необходимо выбрать шаблон!", Toast.LENGTH_SHORT).show();
        }else{

            JSONtoString();//получение информации из JSON файла шаблона
            //основная информация о эксперименте:
            if(experimentInfo.isEmpty()){
                Toast.makeText(this, "Ошибка! Объект пуст.", Toast.LENGTH_SHORT).show();
            }
            TextView TempInfo = findViewById(R.id.new_fromtemplate_tempInfo);
            String TempText = "Область: " + experimentInfo.getArea() + " -> ";
            if(!experimentInfo.getParentSubArea().equals("")){
                TempText = TempText.concat(experimentInfo.getParentSubArea() + " -> ");
            }
            TempText = TempText.concat(experimentInfo.getSubArea() + ";\n" +
                    "Процесс: " + experimentInfo.getPhysicalProcess() + ";\n" +
                    "Оборудование: " + experimentInfo.getPowerEquipment() + ";\n" +
                    "Тип данных: " + experimentInfo.getDataType());

            LinearLayout imgbuttons = findViewById(R.id.new_fromtemplate_linear);
            imgbuttons.setVisibility(View.INVISIBLE);

            if(!(experimentInfo.getMainPicture().equals("")&&experimentInfo.getGeomPicture().equals("")&&
                    experimentInfo.getTeplPicture().equals("")&&experimentInfo.getRegPicture().equals(""))){
                TempText = TempText.concat("\nИзображения:");
                imgbuttons.setVisibility(View.VISIBLE);

                Button mainimg = findViewById(R.id.new_fromtemplate_img_main);
                Button geomimg = findViewById(R.id.new_fromtemplate_img_geom);
                Button regimg = findViewById(R.id.new_fromtemplate_img_reg);
                Button teplimg = findViewById(R.id.new_fromtemplate_img_tepl);

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
            TempInfo.setText(TempText);

            ConstraintLayout WorkArea = findViewById(R.id.new_fromtemplate_workarea);
            WorkArea.setVisibility(View.VISIBLE);//включение видимости части с заполнением параметров
        }
    }

    //----Диалог инфо о параметре + изменить/удалить----//
    public void showEditOrDeleteDialog(Parameter param){
        //вызов диалога с инфо о параметре + изменить/удалить

        String message = "Имя: " + param.getName() + ";\n" +
                "Короткое имя: " + param.getShortName() + ";\n" +
                "Тип: " + param.getStringType() + ";\n";

        if(param.getType()==0){
            message = message.concat("Значение: " + param.getFloatValue() + " " + param.getUnit());
        }else if(param.getType()==1){
            message = message.concat("Значение: " + param.getFloatValue());
        }else if(param.getType()==2){
            message = message.concat("От " + param.getRangeValue1() + " до " + param.getRangeValue2() + " " + param.getUnit());
        }else if(param.getType()==3){
            message = message.concat("Чтобы просмотреть изображение, зажмите эелемент списка.");
        }

        new AlertDialog.Builder(CreateFromExistedActivity.this)
                .setTitle("Информация об измерении")
                .setMessage(message)
                .setPositiveButton(R.string.change, (dialog, which) -> {
                    DialogEnterValue newdialog = new DialogEnterValue();
                    newdialog.setParameterToEdit(Parameters.get(selectedParameter));
                    newdialog.show(getSupportFragmentManager(), "EnterValueDialogFragment");
                    dialog.cancel();
                })
                .setNegativeButton(R.string.delete, (dialog, which) -> {
                    new AlertDialog.Builder(CreateFromExistedActivity.this)
                            .setTitle("Удалить")
                            .setMessage("Данные будут утеряны. Вы уверены, что хотите удалить данный параметр?")
                            .setPositiveButton(R.string.delete, (newdialog, newwhich) -> {
                                Parameters.remove(selectedParameter);
                                showList();
                                selectedParameter=-1;
                            })
                            .show();
                    dialog.cancel();
                })
                .setNeutralButton(R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    //Добавление в список нового параметра
    public void AddParameter(View view){
        showChooseParamDialog();
    }

    //----Диалог выбора параметра для ввода----//
    private void showChooseParamDialog(){
        DialogChooseParam dialog = new DialogChooseParam();
        dialog.setParamsInfo(paramsInfo);
        dialog.show(getSupportFragmentManager(), "ChooseParamDialogFragment");
    }

    @Override
    public void onDialogChooseParamPositiveClick(int selected){
        showEnterValueDialog(selected);
        selectedParamTemp = selected;
    }

    @Override
    public void onDialogChooseParamNothingSelected(){
        Toast.makeText(this, "Необходимо выбрать параметр!", Toast.LENGTH_LONG).show();
        showChooseParamDialog();
    }

    //----Диалог ввода величины-------//
    private void showEnterValueDialog(int selected) {
        //показать диалог ввода величины
        DialogEnterValue dialog = new DialogEnterValue();
        dialog.setParamTemplate(paramsInfo.get(selected));
        dialog.show(getSupportFragmentManager(), "EnterValueDialogFragment");
    }

    //Добавление величины в зависимоти от типа
    @Override
    public void onValueDialogPositiveClick(String value) {
        Parameter param = new Parameter();
        if(selectedParamTemp!=-1 && selectedParameter==-1){
            //добавление нового
            param = new Parameter(paramsInfo.get(selectedParamTemp), value);

            if(param.getType()==3&&!URLUtil.isValidUrl(value)){
                Toast.makeText(this, "Введена недействительная ссылка!", Toast.LENGTH_LONG).show();
                selectedParameter = -1;
                selectedParamTemp = -1;
                return;
            }
            Parameters.add(param);
        }else if(selectedParamTemp==-1 && selectedParameter!=-1){
            //изменение существующего
            param = Parameters.get(selectedParameter);
            param.setStringValue(value);
            Parameters.set(selectedParameter, param);
        }else{
            Toast.makeText(this, "Ошибка. Одновременное добавление и изменение.", Toast.LENGTH_LONG).show();
        }
        showList();
        selectedParameter = -1;
        selectedParamTemp = -1;
    }

    @Override
    public void onValueDialogPositiveClick(float value){
        Parameter param = new Parameter();
        if(selectedParamTemp!=-1 && selectedParameter==-1){
            //добавление нового
            param = new Parameter(paramsInfo.get(selectedParamTemp), value);
            Parameters.add(param);
        }else if(selectedParamTemp==-1 && selectedParameter!=-1){
            //изменение существующего
            param = Parameters.get(selectedParameter);
            param.setValue(value);
            Parameters.set(selectedParameter, param);
        }else{
            Toast.makeText(this, "Ошибка. Одновременное добавление и изменение.", Toast.LENGTH_LONG).show();
        }
        showList();
        selectedParameter = -1;
        selectedParamTemp = -1;
    }

    @Override
    public void onValueDialogPositiveClick(float value1, float value2){
        Parameter param = new Parameter();
        if(value1 > value2){
            Toast.makeText(this, "Значение \"ОТ\" не может быть больше \"ДО\"!", Toast.LENGTH_LONG).show();
        }else if(selectedParamTemp!=-1 && selectedParameter==-1){
            //добавление нового
            param = new Parameter(paramsInfo.get(selectedParamTemp), value1, value2);
            Parameters.add(param);
        }else if(selectedParamTemp==-1 && selectedParameter!=-1){
            //изменение существующего
            param = Parameters.get(selectedParameter);
            param.setDiapason(value1, value2);
            Parameters.set(selectedParameter, param);
        }/*else{
            Toast.makeText(this, "Ошибка. Одновременное добавление и изменение.", Toast.LENGTH_LONG).show();
        }*/
        showList();
        selectedParameter = -1;
        selectedParamTemp = -1;
    }

    @Override
    public void onValueDialogNothingSelected(){
        Toast.makeText(this, "Необходимо ввести значения!", Toast.LENGTH_SHORT).show();
        selectedParameter = -1;
        selectedParamTemp = -1;
    }

    @Override
    public void onValueDialogWrongType(){
        Toast.makeText(this, "Введенное значение не соотвествует параметру!", Toast.LENGTH_LONG).show();
    }

    //--------------Список---------------//
    //--вывести список параметров--//
    private void showList(){
        ArrayList<String> paramElements = new ArrayList<>();
        String buf;

        for (Parameter item:
                Parameters) {
            buf = item.getName() + ":\n";

            if(item.getType()==0){
                buf = buf.concat(item.getFloatValue() + " " + item.getUnit());
            }else if(item.getType()==1){
                buf = buf.concat(item.getStringValue());
            }else if(item.getType()==2){
                buf = buf.concat("От " + item.getRangeValue1() + " до " + item.getRangeValue2() + " " +item.getUnit());
            }
            else if(item.getType()==3){
                buf = buf.concat("Зажмите, чтобы просмотреть изображение.");
            }else{
                buf = buf.concat("ОШИБКА!");
            }
            paramElements.add(buf);
        }

        ListView listView = findViewById(R.id.new_fromtemplate_parlist);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, paramElements);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                selectedParameter = position;
                showEditOrDeleteDialog(Parameters.get(position));
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(Parameters.get(position).getType()==3){
                    GoToImg(Parameters.get(position).getStringValue(), Parameters.get(position).getName());
                }
                return true;
            }
        });


    }

    //--Внести данные в базу данных--//
    public void AddNewExperiment(View view){
        Toast toast;
        //Проверка, все ли заполнено
        if(Parameters.size()==0){
            Toast.makeText(this, "Вы не ввели ни одной величины. Нажмите \"Добавить\", чтобы ввести значение параметра.", Toast.LENGTH_LONG).show();
            return;
        }

        //подключение к бд
        SQLiteDatabase db;
        try{
            db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);
            db.close();
        }catch (SQLiteException e){
            Toast.makeText(this, "Ошибка открытия БД.", Toast.LENGTH_SHORT).show();
            return;
        }
        db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);

        //Добавление
        boolean completed = AddFullExperiment(db, experimentInfo, Parameters);

        //результат
        if (completed){
            db.close();
            new AlertDialog.Builder(CreateFromExistedActivity.this)
                    .setTitle("Готово!")
                    .setMessage("Измерение успешно добавлено в базу данных.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        dialog.cancel();
                    })
                    .setOnDismissListener(DialogInterface::cancel)
                    .setOnCancelListener((dialog) -> {
                        Intent intent = new Intent(CreateFromExistedActivity.this, CreateFromExistedActivity.class);
                        startActivity(intent);
                        CreateFromExistedActivity.this.finish();
                    })
                    .show();
        }else{
            Toast.makeText(this, "Произошла ошибка при добавлении.", Toast.LENGTH_SHORT).show();
        }
    }

    //-----------Изображения-------------//

    public void createfrom_showImageMain(View view){
        GoToImg(experimentInfo.getMainPicture(), "Основное изображение");
    }

    public void createfrom_showImageGeom(View view){
        GoToImg(experimentInfo.getGeomPicture(), "Геометрическое изображение");
    }

    public void createfrom_showImageReg(View view){
        GoToImg(experimentInfo.getRegPicture(), "Регулярное изображение");
    }

    public void createfrom_showImageTepl(View view){
        GoToImg(experimentInfo.getTeplPicture(), "Тепловое изображение");
    }

    private void GoToImg(String imglink, String description){
        if(URLUtil.isValidUrl(imglink)){
            Intent intent = new Intent(CreateFromExistedActivity.this, ImageActivity.class);
            intent.putExtra("link", imglink);
            intent.putExtra("desc", description);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Неверная ссылка.", Toast.LENGTH_SHORT).show();
        }

    }

}
