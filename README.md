# numeri3
RxJava2とかankoとかMVP試すためにTwitterクライアント作る(n回目)

## 開発環境
* gradle3.3
* kotlin-1.0.5-3
* android studio2.3-beta3

## ビルド前に
* 署名用にkeytool使ってrelease.jksとdebug.jksを作成しプロジェクトルートに配置
* プロジェクトルートにあるsigning.propertiesないのプロパティそれぞれにrelease.jksのそれぞれのパスワードを設定
* twitterにサインアップしてAPIキーとか取得したらapp/src/main/res/values/strings-twitter.xmlにそれぞれの値を設定
