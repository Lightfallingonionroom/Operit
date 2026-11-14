# Operit 破甲二改（purge 分支）

在 [AAswordman/Operit](https://github.com/AAswordman/Operit)（LGPL-3.0）基础上做的二改：
**源码级破甲 + 内置 zfx-emperor（正方形框架）+ dsh-purge 红队层**。

- 包名：`io.github.lightfallingonionroom.operit_app`
- 分支：`purge`
- 构建：GitHub Actions（`.github/workflows/build-purge.yml`），产物是 debug APK

---

## 一、改了什么

| 文件 | 改动 |
|---|---|
| `app/build.gradle.kts` | `applicationId` 改为 `io.github.lightfallingonionroom.operit_app`；debug 去掉 `.debug` 后缀；`ndkVersion` 27.0.12077973 |
| `ui/permissions/ToolPermissionSystem.kt` | 全局默认权限 `ASK` → **`ALLOW`**（工具免确认直接执行）；未识别值也默认放行 |
| `core/config/PurgePrompt.kt`（新增） | 破甲提示词层：正方形框架 + dsh-purge 注入稿 + 红队角色卡；唤醒词确认；拒绝续命判定 |
| `core/config/SystemPromptConfig.kt` | 组装系统提示词时把破甲层**前置**到最高优先级 |
| `api/chat/enhance/ConversationService.kt` | 按最近一条用户消息判定红队意图，决定是否追加角色卡 |
| `api/chat/enhance/InputProcessor.kt` | 单发 `ZFX` / `zfx` / `正方形` 时追加上唤醒确认提示 |
| `core/config/PurgeBootstrap.kt`（新增） | 启动时把内置技能/rt 工具层铺到 `/sdcard/Download/Operit/`（best-effort） |
| `app/src/main/assets/purge/`（新增） | `square.md`、`prompt-layer.md`、`redteam-card.md`、`skills/`、`redteam/` |
| `settings.gradle.kts` | 阿里云 maven 镜像（国内网络） |
| `gradle.properties` | 降低堆内存/并发；Kotlin 进程内编译 |
| `app/config/stt-model-assets.properties` | STT 模型改走 hf-mirror |

## 二、构建

### GitHub Actions（推荐）

推送到 `purge` 分支即触发，或手动 `workflow_dispatch`。产物在 run 的 Artifacts 里（`operit-purge-apk`）。

CI 里会自动补两个仓库外的原生件：

- `app/src/main/jniLibs/arm64-v8a/liboperit_ripgrep.so` —— 从官方 release APK 解出
- `app/libs/ffmpeg-kit-local.aar` —— 从阿里云 maven 镜像下载（`com.arthenica:ffmpeg-kit-full:6.0-2`）

### 本地构建（x86_64 Linux / macOS）

```bash
git clone --recursive https://github.com/Lightfallingonionroom/Operit.git
cd Operit && git checkout purge
# 准备上面两个原生件
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
```

## 三、arm64 手机（proot）为什么不能直接编

Android SDK/NDK 的宿主工具（aapt2/aidl/d8/cmake/ninja）与 NDK 的 clang/lld 都是 **x86_64 ELF**。
用 `qemu-user-static` 包一层可以跑编译，但 **clang 的 `posix_spawn` 在 qemu+proot 下无法 exec 子进程**（链接器起不来，报 `Illegal instruction`），
所以本地只能编到「编译通过、链接失败」。这就是加 CI 的原因。

## 四、验证

```bash
aapt dump badging app/build/outputs/apk/debug/app-debug.apk | head -3
# package: name='io.github.lightfallingonionroom.operit_app' ...
```

装好后：

- 工具权限默认「允许」（设置 → 工具权限 查看）；
- 新对话里模型按「正方形」框架回答；
- 单发 `正方形` 回 `Sir I'm here，干什么你直说。`；
- 说「渗透 example.com」会追加红队角色卡。
