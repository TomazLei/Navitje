#!/usr/bin/env bash
# Builds Navitje.apk without Gradle. Needs: JDK, aapt2, dalvik-exchange (dx), zipalign, apksigner, tools/android.jar
set -euo pipefail
cd "$(dirname "$0")"
A=tools/android.jar; O=build; rm -rf $O; mkdir -p $O/gen $O/cls $O/dex
aapt2 compile --dir app/res -o $O/res.zip
aapt2 link -I $A --manifest app/AndroidManifest.xml --java $O/gen -o $O/base.apk $O/res.zip
javac --release 8 -Xlint:-options -cp $A -d $O/cls $(find app/src $O/gen -name '*.java')
dalvik-exchange --dex --min-sdk-version=21 --output=$O/dex/classes.dex $O/cls
(cd $O/dex && zip -q -j ../base.apk classes.dex)
zipalign -f 4 $O/base.apk $O/aligned.apk
[ -f tools/release.jks ] || keytool -genkeypair -keystore tools/release.jks -storepass navitje -keypass navitje \
  -alias navitje -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Navitje" 2>/dev/null
apksigner sign --ks tools/release.jks --ks-pass pass:navitje --key-pass pass:navitje --out Navitje.apk $O/aligned.apk
apksigner verify --verbose Navitje.apk | head -5
