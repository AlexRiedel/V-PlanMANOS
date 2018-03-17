package de.alex_riedel.v_planmanos;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Alexander on 29.10.2015.
 *
 */
class Vplan {


    private String datum="n/A";                                         //Datum des Vertretungsplans zB "Montag, 30. März 2015 (B-Woche)"
    private String veroefdat="n/A";                                     //Datum+Uhrzeit der Veroefentlichung zB "01.04.2015 09:54"
    private String abLehrer="n/A";                                      //Abweseende Lehrer zB "Gol, May, San"
    private String aenLeher="n/A";                                      //Lehrer mit Aenderung zB "Bec, Ber, Gos, Gru, Khn, Mic"
    private String aenKlassen="n/A";                                    //Klassen mit Aenderung zB "5a, 5b, 6a, 7a, 7b, 7c, 8c, 9a, JG11, JG12"
    private String zusInfo ="";                                         //"Zusaetzliche Informatioen"
    private String share ="";
    private String fehler="allgemeiner Fehler";



    private ArrayList<String> klasse = new ArrayList<>();                    //Spalte "Klasse/Kurs"
    private ArrayList<String> stunde = new ArrayList<>();                    //Spalte "Stunde"
    private ArrayList<String> fach = new ArrayList<>();                      //Spalte "Fach"
    private ArrayList<String> lehrer = new ArrayList<>();                    //Spalte "Lehrer"
    private ArrayList<String> raum = new ArrayList<>();                      //Spalte "Raum"
    private ArrayList<String> info = new ArrayList<>();                      //Spalte "Info"

    private boolean zusInfoBool =false;                                 //true, falls "Zusaetzliche Informatioen" vorhanden sind
    private boolean keineAenderung=true;                                //true, wenn keine Aenderungen fuer "meiKlasse" vorliegen
    private boolean problem =false;                                     //true, wenn der Vertretungsplan nicht ausgelesen werden kann

    private int letzteZeile=0;                                          //Letztes Zeichen bei einem Auslesevorgang
    private int letztesZeichen=0;                                       //Letzte Zeile bie einem Auslesevorgang
    private int aenAnzahl =0;                                           //Anzahl der Aenderungen fuer "MeiKlasse"

    private ArrayList<Integer> aenZeilen = new ArrayList<>();                             //Zeilen, in denen die Aenderungen fuer "MeiKlasse" stehen


    public static String PLANURL = "http://manos-dresden.de/man_vertretungsplan/VPlan_Schueler.html";
    //public static String PLANURL = "https://alex-riedel.de/16821611/654254621.html";

    Vplan(String dat, Context context){
//        SharedPreferences einstellungen = context.getSharedPreferences("einstellungen", Context.MODE_PRIVATE);   //Speichervariable erzeugen
//        MeiKlasse=einstellungen.getString("MeiKlasse", "Ladefehlerfehler: klasse");

        lesen(dat, context);
    }

    Vplan(String dat, String[] alles, Context context, boolean speichern){
//        SharedPreferences einstellungen = context.getSharedPreferences("einstellungen", Context.MODE_PRIVATE);   //Speichervariable erzeugen
//        MeiKlasse=einstellungen.getString("MeiKlasse", "Ladefehlerfehler: klasse");


        auswerten2(alles);
        if(!problem&&speichern) {
            speichern(dat, context);
        } else {
            SharedPreferences pref = context.getSharedPreferences(dat, Context.MODE_PRIVATE);  //Speichervariable erzeugen
            SharedPreferences.Editor Editor =pref.edit();              //Editor erzeugen
            Editor.putBoolean("problem", problem);                       //problem speichern
            Editor.apply();                                             //Editor abspeicher


        }
    }

    //Get-Methoden:
     String getDatum(){
        return datum;
    }

    public String getVeroefdat(){
        return veroefdat;
    }

    public String getAbLehrer(){
        return abLehrer;
    }

    public String getAenLeher(){
        return aenLeher;
    }

    public String getAenKlassen(){
        return aenKlassen;
    }

    String getZusInfo(){
        return zusInfo;
    }

    String getShare(){
        return share;
    }

//    public String[] getKlasse(){
//        return klasse;
//    }
//
//    public String[] getStunde(){
//        return stunde;
//    }

    int getAenStunde(int zaehler){
        return Integer.parseInt(stunde.get(aenZeilen.get(zaehler)));
    }

//    public String[] getFach(){
//        return fach;
//    }
//
//    public String[] getLehrer(){
//        return lehrer;
//    }
//
//    public String[] getRaum(){
//        return raum;
//    }
//
//    public String[] getInfo(){
//        return info;
//    }

    boolean isZusInfoBool(){
        return zusInfoBool;
    }

    boolean isKeineAenderung(){
        return keineAenderung;
    }

    boolean isProblem(){
        return problem;
    }

    String getFehler(){
        return fehler;
    }

    int getAenAnzahl(){
        return aenAnzahl;
    }


    //weitere oefentliche Methoden:
    Calendar getCalendar(){

        int Tag=31;                                 //(beim Auslesen des Datums des Vertretungsplans)
        int Jahr=2015;                              //in jedem Fall den Plan neu herunter zu laden

        boolean abbruch=false;                      //Abbruchvariable, um die Schleife abzubrechen

        Calendar time = Calendar.getInstance();
//        time.clear();

        if (!datum.equals("n/A")) {            //Falls eine Eintragung gefunden wurde:
            //Den Monat aus zB "Montag, 4.Mai 2015 (B-Woche)" wird ausgelesen
            if (datum.contains("Januar")) {
                time.set(Calendar.MONTH, 0);
            } else if (datum.contains("Februar")) {
                time.set(Calendar.MONTH, 1);
            } else if (datum.contains("März")) {
                time.set(Calendar.MONTH, 2);
            } else if (datum.contains("April")) {
                time.set(Calendar.MONTH, 3);
            } else if (datum.contains("Mai")) {
                time.set(Calendar.MONTH, 4);
            } else if (datum.contains("Juni")) {
                time.set(Calendar.MONTH, 5);
            } else if (datum.contains("Juli")) {
                time.set(Calendar.MONTH, 6);
            } else if (datum.contains("August")) {
                time.set(Calendar.MONTH, 7);
            } else if (datum.contains("September")) {
                time.set(Calendar.MONTH, 8);
            } else if (datum.contains("Oktober")) {
                time.set(Calendar.MONTH, 9);
            } else if (datum.contains("November")) {
                time.set(Calendar.MONTH, 10);
            } else if (datum.contains("Dezember")) {
                time.set(Calendar.MONTH, 11);
            }

            try {
                while (!abbruch) {//Den Tag auslesen:
                    if (datum.substring(0, 15).contains(String.valueOf(Tag))) {//Falls zB "Montag, 4.Mai" den Tag(start mit 31)enthaelt...
                        abbruch = true;                                        //...Schleife beenden
                    } else
                        Tag--;                                              //sonst: den naechst kleineren Tag versuchen
                    if (Tag < 0) {                                                //Falls Tag den zulaessigen Bereich verlaesst...
                        abbruch = true;                                          //...Schleife beenden und...
                        Tag = 1;                                                 //...den Tag auf 1 zu setzen (->datum liegt in Vergangenheit-> neues herunterladen des Plans)
                    }

                }


                abbruch = false;                //Damit die naechste Schleife durchgefuert werden kann
                while (!abbruch) {//Das Jahr auslesen:
                    if (datum.contains(String.valueOf(Jahr))) {//Falls zB "Montag, 4.Mai 2015 (B-Woche)" den das Jahr(start mit 2015)enthaelt...
                        abbruch = true;                        //...Schleife beenden
                    } else
                        Jahr++;                             //sonst: das naechst groessere Jahr versuchen
                    if (Jahr > 2100) {                            //Falls Jahr den realistischen Bereich verlaesst...
                        abbruch = true;                          //...Schleife beenden und...
                        Jahr = 2000;                             //...den Jahr auf 2000 zu setzen (->datum liegt in Vergangenheit-> neues herunterladen des Plans)
                    }
                }
            } catch (Exception edat) {
                Jahr = 2000;                                     //->datum liegt in Vergangenheit-> neues herunterladen des Plans
            }
            time.set(Calendar.DAY_OF_MONTH,Tag);
            time.set(Calendar.YEAR,Jahr);
        }else {
            time.setTimeInMillis(10000000);
        }

        long l=time.getTimeInMillis();
//        time = time.setTimeInMillis(l);

        Calendar time2=Calendar.getInstance();
        time2.setTimeInMillis(l);


        return time2;
    }

    String getWochentag(){
        String wochentag="Morgen";
        Calendar calendar=getCalendar();

        try {
            if (calendar.get(Calendar.YEAR)>1970) {
                switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                    case 1:
                        wochentag = "Sonntag";
                        break;
                    case 2:
                        wochentag = "Montag";
                        break;
                    case 3:
                        wochentag = "Dienstag";
                        break;
                    case 4:
                        wochentag = "Mittwoch";
                        break;
                    case 5:
                        wochentag = "Donnerstag";
                        break;
                    case 6:
                        wochentag = "Freitag";
                        break;
                    case 7:
                        wochentag = "Samstag";
                        break;
                }

                wochentag = wochentag + ", " + calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1);
            }else {
                wochentag="Heute";
            }


        }catch (NullPointerException e){
            wochentag="n/A";
        }


        return wochentag;
    }

    String[] getAenderung(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isKurs = sharedPref.getBoolean("isKurs",false);

        int zaehler=1;                      //zaehler auf 1 setzen
        String[] ausgabe=new String[50];
        share="Vertretungsplan für " + datum + ":\n\n";
        String kurs="";


        while (zaehler <= aenAnzahl) {
            if (isKurs){
                if (klasse.get(aenZeilen.get(zaehler)).length()>6) {
                    kurs = " (" + klasse.get(aenZeilen.get(zaehler)).substring(6) + ")";
                }else {
                    kurs =  " (" + klasse.get(aenZeilen.get(zaehler)) + ")";
                }
            }


            if ("".equals(raum.get(aenZeilen.get(zaehler)))||" ".equals(raum.get(aenZeilen.get(zaehler)))) {      //Falls kein Raum vorhanden ist:

                ausgabe[zaehler]= stunde.get(aenZeilen.get(zaehler)) + ". Stunde" + kurs + ":\n" +
                        "" + fach.get(aenZeilen.get(zaehler)) + " " + lehrer.get(aenZeilen.get(zaehler));

                share = share +
                        "" + stunde.get(aenZeilen.get(zaehler)) + ". Stunde" + kurs + ":\n" +
                        "" + fach.get(aenZeilen.get(zaehler)) + " " + lehrer.get(aenZeilen.get(zaehler));

            } else {                                        //Wenn ein raum vorhadne ist:

                ausgabe[zaehler]= stunde.get(aenZeilen.get(zaehler)) + ". Stunde" + kurs + ":\n" +
                        "" + fach.get(aenZeilen.get(zaehler)) + " " + lehrer.get(aenZeilen.get(zaehler)) + "; Raum: " + raum.get(aenZeilen.get(zaehler));

                share = share +
                        "" + stunde.get(aenZeilen.get(zaehler)) + ". Stunde" + kurs + ":\n" +
                        "" + fach.get(aenZeilen.get(zaehler)) + " " + lehrer.get(aenZeilen.get(zaehler)) + "; Raum: " + raum.get(aenZeilen.get(zaehler));
            }

            if (!"".equals(info.get(aenZeilen.get(zaehler)))) {      //Falls eine Info vorhanden ist:
                ausgabe[zaehler]=ausgabe[zaehler] + "\n" +
                        "" + "(" + info.get(aenZeilen.get(zaehler)) + ")";
                share = share + "\n" +
                        "" + "(" + info.get(aenZeilen.get(zaehler)) + ")" + "\n\n";
            }else{
                ausgabe[zaehler]=ausgabe[zaehler] + "\n";
                share = share + "\n\n";
            }

            zaehler++;
        }

        share = share.substring(0,share.length()-2);            //Die beiden letzten Leehrzeilen loeschen
        return ausgabe;
    }

    String getTabelle(){
        int zaehler=2;
        String ausgabe= stunde.get(aenZeilen.get(1)) + ".Std: " + fach.get(aenZeilen.get(1)) + " " + lehrer.get(aenZeilen.get(1)) + " ("+ info.get(aenZeilen.get(1))+")" ;

        while (zaehler<= aenAnzahl){
            ausgabe=ausgabe+"\n"+ stunde.get(aenZeilen.get(zaehler)) + ".Std: " + fach.get(aenZeilen.get(zaehler)) + " " + lehrer.get(aenZeilen.get(zaehler))/*+ " ("+info.get(aenZeilen.get(zaehler))+")"*/;
            zaehler++;
        }
        return ausgabe;
    }

    String gibAenderung(int stunde, Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isKurs = sharedPref.getBoolean("isKurs",false);
        boolean alleKurse = sharedPref.getBoolean("alleKurse",false);
        String kurs = "";
        String ausgabe = "";

        if (isKurs&&alleKurse){
            if (klasse.get(aenZeilen.get(stunde)).length()>6) {
                kurs = " (" + klasse.get(aenZeilen.get(stunde)).substring(6) + ")";
            }else {
                kurs =  " (" + klasse.get(aenZeilen.get(stunde)) + ")";
            }
        }

        ausgabe = this.stunde.get(aenZeilen.get(stunde)) + ".Std" + kurs + ": " + fach.get(aenZeilen.get(stunde)) + " " + lehrer.get(aenZeilen.get(stunde));

        if (!"".equals(info.get(aenZeilen.get(stunde)))) {
            ausgabe = ausgabe + " ("+ info.get(aenZeilen.get(stunde))+")";
        }

        return ausgabe;
    }


    void aenderungfiltern(Context context){
        //Filtert die Aenderungen nach meiKlasse und sortiert sie chronoligisch
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        int Zeile = 1;                                              //Zu ueberpruefende Zeile auf 1 setzen
        int zaehler;                                                //zahlt Schleifen durchlaeufe
        int Uebergabe;                                              //Zum tauschen der Werte zweier Variablen


        aenAnzahl = 0;
        aenZeilen.add(0, 0);

        boolean abbruch=false;                                      //zum abbrechen der Schleife
        boolean isKurs = sharedPref.getBoolean("isKurs",false);
        boolean alleKurse = sharedPref.getBoolean("alleKurse",false);

        String meiKlasse = sharedPref.getString("klasse", "13a");


        if (isKurs&&(!alleKurse)){
            String[] a = {"ast","bio","ch","de","en","eth","fr","geo","grw","ge","inf","ku","la","ma","mu","ph","spo","re"};
            String[] kurs=new String[a.length];

            for (int i=0;i<a.length;i++){
                kurs[i]=meiKlasse+"/ "+sharedPref.getString(a[i],"n/A");
            }

            while (!abbruch) {                                   //Auf Aenderungen von "meiKlasse" ueberpruefen
                if (Integer.parseInt(klasse.get(0)) < Zeile) {         //Wenn die Anzahl der Zeilen ueberschritten wird, dann...
                    abbruch = true;                               //..wird die Schleif beendet
                } else {
                    try {
                        for (int i=0;i<a.length;i++){
                            if (klasse.get(Zeile).contains(kurs[i])){
                                aenAnzahl++;                            //Aenderungsanzahl um eins erhoehen
                                aenZeilen.add(aenAnzahl, Zeile);           //Zeile mit Aenderung speichern
                            }

                        }
                        if (klasse.get(Zeile).equals(meiKlasse)){
                            aenAnzahl++;                            //Aenderungsanzahl um eins erhoehen
                            aenZeilen.add(aenAnzahl, Zeile);           //Zeile mit Aenderung speichern
                        }
                    } catch (Exception enull) {
//                        abbruch = true;
                    }
                    Zeile++;                                    //Zeile um 1 erhoehen
                }
            }
            abbruch = false;                                      //Abbruch feur die naechset Schleife false setzen

            keineAenderung = aenAnzahl <= 0;


        }else {
            while (!abbruch) {                                   //Auf Aenderungen von "meiKlasse" ueberpruefen
                if (Integer.parseInt(klasse.get(0)) < Zeile) {         //Wenn die Anzahl der Zeilen ueberschritten wird, dann...
                    abbruch = true;                               //..wird die Schleif beendet
                } else {
                    try {
                        while (klasse.get(Zeile).contains(meiKlasse)) { //So lange die klasse in der Zeile "meiKlasse" enthaelt
                            aenAnzahl++;                            //Aenderungsanzahl um eins erhoehen
                            aenZeilen.add(aenAnzahl, Zeile);           //Zeile mit Aenderung speichern
                            Zeile++;                                //Zeile um 1 erhoehen
                        }
                    } catch (Exception enull) {
                        abbruch = true;
                    }

                    Zeile++;                                    //Zeile um 1 erhoehen
                }
            }
            abbruch = false;                                      //Abbruch feur die naechset Schleife false setzen

            keineAenderung = aenAnzahl <= 0;
        }


        while (!abbruch) {              //nach stunde sortierne (kleinste zu erst)
            zaehler = 1;                  //zaehler auf 1 setzen
            abbruch = true;               //Schleife wird abgebrochen, falls "abbruch" nicht mehr geaendert wird
            while (zaehler < aenAnzahl) { //Feur alle Zeilen:
                if (Integer.parseInt(stunde.get(aenZeilen.get(zaehler))) > Integer.parseInt(stunde.get(aenZeilen.get(zaehler + 1)))) {  //falls die ehere Zahl groesser als die nachfolgende:
                    Uebergabe = aenZeilen.get(zaehler);             //Vertauschen der eheren mit der nachfogenden Zahl;
                    aenZeilen.set(zaehler,aenZeilen.get(zaehler + 1));
                    aenZeilen.set((zaehler + 1),Uebergabe);
                    abbruch = false;                              //Da eine Umsortierung vorgenommen wurde, wird die "erste" Schleife noch Einmal ausgefuert
                }                                               //d.h. Ende der "ersten" Schleife nur wenn bei einem Durchgang keine Umsortierung vorgneommen wurde
                zaehler++;                                      //zaehler um 1 erhoehen
            }

        }
    }

    //Private Methoden:

    private void speichern (String dat, Context context){
//        Speichert datum, abLehrer, aenLeher, aenKlassen, zusInfo, keineAenderung, zusInfoBool, aenAnzahl, klasse[], stunde[], fach[], lehrer[], raum[] und info[]

        SharedPreferences heute = context.getSharedPreferences(dat, Context.MODE_PRIVATE);  //Speichervariable erzeugen
        SharedPreferences.Editor editor =heute.edit();              //Editor erzeugen
        int anzahl= Integer.parseInt(klasse.get(0))+1;                   //anzahl +1 berechen;--> wie viele Daten gespeichert werden muessen
        int zaehler=0;                                              //zaehler 0 setzen
        String name;                                                //Name, unter dem gespeichert wurde

        while (anzahl!=zaehler){                                    //klasse[] speichern
            name="klasse"+ String.valueOf(zaehler);                  //Name erzeugen
            editor.putString(name, klasse.get(zaehler));                 //Speichern
            zaehler++;                                              //zaehler um 1 erhoehen
        }
        zaehler=0;                                                  //zahler auf 0 setzen

        while (anzahl!=zaehler){                                    //stunde[] speichern
            name="stunde"+ String.valueOf(zaehler);                  //Name erzeugen
            editor.putString(name, stunde.get(zaehler));                 //Speichern
            zaehler++;                                              //zaehler um 1 erhoehen
        }
        zaehler=0;                                                  //zahler auf 0 setzen

        while (anzahl!=zaehler){                                    //fach[] speichern
            name="fach"+ String.valueOf(zaehler);                    //Name erzeugen
            editor.putString(name, fach.get(zaehler));                   //Speichern
            zaehler++;                                              //zaehler um 1 erhoehen
        }
        zaehler=0;                                                  //zahler auf 0 setzen

        while (anzahl!=zaehler){                                    //Lerher[] speichern
            name="lehrer"+ String.valueOf(zaehler);                  //Name erzeugen
            editor.putString(name, lehrer.get(zaehler));                 //Speichern
            zaehler++;                                              //zaehler um 1 erhoehen
        }
        zaehler=0;                                                  //zahler auf 0 setzen

        while (anzahl!=zaehler){                                    //raum[] speichern
            name="raum"+ String.valueOf(zaehler);                    //Name erzeugen
            editor.putString(name, raum.get(zaehler));                   //Speichern
            zaehler++;                                              //zaehler um 1 erhoehen
        }
        zaehler=0;                                                  //zahler auf 0 setzen

        while (anzahl!=zaehler){                                    //info[] speichern
            name="info"+ String.valueOf(zaehler);                    //Name erzeugen
            editor.putString(name, info.get(zaehler));                   //Speichern
            zaehler++;                                              //zaehler um 1 erhoehen
        }



        editor.putString("datum", datum);                            //datum speichern
        editor.putString("abLehrer", abLehrer);                      //Abwesende Leher speichern
        editor.putString("aenLeher", aenLeher);                      //lehrer mit Aenderung speichern
        editor.putString("aenKlassen", aenKlassen);                  //Klassen mit Aenderung speichern
        editor.putString("zusInfo", zusInfo);                        //Zusaetzliche Informationen speichern
        editor.putString("veroefdat", veroefdat);                    //datum+Uhrzeit der Veroefentlichung speichern
        editor.putString("fehler", fehler);                          //fehler speichern

        editor.putBoolean("keineAenderung", keineAenderung);         //keineAenderung speichern
        editor.putBoolean("zusInfoBool", zusInfoBool);               //zusInfoBool speichern
        editor.putBoolean("problem", problem);                       //problem speichern

        editor.putInt("aenAnzahl", aenAnzahl);                       //Anzahl der Aenderunge speichern

        editor.apply();                                              //Editor abspeicher


    }

    private void lesen (String dat,Context context){
//        Liest die gespeicherten Daten (datum, abLehrer, aenLeher, aenKlassen, zusInfo, keineAenderung, zusInfoBool, aenAnzahl, klasse[], stunde[], fach[], lehrer[], raum[] und info[]) aus.

        SharedPreferences pref = context.getSharedPreferences(dat, Context.MODE_PRIVATE);  //Speichervariable erzeugen

        klasse.add(0, pref.getString("klasse0","0"));           //Anzahl der Zeilen auslesen
        int anzahl= Integer.parseInt(klasse.get(0))+1;           //anzahl +1 berechen;--> wie viele Daten ausgelesn werden muessen
        int zaehler=1;                                      //zaehler auf 1 setzen (klasse[0] wurde bereits ausgelesen)
        String name;                                        //Name, unter dem gespeichert wurde

        while (anzahl!=zaehler){                            //Auslesen der klasse
            name="klasse"+ String.valueOf(zaehler);          //Name erzeugen
            klasse.add(zaehler, pref.getString(name,"13b"));    //Auslesen
            zaehler++;                                      //zaehler um 1 erhoehen
        }
        zaehler=0;                                          //zaehler 0 setzen

        while (anzahl!=zaehler){                            //Auslesen der stunde
            name="stunde"+ String.valueOf(zaehler);          //Name erzeugen
            stunde.add(zaehler, pref.getString(name,"0"));      //Auslesen
            zaehler++;                                      //zaehler um 1 erhoehen
        }
        zaehler=0;                                          //zaehler 0 setzen

        while (anzahl!=zaehler){                            //Auslesen des Fachs
            name="fach"+ String.valueOf(zaehler);            //Name erzeugen
            fach.add(zaehler, pref.getString(name,"Bsp"));      //Auslesen
            zaehler++;                                      //zaehler um 1 erhoehen
        }
        zaehler=0;                                          //zaehler 0 setzen

        while (anzahl!=zaehler){                            //Auslesen des Lerhers
            name="lehrer"+ String.valueOf(zaehler);          //Name erzeugen
            lehrer.add(zaehler, pref.getString(name,"Kei"));    //Auslesen
            zaehler++;                                      //zaehler um 1 erhoehen
        }
        zaehler=0;                                          //zaehler 0 setzen

        while (anzahl!=zaehler){                            //Auslesen des Raums
            name="raum"+ String.valueOf(zaehler);            //Name erzeugen
            raum.add(zaehler, pref.getString(name,"042"));      //Auslesen
            zaehler++;                                      //zaehler um 1 erhoehen
        }
        zaehler=0;                                          //zaehler 0 setzen

        while (anzahl!=zaehler){                            //Auslesen der info
            name="info"+ String.valueOf(zaehler);            //Name erzeugen
            info.add(zaehler, pref.getString(name, "Beispielstunde"));//Auslesen
            zaehler++;                                      //zaehler um 1 erhoehen
        }



        datum =pref.getString("datum","n/A");               //datum auslesen
        abLehrer =pref.getString("abLehrer","n/A");         //Abwesende Leher auslesen
        aenLeher =pref.getString("aenLeher","n/A");         //Leherer mit Aenderung auslesen
        aenKlassen =pref.getString("aenKlassen","n/A");     //Klassen mit Aenderung auslesen
        zusInfo =pref.getString("zusInfo","n/A");           //Zusaetzliche Informationen auslesen
        veroefdat =pref.getString("veroefdat","n/A");       //datum+Uhrzeit der Veroeffentlichung auslesen
        fehler =pref.getString("fehler","Keine Daten verfügbar");//Fehler auslesen

        keineAenderung=pref.getBoolean("keineAenderung", true); //keineAenderugn auslesen
        zusInfoBool =pref.getBoolean("zusInfoBool",true);   //zusInfoBool auslesen
        problem =pref.getBoolean("problem",true);          //problem auslesen


        aenAnzahl =pref.getInt("aenAnzahl",0);              //Anzahl der Aenderung  auslesen

//        if (dat.equals("vplan2")){
//            datum="Freitag, 9. September 2016 (A-Woche)";
//            Log.i("VPlan","Datum ist Freitag");
//        }


    }

    private void auswerten2(String alles[]){
        //        alles[] wird ausgewertet: Variablen datum, abLehrer, aenLeher, aenKlassen, klasse[], stunde[], fach[], lehrer[], raum[] und info[] werden ausgelesen.

        //Setup des Indexes [0] mit Werten, damit diese ungleich null sind
        klasse.add(0, "13b");
        stunde.add(0, "0");
        fach.add(0, "Bei");
        lehrer.add(0, "Kei");
        raum.add(0, "042");
        info.add(0, "Beispielstunde");

        aenAnzahl =0;

        Calendar c = Calendar.getInstance();
        int jahr = c.get(Calendar.YEAR);




        if (alles[1]==null){
            problem=true;
            fehler="Verbindungsfehler\n" +alles[0];
        }else {
            int zeile=zeileenthaelt(alles, 4, ""+(jahr+1));     //Die erste relevante Zeile wird gesucht
            if (zeile==-1){
                zeile = zeileenthaelt(alles, zeile+1,""+jahr);
            }

            if (zeile==-1){
                problem=true;
            }else {
                datum = zeileauslesen(alles, zeile,0);
            }


            zeile = zeileenthaelt(alles, zeile+1,""+jahr);
            if (zeile==-1){
                zeile = zeileenthaelt(alles, 4,""+(jahr-1));
            }
            if (zeile==-1) {
                problem=true;
            }else {
                veroefdat = zeileauslesen(alles, zeile,0);
            }

            if(!problem) {
                zeile = zeilefinden(alles, 10, "Lehrer mit Änderung:");
                aenLeher = zeileauslesen(alles, zeile + 1, 0);

                zeile = zeilefinden(alles, 10, "Klassen mit Änderung:");
                aenKlassen = zeileauslesen(alles, zeile + 1, 0);

                zeile = zeilefinden(alles, 10, "Abwesende Lehrer:");
                abLehrer = zeileauslesen(alles, zeile + 1, 0);

                zeile = zeilefinden(alles, 10, "Info");
                aenlesen2(alles, zeile + 3);


                zeile = zeilefinden(alles, zeile, "Zusätzliche Informationen:");
                if (zeile != -1) {
                    String ausgabe = "";
                    do {
                        zusInfo = zusInfo + "\n" + ausgabe;
                        ausgabe = zeileauslesen(alles, zeile + 3, 0);
                        zeile = zeile + 3;
                    } while (!ausgabe.equals("IndexOutOfBoundsException"));

                    zusInfo = zusInfo.substring(2);
                    zusInfoBool = true;
                }
            }else {
                fehler="Fehler beim auswerten, bitte manos-dresden.de/vertretungsplan-schueler aufsuchen. Sorry, ich arbeite an einer Lösung;)";
            }





        }



    }

    private void aenlesen2(String[]alles, int zeile){
        int aenNummer=1;
        while (alles[zeile].length()>20){
            klasse.add(aenNummer, zeileauslesen(alles,zeile,0));
            stunde.add(aenNummer, zeileauslesen(alles,zeile+1,0));
            fach.add(aenNummer, zeileauslesen(alles,zeile+2,0));
            lehrer.add(aenNummer, zeileauslesen(alles,zeile+3,0));
            raum.add(aenNummer, zeileauslesen(alles,zeile+4,0));
            info.add(aenNummer, zeileauslesen(alles,zeile+5,0));

            if(stunde.get(aenNummer).contains("-")){
                String stunden[] = stunde.get(aenNummer).split("-");
                int anz = Integer.parseInt(stunden[1])-Integer.parseInt(stunden[0]);

                stunde.set(aenNummer,stunden[0]);
                for (int i=1;i<=anz;i++){
                    klasse.add((aenNummer+i), klasse.get(aenNummer));
                    stunde.add((aenNummer+i), (Integer.parseInt(stunden[0])+i)+"");
                    fach.add((aenNummer+i), fach.get(aenNummer));
                    lehrer.add((aenNummer+i), lehrer.get(aenNummer));
                    raum.add((aenNummer+i), raum.get(aenNummer));
                    info.add((aenNummer+i), info.get(aenNummer));
                }
                aenNummer=aenNummer+anz;
            }


            aenNummer++;
            zeile=zeile+8;
        }
        klasse.set(0, (""+(aenNummer-1)));
    }

    private void auswerten(String[]alles){
//        alles[] wird ausgewertet: Variablen datum, abLehrer, aenLeher, aenKlassen, klasse[], stunde[], fach[], lehrer[], raum[] und info[] werden ausgelesen.

        //Setup des Indexes [0] mit Werten, damit diese ungleich null sind
        klasse.add(0, "13b");
        stunde.add(0, "0");
        fach.add(0, "Bei");
        lehrer.add(0, "Kei");
        raum.add(0, "042");
        info.add(0, "Beispielstunde");

        Calendar c = Calendar.getInstance();
        int jahr = c.get(Calendar.YEAR);



        aenAnzahl =0;

        int Zeile=zeilefinden(alles, 100, "Vertretungsplan für");     //Die erste relevante Zeile wird gesucht
//        int Zeile=zeilefinden(alles,270,"Vertretungsplan für");


        if (alles[1]==null){
            problem=true;
            fehler="Verbindungsfehler\n" +alles[0];
        }else {

            if (Zeile != -1) {

                problem = false;


                datum = zeileauslesen(alles, Zeile, letztesZeichen + 20);           //datum wird ausgelesen
                Zeile = zeileenthaelt(alles, Zeile, ""+jahr);                       //naechste relevante Zeile wird gesucht


                if (Zeile != -1) {

                    problem = false;

                    veroefdat = zeileauslesen(alles, Zeile, 10);
                    Zeile = zeilefinden(alles, Zeile, "Abwesende Lehrer:");

                    if (Zeile == -1) {
                        abLehrer = "n/A";      //abLehrer wird festgeleget
                    }else {
                        abLehrer = zeileauslesen(alles, Zeile, letztesZeichen + 20);      //abLehrer wird ausgelesen

                    }


                    Zeile = zeilefinden(alles, Zeile, "Lehrer mit Änderung:");      //naechste relevante Zeile wird gesucht

                    if (Zeile == -1) {
                        aenLeher = "n/A";   //aenLehrer wird festgelegt
                    }else {
                        aenLeher = zeileauslesen(alles, Zeile, letztesZeichen + 20);      //aenLeher wird ausgelesen
                    }


                    Zeile = zeilefinden(alles, Zeile, "Klassen mit Änderung:");     //naechste relevante Zeile wird gesucht

                    if (Zeile == -1) {
                        aenKlassen = "n/A";    //aenKlassen wird festgelgt
                    }else {
                        aenKlassen = zeileauslesen(alles, Zeile, letztesZeichen + 20);    //aenKlassen wird ausgelesen

                    }


                    Zeile = zeilefinden(alles, Zeile, "Klasse/Kurs");               //naechste relevante Zeile wird gesucht

                    if (Zeile == -1){
                                //es wurden keine Aenderungen gefunden (Fehler?)
                        Zeile = zeilefinden(alles, letzteZeile, "Zusätzliche Informationen:");

                        //Ueberpruefen auf Zusaetzliche Informationen, dh. es gibt wirklich keine Aenderungen (hoffenltlich kein Fehler!)
                        if (Zeile != -1) {
                            zusInfoBool = true;                                                                     //Vermerken, dass es Zusaetzliche Informationen gibt
                            zusInfo = zeileauslesen(alles, Zeile + 6, 10);                                          //Zusaetzliche Informationen auslesen

                        } else {    //da es auch keine Zusaetzklichen Infos gibt, liegt wirklich ein Fehler vor
                            zusInfoBool = false;
                            zusInfo = "";
                            problem = true;
                            fehler = "Leider ist eine Fehler beim Auswerten des Planes aufgetaucht. Bitte die Internetseite (manos-dresden.de/aktuelles/vplan.php) nutzen.\n\nSorry!";
                        }
                    }else {
                        //Aenderungen auslesen
                        aenlesen(alles, Zeile + 9, 1);                                 //klasse[], stunde[], fach[], lehrer[], raum[] und info[] werden ausgelesen

                        if (-1 != zeilefinden(alles, letzteZeile, "Vertretungsplan für")) { //Falls Seite zwei:
                            Zeile = zeilefinden(alles, letzteZeile, "Vertretungsplan für");
                            Zeile = zeileenthaelt(alles, Zeile + 1, "2016");
                            aenlesen(alles, Zeile + 9, Integer.valueOf(klasse.get(0)) + 1);
                        }

                        //TODO dritte Seite ?

                        Zeile = zeilefinden(alles, letzteZeile, "Zusätzliche Informationen:");

                        //Ueberpruefen auf Zusaetzliche Informationen
                        if (Zeile != -1) {
                            zusInfoBool = true;                                                                     //Vermerken, dass es Zusaetzliche Informationen gibt
                            zusInfo = zeileauslesen(alles, Zeile + 6, 10);                                          //Zusaetzliche Informationen auslesen

                        } else {
                            zusInfoBool = false;
                            zusInfo = "";
                        }
                    }



                } else {
                    problem = true;//Der Plan konnte nicht ausgelesen werden
                    fehler = "Leider ist eine Fehler beim Auswerten des Planes aufgetaucht. Bitte die Internetseite (manos-dresden.de/aktuelles/vplan.php) nutzen.\n\nSorry!";
                }
            } else {
                problem = true;   //Der Plan konnte nicht ausgelesen werden
                fehler = "Leider ist eine Fehler beim Auswerten des Planes aufgetaucht. Bitte die Internetseite (manos-dresden.de/aktuelles/vplan.php) nutzen.\n\nSorry!";

            }
        }

    }

    private void aenlesen(String[]alles, int zeile,int zeilnummer){
//        Liest ab Zeile "Zeile" klasse[], stunde[], fach[], lehrer[], raum[] und info[] aus "alles[]" aus und speichert sie zb als klasse [zeilnummer) bzw klasse [zeilnummer+1],....
        String umlaut;          //notwendig, um Sonderzeichen zu erkennen
        String Ausgabe;         //????
        boolean abbruch=false;  //true, wenn die Schleife abgebrochen werden soll
        int index;              //Zeichen, dass gerade ueberprueft wird
        int spalte=0;           //Spalte: 0->klasse[],1->stunde[],...,5->info

        while (!abbruch) {      //Fuer jede zu ueberpruefende Zeile:
            index=10;           //Zeichen von 0-9 werden nicht Ueberprueft
            while (spalte < 6) {//Fuer jede Spalte:
                Ausgabe = "";   //
                try {           //Damit ein Fehler abgefanden wird
                    while (alles[zeile].charAt(index) != ">".charAt(0)) {               //Solange das Zeichen nicht">" ist...
                        index++;                                                        //...das naechste Zeichen versuchen
                    }
                    index++;                                                            //index um 1 erhoehen(">"soll nicht ausgelesen werden!)
                    while ((alles[zeile].charAt(index)) != ("<".charAt(0))) {           //Solangen der Buschstaben nicht "<" ist:
                        if (alles[zeile].charAt(index) == "&".charAt(0)) {              //Auf Sonderzeichen ueberpruefen(beginnen immer mit "&")
                            try {                                                       //Damit ein Fehler abgefanden wird
                                umlaut = alles[zeile].substring(index + 1, index + 6);  //Substring erzeugen, der alle am Sonderzeichen beteiligten Buchstaben (ausser"&") enthaelt
                                if (umlaut.equals("auml;")) {                           //Fuer ä:
                                    Ausgabe = Ausgabe + "ä";
                                    index = index + 6;
                                } else {
                                    if (umlaut.equals("ouml;")) {                       //Fuer ü:
                                        Ausgabe = Ausgabe + "ö";
                                        index = index + 6;
                                    } else {
                                        if (umlaut.equals("uuml;")) {                   //Fuer ö:
                                            Ausgabe = Ausgabe + "ü";
                                            index = index + 6;
                                        } else {
                                            if (umlaut.equals("nbsp;")) {               //Fuer geschuetzte Lehrzeichen:
                                                Ausgabe = Ausgabe + "";
                                                index = index + 6;
                                            } else {                                    //Falls ein nicht abgedecktes Sonderzeichen auftritt:
                                                Ausgabe = Ausgabe + "?";
                                                index++;
                                            }
                                        }
                                    }
                                }

                            } catch (Exception e1) {                                    //Wenn bei der Sonderzeichenanalyse ein Fehler auftritt
                                Ausgabe = Ausgabe + ".";
                                index++;
                            }

                        } else {                                                        //Wenn kein Sonderzeichen vorhanden ist:
                            Ausgabe = Ausgabe + alles[zeile].charAt(index);             //Buchstaben hinzufuegen
                            index++;                                                    //naechstes Zeichen
                        }
                    }
                } catch (Exception e2) {                                                //Falls ein Fehler auftritt(normalerweise IndexOutOfBoundsException, da die zeile zu ende ist)
                    Ausgabe = Ausgabe + "IndexOutOfBoundsException";                    //IndexOutOfBoundsException ausgeben
                }



                index = index + 5;                                                      //Fuenf Zeichen ueberspringen

                //Ausgabe der Richtigen Variable Zuordnen:
                if (spalte == 0) {                                                      //Falls 0, ->klasse[]
                    klasse.add(zeilnummer, Ausgabe);
                } else {
                    if (spalte == 1) {                                                  //Falls 1, ->stunde[]
                        stunde.add(zeilnummer, Ausgabe);
                    } else {
                        if (spalte == 2) {                                              //Falls 2, ->fach[]
                            fach.add(zeilnummer, Ausgabe);
                        } else {
                            if (spalte == 3) {                                          //Falls 3, ->lehrer[]
                                lehrer.add(zeilnummer, Ausgabe);
                            } else {
                                if (spalte == 4) {                                      //Falls 4, ->raum[]
                                    raum.add(zeilnummer, Ausgabe);
                                } else {
                                    if (spalte == 5) {                                  //Falls 5, ->info[]
                                        info.add(zeilnummer, Ausgabe);
                                    }
                                }
                            }
                        }
                    }
                }

                spalte++;                                                               //neachste Spalte!

            }


            if ("".equals(klasse.get(zeilnummer))) {                                        //Abbruchbedingung der aeussersten Schleife
                klasse.set(0, String.valueOf(zeilnummer - 1));                             //Anzahl der Zeilen mit Aenderung (feur alle Klassen) speichern
                abbruch = true;                                                         //Schleife abbrechen
            } else {
                if (klasse.get(zeilnummer).equals("IndexOutOfBoundsException")) {           //Abbruchbedingung der aeussersten Schleife
                    klasse.set(0, String.valueOf(zeilnummer - 1));                         //Anzahl der Zeilen mit Aenderung (feur alle Klassen) speichern
                    abbruch = true;                                                     //Schleife abbrechen
                    if(zeilnummer<=1){
                        problem =true;
                    }
                } else {                                                                //Veraenderung einiger Variablen
                    if ("---".equals(fach.get(zeilnummer))) {                               //"faelt aus" sieht besser aus als "---"
                        fach.add(zeilnummer, "Ausfall");
                    }
                    if ("".equals(info.get(zeilnummer))) {                                  //Wenn keine info vorhanden ist...
                        info.add(zeilnummer, "geändert");                                  //...wird eine Ersatzinfo kreiert
                    }
                    zeilnummer++;                                                       //zeilenummer um 1 erhoehen
                    letzteZeile = zeile;                                                //Letzte bearbeitet Zeile speichern
                    zeile = zeile + 9;                                                  //9 Zeilen(die zur Styleinformatione enthalten) ueberspringen
                    spalte = 0;                                                         //Wieder bei Spalte 0(->Klsse[]) beginnen
                }
            }


        }

    }

    private int zeileenthaelt (String alles[], int zeile, String name){
//        Durchsucht nach einer Zeile mit dem Inhalt "name" (contains), ab Zeile "Zeile"

        String Vergleich =zeileauslesen(alles,zeile, 2);
        while (!Vergleich.contains(name)){                        //Solange der Inhalt der Zeile nicht "name" ist...
            zeile++;                                            //...ueberpruefe die naechste Zeile
            Vergleich =zeileauslesen(alles,zeile, 2);
            if (Vergleich.equals("Zeile nicht gefunden")){
                Vergleich=name;                                 //Um die Schleife abzuberechen
                zeile=-1;
            }
            //int i=alles.length;
        }

        return zeile;                                           //gefundene Zeile zurueckgegen
    }

    private int zeilefinden (String alles[], int zeile, String name){
//        Durchsucht nach einer Zeile mit dem Inhalt "name" (equals), ab Zeile "Zeile"

        String Vergleich =zeileauslesen(alles, zeile, 2);
        while (!Vergleich.equals(name)){                        //Solange der Inhalt der Zeile nicht "name" ist...
            zeile++;                                            //...ueberpruefe die naechste Zeile
            Vergleich =zeileauslesen(alles,zeile, 2);
            if (Vergleich.equals("Zeile nicht gefunden")){
                Vergleich=name;                                 //Um die Schleife abzuberechen
                zeile=-1;
            }
            //int i=alles.length;
        }

        return zeile;                                           //gefundene Zeile zurueckgegen
    }

    private String auslesen(String[]alles, int zeile, int index) {
//        Liest eine Zeile "Zeile" ab Zeichen "index" aus
        String Ausgabe="";      //Ausgabevariable
        String umlaut;          //notwendig, um Sonderzeichen zu erkennen
        try {
            while ((alles[zeile].charAt(index)) != ("<".charAt(0))) {
                if (alles[zeile].charAt(index) == "&".charAt(0)) {
                    try {
                        umlaut = alles[zeile].substring(index + 1, index + 6);
                        if (umlaut.equals("auml;")) {
                            Ausgabe = Ausgabe + "ä";
                            index = index + 6;
                        } else {
                            if (umlaut.equals("ouml;")) {
                                Ausgabe = Ausgabe + "ö";
                                index = index + 6;
                            } else {
                                if (umlaut.equals("uuml;")) {
                                    Ausgabe = Ausgabe + "ü";
                                    index = index + 6;
                                }else {
                                    if (umlaut.equals("Auml;")){
                                        Ausgabe = Ausgabe+ "Ä";
                                        index = index + 6;
                                    }else {
                                        if (umlaut.equals("Ouml;")) {
                                            Ausgabe = Ausgabe + "Ö";
                                            index = index + 6;
                                        } else {
                                            if (umlaut.equals("Uuml;")) {
                                                Ausgabe = Ausgabe + "Ü";
                                                index = index + 6;
                                            } else {

                                                if (umlaut.equals("nbsp;")) {
                                                    Ausgabe = Ausgabe + "";
                                                    index = index + 6;
                                                }else {
                                                    if (umlaut.equals("amp;")) {
                                                        Ausgabe = Ausgabe + "&";
                                                        index = index + 5;
                                                    } else {
                                                        Ausgabe = Ausgabe + "?";
                                                        index++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        Ausgabe = Ausgabe + ".";
                        index++;
                    }

                } else {
                    Ausgabe = Ausgabe + alles[zeile].charAt(index);
                    index++;
                }
                letztesZeichen=index;
            }

            if (alles[zeile].substring(index,index+4).equals("<br>")){  //"<br> Muss als Zeilenumbruch erkannt und dargestellt werden
                Ausgabe=Ausgabe+"\n"+auslesen(alles,zeile,index+4);
            }
        }catch (IndexOutOfBoundsException e){
            Ausgabe=Ausgabe+"IndexOutOfBoundsException";
            letztesZeichen=999;
        }


        return Ausgabe;
    }

    private String zeileauslesen (String[] alles, int zeile,int index){
//        Liest eine Zeile "Zeile" ab dem naechsten ">" nach Zeichen "index" aus
        String Ausgabe;                                             //Ausbabevariable
        try {
            try {                                                       //Falls ein Fehler auftritt
                while (alles[zeile].charAt(index) != ">".charAt(0)) {   //Solange das Zeichen nicht">" ist...
                    index++;                                            //...das naechste Zeichen versuchen
                }
                index++;                                                //index um 1 erhoehen(">"soll nicht ausgelesen werden!)
                Ausgabe = auslesen(alles, zeile, index);                    //Ab Zeichen "index" in der Zeile "Zeile" auslesen
            } catch (IndexOutOfBoundsException ez) {                      //Falls die zeile zu ende ist:
                Ausgabe = "IndexOutOfBoundsException";
                letztesZeichen = 999;
            }
        }catch (NullPointerException en){
            Ausgabe="Zeile nicht gefunden";
        }

        return Ausgabe;                                             //Ausgabe zurueckgeben
    }




}