## frontend
### Scan de sécurité npm audit  
undici  7.0.0 - 7.23.0  
Severity: high  
CVE-2026-1528: Undici: Malicious WebSocket 64-bit length overflows parser and crashes the client - https://github.com/advisories/GHSA-f269-vfmq-vjvj  
CVE-2026-1525: Undici has an HTTP Request/Response Smuggling issue - https://github.com/advisories/GHSA-2mjp-6q6p-2qxm  
CVE-2026-1526: Undici has Unbounded Memory Consumption in WebSocket permessage-deflate Decompression - https://github.com/advisories/GHSA-vrm6-8vpv-qv8q  
CVE-2026-2229: Undici has Unhandled Exception in WebSocket Client Due to Invalid server_max_window_bits Validation - https://github.com/advisories/GHSA-v9p9-hfj2-hcw8  
CVE-2026-1527: Undici has CRLF Injection in undici via `upgrade` option - https://github.com/advisories/GHSA-4992-7rv2-5pvq  
CVE-2026-2581: Undici has Unbounded Memory Consumption in its DeduplicationHandler via Response Buffering that leads to DoS - https://github.com/advisories/GHSA-phc3-fgpg-7m6h  

### Solutions
- CVE-2026-1528, CVE-2026-1526, CVE-2026-2229: Ignoré tant que l'on utilise pas de WebSocket
- CVE-2026-1525: Ignoré car on ne touche pas au header HTTP Content-Length
- CVE-2026-1527: Ignoré car on n'utilise pas option upgrade d'Undici
- CVE-2026-2581: Ignoré car on n'utilise pas l'intercepteur de deduplication d'Undici
## backend
### Vulnérabilité du pom.xml
GHSA-72hv-8253-57qq: jackson-core: Number Length Constraint Bypass in Async Parser Leads to Potential DoS Condition  
CVE-2026-29062: jackson-core  

### Solutions
- GHSA-72hv-8253-57qq: Cette vulnérabilité peut être ignoré tant que l'on utilise pas com.fasterxml.jackson.core.StreamReadConstraints
- CVE-2026-29062: Cette vulnérabilité peut être ignorée tant que l'on utilise pas java.io.DataInput
