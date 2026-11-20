package si.navitje.kalkulator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {
    private EditText debelina, velikost, stevilo, sirina;
    private TextView rezultat, enota, vrednost, nad, pod, opozorilo, dolzina, teza;

    private static final int MENI_PONASTAVI = 1, MENI_TEMA = 2;
    private boolean temno;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENI_PONASTAVI, 0, R.string.ponastavi)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        // ikona kaze trenutno aktiven nacin: luna = temni, sonce = svetli
        menu.add(Menu.NONE, MENI_TEMA, 1, temno ? R.string.svetli_nacin : R.string.temni_nacin)
                .setIcon(temno ? R.drawable.ic_luna : R.drawable.ic_sonce)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENI_PONASTAVI:
                debelina.setText("");
                velikost.setText("");
                stevilo.setText("");
                sirina.setText("");
                debelina.requestFocus();
                return true;
            case MENI_TEMA:
                // apply() takoj posodobi vrednost v pomnilniku; vnosi se ohranijo ob recreate()
                getPreferences(MODE_PRIVATE).edit().putBoolean(KLJUC_TEMNO, !temno).apply();
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static final String KLJUC_TEMNO = "temno";

    /** Shranjena izbira; brez izbire sledi nastavitvi sistema. */
    private boolean temniNacin() {
        SharedPreferences p = getPreferences(MODE_PRIVATE);
        if (p.contains(KLJUC_TEMNO)) return p.getBoolean(KLJUC_TEMNO, false);
        int noc = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return noc == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    protected void onCreate(Bundle b) {
        temno = temniNacin();
        setTheme(temno ? R.style.AppTheme_Dark : R.style.AppTheme);
        super.onCreate(b);
        setContentView(R.layout.main);
        debelina = (EditText) findViewById(R.id.debelina);
        dolzina = (TextView) findViewById(R.id.dolzina);
        teza = (TextView) findViewById(R.id.teza);
        sirina = (EditText) findViewById(R.id.sirina);
        velikost = (EditText) findViewById(R.id.velikost);
        stevilo = (EditText) findViewById(R.id.stevilo);
        rezultat = (TextView) findViewById(R.id.rezultat);
        enota = (TextView) findViewById(R.id.enota);
        vrednost = (TextView) findViewById(R.id.vrednost);
        nad = (TextView) findViewById(R.id.vrstica_nad);
        pod = (TextView) findViewById(R.id.vrstica_pod);
        opozorilo = (TextView) findViewById(R.id.opozorilo);
        TextWatcher w = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            public void onTextChanged(CharSequence s, int a, int c, int d) {}
            public void afterTextChanged(Editable e) { osvezi(); }
        };
        debelina.addTextChangedListener(w);
        velikost.addTextChangedListener(w);
        stevilo.addTextChangedListener(w);
        sirina.addTextChangedListener(w);
        osvezi();
    }

    private static Double beri(EditText t) {
        String s = t.getText().toString().trim().replace(',', '.');
        if (s.isEmpty()) return null;
        try {
            double d = Double.parseDouble(s);
            return (Double.isNaN(d) || Double.isInfinite(d)) ? Double.valueOf(Double.NaN) : Double.valueOf(d);
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    private static String f3(double d) {
        return String.format(Locale.ROOT, "%.3f", d).replace('.', ',');
    }

    private static String f2(double d) {
        return String.format(Locale.ROOT, "%.2f", d).replace('.', ',');
    }

    private static String f1(double d) {
        return String.format(Locale.ROOT, "%.1f", d).replace('.', ',');
    }

    private static String vnos(EditText t) {
        return t.getText().toString().trim().replace('.', ',');
    }

    /** Vrstica tabele (navitje, fi 150/160 in dolzina traku, ce je debelina znana) ali prazno izven tabele. */
    private String vrstica(int i, Double t) {
        if (i < 0 || i >= Tabela.NAVITJE.length) return "";
        String nav = String.valueOf(Tabela.NAVITJE[i]), fi = f3(Tabela.FI_150_160[i]);
        if (t == null || !(t > 0)) return getString(R.string.vrstica, nav, fi);
        return getString(R.string.vrstica_dolzina, nav, fi, String.valueOf(Izracun.dolzinaTrakuCeliM(Tabela.NAVITJE[i], t)));
    }

    private void brezDolzine() {
        dolzina.setText("\u2013");
        teza.setText("\u2013");
    }

    /** Dolzina traku na enem navitju (jedro fi 160), iz izracunanega navitja in debeline. */
    private void osveziDolzino(Double t, int navitje) {
        if (t == null || !(t > 0)) { brezDolzine(); return; }
        long l = Izracun.dolzinaTrakuCeliM(navitje, t);
        dolzina.setText(getString(R.string.dolzina_m, String.valueOf(l)));
        Double s = beri(sirina);
        if (s != null && s > 0)
            teza.setText(getString(R.string.teza_kg, String.valueOf((long) Math.floor(Izracun.tezaKg(l, t, s)))));
        else
            teza.setText(getString(R.string.teza_kg_mm, f3(Izracun.tezaKg(l, t, 1.0))));
    }

    private void prazno(String sporocilo) {
        rezultat.setText("–");
        enota.setVisibility(View.INVISIBLE);
        vrednost.setText("");
        nad.setText("");
        pod.setText("");
        opozorilo.setText(sporocilo);
        opozorilo.setVisibility(sporocilo.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void osvezi() {
        debelina.setError(null);
        velikost.setError(null);
        stevilo.setError(null);
        sirina.setError(null);
        Double sir = beri(sirina);
        if (sir != null && !(sir > 0)) sirina.setError(getString(R.string.napaka_stevilo));
        Double t = beri(debelina), v = beri(velikost), n = beri(stevilo);
        boolean ok = true;
        if (t != null && !(t > 0)) debelina.setError(getString(R.string.napaka_stevilo));
        if (v != null && !(v > 0)) { velikost.setError(getString(R.string.napaka_stevilo)); ok = false; }
        if (n != null && !(n > 0)) { stevilo.setError(getString(R.string.napaka_stevilo)); ok = false; }
        if (v == null || n == null || !ok) { prazno(""); return; }

        Izracun.Rezultat r;
        try {
            r = Izracun.izracunaj(v, n);
        } catch (IllegalArgumentException ex) {
            prazno(getString(R.string.napaka_stevilo));
            return;
        }
        osveziDolzino(t, r.navitjeRezultat);
        rezultat.setText(String.valueOf(r.navitjeRezultat));
        enota.setVisibility(View.VISIBLE);
        int i = r.indeksRezultat;
        vrednost.setText(f3(r.fi150Najblizje));
        nad.setText(vrstica(i - 1, t));
        pod.setText(vrstica(i + 1, t));
        StringBuilder o = new StringBuilder();
        if (r.vhodIzvenTabele) o.append(getString(R.string.opozorilo_vhod));
        if (r.ciljIzvenTabele) {
            if (o.length() > 0) o.append('\n');
            o.append(getString(R.string.opozorilo_cilj));
        }
        opozorilo.setText(o);
        opozorilo.setVisibility(o.length() == 0 ? View.GONE : View.VISIBLE);
    }
}
