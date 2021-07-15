package vo.aliabyev.measurements;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import java.util.Objects;

//диалог ввода значения
public class DialogEnterValue extends DialogFragment {

    private TemplateParam template;
    private Parameter parameterToEdit;
    private boolean editing = false;

    public void setParamTemplate(TemplateParam temp){
        this.template = temp;
    }

    public void setParameterToEdit(Parameter param){
        this.parameterToEdit = param;
        this.template = param;
        this.editing = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_valueinput, null);

        final TextView textValue1 = (TextView) view.findViewById(R.id.dialogvalue_textValue1);
        final TextView textValue2 = (TextView) view.findViewById(R.id.dialogvalue_textValue2);
        final EditText value1 = (EditText) view.findViewById(R.id.dialogvalue_value1Input);
        final EditText value2 = (EditText) view.findViewById(R.id.dialogvalue_value2Input);

        textValue2.setVisibility(View.INVISIBLE);
        value2.setVisibility(View.INVISIBLE);

        if(template.getType()==0) {
            textValue1.setText("Значение:");
        }else if(template.getType()==1) {
            textValue1.setText("Строка:");
        }else if(template.getType()==2) {
            textValue1.setText("От:");
            textValue2.setText("До:");
            textValue2.setVisibility(View.VISIBLE);
            value2.setVisibility(View.VISIBLE);
        }else if(template.getType()==3){
            textValue1.setText("Ссылка:");
        }

        if(editing){
            String buf;
            if(template.getType()==0) {
                value1.setText(String.valueOf(parameterToEdit.getFloatValue()));
            }else if(template.getType()==1||template.getType()==3) {
                value1.setText(parameterToEdit.getStringValue());
            }else if(template.getType()==2) {
                value1.setText(String.valueOf(parameterToEdit.getRangeValue1()));
                value2.setText(String.valueOf(parameterToEdit.getRangeValue2()));
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(value1.getText().toString().equals("")){
                        listener.onValueDialogNothingSelected();
                }else if(template.getType()==0) {
                    try{
                        listener.onValueDialogPositiveClick(Float.parseFloat(value1.getText().toString()));
                    }catch (NumberFormatException e){
                        listener.onValueDialogWrongType();
                    }
                }else if(template.getType()==2) {

                    try{
                        listener.onValueDialogPositiveClick(Float.parseFloat(value1.getText().toString()), Float.parseFloat(value2.getText().toString()));
                    }catch (NumberFormatException e){
                        listener.onValueDialogWrongType();
                    }
                    listener.onValueDialogPositiveClick(Float.parseFloat(value1.getText().toString()), Float.parseFloat(value2.getText().toString()));
                }else if(template.getType()==1||template.getType()==3) {
                    listener.onValueDialogPositiveClick(value1.getText().toString());
                }
                dialog.cancel();
            }
        })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setTitle("Введите значение:");
        editing = false;
        return builder.create();
    }

    //Интерфейс для передачи данных обратно
    public interface  DialogEnterValueListener {
        void onValueDialogPositiveClick(String value);
        void onValueDialogPositiveClick(float value1);
        void onValueDialogPositiveClick(float value1, float value2);
        void onValueDialogNothingSelected();
        void onValueDialogWrongType();
    }

    //Обрабочик действий
    DialogEnterValueListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Проверить, что активити включает в себя интерфейс
        try {
            listener = ( DialogEnterValueListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NoticeDialogListener");
        }
    }
}

