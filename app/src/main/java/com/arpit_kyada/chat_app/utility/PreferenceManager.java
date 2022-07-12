package com.arpit_kyada.chat_app.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final SharedPreferences sp;
    public PreferenceManager(Context context)
    {
        sp = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public Boolean getBoolean(String key)
    {
        return sp.getBoolean(key,false);
    }

    public void setString(String key, String value)
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public String getString(String key)
    {
        return sp.getString(key,null);
    }

    public void clear()
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

}
