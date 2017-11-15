# numeri3
Dagger2+Android Architecture ComponentsでTwitterクライアント作成

## 開発環境
* Gradle4.2.1
* Kotlin-1.1.51
* Android Studio3.0

## ビルド前に
* 署名用にkeytool使ってrelease.jksとdebug.jksを作成しプロジェクトルートに配置
* debug.jksの設定はapp/build.gradleのsigningConfigs内のdebugを参照
* release.jksのstorePasswerdとkeyPasswordそれぞれを同名のプロパティとして設定したsigning.propertiesをルートに配置

例[signing.properties]
```properties
storePassword=hogehoge
keyPassword=hogehoge
```


* strings-twitter.xmlを作ってapp/src/main/res/values/strings-twitter.xmlに配置

例[strings-twitter.xml]
```xml
<resources>		
    <string name="twitter_api_key">hoge_api_key</string>
    <string name="twitter_secret_key">hoge_secret_key</string>
    <string name="twitter_callback_host">hoge_host</string>
    <string name="twitter_callback_scheme">hoge_scheme</string>
</resources>
```
