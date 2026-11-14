---
name: active-scan
description: 主动端口与服务扫描（nmap/masscan），严格遵守授权范围
whenToUse: 需要确认资产存活与开放端口时
role: recon
enabled: true
---

<!-- PORTABLE-BINDING
本技能原文引用 dsh 的 redteam_* 工具；在非 dsh 环境用等价的 `rt` CLI：
  redteam_asset_add        → rt asset add
  redteam_asset_query      → rt asset query
  redteam_asset_get        → rt asset get --id IP|ID
  redteam_asset_test       → rt asset test --id IP --status ... --test ...
  redteam_asset_assess     → rt asset assess --id IP --priority ... --potential ... --reason ...
  redteam_asset_stats      → rt asset stats
  redteam_vuln_add         → rt vuln add
  redteam_vuln_query       → rt vuln query
  redteam_vuln_update      → rt vuln update --id N --status ...
  redteam_credential_add   → rt cred add
  redteam_credential_list  → rt cred list
  redteam_access_add       → rt access add
  redteam_access_list      → rt access list
  redteam_webshell_add     → rt webshell add --url ... --type behinder|godzilla --pass-key ...
  redteam_webshell_list    → rt webshell list
  redteam_tunnel_add       → rt tunnel add --kind ... --listen ... --entry-kind target-http|target-outbound|target-agent|self-only
  redteam_tunnel_list      → rt tunnel list
  redteam_tunnel_update    → rt tunnel update --id N --status ...
  redteam_session_check    → rt session check
  redteam_sessions         → rt session list
  redteam_chain_add        → rt chain add --stage-code ... --title ... --tool ... --result ...
  redteam_chain            → rt chain list
  redteam_score_hit        → rt score hit --code ... --points ... --target ... --evidence ...
  redteam_score_list       → rt score list
  redteam_http_evidence_add→ rt evidence add --label ... --request ...
  redteam_poc_search       → rt poc search --q ...
  redteam_poc_add          → rt poc add --title ... --content ...
  redteam_poc_get          → rt poc get --code ...
  redteam_preflight        → rt preflight
  redteam_engagement_open  → rt engage open --target "..."
-->

## 前置检查
1. 确认目标 CIDR 在授权范围内。
2. 确认当前时间窗口允许主动扫描。

## 命令
- 快速存活：`nmap -sn <cidr>`
- 全端口（限速）：`nmap -sS -p- --min-rate 1000 -T4 <target> -oX runs/nmap-full.xml`
- 服务版本：`nmap -sV -sC -p <ports> <target> -oX runs/nmap-svc.xml`

## 本地已备好的工具（绝对路径，优先用它们，不要手搓）
| 用途 | 命令 |
| --- | --- |
| 端口扫描 | `nmap` / `masscan` / `$RT_TOOLKIT/naabu/naabu`（SYN 需 root，否则加 `-scan-type c`） |
| 内网综合扫描 | `$RT_TOOLKIT/fscan/fscan`（技能 fscan-intranet） |
| 内网测绘/指纹 | `$RT_TOOLKIT/gogo/gogo`（技能 gogo-intranet） |
| HTTP 探测（ProjectDiscovery） | `~/.local/bin/pd-httpx`（**注意：`/usr/bin/httpx` 是 Python httpx 库的 CLI，不是这个**） |
| 子域/解析 | `$RT_TOOLKIT/subfinder/subfinder`、`$RT_TOOLKIT/dnsx/dnsx` |
| POC 扫描 | `nuclei`（模板已在 `~/.local/nuclei-templates`） |
| 目录爆破 | `ffuf`、`$RT_TOOLKIT/dirsearch/dirsearch` |
| 隧道 | `$RT_TOOLKIT/suo5/suo5-linux-amd64`、`toolkit/chisel/chisel`、`toolkit/frp/{frpc,frps}` |
| 完整清单 | `$RT_TOOLKIT/清单.md`、`技能工具清单.md` |

`setup.sh` 不下载 dirsearch。`清单.md` 是说明文件，缺了不代表扫描器没装。Windows 与 ARM 构建也不在安装脚本里。

## 落库要求
provenance = `active`，tool = `nmap`/`masscan`，记录 scan_run。
