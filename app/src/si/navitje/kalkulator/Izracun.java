package si.navitje.kalkulator;

/** Cista logika izracuna, brez odvisnosti od Androida. */
final class Izracun {
    private Izracun() {}

    static final class Rezultat {
        int vrsticaVhod;        // najblizja vrednost v 1. stolpcu
        double fi570;           // vrednost v stolpcu fi 570
        double deljeno;         // fi570 / stevilo navitij
        double fi150Najblizje;  // najblizja vrednost v stolpcu fi 150/160
        int indeksRezultat;     // indeks vrstice rezultata v tabeli
        int navitjeRezultat;    // 1. stolpec v isti vrstici
        boolean vhodIzvenTabele;
        boolean ciljIzvenTabele;
    }

    static final double JEDRO_160_MM = 160.0;

    /** Zunanji premer navitja na jedru fi 160: navitje je radialno, zato x2. */
    static double zunanjiPremer(double velikostNavitja) {
        return JEDRO_160_MM + 2.0 * velikostNavitja;
    }

    /** Dolzina traku v metrih: pi * (D^2 - d^2) / (4 * debelina). */
    static double dolzinaTrakuM(double velikostNavitja, double debelinaMm) {
        if (!(velikostNavitja > 0) || Double.isInfinite(velikostNavitja))
            throw new IllegalArgumentException("velikost");
        if (!(debelinaMm > 0) || Double.isInfinite(debelinaMm))
            throw new IllegalArgumentException("debelina");
        double D = zunanjiPremer(velikostNavitja), d = JEDRO_160_MM;
        return Math.PI * (D * D - d * d) / (4.0 * debelinaMm) / 1000.0;
    }

    /** Gostota aluminija v kg/mm^3 (2,70 g/cm^3). */
    static final double GOSTOTA_AL_KG_MM3 = 2.70e-6;

    /** Dolzina traku, zaokrozena navzdol na cel meter. */
    static long dolzinaTrakuCeliM(double velikostNavitja, double debelinaMm) {
        return (long) Math.floor(dolzinaTrakuM(velikostNavitja, debelinaMm));
    }

    /** Teza aluminijastega traku v kg: gostota * dolzina * debelina * sirina. */
    static double tezaKg(long dolzinaM, double debelinaMm, double sirinaMm) {
        if (dolzinaM < 0) throw new IllegalArgumentException("dolzina");
        if (!(debelinaMm > 0) || Double.isInfinite(debelinaMm))
            throw new IllegalArgumentException("debelina");
        if (!(sirinaMm > 0) || Double.isInfinite(sirinaMm))
            throw new IllegalArgumentException("sirina");
        return GOSTOTA_AL_KG_MM3 * (dolzinaM * 1000.0) * debelinaMm * sirinaMm;
    }

    /** Indeks najblizje veljavne (ne-NaN) vrednosti; pri enaki razdalji zmaga manjsa vrednost. */
    static int najblizji(double[] stolpec, double cilj) {
        int best = -1;
        double bestD = Double.MAX_VALUE;
        for (int i = 0; i < stolpec.length; i++) {
            double x = stolpec[i];
            if (Double.isNaN(x)) continue;
            double d = Math.abs(x - cilj);
            if (d < bestD) { bestD = d; best = i; }
        }
        return best;
    }

    static Rezultat izracunaj(double velikostNavitja, double steviloNavitij) {
        if (!(velikostNavitja > 0) || Double.isInfinite(velikostNavitja))
            throw new IllegalArgumentException("velikost");
        if (!(steviloNavitij > 0) || Double.isInfinite(steviloNavitij))
            throw new IllegalArgumentException("stevilo");
        int n = Tabela.NAVITJE.length;
        // 1. stolpec, samo vrstice ki imajo vrednost fi 570
        double[] prvi = new double[n];
        for (int i = 0; i < n; i++)
            prvi[i] = Double.isNaN(Tabela.FI_570[i]) ? Double.NaN : Tabela.NAVITJE[i];
        int i1 = najblizji(prvi, velikostNavitja);
        Rezultat r = new Rezultat();
        r.vrsticaVhod = Tabela.NAVITJE[i1];
        r.fi570 = Tabela.FI_570[i1];
        r.deljeno = r.fi570 / steviloNavitij;
        int i2 = najblizji(Tabela.FI_150_160, r.deljeno);
        r.indeksRezultat = i2;
        r.fi150Najblizje = Tabela.FI_150_160[i2];
        r.navitjeRezultat = Tabela.NAVITJE[i2];
        r.vhodIzvenTabele = velikostNavitja < Tabela.NAVITJE[0] - 2.5
                || velikostNavitja > Tabela.NAVITJE[n - 1] + 2.5;
        double min = Tabela.FI_150_160[0], max = Tabela.FI_150_160[n - 1];
        r.ciljIzvenTabele = r.deljeno < min - (Tabela.FI_150_160[1] - min) / 2
                || r.deljeno > max + (max - Tabela.FI_150_160[n - 2]) / 2;
        return r;
    }
}
