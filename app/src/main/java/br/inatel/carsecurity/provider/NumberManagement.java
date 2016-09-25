package br.inatel.carsecurity.provider;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Lucas on 25/09/2016.
 */

public class NumberManagement {
    SharedPreferences pref; //Criando SharedPreferences
    SharedPreferences.Editor editor; //Criando o editor do SharedPreferences
    Context _context; //Criando o context

    int PRIVATE_MODE = 0; //Setando o mode do SharedPreferences para private
    private static final String PREF_NAME = "CarNumber"; //Nome do arquivo do SharedPref
    public static final String KEY_CAR = "car"; //Número do carro

    public NumberManagement(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setCarNumber(String number){
        editor.putString(KEY_CAR, number);
        editor.commit();
    }

    public String getCarNumber(){
        return pref.getString(KEY_CAR, null);
    }
}
