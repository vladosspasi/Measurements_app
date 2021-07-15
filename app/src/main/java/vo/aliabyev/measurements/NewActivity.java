package vo.aliabyev.measurements;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import static vo.aliabyev.measurements.DatabaseActions.*;

//TODO валидация строк везде - строки не могут быть длинее 100 символов

//активность выбора создания шаблона или его заполнения
public class NewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
    }

    public void GoToCreateFromExisted(View view){
        //Взять за основу готовый файл с таблицей и просто его заполнить
        //Переход на новую активити
        Intent intent = new Intent(NewActivity.this, CreateFromExistedActivity.class);
        startActivity(intent);
    }

    public void GoToCreateNew(View view){
        //Создать абсолютно новую таблицу и заполнить ее
        //Переход на новую активити
        Intent intent = new Intent(NewActivity.this, CreateNewActivity.class);
        startActivity(intent);
    }

}
