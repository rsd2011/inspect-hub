# 🧮 데이터 구조 / 모델 (Data & Schema)

## 스냅샷 기반 기준 마스터(예시 주요 컬럼)

### STANDARD_SNAPSHOT

```
id, type[KYC/STR/CTR/RBA/WLF/FIU], version, effective_from/to,
status, created_by, approved_by, prev_id, next_id
```

### KYC_RISK_FACTOR

```
snapshot_id, risk_type[고객/국가/상품/행동], factor_code,
value_set, weight, score_min/max, grade_band
```

### WLF_THRESHOLD

```
snapshot_id, algo(ver/sim), threshold, country_bias, update_source
```

### FIU_REPORT_FIELD

```
snapshot_id, field_code, label, type, required, validation_rule, mapping
```

### STR_RULE

```
snapshot_id, rule_code, severity, description, enabled
```

### STR_RULE_FACTOR

```
rule_code, factor_code, operator, value, weight
```

### RBA_CHECK_ITEM

```
snapshot_id, item_code, description, weight, periodicity
```

## 탐지/조사/보고

### TX_STAGING

```
tx_id, cust_id, acct_id, amount, channel, ts, …
```

### DETECTION_EVENT

```
event_id, tx_id, rule_code, score, matched_fields, snapshot_ver
```

### ALERT_CASE

```
case_id, status, priority, owner, created_ts, snapshot_ver
```

### CASE_ACTIVITY

```
case_id, action, actor, ts, comment, attachment_ref
```

### REPORT_STR/CTR

```
report_id, case_id, fields(json), status, sent_ts, ack_ts
```

## 점검 배치(Spring Batch 유사)

### INSPECTION_INSTANCE

```
instance_id, type[STR/CTR/WLF/RBA], snapshot_ver, created_ts
```

### INSPECTION_EXECUTION

```
exec_id, instance_id, status, started_ts, ended_ts,
read/write/skip_cnt, exit_msg
```

## 이력/감사

### AUDIT_LOG

```
who, when, what, before/after
```

각 마스터 prev/next 체인

## 식별자/키

전역 **ULID/UUID** 권장, 외부 연계키 별도 매핑
