package vo.aliabyev.measurements;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import java.util.Objects;

//диалог ввода параметров
public class DialogEnterParam extends DialogFragment {

    private int selectedType = -1;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_enterparam, null);
        final TextView unitText = (TextView) view.findViewById(R.id.dialog_textUnit);
        final EditText nameInput = (EditText) view.findViewById(R.id.dialog_nameInput);
        final EditText shortnameInput = (EditText) view.findViewById(R.id.dialog_shortnameInput);
        final EditText unitInput = (EditText) view.findViewById(R.id.dialog_unitInput);
        final Spinner spinner = (Spinner) view.findViewById(R.id.dialog_typespinner);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(getContext(), R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = position;

                if(position == 0||position==2){
                    unitInput.setVisibility(View.VISIBLE);
                    unitText.setVisibility(View.VISIBLE);
                }else{
                    unitInput.setVisibility(View.INVISIBLE);
                    unitText.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    listener.onDialogParamPositiveClick(DialogEnterParam.this, nameInput.getText().toString(),
                            shortnameInput.getText().toString(), selectedType, unitInput.getText().toString());
                    dialog.cancel();
            }
        })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                })

                .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogParamDeleteClick();
                        dialog.cancel();
                    }
                });
        return builder.create();
    }


    //Интерфейс для передачи данных обратно
    public interface DialogEnterParamListener {
        void onDialogParamPositiveClick(DialogFragment dialog, String name, String shortname, int type, String unit);
        void onDialogParamDeleteClick();
    }

    //Обрабочик действий
    DialogEnterParam.DialogEnterParamListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Проверить, что активити включает в себя интерфейс
        try {
            listener = (DialogEnterParam.DialogEnterParamListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement Listener");
        }
    }

}
