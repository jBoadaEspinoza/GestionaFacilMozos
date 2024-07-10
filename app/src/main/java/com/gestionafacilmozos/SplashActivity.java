package com.gestionafacilmozos;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gestionafacilmozos.api.models.MenuItem;
import com.gestionafacilmozos.api.models.User;
import com.gestionafacilmozos.api.requests.LoginRequest;
import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.databinding.ActivitySplashBinding;
import com.gestionafacilmozos.repositories.ResultCallback;
import com.gestionafacilmozos.repositories.UserRepository;

import android.os.Handler;
import android.widget.Toast;


public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private ImageView[] circles;
    private int currentIndex = 0;
    private Handler handler = new Handler();
    private static final int SPLASH_DISPLAY_LENGTH = 5000; // Duración de la pantalla de splash (5 segundos)

    private SharedPreferences sharedPreferences;
    private UserRepository userRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        circles = new ImageView[]{
                binding.circle1,
                binding.circle2,
                binding.circle3,
                binding.circle4,
                binding.circle5
        };
        startAnimation();

        userRepository = new UserRepository(getBaseContext());
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String token = getToken();
                if(token!=null){
                    connectUsingToken(token);
                }else{
                    openActivity(LoginActivity.class);
                }
            }
        },SPLASH_DISPLAY_LENGTH);
    }
    private void connectUsingToken(String token){
        userRepository.getInfo(token, new ResultCallback.UserInfo() {
            @Override
            public void onSuccess(User user) {
                openActivity(MainActivity.class);
            }
            @Override
            public void onError(ErrorResponse errorMessage) {
                runOnUiThread(() -> {
                    if (errorMessage.getFrom().equals("server")) {
                        switch (errorMessage.getCode()) {
                            case "invalid_token":
                                openActivity(LoginActivity.class);
                                break;
                        }
                    } else {
                        if (errorMessage.getCode().equals("no_internet_connection")) {
                            if (!isFinishing()) { showAlert(errorMessage.getCode(),errorMessage.getMessage(),"Reintentar"); }
                        } else {
                            showAlert("Error del sistema",errorMessage.getMessage(),"Ok");
                        }
                    }
                });
            }
        });
    }
    private void showAlert(String title,String message,String buttonTitle){
        AlertDialog alertDialog=new AlertDialog.Builder(SplashActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonTitle, (dialog, which) -> {recreate();}).create();
        alertDialog.setOnShowListener(dialog->{
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(SplashActivity.this, R.color.crimson_700));
        });
        alertDialog.show();
    }
    private String getToken(){
        String token=sharedPreferences.getString("token", null);
        if(token!=null){
            return "Bearer " +token;
        }
        return token;
    }
    private void startAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentIndex == circles.length) {
                    currentIndex = 0;
                    for (ImageView circle : circles) {
                        circle.setImageResource(R.drawable.circle_off);
                    }
                } else {
                    circles[currentIndex].setImageResource(R.drawable.circle_on);
                    currentIndex++;
                }
                handler.postDelayed(this, 500);
            }
        }, 500);
    }
    private void openActivity(Class<?> activity) {
        Intent mainIntent = new Intent(SplashActivity.this, activity);
        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}