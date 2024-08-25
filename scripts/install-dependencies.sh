#!/bin/zsh

# Install Android SDK and Java 17 on Mac
brew install android-commandlinetools openjdk@17

export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.12/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools/

sdkmanager --install 'platforms;android-34'
sdkmanager --install 'build-tools;33.0.1'
sdkmanager --install 'platform-tools'

# ./gradlew build
