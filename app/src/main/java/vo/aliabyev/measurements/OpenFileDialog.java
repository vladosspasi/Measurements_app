package vo.aliabyev.measurements;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import java.util.ArrayList;

//диалог открытия файлов
public class OpenFileDialog extends DialogFragment {

    int selected = -1;
    String[] files;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Send the positive button event back to the host activity
                    if(selected==-1){
                        listener.onFileDialogNothingSelected(OpenFileDialog.this);
                    }else{
                        listener.onFileDialogPositiveClick(OpenFileDialog.this, selected, files);
                        dialog.cancel();
                    }
                }
                })
                .setTitle("Выберите нужный шаблон.")
                .setSingleChoiceItems(getFiles(getContext()), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected = which;
                        files = getFiles(getContext());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                });
        return builder.create();
    }

    //Получение списка
    public String[] getFiles(Context context){
        String[] filesArray = context.fileList();

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

        return filesArray;
    }

    //Интерфейс для передачи данных обратно
    public interface OpenFileDialogListener {
        void onFileDialogPositiveClick(DialogFragment dialog, int selected, String[] files);
        void onFileDialogNothingSelected(DialogFragment dialog);
    }

    //Обрабочик действий
    OpenFileDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Проверить, что активити включает в себя интерфейс
        try {
            listener = (OpenFileDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }

}



