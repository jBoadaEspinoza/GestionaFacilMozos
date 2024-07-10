package com.gestionafacilmozos;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gestionafacilmozos.api.responses.ErrorResponse;
import com.gestionafacilmozos.interfaces.OnBackPressedListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gestionafacilmozos.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static SharedPreferences sharedPreferences;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_room, R.id.navigation_history, R.id.navigation_explore)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                if(navDestination.getId()==R.id.navigation_order_ticket || navDestination.getId()==R.id.navigation_menu_items){
                    navView.setVisibility(View.GONE);
                }else{
                    navView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main);

        if (fragment != null && fragment.getChildFragmentManager().getFragments().get(0) instanceof OnBackPressedListener) {
            ((OnBackPressedListener) fragment.getChildFragmentManager().getFragments().get(0)).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
    public static String getToken(){
        return "Bearer " +  sharedPreferences.getString("token", null);
    }
    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void loadErrorMessageAlert(ErrorResponse errorMessage) {
        runOnUiThread(() -> {
            if (errorMessage.getFrom().equals("server")) {
                switch (errorMessage.getCode()) {
                    case "invalid_token":
                        showAlert(
                                errorMessage.getCode(),
                                errorMessage.getMessage(),
                                "Aceptar",
                                this::navigateToLogin);
                        break;
                    default:
                        showAlert(
                                "Error de servidor",
                                errorMessage.getMessage(),
                                "Aceptar",
                                null);
                        break;
                }
            } else {
                if (errorMessage.getCode().equals("no_internet_connection")) {
                    if (!isFinishing()) {
                        showAlert(
                                errorMessage.getCode(),
                                errorMessage.getMessage(),
                                "Reintentar",
                                this::recreate);
                    }
                } else {
                    showAlert(
                            "Error de sistema",
                            errorMessage.getMessage(),
                            "Aceptar",
                            this::recreate);
                }
            }
        });
    }
    private void showAlert(String alertTitle, String alertContentMessage, String alertButtonTitle, Runnable action) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(alertTitle)
                .setMessage(alertContentMessage)
                .setPositiveButton(alertButtonTitle, (dialog, which) -> {
                    if (action != null) {
                        action.run();
                    }
                }).create();
        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.crimson_700));
        });
        alertDialog.show();
    }
}