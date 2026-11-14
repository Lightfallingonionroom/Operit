---
name: web-fingerprint
description: Web 服务指纹识别：框架、中间件、CMS、组件版本
whenToUse: 已发现 HTTP/HTTPS 服务，需要识别技术栈时
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

## 方法
1. 响应头：`Server`、`X-Powered-By`、`Set-Cookie` 特征。
2. 页面特征：favicon 哈希、静态资源路径、报错页、robots.txt。
3. 主动探测：nuclei 技术识别模板 `nuclei -u <url> -tags tech`；HTTP 存活与技术栈汇总用 `~/.local/bin/pd-httpx -l urls.txt -tech-detect -title -status-code -web-server`（**ProjectDiscovery 版必须用 pd-httpx，`/usr/bin/httpx` 是 Python 库的 CLI**）。
4. 大网段批量指纹用技能 `gogo-intranet`（`gogo -i <cidr> -p top2 -v --af`，主动指纹要加 `-v`）。
5. 版本比对：从指纹推断产品与版本，为漏洞检测做准备。

## 落库要求
写入 fingerprint 表：category（框架/中间件/CMS/组件）、vendor、product、version、evidence。
