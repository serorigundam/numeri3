# numeri3
RxJava2とかankoとかMVP試すためにTwitterクライアント作る(n回目)

## 開発環境
* gradle3.3
* kotlin-1.0.5-3
* android studio2.3-beta3

## 必要なもの
* 署名用にkeytool使ってrelease.jksとdebug.jksをプロジェクトルートに配置
* release.jksのstorePasswerdとkeyPasswordそれぞれを同名のプロパティとして設定したsigning.propertiesを配置
* strings-twitter.xmlをこんな感じで作ってapp/src/main/res/values/strings-twitter.xmlに配置

'''xml
<resources>
    <string name="twitter_api_key">hoge_api_key</string>
    <string name="twitter_secret_key">hoge_secret_key</string>
    <string name="twitter_callback_host">hoge_host</string>
    <string name="twitter_callback_scheme">hoge_scheme</string>
</resources>
'''

