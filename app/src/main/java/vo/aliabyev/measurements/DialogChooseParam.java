package vo.aliabyev.measurements;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import java.util.ArrayList;

//TODO засунуть этот диалог прямо в код

//диалог выбора параметров для добавления к измерению
public class DialogChooseParam extends DialogFragment {

    private int selected = -1;
    ArrayList<TemplateParam> templatesList;

    public void setParamsInfo(ArrayList<TemplateParam> list){
        this.templatesList = list;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(selected==-1){
                    listener.onDialogChooseParamNothingSelected();
                }else{
                    listener.onDialogChooseParamPositiveClick(selected);
                    dialog.cancel();
                }
            }
        })
                .setTitle("Выберите нужный параметр.")
                .setSingleChoiceItems(getParamNames(), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected = which;
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                });

        return builder.create();
    }

    private String[] getParamNames(){
        String[] nameList = new String[templatesList.size()];
        for(int i = 0; i< nameList.length; i++){
            nameList[i] = templatesList.get(i).getName();
        }
        return nameList;
    }

    //Интерфейс для передачи данных обратно
    public interface DialogChooseParamListener {
        void onDialogChooseParamPositiveClick(int selected);
        void onDialogChooseParamNothingSelected();
    }

    //Обрабочик действий
    DialogChooseParam.DialogChooseParamListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Проверить, что активити включает в себя интерфейс
        try {
            listener = (DialogChooseParam.DialogChooseParamListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement Listener");
        }
    }
}

