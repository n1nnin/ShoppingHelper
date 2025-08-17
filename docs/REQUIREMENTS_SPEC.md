🛒 買い物支援アプリ仕様書
1. アプリケーション概要
   1.1 基本情報
   アプリ名: （未定）
   プラットフォーム: iOS / Android
   開発フレームワーク: Kotlin Multiplatform / Compose Multiplatform
   主要機能: 位置情報連動型買い物リスト管理アプリ
   1.2 コンセプト
   買い物リストと位置情報を連動させ、お店の近くに来たら通知する機能を中心とした、日常の買い物を効率化するアプリケーション

2. 機能要件
   2.1 マップ・位置情報機能
   2.1.1 お店の自動検出
   Google Places APIを使用して近隣の店舗を自動検出
   対象店舗：スーパーマーケット、薬局、コンビニエンスストア等
   検出範囲：現在地から半径2km以内（設定で変更可能）
   2.1.2 カスタムお店登録
   ユーザーによる手動店舗登録機能
   登録項目：
   店舗名（必須）
   住所・位置情報（必須）
   カテゴリ（任意）
   メモ（任意）
   2.1.3 お店の詳細情報
   営業時間の表示・編集
   連絡先（電話番号）の登録
   個人的なメモ機能
   お気に入り登録機能
   2.1.4 ルート案内
   複数店舗を効率的に回るルート提案
   Google Maps / Apple Mapsとの連携
   推定所要時間の表示
   2.2 買い物リスト機能
   2.2.1 リスト管理
   カテゴリ別整理（食品、日用品、薬品等）
   カスタムカテゴリの作成
   ドラッグ＆ドロップによる並び替え
   2.2.2 アイテム詳細設定
   価格メモ機能
   数量・単位設定（例：牛乳 1L、りんご 3個）
   優先度設定（高/中/低）
   購入予定店舗の指定
   商品写真の添付（将来機能）
   バーコード情報の紐付け（将来機能）
   2.2.3 テンプレート機能
   よく買う商品セットの保存
   テンプレートからの一括追加
   テンプレートの共有機能
   2.3 通知機能
   2.3.1 位置情報連動通知
   Geofence技術による店舗接近通知
   通知範囲の設定（50m〜500m）
   通知する時間帯の設定
   2.3.2 リマインダー機能
   時間指定通知
   定期的な買い物リマインダー
3. 非機能要件
   3.1 パフォーマンス
   アプリ起動時間：3秒以内
   地図表示：スムーズなスクロール（60fps）
   オフライン時の基本機能利用
   3.2 セキュリティ
   位置情報のプライバシー保護
   データの暗号化保存
   セキュアな通信（HTTPS）
   3.3 ユーザビリティ
   直感的なUI/UX
   アクセシビリティ対応
   ダークモード対応
4. 技術仕様
   4.1 アーキテクチャ
   kotlin

// 共通ビジネスロジック例
expect class LocationManager {
fun getCurrentLocation(): Location
fun startGeofencing(shops: List<Shop>)
}

// 共通UI（Compose Multiplatform）
@Composable
fun ShoppingMapScreen(
viewModel: ShoppingMapViewModel
) {
// iOS/Android共通のUI実装
}
4.2 使用技術
領域	技術スタック
データ永続化	SQLDelight, Room
ネットワーク	Ktor Client
状態管理	Compose Navigation, ViewModel
地図	Google Maps SDK (Android), MapKit (iOS)
プッシュ通知	Firebase Cloud Messaging
位置情報	Android Location Services, Core Location (iOS)
5. 将来的な拡張機能
   5.1 無料版拡張機能
   5.1.1 基本的な利便性向上
   音声入力対応
   カテゴリ別アイコン表示
   お店のロゴ表示
   店舗レビュー・評価
   近隣ユーザーとの情報共有（読み取り専用）
   
   5.1.2 データ分析（基本）
   購入頻度の基本統計
   よく行く店舗の表示
   
   5.2 プレミアム版拡張機能（月額300-500円想定）
   5.2.1 ビジュアル機能
   買い物アイテムの写真添付
   商品バーコードスキャン
   高解像度マップ表示
   カスタムテーマ・デザイン
   
   5.2.2 AI・機械学習機能
   購入パターンの学習と提案
   在庫切れ予測
   価格変動トラッキングと通知
   スマート買い物ルート提案
   
   5.2.3 ソーシャル・共有機能
   家族・グループでのリスト共有
   リアルタイム同期
   共有テンプレート作成・配布
   メンバー間でのタスク割り当て
   
   5.2.4 高度な便利機能
   レシート撮影によるOCR自動入力
   家庭内在庫管理
   無制限のテンプレート保存
   データエクスポート機能
   広告非表示
   優先サポート
   
   5.2.5 ビジネス・業務利用機能
   複数店舗の一括管理
   購入履歴の詳細分析
   予算管理・支出追跡
   CSVデータのインポート/エクスポート
6. 開発フェーズ
   Phase 1: MVP（最小限の製品）
   基本的な買い物リストCRUD機能
   シンプルな地図表示
   手動での店舗登録
   基本的なGeofence通知
   Phase 2: 実用性向上
   Google Places API統合
   データ同期機能
   複数デバイス対応
   バックアップ・復元機能
   Phase 3: 高度な機能
   AI機能の実装
   ソーシャル機能
   パフォーマンス最適化
   高度なUIカスタマイズ
7. 画面構成
   7.1 主要画面
   ホーム画面: 買い物リストと地図の切り替え
   マップ画面: 店舗位置と現在地表示
   リスト画面: 買い物リストの管理
   店舗詳細画面: 店舗情報の表示・編集
   設定画面: 通知設定、プライバシー設定等
   7.2 画面遷移
   mermaid

graph LR
A[ホーム] --> B[マップ]
A --> C[リスト]
B --> D[店舗詳細]
C --> E[アイテム詳細]
A --> F[設定]
8. データモデル
   8.1 主要エンティティ
   kotlin

data class Shop(
val id: String,
val name: String,
val location: Location,
val category: String,
val openingHours: List<OpeningHour>,
val notes: String?
)

data class ShoppingItem(
val id: String,
val name: String,
val quantity: Int,
val unit: String,
val price: Double?,
val priority: Priority,
val shopId: String?,
val imageUrl: String? = null, // 将来的な拡張: アイテム写真
val barcode: String? = null   // 将来的な拡張: バーコード
)

data class ShoppingList(
val id: String,
val name: String,
val items: List<ShoppingItem>,
val createdAt: DateTime,
val updatedAt: DateTime
)
9. テスト要件
   9.1 単体テスト
   ビジネスロジックのテストカバレッジ: 80%以上
   共通コードのテスト
   9.2 統合テスト
   API連携テスト
   データベース操作テスト
   9.3 UIテスト
   主要な画面遷移
   ユーザー操作フロー
10. リリース計画
    10.1 ベータ版
    Phase 1完了後
    限定ユーザーでのテスト
    10.2 正式版
    Phase 2完了後
    App Store / Google Playでの公開

11. CI/CD環境
    11.1 継続的インテグレーション (GitHub Actions)
    11.1.1 ビルドパイプライン
    プルリクエスト時の自動ビルド
    マルチプラットフォーム並列ビルド（Android/iOS）
    ビルドキャッシュの活用
    アーティファクトの保存（APK/AAB）
    
    11.1.2 自動テスト
    単体テストの実行（全プラットフォーム）
    UIテスト（Compose UI Testing）
    テストカバレッジレポート生成
    テスト結果のPRコメント投稿
    
    11.1.3 静的解析
    Kotlin linter (ktlint/detekt)
    コード品質チェック
    セキュリティスキャン（dependency check）
    ライセンスチェック
    
    11.1.4 リリース自動化
    バージョンタグによる自動リリース
    リリースノート自動生成
    Play Console/App Store Connect へのアップロード準備
    署名済みAPK/AABの生成
    
    11.2 ワークフロー設定
    ```yaml
    # .github/workflows/ci.yml
    name: CI/CD Pipeline
    
    on:
      push:
        branches: [ main, develop ]
      pull_request:
        branches: [ main ]
      release:
        types: [ created ]
    
    jobs:
      test:
        runs-on: ubuntu-latest
        steps:
          - uses: actions/checkout@v4
          - uses: actions/setup-java@v4
            with:
              java-version: '17'
          - name: Run Tests
            run: ./gradlew test
          
      build-android:
        runs-on: ubuntu-latest
        steps:
          - uses: actions/checkout@v4
          - name: Build Android
            run: ./gradlew assembleRelease
            
      build-ios:
        runs-on: macos-latest
        steps:
          - uses: actions/checkout@v4
          - name: Build iOS Framework
            run: ./gradlew linkReleaseFrameworkIosArm64
    ```
    
    11.3 環境変数・シークレット管理
    GitHub Secrets での管理項目：
    - MAPS_API_KEY: Google Maps APIキー
    - KEYSTORE_FILE: Android署名用キーストア（Base64）
    - KEYSTORE_PASSWORD: キーストアパスワード
    - KEY_ALIAS: キーエイリアス
    - KEY_PASSWORD: キーパスワード
    - PLAY_SERVICE_ACCOUNT_KEY: Google Play Console API
    - APP_STORE_CONNECT_API_KEY: App Store Connect API
    
    11.4 ブランチ戦略 (Trunk-Based Development)
    - main: 単一の統合ブランチ（常にデプロイ可能な状態を維持）
    - feature/*: 短命な機能ブランチ（1-2日以内にマージ）
    - hotfix/*: 緊急修正用の短命ブランチ
    
    運用ルール：
    - mainブランチは常にリリース可能な状態を保つ
    - 機能ブランチは小さく、頻繁にマージ（最長でも2日以内）
    - フィーチャーフラグで未完成機能を制御
    - 全ての変更はPR経由でmainへマージ
    - リリースはmainブランチのタグから実施
    
    11.5 品質ゲート
    プルリクエストマージ条件：
    - 全テスト成功
    - コードカバレッジ 80%以上
    - 静的解析エラーなし
    - コードレビュー承認（1名以上）
    - ビルド成功（全プラットフォーム）
    
    11.6 モニタリング・通知
    - ビルド失敗時のSlack/Discord通知
    - デプロイ完了通知
    - 週次ビルドレポート
    - 依存関係更新通知（Dependabot）
    
    11.7 パフォーマンス最適化
    - Gradle Build Cacheの活用
    - GitHub Actions Cacheの設定
    - 並列ジョブ実行
    - インクリメンタルビルド
    - 不要なステップのスキップ（path filters）