package com.gestionafacilmozos;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.LeadingMarginSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gestionafacilmozos.api.requests.LoginRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.ActivityLoginBinding;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.gestionafacilmozos.repositories.UserRepository;
import com.gestionafacilmozos.utilities.LoadingDialogFragment;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoadingDialogFragment loading;
    private UserRepository userRepository;
    public LoginActivity(){
        userRepository=new UserRepository(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View root= binding.getRoot();
        setContentView(root);

        // Set hints with padding
        setHintWithPadding(binding.txtRuc, getString(R.string.login_ruc));
        setHintWithPadding(binding.txtUserName, getString(R.string.login_username));
        setHintWithPadding(binding.txtPassword, getString(R.string.login_password));

        // Add TextWatchers
        addPaddingTextWatcher(binding.txtRuc, true);
        addPaddingTextWatcher(binding.txtUserName, false);
        addPaddingTextWatcher(binding.txtPassword, false);

        binding.btnEnter.setOnClickListener(v->{
            LoginRequest request=new LoginRequest(
                    binding.txtRuc.getText().toString(),
                    binding.txtUserName.getText().toString(),
                    binding.txtPassword.getText().toString()
            );
            loading = new LoadingDialogFragment("Validando usuario...");
            loading.show(getSupportFragmentManager(), "loading");
            userRepository.login(request, new ResultCallback.Login() {
                @Override
                public void onSuccess(String token) {
                    saveToken(token);
                    loading.dismiss();
                    openActivity(MainActivity.class);
                }
                @Override
                public void onError(ErrorResponse errorMessage) {
                    loading.dismiss();
                    runOnUiThread(() -> {
                        if (errorMessage.getFrom().equals("server")) {
                            showAlert(errorMessage.getCode(),errorMessage.getMessage(),"Aceptar");
                        } else {
                            if (errorMessage.getCode().equals("no_internet_connection")) {
                                if (!isFinishing()) {
                                    showAlert(errorMessage.getCode(),errorMessage.getMessage(),"Reintentar");
                                }
                            } else {
                                showAlert("Error del sistema",errorMessage.getMessage(),"Ok");
                            }
                        }
                    });
                }
            });
        });
    }
    private void showAlert(String title,String message,String buttonTitle){
        AlertDialog alertDialog=new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonTitle, (dialog, which) -> {recreate();}).create();
        alertDialog.setOnShowListener(dialog->{
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.crimson_700));
        });
        alertDialog.show();
    }
    private void setHintWithPadding(View view, String hint) {
        SpannableString spannableString = new SpannableString(hint);
        spannableString.setSpan(new LeadingMarginSpan.Standard(50), 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (view instanceof EditText) {
            ((EditText) view).setHint(spannableString);
        }
    }

    private void addPaddingTextWatcher(EditText editText, boolean isNumeric) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (isNumeric) {
                    text = text.replaceAll("[^\\d]", "");
                }

                // Remove the listener to avoid infinite loop
                editText.removeTextChangedListener(this);

                // Create a SpannableString with the padding applied
                SpannableString spannable = new SpannableString(text);
                spannable.setSpan(new LeadingMarginSpan.Standard(50), 0, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                // Update the EditText with the SpannableString
                editText.setText(spannable);
                editText.setSelection(spannable.length());

                // Reattach the listener
                editText.addTextChangedListener(this);
            }
        });
    }
    private void saveToken(String token) {
        // Guardar el token en SharedPreferences
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply(); //
    }
    private void openActivity(Class<?> activity) {
        Intent mainIntent = new Intent(LoginActivity.this, activity);
        LoginActivity.this.startActivity(mainIntent);
        LoginActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}