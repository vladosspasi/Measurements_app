package vo.aliabyev.measurements;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//TODO заменить все hardстроки на строки в string.xml
//TODO Элементы списка не занимают всю ширину

//Активность создания нового шаблона измерения
public class CreateNewActivity extends AppCompatActivity implements DialogEnterParam.DialogEnterParamListener{

    private ImageLoader imageLoader;

    private ArrayList<TemplateParam> paramArrayList;

    private int selectedParameter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createnew);

        paramArrayList = new ArrayList<>(); //обнуление списка
        TemplateParam param = new TemplateParam();
        paramArrayList.add(param);

        showList();

    }

    public void CreateNewTemplate(View view) throws JSONException {
        //Считывание полей формы и перевод структуры в файл JSON

        //Считывание формы
        TemplateExperiment tempConfig = new TemplateExperiment();

        EditText FormElem = findViewById(R.id.TemplateNameInput);
        tempConfig.setName(FormElem.getText().toString());
        FormElem = findViewById(R.id.AreaInput);
        tempConfig.setArea(FormElem.getText().toString());
        FormElem = findViewById(R.id.SubAreaInput);
        tempConfig.setSubArea(FormElem.getText().toString());
        FormElem = findViewById(R.id.ParentSubAreaInput);
        tempConfig.setParentSubArea(FormElem.getText().toString());
        FormElem = findViewById(R.id.dataTypeInput);
        tempConfig.setDataType(FormElem.getText().toString());
        FormElem = findViewById(R.id.PhpInput);
        tempConfig.setPhysicalProcess(FormElem.getText().toString());
        FormElem = findViewById(R.id.PweInput);
        tempConfig.setPowerEquipment(FormElem.getText().toString());
        FormElem = findViewById(R.id.MainPicInput);
        tempConfig.setMainPicture(FormElem.getText().toString());
        FormElem = findViewById(R.id.GeomPicInput);
        tempConfig.setGeomPicture(FormElem.getText().toString());
        FormElem = findViewById(R.id.RegPicInput);
        tempConfig.setRegPicture(FormElem.getText().toString());
        FormElem = findViewById(R.id.TeplPicInput);
        tempConfig.setTeplPicture(FormElem.getText().toString());
        //параметры содержатся в массиве

        //проверка имени файла на длину
        if(tempConfig.getName().length()>120){
            Toast.makeText(this, "Имя шаблона слишком длинное! Максимальная разрешенная длина - 120 символов.", Toast.LENGTH_LONG).show();
            return;
        }

        //проверка имени файла на символы
        String regex = "[^a-zA-Z_0-9а-яА-Я]";
        Pattern pattern = Pattern.compile(regex);
        Matcher mtch = pattern.matcher(tempConfig.getName());

        if(mtch.matches()){
            Toast.makeText(this, "Имя шаблона содержит запрещенные символы! (Разрешены цифры, буквы латиницы и киррилицы и \"_\")", Toast.LENGTH_LONG).show();
            return;
        }

        String FILE_NAME = tempConfig.getFileName();

        //Существует ли файл
        boolean fileExists = true;
        FileInputStream fin = null;
        try {
            //Попытка открыть на чтение
            fin = openFileInput(FILE_NAME);
            fin.close();
        }
        catch(IOException ex) {
            fileExists = false;
        }


        boolean arrayIsEmpty = false;

        if(paramArrayList.size()==0){
            arrayIsEmpty=true;
        }else{
            for (TemplateParam param:
                    paramArrayList) {
                if(param.isEmpty()){
                    arrayIsEmpty = true;
                    break;
                }
            }
        }

        //Проверка пустоты полей
        if(tempConfig.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля!", Toast.LENGTH_SHORT).show();

        } else if(arrayIsEmpty){
            Toast.makeText(this, "Заполните массив параметров!", Toast.LENGTH_SHORT).show();
        }else if(fileExists){
            //Проверка на существование файла
            Toast.makeText(this, "Шаблон с таким именем уже существует. Выберите другое имя.", Toast.LENGTH_LONG).show();
        }
        else{

            //Начать сборку json-объекта
            JSONObject experiment = new JSONObject();
            //информация об эксперименте
            JSONObject experimentInfo = new JSONObject()
                    .put("area", tempConfig.getArea());
            if(!tempConfig.getParentSubArea().equals(""))
                experimentInfo.put("parentsubarea", tempConfig.getParentSubArea());
            experimentInfo.put("subarea", tempConfig.getSubArea())
                .put("phprocess", tempConfig.getPhysicalProcess())
                .put("powequ", tempConfig.getPowerEquipment())
                    .put("datatype", tempConfig.getDataType());
            if(!tempConfig.getMainPicture().equals(""))
                experimentInfo.put("mainpic", tempConfig.getMainPicture());
            if(!tempConfig.getGeomPicture().equals(""))
                experimentInfo.put("geompic", tempConfig.getGeomPicture());
            if(!tempConfig.getRegPicture().equals(""))
                experimentInfo.put("regpic", tempConfig.getRegPicture());
            if(!tempConfig.getTeplPicture().equals(""))
                experimentInfo.put("teplpic", tempConfig.getTeplPicture());
            //параметры
            JSONArray experimentParams = new JSONArray();
            for (TemplateParam param:
                 paramArrayList) {
                JSONObject parameter = new JSONObject()
                        .put("name", param.getName())
                        .put("shortname", param.getShortName())
                        .put("type", param.getType());
                if(param.getType()==0 || param.getType()==2){
                    parameter.put("unit", param.getUnit());
                }
                experimentParams.put(parameter);
            }
            //поместить все в общий объект эксперимента
            experiment.put("experiment_info", experimentInfo);
            experiment.put("experiment_params", experimentParams);

            //Создание файла JSON
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                fos.write(experiment.toString().getBytes());
            }
            catch(IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            finally{
                try{
                    if(fos!=null)
                        fos.close();
                }
                catch(IOException ex){
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
                finally {
                    new AlertDialog.Builder(CreateNewActivity.this)
                            .setTitle("Готово!")
                            .setMessage("Шаблон с именем \"" + tempConfig.getName() + "\" сохранен.")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialog.cancel();
                            })
                            .setOnDismissListener(DialogInterface::cancel)
                            .setOnCancelListener((dialog) -> {
                                CreateNewActivity.this.finish();
                            })
                            .show();
                }

            }
        }
    }

    public void addNewParameter(View view){
        TemplateParam param = new TemplateParam();
        paramArrayList.add(param);
        showList();
    }

    private void showEnterParamDialog(){
        DialogFragment dialog = new DialogEnterParam();
        dialog.show(getSupportFragmentManager(), "DialogEnterParamFragment");
    }

    @Override
    public void onDialogParamPositiveClick(DialogFragment dialog, String name, String shortname, int typeInt, String unit){
        TemplateParam param = new TemplateParam();
        param.setName(name);
        param.setShortName(shortname);
        param.setUnit(unit);
        param.setType(typeInt);
        if(param.isEmpty()){
            Toast.makeText(this, "Заполните все необходимые поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        paramArrayList.remove(selectedParameter);
        TemplateParam item = new TemplateParam();
        item.setName(name);
        item.setShortName(shortname);
        item.setType(typeInt);
        item.setUnit(unit);
        paramArrayList.add(item);

        showList();
    }

    public void onDialogParamDeleteClick(){

        new AlertDialog.Builder(CreateNewActivity.this)
                .setTitle("Удалить")
                .setMessage("Данные будут утеряны. Вы уверены, что хотите удалить данный параметр?")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    paramArrayList.remove(selectedParameter);
                    showList();
                })
                .show();
    }

    private void showList(){

        String[] paramTempsElements = new String[paramArrayList.size()];
        //заполнение массива строк

        for(int i =0; i<paramArrayList.size(); i++) {
            String buf = "Параметр " + Integer.toString(i + 1) + "\n";
            if (!paramArrayList.get(i).isEmpty()) {

                buf = buf.concat("Название: " + paramArrayList.get(i).getName() + "\n" +
                        "Короткое название: " + paramArrayList.get(i).getShortName() + "\n" +
                        "Тип: ");

                if (paramArrayList.get(i).getType() == 0) {
                    buf = buf.concat("Численный");
                } else if (paramArrayList.get(i).getType() == 1) {
                    buf = buf.concat("Строковый");
                } else if (paramArrayList.get(i).getType() == 2) {
                    buf = buf.concat("Диапазон");
                } else if (paramArrayList.get(i).getType() == 3) {
                    buf = buf.concat("Изображение");
                } else {
                    buf = buf.concat("Ошибка");
                    return;
                }

                if (paramArrayList.get(i).getType() == 0 || paramArrayList.get(i).getType() == 2) {
                    buf = buf.concat("\nЕдиница измерения: " + paramArrayList.get(i).getUnit());
                }
            } else {
                buf = buf.concat("Нажмите, чтобы ввести данные.");
            }
            paramTempsElements[i] = buf;
        }

        ListView listView = findViewById(R.id.template_paramList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, paramTempsElements);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                selectedParameter = position;
                showEnterParamDialog();
            }
        });
    }

    public void createnew_showImageMain(View view){
        EditText imgURL = findViewById(R.id.MainPicInput);
        if(imgURL.getText().toString().equals("")){
            Toast.makeText(this, "Введите ссылку.", Toast.LENGTH_SHORT).show();
            return;
        }
        GoToImg(imgURL.getText().toString(), "Основное изображение");
    }

    public void createnew_showImageGeom(View view){
        EditText imgURL = findViewById(R.id.GeomPicInput);
        if(imgURL.getText().toString().equals("")){
            Toast.makeText(this, "Введите ссылку.", Toast.LENGTH_SHORT).show();
            return;
        }
        GoToImg(imgURL.getText().toString(), "Геометрическое изображение");
    }

    public void createnew_showImageReg(View view){
        EditText imgURL = findViewById(R.id.RegPicInput);
        if(imgURL.getText().toString().equals("")){
            Toast.makeText(this, "Введите ссылку.", Toast.LENGTH_SHORT).show();
            return;
        }
        GoToImg(imgURL.getText().toString(), "Регулярное изображение");
    }

    public void createnew_showImageTepl(View view){
        EditText imgURL = findViewById(R.id.TeplPicInput);
        if(imgURL.getText().toString().equals("")){
            Toast.makeText(this, "Введите ссылку.", Toast.LENGTH_SHORT).show();
            return;
        }
        GoToImg(imgURL.getText().toString(), "Тепловое изображение");
    }

    private void GoToImg(String imglink, String description){

        if(URLUtil.isValidUrl(imglink)){
            Intent intent = new Intent(CreateNewActivity.this, ImageActivity.class);
            intent.putExtra("link", imglink);
            intent.putExtra("desc", description);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Введена неверная ссылка.", Toast.LENGTH_SHORT).show();
        }

    }

}



