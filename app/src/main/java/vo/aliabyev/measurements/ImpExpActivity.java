package vo.aliabyev.measurements;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.*;

import static vo.aliabyev.measurements.DatabaseActions.*;

//активность импорта-экспорта
public class ImpExpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impexp);
    }

    //нажатие кнопки экспорт
    public void onExportClick(View view){

        //подключение к бд
        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);
        if(isDBEmpty(db)==1){
            Toast.makeText(this, "База данных пуста.", Toast.LENGTH_SHORT).show();
            return;
        }else if(isExperimentTableEmpty(db)==-1){
            Toast.makeText(this, "Ошибка чтения БД.", Toast.LENGTH_SHORT).show();
            return;
        }

        //получение объекта с содержимым бд
        JSONObject dbObj = GetDbContent(db);
        db.close();
        //имя файла
        String FILE_NAME = "MeasurementsDB_dump.json";

        //проверка, доступно ли хранилище
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "Нет доступа к хранилищу.", Toast.LENGTH_SHORT).show();
            return;
        }

        File file;
        //создание директории импорта
        File docsFolder = new File(getExternalFilesDir(null) + "/export");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdir();
        }
        if (isPresent) {
            //выбор имени файла
            String newFileName = FILE_NAME;
            file = new File(docsFolder.getAbsolutePath(),FILE_NAME);
            int i = 1;
            while (file.exists()){
                newFileName = "MeasurementsDB_dump" + i + ".json";
                file = new File(docsFolder.getAbsolutePath(),newFileName);
                i++;
            }

            FILE_NAME = newFileName;
        } else {
            Toast.makeText(this, "Не удалось создать файл.", Toast.LENGTH_SHORT).show();
            return;
        }

        //запись в файл
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, false);
            outputStream.write(dbObj.toString().getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка сохранения файла.", Toast.LENGTH_SHORT).show();
            return;
        }
        //результат
        new AlertDialog.Builder(ImpExpActivity.this)
                .setTitle("Готово!")
                .setMessage("Дамп базы данных сохранен в файле "+ FILE_NAME +". Его можно найти в Android/data/vo.aliabyev.measurements/files/export/.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    //нажатие кнопки импорт
    public void onImportClick(View view){
        MimeTypeMap.getSingleton().getMimeTypeFromExtension("json");
        Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filePickerIntent.setType("application/*");
        Intent i = Intent.createChooser(filePickerIntent, "Выберите файл для импорта.");
        startActivityForResult(i, 1);
    }

    //по выбору файла
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        String filePath;
        Uri uri;
        if(requestCode==1){
            if (resultCode == RESULT_OK)
            {
                filePath = data.getData().getPath();
                uri = data.getData();
            }else {
                Toast.makeText(this, "Ошибка.", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            Toast.makeText(this, "Файл не выбран.", Toast.LENGTH_SHORT).show();
            return;
        }

        //проверка, является ли JSON
        String[] filePathElements = filePath.split("\\.");
        if(!filePathElements[filePathElements.length-1].equalsIgnoreCase("json")){
            //если нет, то выход
            Toast.makeText(this, "Файл должен быть в формате JSON!", Toast.LENGTH_SHORT).show();
            return;
        }
        //если является, выбор, что дальше

        new AlertDialog.Builder(ImpExpActivity.this)
                .setTitle("Импорт данных.")
                .setMessage("При импорте все текущие данные будут заменены на данные из файла. Продолжить?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    //добавление новых данных к существующим
                    ClearDataBase();
                    AddFileContent(uri);
                })
                .setNeutralButton(R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    //удалить бд
    private void ClearDataBase(){
        Toast toast;
        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);
        if(!ResetDB(db)){
            Toast.makeText(this, "Ошибка! Не удалось очистить БД.", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    //добавить в бд содержимое файла
    private void AddFileContent(Uri fileURI){

        Toast toast;

        JSONObject obj;
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileURI);
                if(inputStream == null){
                    throw new Exception();
                }
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }

               obj = new JSONObject(total.toString());

            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "Ошибка чтения файла!", Toast.LENGTH_SHORT).show();
                return;
            }

        try {
            SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MesDB.db", MODE_PRIVATE, null);
            if(!AddFromJSON(db, obj)){
                Toast.makeText(this, "Ошибка добавления данных!", Toast.LENGTH_SHORT).show();
                db.close();
                return;
            }else{
                new AlertDialog.Builder(ImpExpActivity.this)
                        .setTitle("Импорт данных.")
                        .setMessage("Данные успешно добавлены.")
                        .setPositiveButton(R.string.ok, (dialog, which) -> {

                        })
                        .show();
            }
            db.close();
        } catch (SQLiteException ex) {
            Toast.makeText(this, "Ошибка добавления данных! " + ex.toString(), Toast.LENGTH_SHORT).show();
        }



    }
}
