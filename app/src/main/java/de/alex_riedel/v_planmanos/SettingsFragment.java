package de.alex_riedel.v_planmanos;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);


        //Summeries fuer die Listeneinstellungen setzen:
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        findPreference("klasse").setSummary(sharedPref.getString("klasse","Bitte auswählen"));


        String klasse = sharedPref.getString("klasse","n/A");
        String[] a = {"ast","bio","ch","de","en","eth","fr","geo","grw","ge","inf","ku","la","ma","mu","ph","spo", "re"}; //Fuer jede Einstellung (key)


        Boolean alleKurse = sharedPref.getBoolean("alleKurse",false);


        if (!(klasse.equals("JG11")||klasse.equals("JG12"))||alleKurse){   //Falls nicht JG11 oder JG12, sollen die Kurswahleinstelllungen ausgeblendet werden
            for (int i=0;i<a.length;i++){
                findPreference(a[i]).setSummary(sharedPref.getString(a[i],"Bitte auswählen"));
                findPreference(a[i]).setEnabled(false);
            }
            findPreference("kurswahl").setEnabled(false);       //Genauso wie deren Ueberschrift

        }else {
            for (int i=0;i<a.length;i++){
                findPreference(a[i]).setSummary(sharedPref.getString(a[i],"Bitte auswählen"));  //Sonst bleiben sie eingeblendet, nur die Summery wird gesetz
            }
        }

        if (!(klasse.equals("JG11")||klasse.equals("JG12"))){
            findPreference("alleKurse").setEnabled(false);
        }

    }
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {


    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        //Wenn die Einstellungen veraendert wurden, die Summery anpassen
        String[] a = {"ast","bio","ch","de","en","eth","fr","geo","grw","ge","inf","ku","la","ma","mu","ph","spo"};
        Preference chagedPref = findPreference(key);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            chagedPref.setSummary(sharedPreferences.getString(key,"FEHLER"));
        }catch (ClassCastException ecast){
            ecast.printStackTrace();
        }
        if (key.equals("klasse")){
            if (sharedPreferences.getString("klasse","n/A").contains("JG")){    //Wenn ein Jahrgang ausgewaehlt wurde
                findPreference("alleKurse").setEnabled(true);                   //... die Listeneinstellungen aktivieren

                if(!sharedPreferences.getBoolean("alleKurse",false)) {
                    for (int i = 0; i < 17; i++) {
                        findPreference(a[i]).setEnabled(true);
                    }
                }
                findPreference("kurswahl").setEnabled(true);
                editor.putBoolean("isKurs",true);                               //Und speichern, dass das Kurssystem genutzt wird
                editor.putBoolean("anderePause",false);                         //Und speichern, dass keine alternativen Pausen genutzt werden
            }else {
                findPreference("alleKurse").setEnabled(false);
                for (int i = 0; i < 17; i++) {
                    findPreference(a[i]).setEnabled(false);                 //...sonst die Listeneinstellugen deaktivieren
                }
                findPreference("kurswahl").setEnabled(false);
                editor.putBoolean("isKurs", false);                          //Und speichern, dass das Kurssystem nicht genutzt wird
                String kl = sharedPreferences.getString("klasse", "n/A");
                if (kl.contains("5") || kl.contains("6")) {    //Falls 5. oder 6. Klasse:
                    editor.putBoolean("anderePause", true);  //...alternative Pausen
                } else {
                    editor.putBoolean("anderePause", false);//...sonst normale Pausen
                }
            }
        }
        else if (key.equals("alleKurse")){
            if(sharedPreferences.getBoolean("alleKurse",false)) {
                for (int i = 0; i < 17; i++) {
                    findPreference(a[i]).setEnabled(false);
                }
                findPreference("kurswahl").setEnabled(false);
            }else {
                for (int i = 0; i < 17; i++) {
                    findPreference(a[i]).setEnabled(true);
                }
                findPreference("kurswahl").setEnabled(true);
            }
        }
        editor.apply();     //Editor speichern



    }
}
