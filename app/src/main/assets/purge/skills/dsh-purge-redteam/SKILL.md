---
name: dsh-purge-redteam
description: 红队作战总入口：角色分工、五阶段流程、rt 工具层用法与落库纪律。做渗透/攻防演练/资产测绘/漏洞利用/内网横向时先读这个。
---

# dsh-purge 红队作战总入口

本技能是 dsh-purge 红队体系的**入口索引**。具体手法在其余 23 个技能里，
本文件只讲：**谁做、按什么顺序做、成果落到哪、怎么记分**。

## 一、工具层位置

红队台账用 `rt` CLI（零依赖 Python）。装好后：

```bash
export RT_HOME=/sdcard/Download/Operit/redteam
export PATH="$RT_HOME/bin:$PATH"
rt engage open --target "靶标单位名称"
```

**在 Operit 的 Ubuntu 终端里执行**（`environment="linux"`）。
`/sdcard` 在 Ubuntu 空间里是挂载可见的，可直接执行 `$RT_HOME/bin/rt`。

首次使用先自检：

```bash
rt preflight      # 列出缺的工具与环境变量（FOFA_KEY / REDTEAM_VPS_HOST）
```

缺 FOFA → 降级用 crt.sh + subfinder；缺 VPS → 先做不需要落地的成果（账号、数据、未授权）。
**缺什么一次性列给用户，不要硬开工。**

## 二、五个角色（按阶段顺序推进）

| 阶段 | stage_code | 角色 | 职责 | 收口标准 |
|---|---|---|---|---|
| ① 信息收集 | `recon` | 信息收集 | 只收资产，不碰漏洞 | 无新域名/新网段/新主体关联 |
| ② 互联网资产权限 | `internet` | 漏洞发现 → 漏洞利用 | 先发现后利用 | 能拿的得分点都判定过 |
| ③ 边界突破 | `boundary` | 漏洞利用 | 建隧道并实测可达内网 | 隧道 `legit=true` 且真的通 |
| ④ 内网资产权限 | `internal` | 内网渗透 | 走已有隧道做内网 | 网段挖全，无新存活 |
| ⑤ 靶标权限 | `target` | 漏洞利用/内网 | 集权/核心系统 | 目标系统拿下 |

**单会话就自己顺序切换角色；支持子智能体就按角色派活**（同一靶标最多 3 个并发）。

内网信息收集**必须用 `gogo-intranet` + `fscan-intranet`**，不要把外网那套 nmap 一台台扫的法子带进内网。
每发现一个新网段就再跑一轮，直到没有新网段、没有新存活。

## 三、落库纪律（没落库 = 没发生）

| 要记什么 | 命令 |
|---|---|
| 资产 | `rt asset add --ip ... --port ... --service ... --provenance active --tool nmap` |
| 评估 | `rt asset assess --id IP --priority high --potential "..." --reason "..."` |
| 测过状态 | `rt asset test --id IP --status tested --test "..." --surface "..."` |
| 漏洞 | `rt vuln add --title ... --severity ... --target ... --evidence ... --status confirmed` |
| 原始请求 | `rt evidence add --label ... --request "完整原始请求"` |
| 凭据 | `rt cred add --host ... --username ... --secret ... --privilege ... --source ...` |
| WebShell | `rt webshell add --url ... --type godzilla --pass-key ...` |
| 隧道 | `rt tunnel add --kind suo5 --listen 127.0.0.1:1080 --entry-kind target-http --reach "10.0.0.0/8"` |
| 攻击步骤 | `rt chain add --stage-code boundary --title ... --tool "实际命令" --result "回显"` |
| 得分 | `rt score hit --code server-host --points 50 --target IP:PORT --evidence "结果"` |

**每个关键动作必须写 `rt chain add`**：`--tool` 写实际命令原文、`--result` 写回显摘要。
报告里「账号怎么来的、隧道怎么搭的」全靠这两个字段。

## 四、记分要点

先 `rt score list` 读实际 code 与档位，**不要凭记忆写**。常用：

- `server-host` 服务器主机（普通 10 / 管理员 50）
- `web-app` Web 应用系统（管理员 100 / 普通 50）
- `db-credential` 数据库（管理员 50 / 普通·未授权 10）
- `central-system` 集权系统（管理员 500）
- `boundary-logical` 进入逻辑隔离内网（1000）

三条硬规矩：
1. **权限取高**：同一系统只按最高权限计一次，先记低档再记高档会自动顶替。
2. **账号必须实测登录**：只有凭据不算拿到账号；自己注册的账号加 `--self-created`，只留痕不计分。
3. **隧道必须跨边界**：自己 VPS 上开的代理填 `--entry-kind self-only`，不算突破。

到上限后 `rt score hit` 会返回 warning —— 当停止信号，换条目或换资产，不要在同一条上刷。

## 五、打之前先查库（禁止重复劳动）

```bash
rt asset query --sort priority    # 从 high 打起
rt vuln query                     # 已确认的不要重复验证
rt session list                   # 有没有现成 WebShell/隧道/凭据
```

`test_status=tested` 的资产不要重复扫；`abandoned`（被封 >3 次）直接跳过。

## 六、技能索引

| 阶段 | 技能 |
|---|---|
| 信息收集 | `passive-recon` `recon-pipeline` `fofa-recon` `active-scan` `web-fingerprint` `asset-correlation` |
| 漏洞发现 | `nuclei-scan` `unauth-exploit` `dir-bruteforce` `cn-proxy-pool` |
| 漏洞利用 | `credential-attack` `webshell-toolkit` `shell-handler` `vps-reverse-shell` |
| 内网渗透 | `gogo-intranet` `fscan-intranet` `lateral-movement` `suo5-tunnel` `chisel-tunnel` `frp-tunnel` |
| 辅助 | `browser-automation` `kimi-webbridge` `redteam-setup` |

## 七、交付

```bash
rt report --markdown > /sdcard/Download/redteam-report.md
```

只收录真正拿到分的成果，每条带：目标 → 拿到什么 → 怎么拿到的（命令）→ 原始请求。
**没拿到分的不进报告**；不确定的写成"待验证"，不要当成果交。
