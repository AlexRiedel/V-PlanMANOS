package de.alex_riedel.v_planmanos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;


    private MyFragment fragment1;
    private MyFragment fragment2;

    private boolean isReloading = false;





//Oeffentliche Methoden:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //TODO Dafuer sorgen, dass die Fragmente wieder erkannt werden

        //Userfacesetup:

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);                                       //ViewPager einrichten

        if (viewPager != null) {
            setupViewPager(viewPager);
        }
                                                                                                    //Tablayout einrichten
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        //Beim ersten Start Dialog anzeigen
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String meiKlasse = sharedPref.getString("klasse", "13a");

        if (meiKlasse.equals("13a")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Es wird ausdrücklich darauf hingewiesen, dass die im Folgenden durch die App dargestellten Informationen in keinster Weise verbindlich sind und keinen Anspruch auf Vollständigkeit erheben. Verbindlich ist nur der jeweils im Schaukasten aushängende Vertretungsplan. Es liegt in der Verantwortung des Nutzers, die angezeigten Informationen auf Richtigkeit und Vollständigkeit zu überprüfen.\nDamit der Nutzerkomfort nicht zu hoch ist gibt es keine Anleitung für diese App. Es gilt hier \"lerning by ausprobiering\".\n\nFragen, Anregungen, Morddrohungen und Bugreports können über die Schaltfläche \"Kontakt\" per Email eingesendet werden. (Selbstverständlich wird nicht verraten, wo sich diese Schaltfläche befindet\uD83D\uDE09)")
                    .setPositiveButton("Ok, verstanden!\nJetzt Klasse wählen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setTitle("Warnung")
                    .setIcon(R.drawable.ic_warning_black_24dp);



            AlertDialog dialog = builder.create();
            dialog.show();


        }


        //VPlan auslesen:

        Vplan vplan1 = new Vplan("vplan1",this);
        Vplan vplan2 = new Vplan("vplan2",this);

        Calendar heute = Calendar.getInstance();    //Heutiges Datum
        Calendar date1 = vplan1.getCalendar();
        Calendar date2 = vplan2.getCalendar();


        //Pruefen, ob ein neuer Plan heruntergeladen werden muss
        if ((!(vergleiche(heute,date1) < 0))&(!(vergleiche(heute,date2) < 0))){
            if (isOnline()){
                herunterladen();    //Da keiner feur morgen verfuegbar ist, neuen herunterladen
            }else {
                setVplan(fragment2,null,"Keine Internetverbindung",0);    //Fehler anzeigen
            }
        }





    }

    public void onResume(){
        super.onResume();

        //VPlan auslesen:

        Vplan vplan1 = new Vplan("vplan1",this);
        Vplan vplan2 = new Vplan("vplan2",this);

        Calendar heute = Calendar.getInstance();    //Heutiges Datum
        Calendar date1 = vplan1.getCalendar();
        Calendar date2 = vplan2.getCalendar();


        //Vplan fuer linkes Fragment auswaehlen
        if (vergleiche(heute,date1)== 0){
            setVplan(fragment1,vplan1,null,0);
        }else if (vergleiche(heute,date2) == 0){
            setVplan(fragment1,vplan2,null,0);
        }else if (vergleiche(date1,date2)<= 0 && vergleiche(heute,date1) > 0){
            setVplan(fragment1,vplan1,null,0);
        }else if (vergleiche(date2,date1)<= 0 && vergleiche(heute,date2) > 0){
            setVplan(fragment1,vplan2,null,0);
        }else {
            setVplan(fragment1,null,"Keine Daten verfügbar",0);

        }

        //Vplan fuer rechtes Fragment auswaehlen
        if (vergleiche(heute,date1) < 0){
            setVplan(fragment2,vplan1,null,0);
        }else if (vergleiche(heute,date2) < 0){
            setVplan(fragment2,vplan2,null,0);
        }else if(fragment2.getVplan()!=null){   //Wenn ein Plan gesetzt ist, diesen loeschen:
            setVplan(fragment2,null,"kein Vertretungsplan verfügbar",0);
        }


    }

    public void herunterladen(){
        //Herunterladen eines neuen Planes
        if (isOnline()) {
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isReloading) {
                        Log.w("herunterladen", "is true");
                        fragment1.manRefrech();     //Ladekreieslanimation erst nach 1000 ms starten, wenn die Fragmente vollstaendig aufgebaut sind
                        fragment2.manRefrech();
                    }
                }
            }, 1000);

            getData task = new getData();       //Aufgabe zum Herunterladen der Daten erzeugen
            task.execute();                     //Aufgabe zum Herunterladen der Daten ausfueren
        }else {
            Toast toast = Toast.makeText(getApplicationContext(), "Keine Internetverbindung!", Toast.LENGTH_LONG);
            toast.show();                       //Fehler anzeigen

            fragment1.onItemsLoadComplete();    //Ladekreiselanimation soppen
            fragment2.onItemsLoadComplete();

        }

    }

    public void swipeToFrag2(){
        Handler h = new Handler();                  //nach 900 ms zum rechten Fragment swipen
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(1,true);
                tabLayout.setScrollPosition(1,0,true);
            }
        }, 900);

    }

    public void buttonRefresh(View v){
        //Methode, die der Butten aufruft
        if (isOnline()) {
            fragment1.manRefrech();
            fragment2.manRefrech();
        }
        herunterladen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {       //Service starten
            startService(new Intent(getApplicationContext(), CheckService.class));
            return true;

        }else if(id==R.id.action_settings){ //Einstellungen aufrufen
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_share){  //Teilen
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getShare());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent,"Test"));
        }else if (id==R.id.action_kontakt){ //Email
            Intent sendintent = new Intent(Intent.ACTION_SEND);
            sendintent.setType("message/rfc82");
            sendintent.putExtra(Intent.EXTRA_EMAIL, new String[]{"app@alex-riedel.de"});
            sendintent.putExtra(Intent.EXTRA_SUBJECT, "Feedback V-Plan MANOS");
            sendintent.putExtra(Intent.EXTRA_TEXT, "ich vinde deinen app fol schiese !!11!");

            startActivity(Intent.createChooser(sendintent, "Email senden"));
        }



        return super.onOptionsItemSelected(item);
    }




//Private Methoden
    private boolean isOnline() {
    //True, wenn Internetverbindung vorhanden
    ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnected();
}

    private String getShare(){
        //Gibt den Sharetext des aktuell sichtbaren Fragments zurueck
        if (viewPager.getCurrentItem()==0){
            return fragment1.getShare();
        }else{
            return fragment2.getShare();
        }
    }

    private int vergleiche(Calendar c1, Calendar c2){
        int unterschied;
        if (c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR)){
            unterschied=c1.get(Calendar.DAY_OF_YEAR)-c2.get(Calendar.DAY_OF_YEAR);
        }else {
            unterschied=c1.get(Calendar.YEAR)-c2.get(Calendar.YEAR);
        }
        return unterschied;
    }

    private void setVplan(final MyFragment fragment, final Vplan vplan, final String text, final int zaehler){
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {                 //100 ms warten, damit die Fragmente vollstaendig aufgebaut sind
                try {
                    fragment.setVplan(vplan);
                    if (text != null) {
                        fragment.setAlternativtext(text,true);
                        if (fragment.getName().equals("fragment2")) {    //Bei Fragment2 (rechts) Morgen in den Tab-Titel schreiben
                            int item = viewPager.getCurrentItem();      //Da der ViewPager neu aufgesetz wird muss zum aktuellen Fragment zurueck gesprungen wereden
                            adapter.changeTitle("Morgen", 1);
                            tabLayout.setupWithViewPager(viewPager);
                            viewPager.setCurrentItem(item, false);
                            tabLayout.setScrollPosition(item, 0, true);
                        }
                    }
                    if (fragment.getName().equals("fragment2")) {    //Bei Fragment2 (rechts) den Wochentag in den Tab-Titel schreiben
                        int item = viewPager.getCurrentItem();      //Da der ViewPager neu aufgesetz wird muss zum aktuellen Fragment zurueck gesprungen wereden
                        adapter.changeTitle(fragment2.getWochentag(), 1);
                        tabLayout.setupWithViewPager(viewPager);
                        viewPager.setCurrentItem(item, false);
                        tabLayout.setScrollPosition(item, 0, true);
                    }else if (vplan != null) {
                        Calendar heute = Calendar.getInstance();
                        Calendar date = vplan.getCalendar();
                        int item = viewPager.getCurrentItem();      //Da der ViewPager neu aufgesetz wird muss zum aktuellen Fragment zurueck gesprungen wereden
                        if (vergleiche(heute, date) > 0) {
                            adapter.changeTitle(fragment1.getWochentag(), 0);
                        }else {
                            adapter.changeTitle("Heute", 0);
                        }
                        tabLayout.setupWithViewPager(viewPager);
                        viewPager.setCurrentItem(item, false);
                        tabLayout.setScrollPosition(item, 0, true);
                    }
                }catch (NullPointerException enull){
                    enull.printStackTrace();
                    if (zaehler<2) {
                        setVplan(fragment, vplan, text, zaehler + 1);
                    }else{          //nach zwei Versuchen Neustart der App:
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                }
            }
        }, 100);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        fragment1 = new MyFragment();
        fragment2 = new MyFragment();
        fragment1.setName("fragment1");
        fragment2.setName("fragment2");

        adapter.addFragment(fragment1, "Heute");
        adapter.addFragment(fragment2, "Morgen");

        viewPager.setAdapter(adapter);

    }


//Private Klassen
    private class getData extends AsyncTask<Void, Void, String[]> {                                 //Laedt die Daten herunter

        @Override
        protected String[] doInBackground(Void... voids) {
            isReloading=true;

            URL url;                                                                                //Variablen erzeugen
            HttpURLConnection verbindung;
            String zeile;
            String[] alles=new String[2000];                                                        //Speicher für den heruntergeladenen HTML-Code
            int zeilennummer=1;

            alles[0]="";


            try { //erzeugen der URL
                url = new URL("http://manos-dresden.de/aktuelles/quellen/VPlan_Schueler.html");     //Neue Adresse
                verbindung = (HttpURLConnection) url.openConnection();                              //Starten der Verbindung
                verbindung.connect();
                alles[0]=String.valueOf(verbindung.getResponseCode());                              //Responsecode zur evt. Fehleranalyse speichern

                try {
                    InputStream stream = new BufferedInputStream(verbindung.getInputStream());      //Auslesen
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream,"iso-8859-1"));


                    while ((zeile=reader.readLine())!=null){
                        alles[zeilennummer]=zeile;
                        zeilennummer++;
                    }
                    reader.close();
                    stream.close();


                } finally{
                    verbindung.disconnect();                                                        //Verbindung beenden
                }
            } catch (Exception ex0) {
                ex0.printStackTrace();
                if (alles[0].equals("")) {                                                          //Falls kein Reponsecode vorhanden:
                    alles[0] = ex0.getMessage();                                                    //...Exceptionfehler speichern
                }
            }



            return alles;
        }
        @Override
        protected void onPostExecute(String[] alles) {
            //Vplan1 = 1. gespeicherter Plan            ->date1
            //Vplan2 = 2. gespeicherter Plan            ->date2
            //Vplan3 = soeben heruntergeladener Plan    ->date3

            isReloading=false;

            int item = viewPager.getCurrentItem();

            Vplan vplan1 = new Vplan("vplan1",MainActivity.this.getApplicationContext());
            Vplan vplan2 = new Vplan("vplan2",MainActivity.this.getApplicationContext());
            Vplan vplan3 = new Vplan("vplan3",alles,MainActivity.this.getApplicationContext(),false);       //neuen Vplan aus heruntergeladenen Daten



            Calendar heute = Calendar.getInstance(); //Heutiges Datum
            Calendar date1 = vplan1.getCalendar();
            Calendar date2 = vplan2.getCalendar();
            Calendar date3;


            if (!vplan3.isProblem()) {               //Falls kein Problem beim auslesen der Infos aus dem HTML-Code (zB Layoutaenderung) aufgetreten ist:

                date3 = vplan3.getCalendar();                                                       //date3 auslesen

                //Auswhal der beiden anzuzeigenden Plaene:

                if (vergleiche(heute, date3) < 0) {                            //Wenn Plan3 von (ueber-)morgen:
                    if (vergleiche(date1, date2) < 0) {                       //...diesen als Plan1 bzw. Plan2 (jenachdem, welcher der beiden aelter ist) speichern und anzeigen
                        vplan1 = new Vplan("vplan1", alles, MainActivity.this.getApplicationContext(),true);
                        setVplan(fragment2,vplan1,null,0);
                    } else {
                        vplan2 = new Vplan("vplan2", alles, MainActivity.this.getApplicationContext(),true);
                        setVplan(fragment2,vplan2,null,0);
                    }
                } else if (vergleiche(heute, date3) == 0) {                                     //Wenn Plan3 von heute
                    setVplan(fragment2,null,"Vertretungsplan noch nicht verfügbar",0);
                    if (vergleiche(date1, date2) < 0) {                //...diesen als Plan1 bzw. Plan2 (jenachdem, welcher der beiden aelter ist) speichern und anzeigen
                        vplan1 = new Vplan("vplan1", alles, MainActivity.this.getApplicationContext(),true);
                        setVplan(fragment1,vplan1,null,0);
                    } else {
                        vplan2 = new Vplan("vplan2", alles, MainActivity.this.getApplicationContext(),true);
                        setVplan(fragment1,vplan2,null,0);
                    }

                } else {
                    setVplan(fragment2,null,"Vertretungsplan noch nicht verfügbar",0);
                }




            }else { //Fehlerauswertung
                setVplan(fragment2,null,vplan3.getFehler(),0);
            }

            //Aktualisierungszeit speichern
            String uhrzeit;
            Calendar calendar = Calendar.getInstance();

            if (calendar.get(Calendar.MINUTE)<10){  //18:1 -> 18:01
                uhrzeit = calendar.get(Calendar.HOUR_OF_DAY)+":0"+calendar.get(Calendar.MINUTE)+" Uhr";
            }else {     //Die lezte Aktualisierungszeit wird gespeichert, um sie anzuzeigen
                uhrzeit = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + " Uhr";
            }

            SharedPreferences einstellungen = getApplicationContext().getSharedPreferences("setup",MODE_PRIVATE);  //Speichervariable erzeugen
            SharedPreferences.Editor editor =einstellungen.edit();

            editor.putString("uhrzeit",uhrzeit);
            editor.putInt("tag",calendar.get(Calendar.DAY_OF_MONTH));
            editor.putInt("jahrestag",calendar.get(Calendar.DAY_OF_YEAR));
            editor.putInt("monat",calendar.get(Calendar.MONTH));
            editor.putInt("jahr",calendar.get(Calendar.YEAR));
            editor.commit();    //soll sofort gepeichert werden


            fragment1.aktualisieren();


            adapter.changeTitle(fragment2.getWochentag(),1);                    //Tab-Titel setzen
            tabLayout.setupWithViewPager(viewPager);

            viewPager.setCurrentItem(item,false);
            tabLayout.setScrollPosition(item,0,true);


            fragment1.onItemsLoadComplete();                                                         //Ladekreiselanimation soppen
            fragment2.onItemsLoadComplete();

        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        void changeTitle(String title, int position){

            mFragmentTitleList.remove(position);
            mFragmentTitleList.add(position,title);
        }
    }


}