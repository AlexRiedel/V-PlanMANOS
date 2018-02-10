package de.alex_riedel.v_planmanos;


import android.animation.LayoutTransition;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class MyFragment extends Fragment {


    //Layoutkompnenten:
    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;
    private SwipeRefreshLayout mSwipeRefreshLayout1;

    private TextView textDatum;
    private TextView textInfo;
    private TextView textAktZeit;
    private TextView textUnten1;
    private TextView textUnten2;
    private TextView textVeroef1;
    private TextView textVeroef2;

    private TextView textAbLehrer1;
    private TextView textAbLehrer2;

    private TextView textAenLehrer1;
    private TextView textAenLehrer2;

    private TextView textAenKlasse1;
    private TextView textAenKlasse2;

    private CardView cardOben;
    private CardView cardMitte;
    private CardView cardUnten;

    private Button buttonRefresh;
    private Button buttonRefreshDark;
    private Button buttonMehr;

    public CardView[] cardViews = new CardView[50];
    public TextView[] textViews = new TextView[50];



    //Andere Variablen:
    private int anzahlCards = 0;
    private MainActivity mainActivity;
    private Vplan vplan;

    private String name ="";

    private boolean darkTheme = false;

//Oeffentliche Methoden:
    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_my, container, false);

        //linearLayout1 = (LinearLayout) layout.findViewById(R.id.linearLayout1);
        linearLayout2 = (LinearLayout) layout.findViewById(R.id.linearLayout2);
        mSwipeRefreshLayout1 =(SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout1);


        textDatum = (TextView) layout.findViewById(R.id.textDatum);
        textInfo = (TextView) layout.findViewById(R.id.textInfo);
        textAktZeit = (TextView) layout.findViewById(R.id.textAktZeit);
        textUnten1 = (TextView) layout.findViewById(R.id.textUnten1);
        textUnten2 = (TextView) layout.findViewById(R.id.textUnten2);

        cardOben = (CardView) layout.findViewById(R.id.cardOben);
        cardMitte = (CardView) layout.findViewById(R.id.cardMitte);
        cardUnten = (CardView) layout.findViewById(R.id.cardUnten);

        buttonRefresh = (Button) layout.findViewById(R.id.buttonRefresh);
        buttonRefreshDark = (Button) layout.findViewById(R.id.buttonRefreshDark);


        textVeroef1 = (TextView) layout.findViewById(R.id.textVeroef1);
        textVeroef2 = (TextView) layout.findViewById(R.id.textVeroef2);

        textAbLehrer1 = (TextView) layout.findViewById(R.id.textAbLehrer1);
        textAbLehrer2 = (TextView) layout.findViewById(R.id.textAbLehrer2);

        textAenLehrer1 = (TextView) layout.findViewById(R.id.textAenLehrer1);
        textAenLehrer2 = (TextView) layout.findViewById(R.id.textAenLehrer2);

        textAenKlasse1 = (TextView) layout.findViewById(R.id.textAenKlasse1);
        textAenKlasse2 = (TextView) layout.findViewById(R.id.textAenKlasse2);

        buttonMehr = (Button) layout.findViewById(R.id.ButtonMehr);
        buttonMehr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeVisible();
            }
        });

        linearLayout1 = (LinearLayout) layout.findViewById(R.id.linearLayout1);
        LayoutTransition  transition = linearLayout1.getLayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);



        mSwipeRefreshLayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        darkTheme = sharedPref.getBoolean("Dark",false);


        if (darkTheme) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buttonRefresh.setBackgroundTintList(this.getResources().getColorStateList(R.color.button_selector));
            } else {
                buttonRefresh.setVisibility(View.GONE);
                buttonRefreshDark.setVisibility(View.VISIBLE);
            }
            cardUnten.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorDarkCardBackground));
            cardOben.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorDarkCardBackground));
            cardMitte.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorDarkCardBackground));

        }


        return layout;
    }

    public void setName(String name){
        this.name=name;
    }

    public void setVplan(Vplan vplan)throws NullPointerException{
        this.vplan = vplan;
        try {
            //Vplan anzeigen:
            if (vplan != null) {              //Nur, wenn nicht null
                if (!vplan.isProblem()) {
                    vplanZeigen();          //Anzeigen, wenn kein Problem
                    if (name.equals("fragment1")) {      //evtl. zum linken Fragment swipen, falls keine relavanten Daten zur verfuegung stehen
                        if (nextStunde() > vplan.getAenStunde(vplan.getAenAnzahl()) || nextStunde() == 0) {
                            this.mainActivity.swipeToFrag2();
                        }
                    } else if (vplan.isKeineAenderung() && nextStunde() >= 5) {
                        this.mainActivity.swipeToFrag2();
                    }
                } else {//Wenn Problem:
                    setAlternativtext(vplan.getFehler(),true);
                    if (name.equals("fragment1")) {      //evtl. zum linken Fragment swipen, falls keine relavanten Daten zur verfuegung stehen
                        this.mainActivity.swipeToFrag2();
                    }
                }
            } else {//Wenn kein Vplan:
                if (name.equals("fragment1")) {
                    this.mainActivity.swipeToFrag2();   //zum linken Fragment swipen, falls rechtes Fragment
                }

            }
        }catch (NullPointerException enull){
            enull.printStackTrace();
            throw enull;
        }


    }

    public void setAlternativtext(String text, boolean buttonVisible) throws NullPointerException{
        //Setzt einen alternativen Text, falls kein Vplan verfuegbar ist
        try {
            removeCardViews();  //eventuelle CardViews enstfernen, damit Platz fuer den alternativen Text ist

            cardMitte.setVisibility(View.VISIBLE);       //Benoetigte Elemente sichtbar machen
            textInfo.setVisibility(View.VISIBLE);
            textInfo.setText(text);

            if (buttonVisible){
                if (darkTheme&&Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                    buttonRefreshDark.setVisibility(View.VISIBLE);
                }else {
                    buttonRefresh.setVisibility(View.VISIBLE);
                }

                textAktZeit.setVisibility(View.VISIBLE);
                cardOben.setVisibility(View.GONE);
                aktualisieren();

            }else {
                if (darkTheme&&Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                    buttonRefreshDark.setVisibility(View.GONE);
                }else {
                    buttonRefresh.setVisibility(View.GONE);
                }
                textAktZeit.setVisibility(View.GONE);
            }




        }catch (NullPointerException enull){
            enull.printStackTrace();
            throw enull;
        }

    }

    public Vplan getVplan(){
        return vplan;
    }

    public String getName(){
        return name;
    }

    public String getWochentag(){
        try {
            return vplan.getWochentag();
        }catch (NullPointerException e){
            return "Morgen";
        }
    }

    public String getShare(){
        if (vplan!=null){
            return vplan.getShare();
        }else {
            return "Nichts zum teilen!\nAber ich versuch's trotdem mal";
        }
    }

    public void aktualisieren() {
        //Um die Zeit des letzten Planherunterladens zu aktualisieren
        try {

            SharedPreferences pref = mainActivity.getApplicationContext().getSharedPreferences("setup", Context.MODE_PRIVATE);  //Speichervariable erzeugen
            Calendar calendar = Calendar.getInstance();
            String uhrzeit = pref.getString("uhrzeit", "");


            if (!uhrzeit.equals("")) {

                String letzteAktualisierung;

                int hJahrestag = calendar.get(Calendar.DAY_OF_YEAR);
                int hJahr = calendar.get(Calendar.YEAR);

                int gTag = pref.getInt("tag", calendar.get(Calendar.DAY_OF_MONTH));
                int gJahrestag = pref.getInt("jahrestag", hJahrestag);
                int gMonat = pref.getInt("monat", calendar.get(Calendar.MONTH));
                int gJahr = pref.getInt("jahr", hJahr);



                if ((gJahr - hJahr) == -1) {    //letztes Jahr
                    GregorianCalendar cal = new GregorianCalendar();
                    if (cal.isLeapYear(gJahr)) {  //wenn Schaltjahr
                        gJahrestag = gJahrestag - 366;
                    } else {
                        gJahrestag = gJahrestag - 365;
                    }
                }
                if (gJahrestag == hJahrestag) {   //heute
                    letzteAktualisierung = "Letzte Aktualisierung: " + uhrzeit;
                } else if ((gJahrestag - hJahrestag) == -1) {     //gestern
                    letzteAktualisierung = "Letzte Aktualisierung: gestern, " + uhrzeit;
                } else if ((gJahrestag - hJahrestag) == -2) {
                    letzteAktualisierung = "Letzte Aktualisierung: vorgestern, " + uhrzeit;
                } else {
                    letzteAktualisierung = "Letzte Aktualiserung: " + (gTag) + "." + (gMonat + 1) + "." + gJahr + ", " + uhrzeit;
                }



                textAktZeit.setText(letzteAktualisierung);
            }else {
                textAktZeit.setVisibility(View.GONE);
            }
        }catch (NullPointerException enull){
            enull.printStackTrace();
            //throw enull;
        }

    }

    public void onItemsLoadComplete() {
        try {
            mSwipeRefreshLayout1.setRefreshing(false);  //Ladeanimation stoppen
        }catch (NullPointerException enull){
            enull.printStackTrace();
        }

    }

    public void manRefrech(){
        mSwipeRefreshLayout1.post(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout1.setRefreshing(true);   //Ladeanimation manuel starten
            }
        });

    }


//private Methoden:
    private void vplanZeigen()throws NullPointerException{
        //Vertretungsplan darstellen:
        try {
            cardMitte.setVisibility(View.GONE);      //nicht benoetigte Elemente unsichtbar machen




            vplan.aenderungfiltern(getContext());
            if (vplan.isKeineAenderung()){
                setAlternativtext("Keine Änderung",false);
            }else {
                String[] aenderung=vplan.getAenderung(getContext());
                int next = nextStunde();    //Was die naechste Sunde ist
                boolean aktiv;
                boolean letzte;

                removeCardViews();  //Vorherige Cards entfernen, da diese sonst bestehen bleiben


                for (int i=1;i<=vplan.getAenAnzahl();i++){
                    aktiv = vplan.getAenStunde(i) == next;  //Karten werden markiert, wenn sie die naechste Stunde betrefen, also "aktiv" sind

                    letzte = i==vplan.getAenAnzahl();       //Bei der letzten Karte gibt es andere Abstaende

                    addCardView(aenderung[i],aktiv,letzte); //Kate hinzufuegen
    //                addCardView(aenderung[i],true,letzte);

                }
            }

            cardOben.setVisibility(View.VISIBLE);
            textDatum.setText(vplan.getDatum());
            textVeroef2.setText(vplan.getVeroefdat());
            textAenLehrer2.setText(vplan.getAenLeher());
            textAbLehrer2.setText(vplan.getAbLehrer());
            textAenKlasse2.setText(vplan.getAenKlassen());


            if (vplan.isZusInfoBool()){ //Zusaetzliche Informationen anzeigen
                setZusInfo(vplan.getZusInfo());
            }else {                     //Sonst die betreffenden Elemente unsichtbar machen
                cardUnten.setVisibility(View.GONE);
                textUnten2.setVisibility(View.GONE);
                textUnten1.setVisibility(View.GONE);

            }

        }catch (NullPointerException enull){
            enull.printStackTrace();

            Log.e("MyFragment","vplanZeigen NullPointerException");
            throw enull;
        }

//        setZusInfo("Der Unterricht findet laut Sommerplan (Kurzplan)"); //TEST



    }

    private void setZusInfo(String zusInfo)throws NullPointerException{
        try {
            cardUnten.setVisibility(View.VISIBLE);      //Benoetigte Elemente sichtbar machen
            textUnten2.setVisibility(View.VISIBLE);
            textUnten1.setVisibility(View.VISIBLE);
            textUnten2.setText(zusInfo);                //Und Text setzten
        }catch (NullPointerException enull){
            enull.printStackTrace();
            throw enull;
        }
    }

    private void addCardView(String text, boolean aktiv, boolean letzte){
        int cardPaddingLeft = this.getResources().getDimensionPixelSize(R.dimen.cardPaddingLeft);
        int cardPaddingTop = this.getResources().getDimensionPixelSize(R.dimen.cardPaddingTop);
        int cardPaddingRight = this.getResources().getDimensionPixelSize(R.dimen.cardPaddingRight);
        int cardPaddingBottom = this.getResources().getDimensionPixelSize(R.dimen.cardPaddingBottom);
        int margLeft = this.getResources().getDimensionPixelSize(R.dimen.margLeft);
        int margTop = this.getResources().getDimensionPixelSize(R.dimen.margTop);
        int margRight = this.getResources().getDimensionPixelSize(R.dimen.margRight);
        int margBottom = this.getResources().getDimensionPixelSize(R.dimen.margBottom);

        if (letzte) margBottom = this.getResources().getDimensionPixelSize(R.dimen.margBottomLast);             //Bei erster und lezter Karte andere Margins verwenden
        if (anzahlCards==0) margTop = this.getResources().getDimensionPixelSize(R.dimen.margTopFirst);

        cardViews[anzahlCards] = new CardView(this.getContext());   //neue Karte erzeugen

        LinearLayout.LayoutParams linLayParmsCard = new LinearLayout.LayoutParams(  //neue Layoutparameter erzeugen
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        linLayParmsCard.setMargins(margLeft, margTop, margRight, margBottom);
        linLayParmsCard.setMarginStart(margLeft);
        linLayParmsCard.setMarginEnd(margRight);
        linLayParmsCard.gravity = Gravity.TOP;

        cardViews[anzahlCards].setLayoutParams(linLayParmsCard);    //Layoutparameter in Kart einsetzen
        if (darkTheme) {
            cardViews[anzahlCards].setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorDarkCardBackground));
        }

        cardViews[anzahlCards].setContentPadding(cardPaddingLeft,cardPaddingTop,cardPaddingRight,cardPaddingBottom);

        if (aktiv){     //Aktive Karten haben eine andere Farbe und Hoehe
            if (darkTheme) {
                cardViews[anzahlCards].setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorDarkNext));
            }else {
                cardViews[anzahlCards].setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.colorNext));
            }

            cardViews[anzahlCards].setCardElevation(this.getResources().getDimension(R.dimen.elevCardHigh));
        }else {
            cardViews[anzahlCards].setCardElevation(this.getResources().getDimension(R.dimen.elevCardNorm));
        }

        cardViews[anzahlCards].setRadius(this.getResources().getDimension(R.dimen.radiusCard));

        //Layout fuer den Text
        LinearLayout.LayoutParams linLayParmsText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,1f);


        textViews[anzahlCards] = new TextView(this.getContext());   //Textfeld erzeugen
        textViews[anzahlCards].setLayoutParams(linLayParmsText);
        textViews[anzahlCards].setText(text);


        cardViews[anzahlCards].addView(textViews[anzahlCards]);     //Textfeld der Karte hinzufuegen
        linearLayout2.addView(cardViews[anzahlCards]);               //Karte dem Layout hinzufuegen

        anzahlCards++;  //Weiterzaehlen
    }

    private void removeCardViews()throws NullPointerException{
        //Entfernen der Karten, die den Vertretungsplan darstellen
        try {
            for (int i=0;i<=anzahlCards;i++){
                linearLayout2.removeView(cardViews[i]);
            }
        }catch (NullPointerException enull){
            enull.printStackTrace();
            throw enull;
        }

        anzahlCards=0;  //Anzahl wieder auf Null setzten

    }

    private void refreshItems() {
        mainActivity.herunterladen();
    }

    private int nextStunde () throws NullPointerException {
        //Liefert, welche Stunde gerade ist, oder als naechstes gehalten wird; evtl 0, falls der Unterricht beendet ist, oder noch nicht begonnen hat
        try {
            SharedPreferences einstellungen = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());

            int minute;                         //Aktuelle Minute
            int stunde;                         //Aktelle Stunde (Uhrzeit)
            int next = 0;                       //Rueckgabevariable

            boolean abbruch = false;            //Abbruchvariable, zum  Abbrechen von Schleifen

            int[] endeStund = {0, 8, 8,10,10,11,42,14,14,15,16,17,18};    //Wann zB die 1. Stunde endet, in Uhrzeitstuden (Um 12 Minuten nach vorne gezogen!)
            int[] endeMinut = {0, 3,58, 3,58,53,42, 3,53,43,33,23,13};    //Wann zB die 1. Stunde endet, in Uhrzeitminuten

            if (einstellungen.getBoolean("anderePause",false)) {     //Die Mittagspause der 5. und 6. Klassen ist verschoben!
                endeStund[6] = 13;
                endeMinut[6] = 20;
            } else {
                endeStund[6] = 12;
                endeMinut[6] = 48;
            }




            Calendar time=Calendar.getInstance();       //Kalender erzeugen
            minute=time.get(Calendar.MINUTE);           //Minute auslesen
            stunde=time.get(Calendar.HOUR_OF_DAY);      //Stunde auslesen

            //Herausbekommen, welche die naechste Stunde ist
            while (!abbruch) {
                next++;
                try {
                    if (stunde == endeStund[next]) {
                        if (minute <= endeMinut[next]) {
                            abbruch = true;
                        }
                    }
                    if (stunde < endeStund[next]) {
                        abbruch = true;
                    }
                } catch (IndexOutOfBoundsException ebound) {
                    next = 0;
                    abbruch = true;
                }
            }

            //Bedingungen, unter dene es keine naechste Stunde gibt:

            Calendar heute = Calendar.getInstance();    //Heutiges Datum
            Calendar date = vplan.getCalendar();

            if (vergleiche(heute,date)!=0){ //Wenn der Plan nicht von heute ist
                next = 0;
            }

            return next;
        }catch (NullPointerException enull){
            enull.printStackTrace();
            throw enull;
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

    private void makeVisible(){
        if (textVeroef1.getVisibility() == View.VISIBLE) {
            View[] view = {textAbLehrer1, textAbLehrer2, textAenKlasse1, textAenKlasse2, textAenLehrer1, textAenLehrer2, textVeroef1, textVeroef2};

            for (final View aView : view) {
                aView.setVisibility(View.GONE);
            }
            buttonMehr.setText("mehr");

        }else {
            View[] view = {textAbLehrer1, textAbLehrer2, textAenKlasse1, textAenKlasse2, textAenLehrer1, textAenLehrer2, textVeroef1, textVeroef2};
            for (final View aView : view) {
                aView.setVisibility(View.VISIBLE);
            }
            buttonMehr.setText("weniger");
        }
    }

}