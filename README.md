# Navitje (Android)

Kalkulator navitij za aluminijast trak.

## Kaj naredi

Vnosi: **Debelina (mm)**, **Velikost navitja (mm)**, **Število navitij**, **Širina traku (mm)** (neobvezno).

1. Poišče najbližjo vrednost velikosti navitja v 1. stolpcu tabele in prebere vrednost v stolpcu fi 570.
2. Vrednost deli s številom navitij.
3. Poišče najbližjo vrednost v stolpcu fi 150/160 in prikaže navitje (1. stolpec) iste vrstice ter vrstico nad in pod njo.
4. Dolžina traku na jedru fi 160: `π × ((160 + 2 × navitje)² − 160²) / (4 × debelina)`, zaokroženo navzdol na cel meter.
5. Teža (aluminij, 2,70 g/cm³): `gostota × dolžina × debelina × širina`, zaokroženo navzdol na cel kg. Brez širine prikaže kg/mm.

Tabela (80–710 mm, stolpca fi 150/160 in fi 570) je v `app/src/si/navitje/kalkulator/Tabela.java`.

## Namestitev

Pripravljen APK: `release/Navitje.apk` (Android 5.0+, brez dovoljenj).

## Gradnja

Brez Gradla; potrebni so JDK, `aapt2`, `dalvik-exchange` (dx), `zipalign`, `apksigner`
(Ubuntu: `apt install aapt apksigner zipalign dalvik-exchange`) in `tools/android.jar` (API 34).

```
./build.sh
```

Skripta ob prvem zagonu ustvari podpisni ključ `tools/release.jks`. Ključ ni v repozitoriju;
APK, podpisan z drugim ključem, se ne namesti čez obstoječo aplikacijo (najprej jo je treba odstraniti).
