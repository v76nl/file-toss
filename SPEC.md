# file-toss 詳細設計書 (SPEC.md)

## 1. プロジェクト概要

### 1.1 プロジェクト名
- **リポジトリ名 / アプリ名**: `file-toss`
- **対象プラットフォーム**: Windows 11 (64-bit)
- **開発言語 / フレームワーク**: Kotlin 2.x + Compose Multiplatform for Desktop (JVM)

### 1.2 背景と目的
従来のファイル転送GUI (WinSCP等) は多機能である一方、UIがクラシックで情報量が多く、現代のデスクトップ環境では煩雑に感じられる課題があった。
また、CLIでの `scp` コマンドは手軽であるものの、毎回ホスト名や長いパスを入力する手間が発生する。
`file-toss` は、**「接続先とパスをあらかじめ登録し、ドラッグ＆ドロップで軽快に投げる (TOSS) / 受信する (CATCH)」** という単一目的に特化し、**クラシック・バウハウス様式の幾何学的で動的なデザイン** を備えた、実用性と美しさを両立するモダンなファイル送受信GUIツールである。

### 1.3 設計方針
- **Form follows function (機能主義)**: 余計な装飾を排し、幾何学的なグリッドとタイポグラフィで直感的な操作性を実現する。
- **プロトコル中立・拡張性**: 将来のSCPやクラウドストレージ対応を見据え、転送エンジンをインターフェースとして完全に抽象化する (MVPはSFTPを実装)。
- **ゼロコンフィグ感覚の操作性**: 接続先プロファイルを選んだら、あとはファイルを放り込むだけで転送が完了する。

---

## 2. アーキテクチャ設計

### 2.1 レイヤー構造

```
┌─────────────────────────────────────────────────────────────┐
│                       Presentation Layer                    │
│   Compose Multiplatform Desktop (Material 3 / Bauhaus Theme)│
│   - MainScreen (TOSS / CATCH Mode)                          │
│   - DropZoneComponent (DnD Handler, Bauhaus Animation)      │
│   - RemoteBrowserComponent (CATCH Grid Cards)               │
│   - ProfileSidebar (Sliding Grid Drawer)                    │
│   - ViewModels (StateFlow, MVI Pattern)                     │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                         Domain Layer                        │
│   - Models (Profile, TransferTask, RemoteFile, Credentials) │
│   - TransferClient (Interface for Protocol Abstraction)    │
│   - UseCases (TossFileUseCase, CatchFileUseCase, etc.)      │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                         Data Layer                          │
│   - SftpTransferClient (Implementation using sshj)          │
│   - ProfileRepository (JSON Persistence via kotlinx)        │
│   - CredentialStore (Windows DPAPI / Credential Manager)    │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 プロトコル抽象化 (Protocol Abstraction)
将来的にSCPや他プロトコルへ差し替え・拡張できるよう、転送層はインターフェースで疎結合にする。

```kotlin
// ドメイン層インターフェース
interface TransferClient : AutoCloseable {
    val protocol: TransferProtocol
    val isConnected: Boolean

    suspend fun connect(profile: TransferProfile, credentials: AuthCredentials)
    suspend fun disconnect()

    /**
     * ファイル/ディレクトリをリモートへ送信 (TOSS)
     */
    suspend fun upload(
        localPath: Path,
        remoteDirectory: String,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult

    /**
     * リモートファイルをローカルへ受信 (CATCH)
     */
    suspend fun download(
        remoteFilePath: String,
        localDirectory: Path,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult

    /**
     * リモートディレクトリのファイル一覧取得 (CATCH画面用)
     */
    suspend fun listRemoteFiles(remotePath: String): List<RemoteFileInfo>
}

enum class TransferProtocol {
    SFTP,
    SCP // 将来実装用
}
```

### 2.3 主要採用ライブラリ
| 種別 | ライブラリ | バージョン目安 | 用途 |
|---|---|---|---|
| UI Framework | `org.jetbrains.compose` | 1.7+ | Compose for Desktop, Material 3 |
| SSH/SFTP Client | `com.hierynomus:ssh-j` | 0.39+ | SFTP接続・ファイル転送・進捗取得 |
| 非同期処理 | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 1.8+ | バックグラウンド転送・UI連携 |
| シリアライズ | `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.6+ | プロファイル設定のJSON保存 |
| 安全な資格情報保存 | `net.java.dev.jna:jna` / `jna-platform` | 5.14+ | Windows Credential Manager / DPAPI連携 |

---

## 3. UI/UX & バウハウスデザイン仕様

### 3.1 カラーパレット (Classic Bauhaus Palette)
オフホワイトを基調に、原色の幾何学ブロックと力強い黒のグリッド線で構成する。

| 名称 | HEX値 | 用途 |
|---|---|---|
| **Bauhaus Chalk (基調色)** | `#F5F3EC` | アプリ全体の背景色、余白 |
| **Bauhaus Frame Black** | `#1A1A1A` | グリッドの境界線 (2.5px〜3px)、主要フォント |
| **Bauhaus Crimson Red** | `#D93829` | TOSS (送信) のアクセント、アクティブ枠、警告 |
| **Bauhaus Cobalt Blue** | `#205493` | CATCH (受信) のアクセント、進行バー、接続状態 |
| **Bauhaus Cadmium Yellow** | `#F3BE22` | 注意、プロファイルタグ、幾何学アクセント |
| **Bauhaus Paper White** | `#FFFFFF` | カードや入力エリアの背景 |

### 3.2 タイポグラフィ
- **書体**: サンセリフ幾何学フォント (Roboto, Inter, または Futura系統のシステムフォント)
- **大見出し**: Heavy / Bold、アッパーケース (`TOSS`, `CATCH`, `PROFILE`, `CONNECTING...`)
- **グリッド数字・目盛り**: Monospace / 太字 (転送バイト数、速度表示)

### 3.3 アニメーション仕様 (Mechanical Grid + Poster Line)
1. **メカニカル・グリッド展開 (サイドバー)**:
   - 設定/プロファイルボタンを押すと、メイン画面が黒の太枠ごと左にスライドし、右から幾何学ブロックで構成されたプロファイル管理パネルがカチッと展開する。
   - イージング: キレのあるスナップ感 (`FastOutSlowInEasing` または `CubicBezier(0.2, 0.0, 0.0, 1.0)`)
2. **タイポグラフィ＆ライン・モーフィング (TOSS ⇔ CATCH 切替)**:
   - モード切替時、上部の極太文字 (`TOSS` / `CATCH`) と下線の太い黒ラインが勢いよく左右へスライド＆変形する。
   - 背景のアクセントカラーブロックが赤 (TOSS) から青 (CATCH) へメカニカルに切り替わる。
3. **ドロップゾーン (キネティック・メカニズム)**:
   - **待機時**: 中央に太枠の幾何学グリッドと十字目盛り、`DROP FILES TO TOSS` のボールド文字。
   - **ドラッグオーバー時**: 外枠がバウハウスレッドに反転し、枠線が点線から実線へ伸縮、四隅の幾何学ブロックが中央に向かってスライドして受け入れ態勢をとる。
   - **ドロップ時**: ファイルが中心の四角形に吸い込まれるようなスケールダウン演出。
   - **転送中**: 幾何学的なバーと目盛りがリズミカルに拍動・充填され、進捗率 (%) がメカニカルにカウントアップ。
   - **完了時**: バウハウスイエローとブルーの図形が弾け、太字で `TOSSED!` とスナップ表示。

---

## 4. 画面詳細仕様

### 4.1 メイン画面構成

```
┌─────────────────────────────────────────────────────────────┐
│ [≡ PROFILES]  [ T O S S ]   [ C A T C H ]      [ ● ONLINE ] │  <-- 上部ヘッダー
├─────────────────────────────────────────────────────────────┤
│ Target: [ Production-Web ] -> /var/www/html/assets          │  <-- 現在の宛先
├─────────────────────────────────────────────────────────────┤
│                                                             │
│    ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐             │
│    │  +                                     +  │             │
│    │                                           │             │
│    │              ■  ▲  ●                      │             │
│    │                                           │             │
│    │         DROP FILES HERE TO TOSS           │             │
│    │                                           │             │
│    │  +                                     +  │             │
│    └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘             │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│ [ PROGRESS: 45.2 MB / 120 MB | 12.4 MB/s | ========>---- ]  │  <-- 転送状態
└─────────────────────────────────────────────────────────────┘
```

#### A. TOSS モード (送信・アップロード)
- 中央の巨大なドロップゾーンに Windows エクスプローラーからファイル・フォルダを直接ドラッグ＆ドロップ可能。
- ドロップされた瞬間、選択中のプロファイルの `remoteDirectory` に向けて自動転送が開始。
- 単一ファイル、複数ファイル、およびフォルダ全体の再帰的アップロードに対応。

#### B. CATCH モード (受信・ダウンロード)
- 接続先サーバーの `remoteDirectory` 内にあるファイル/ディレクトリが、バウハウス調の幾何学カード一覧として表示される。
- 各カードにはファイル名、サイズ、更新日時が表示。
- カードをクリック、または「CATCH」ボタンを押すことで、指定されたローカルディレクトリ (`localDirectory`) にダウンロードされる。
- リストの更新ボタン（幾何学回転アイコン）で最新状態を再取得。

#### C. プロファイル・スライドインサイドバー
- 左端の `[≡ PROFILES]` を押すとスライド展開。
- 保存済みプロファイル一覧：
  - 各プロファイルに固有の幾何学識別アイコン（赤の円、青の四角、黄の三角など）。
  - クリックでアクティブプロファイルを即時切り替え（接続テストも自動実行）。
- プロファイル編集 / 新規追加フォーム：
  - プロファイル名 (例: `GPU-Server`, `Dev-Frontend`)
  - プロトコル (SFTP固定 / 将来選択可能)
  - ホスト名 / IPアドレス
  - ポート番号 (デフォルト: `22`)
  - ユーザー名
  - 認証方式 (秘密鍵 / パスワード)
    - 秘密鍵の場合: 鍵ファイルパスの参照ボタン、パスフレーズ入力
    - パスワードの場合: パスワード入力
  - デフォルト・リモートディレクトリ (送信先パス)
  - デフォルト・ローカルディレクトリ (受信先パス)
  - 保存ボタン / 接続テストボタン

---

## 5. データモデル定義

```kotlin
package com.filetoss.model

import kotlinx.serialization.Serializable

@Serializable
data class TransferProfile(
    val id: String,
    val name: String,
    val protocol: TransferProtocol = TransferProtocol.SFTP,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType,
    val privateKeyPath: String? = null,
    val remoteDirectory: String,
    val localDirectory: String,
    val colorTag: BauhausColor = BauhausColor.RED
)

enum class AuthType {
    PASSWORD,
    PRIVATE_KEY
}

enum class BauhausColor {
    RED,
    BLUE,
    YELLOW
}

data class AuthCredentials(
    val password: String? = null,
    val passphrase: String? = null
)

data class TransferProgress(
    val currentFileName: String,
    val transferredBytes: Long,
    val totalBytes: Long,
    val bytesPerSecond: Long,
    val isComplete: Boolean = false
) {
    val percentage: Float
        get() = if (totalBytes > 0) transferredBytes.toFloat() / totalBytes else 0f
}

data class RemoteFileInfo(
    val name: String,
    val fullPath: String,
    val size: Long,
    val isDirectory: Boolean,
    val modifiedTime: Long
)
```

---

## 6. 実装ロードマップ & タスク分解 (Agent向け手順)

開発エージェントが自律的にタスクを遂行できるよう、フェーズごとに実装タスクを分解する。

### Phase 1: プロジェクト基盤の構築
1. **Gradle プロジェクト初期化**:
   - Compose Multiplatform (Desktop) プラグイン設定
   - JVMターゲット (Java 17 or 21)
   - 依存関係の追加 (`sshj`, `kotlinx-coroutines`, `kotlinx-serialization`, `jna`)
2. **パッケージ構成**:
   - `com.filetoss.domain`
   - `com.filetoss.data`
   - `com.filetoss.ui.theme` (バウハウスカラー・タイポグラフィ)
   - `com.filetoss.ui.components` (ドロップゾーン、サイドバー、カード)
   - `com.filetoss.ui.screens`

### Phase 2: データ層・通信エンジンの実装
1. **`TransferClient` インターフェース定義**
2. **`SftpTransferClient` の実装 (sshj)**:
   - 接続・切断管理 (キーペア検証、パスワード認証)
   - アップロード処理 (ディレクトリの再帰的アップロード対応)
   - ダウンロード処理
   - リモートファイル一覧取得 (`ls`)
   - コールバックによる進捗通知 (`TransferProgress`)
3. **`ProfileRepository` の実装**:
   - `%APPDATA%/file-toss/profiles.json` への読み書き
4. **安全なクレデンシャル管理**:
   - パスワード / パスフレーズの暗号化保存 (Windows DPAPI / Credential Manager)

### Phase 3: バウハウスUI基盤 & アニメーション
1. **テーマ定義 (`BauhausTheme`)**:
   - カラー定義 (`ChalkWhite`, `FrameBlack`, `CrimsonRed`, `CobaltBlue`, `CadmiumYellow`)
   - 極太ボーダー (`Modifier.border(2.5.dp, FrameBlack)`)
   - 幾何学シェイプ (正方形、円、鋭角三角形)
2. **キネティック・コンポーネント**:
   - `BauhausButton`: カチッとした沈み込みアニメーション
   - `ModeToggle`: `TOSS` と `CATCH` の太字スライド切替アニメーション
   - `MechanicalProgressBar`: 目盛り付きの幾何学的プログレスバー

### Phase 4: 機能実装 (TOSS & CATCH & サイドバー)
1. **TOSS 画面 (ドラッグ＆ドロップ)**:
   - Compose DesktopのファイルDnDイベント検知
   - ドラッグオーバー時の幾何学モーフィング
   - ドロップ時のアップロード開始 ＆ 進捗バー表示
2. **CATCH 画面 (リモートファイル受信)**:
   - リモートファイル一覧のカード表示
   - カードクリックでのローカルダウンロード
3. **プロファイル・スライドサイドバー**:
   - スライドイン/アウトのアニメーション
   - プロファイルの追加・編集・削除UI

### Phase 5: 配布パッケージング & 仕上げ
1. **インストーラー生成設定**:
   - `packageMsi` / `packageExe` の設定 (JREバンドル)
   - アプリアイコン (バウハウス調の幾何学アイコン)
2. **動作検証**:
   - ローカル/リモートSFTPサーバーでのファイル送受信テスト
   - 特殊文字・大容量ファイル・日本語ファイル名転送の検証

---

## 7. コーディング規約 (共通ルール)
- 日本語文では、全角丸括弧 `（）` ではなく半角丸括弧 `()` を使用する。
- 三点リーダーには「……」を使用する。
- コード内コメントに罫線装飾は不要とし、です・ます調は使用しない。
- コミットは Angular 準拠のタイトル形式 (`feat: ...`, `fix: ...`) とし、日本語で簡潔に記述する。
