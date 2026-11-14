---
name: asset-correlation
description: 资产关联：域名↔IP↔C 段↔证书↔服务的图谱化
whenToUse: 信息收集阶段性收口，需要形成资产关系视图时
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

## 关联规则
- 域名解析 → `resolves` 边（domain → asset）
- C 段归属 → `contains` 边（segment → asset）
- 端口开放 → `exposes` 边（asset → port）
- 证书 SAN 共享 → `shares_cert` 边

## 落库要求
通过 redteam_asset_link 写入 edge 表；重复关系幂等。
