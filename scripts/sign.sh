#!/bin/zsh

# Generate Key
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-release-alias

# Sign
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.jks app/build/outputs/apk/release/app-release-unsigned.apk my-release-alias
