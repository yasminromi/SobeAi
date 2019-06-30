package br.com.uberhack.testinho;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class SignUpApplication extends Application {

    private static SharedPreferences sPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sPreferences = getSharedPreferences("SignUpApplication", Context.MODE_PRIVATE);
    }

    public static String getUserName() {
        return sPreferences.getString("USER_NAME", "");
    }

    public static void setUserName( String name) {
        sPreferences
                .edit()
                .putString("USER_NAME", name)
                .apply();
    }

    public static String getPhone() {
        return sPreferences.getString("PHONE", "");
    }

    public static void setPhone (String phone) {
        sPreferences
                .edit()
                .putString("PHONE", phone)
                .apply();
    }

    public static String getPhoto() {
        return sPreferences.getString("PHOTO_URL", "");
    }

    public static void setPhoto (String photoURL) {
        sPreferences
                .edit()
                .putString("PHOTO_URL", photoURL)
                .apply();
    }

}
