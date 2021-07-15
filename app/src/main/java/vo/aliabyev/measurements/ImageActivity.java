package vo.aliabyev.measurements;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;

public class ImageActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //получение переданных данных
        Bundle arguments = getIntent().getExtras();
        String imgLink = arguments.getString("link");
        String imgDesc = arguments.getString("desc");

        TextView text = findViewById(R.id.image_text);

        //инициализация
        ImageView imageView = findViewById(R.id.image_imgView);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        //загрузка
        imageLoader.displayImage(imgLink, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String arg0, View arg1) {
                text.setText("Подождите...");
            }
            @Override
            public void onLoadingFailed(String arg0, View arg1, FailReason failed) {
                if(failed.getCause() instanceof FileNotFoundException){
                    text.setText("Ошибка. Файл не найден.");
                }
                if(failed.getCause() instanceof SocketTimeoutException){
                    text.setText("Ошибка. Потеряно подключение.");
                }else {
                    text.setText("Ошибка. Проверьте подключение к сети.");
                }
            }
            @Override
            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                text.setText(imgDesc);
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                text.setText("Загрузка отменена.");
            }
        });

    }

}
