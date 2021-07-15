package vo.aliabyev.measurements;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

//активность меню
public class MenuActivity extends AppCompatActivity {
    //Главное меню

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    //Переход к созданию нового измерения
    public void GoToNew(View view){
        Intent intent = new Intent(MenuActivity.this, NewActivity.class);
        startActivity(intent);
    }

    //Переход к списку измерений
    public void GoToMesList(View view){
        Intent intent = new Intent(MenuActivity.this, ListActivity.class);
        startActivity(intent);
    }

    //Переход к импорту/экспорту
    public void GoToImpExp(View view){
        Intent intent = new Intent(MenuActivity.this, ImpExpActivity.class);
        startActivity(intent);
    }

    //Переход к настройкам
    public void GoToSettings(View view){
        Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}