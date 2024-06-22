package com.gestionafacilmozos.utilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.gestionafacilmozos.R;
import com.gestionafacilmozos.databinding.FragmentLoadingDialogBinding;
public class LoadingDialogFragment extends DialogFragment {
    private FragmentLoadingDialogBinding binding;
    private String title;
    public LoadingDialogFragment(String title){
        this.title=title;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding= FragmentLoadingDialogBinding.inflate(inflater,container,false);
        binding.txt.setText(title);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null  && getDialog().getWindow()!=null){
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null){
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);
        }
    }

    public void dismiss() {
        if (getDialog() != null) {
            dismiss();
        }
    }
}
