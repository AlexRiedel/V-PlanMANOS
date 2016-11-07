package de.alex_riedel.v_planmanos;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class CheckService extends Service {


    Vplan vplan1;
    Vplan vplan2;




    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if (isOnline()) {
            vplan1 = new Vplan("vplan1", this);          //Variblen erzeugen
            vplan2 = new Vplan("vplan2", this);

            Calendar heute = Calendar.getInstance(); //Heutiges Datum
            Calendar date1 = vplan1.getCalendar();
            Calendar date2 = vplan2.getCalendar();


            if ((!(vergleiche(heute,date1) < 0))&(!(vergleiche(heute,date2) < 0))) {
                getData task = new getData();               //Aufgabe zum Herunterladen der Daten erzeugen
                task.execute();
            } else {
                if (vergleiche(date1,date2)>0) {
                    setNextService(1, vplan1);
                }else {
                    setNextService(1,vplan2);
                }
            }

        }else {
            setNextService(0,null);
        }


        return super.onStartCommand(intent, flags, startId);
    }


//Private Methoden

    private void setNextService(int what, Vplan vplan){
        //Bestimmt wann der Service das naechste mal getartet wird
        //0->nach der naechsten Stunde
        //1->am Tag des neuen Planes nach der 4. Stunde
        //2->morgen nach der 4. Stunde
        final AlarmManager m = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        final Intent i = new Intent(this, CheckService.class);
        SharedPreferences einstellungen = PreferenceManager.getDefaultSharedPreferences(this);
        PendingIntent service = PendingIntent.getService(this, 0, i, 0);
        Calendar time= Calendar.getInstance();

        int Minute;
        int Stunde;
        int next = 0;


        boolean abbruch = false;

        int[] endeStund = {0, 8, 9,10,11,12,13,14,15,15,16};    //Wann zB die 1. Stunde endet, in Uhrzeitstuden
        int[] endeMinut = {0,15,10,15,10, 5,42,15, 5,55,45};    //Wann zB die 1. Stunde endet, in Uhrzeitminuten

        if (einstellungen.getBoolean("anderePause",false)) {     //Die Mittagspause der 5. und 6. Klassen ist verschoben!
            endeMinut[6] = 20;
        } else {
            endeMinut[6] = 0;
        }




        Minute=time.get(Calendar.MINUTE);
        Stunde=time.get(Calendar.HOUR_OF_DAY);

        switch (what) {
            case 0: //nach der naechsten Stunde
                while (!abbruch) {
                    next++;
                    try {
                        if (Stunde == endeStund[next]) {
                            if (Minute <= endeMinut[next]) {
                                abbruch = true;
                            }
                        }
                        if (Stunde < endeStund[next]) {
                            abbruch = true;
                        }
                    } catch (IndexOutOfBoundsException ebound) {
                        next = 0;
                        abbruch = true;
                    }

                }


                if (next < 4 && next!=0) {
                    next = 4;
                }

                if (next > 10||next==0) {
                    if (time.get(Calendar.HOUR_OF_DAY)>21){
                        time.set(Calendar.HOUR_OF_DAY, endeStund[4]);
                        time.set(Calendar.MINUTE, endeMinut[4] + 2);
                        time.set(Calendar.SECOND,1);

                        time.add(Calendar.DAY_OF_YEAR, 1);
                    }else {
                        time.add(Calendar.HOUR_OF_DAY, 1);
                    }

                } else {
                    time.set(Calendar.HOUR_OF_DAY, endeStund[next]);
                    time.set(Calendar.MINUTE, endeMinut[next] + 2);
                    time.set(Calendar.SECOND,1);

                }
                break;
            case 1: //am Tag des neuen Planes nach der 4. Stunde

                time=vplan.getCalendar();
                time.set(Calendar.HOUR_OF_DAY,endeStund[4]);
                time.set(Calendar.MINUTE,endeMinut[4]+2);
                time.set(Calendar.SECOND,1);

                break;
            case 2://morgen nach der 4. Stunde
                time.set(Calendar.HOUR_OF_DAY,endeStund[4]);
                time.set(Calendar.MINUTE,endeMinut[4]+2);
                time.set(Calendar.SECOND,1);

                time.set(Calendar.DAY_OF_YEAR,time.get(Calendar.DAY_OF_YEAR)+1);
                break;
        }

        String s=time.getTime().toString();



        m.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), service);                        //Alarmmanger konfigurieren (=starten)


        Nachricht("Nächster:",s,5); //Zu debuging Zwecken

        stopSelf();                                                                             //Service beenden
    }

    private void Nachricht(String titel, String text, int id){
    //Erzeugt eine Pushup-Nachricht
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentInfo("\ud83d\ude16")
                        .setContentTitle(titel)
                        .setContentText(text)
                        .setContentIntent(pIntent)
                        .setSound(soundUri)
                        .setAutoCancel(true)
                        .setPriority(1);
        NotificationManager mNotifyMgr =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(id, mBuilder.build());

    }

    private void NachrichtLang(String titel, Vplan vplan, int id){
        //Zeigt den Vertretungsplan tw in einer Pushup-Nachricht
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.InboxStyle myStyle = new NotificationCompat.InboxStyle();
        myStyle.setSummaryText(vplan.getDatum());
        for (int i=1;i<=vplan.getAenAnzahl();i++){
            myStyle.addLine(vplan.gibAenderung(i));
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Vertretungsplan für " + vplan.getWochentag() + ":\n" + vplan.getTabelle())
                .setContentInfo("\ud83d\ude01")
                .setContentTitle(titel)
                .setContentIntent(pIntent)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setStyle(myStyle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        if (vplan.getAenAnzahl()>1){
            mBuilder.setContentText(vplan.getAenAnzahl() + " Änderungen für  " + vplan.getWochentag());
        } else {
            mBuilder.setContentText("Eine Änderung für  " + vplan.getWochentag());
        }

        NotificationManager mNotifyMgr =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(id, mBuilder.build());
    }

    private void setVplan(Vplan vplan){
        //Vplan der angezeigt werden soll auswerten
        vplan.aenderungfiltern(this.getApplicationContext());

        if (!vplan.isKeineAenderung()) {
            NachrichtLang("Neuer Vertretungsplan:",vplan, 1);
        } else {
            Nachricht("Neuer Vertretungsplan:", "Leider keine Änderung für dich :(", 1);
        }

        if (vplan.isZusInfoBool()){
            Nachricht("Zusätzliche Information:",vplan.getZusInfo(),2);
        }

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


        setNextService(1,vplan);
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

    private boolean isOnline() {
//        Prueft ob eine Netzwerkverbindung vorhanden ist
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


//Private Klasse(n):
    private class getData extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            URL url;
            HttpURLConnection verbindung;
            String zeile;
            String[] alles=new String[2000];
            int zeilennummer=1;



            try { //erzeugen der URL
//                url = new URL("http://manos-dresden.de/aktuelles/vplan.php");
                url = new URL("http://manos-dresden.de/aktuelles/vplan.php?view=print");
                verbindung = (HttpURLConnection) url.openConnection();
                try {
                    InputStream stream = new BufferedInputStream(verbindung.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));


                    while ((zeile=reader.readLine())!=null){
                        alles[zeilennummer]=zeile;
                        zeilennummer++;
                    }
                    reader.close();
                    stream.close();



                } finally{
                    verbindung.disconnect();
                }
            } catch (Exception ex0) {
                ex0.printStackTrace();
            }



            return alles;
        }
        @Override
        protected void onPostExecute(String[] alles) {

            Vplan vplan3=new Vplan("vplan3",alles,CheckService.this.getApplicationContext(),false);       //neuen Vplan aus heruntergeladenen Daten

            Calendar heute = Calendar.getInstance();                                          //Heutiges Datum
            Calendar date1 = vplan1.getCalendar();
            Calendar date2 = vplan2.getCalendar();
            Calendar date3;



            if (!vplan3.isProblem()){
                date3 = vplan3.getCalendar();                          //date3 auslesen

                if (vergleiche(heute,date3) < 0) {                    //Wenn Plan3 von (ueber-)morgen:
                    if (vergleiche(date1,date2) < 0) {                //...diesen als Plan1 bzw. Plan2 (jenachdem, welcher der beiden aelter ist) speichern und anzeigen
                        vplan1 = new Vplan("vplan1", alles,CheckService.this.getApplicationContext(),true);
                        setVplan(vplan1);
                    } else {
                        vplan2 = new Vplan("vplan2", alles,CheckService.this.getApplicationContext(),true);
                        setVplan(vplan2);
                    }
                }else{
                    setNextService(0,null);
                }


            }else {
                Nachricht("Fehler",vplan3.getFehler(),1);
                setNextService(2,null);
            }




        }
    }


}