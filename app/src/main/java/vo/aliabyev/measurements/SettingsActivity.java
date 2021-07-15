package vo.aliabyev.measurements;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import static vo.aliabyev.measurements.DatabaseActions.ResetDB;


//активность с настройками
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void ClearData(View view){
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Вы уверены?")
                .setMessage("Все данные из базы данных будут удалены. Удостоверьтесь, что вы сделали копию данных в меню \"Импорт/экспорт\". Продолжить?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    //добавление новых данных к существующим
                    ClearDataBase();
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
            Toast.makeText(this, "Не удалось очистить базу данных.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "База данных очищена.", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

}
