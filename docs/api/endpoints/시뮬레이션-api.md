# 🧪 시뮬레이션 API

**Base URL:** `/api/v1/simulations`

## POST /simulations - 시뮬레이션 실행

**Request:**
```json
{
  "type": "WHAT_IF",
  "policySnapshotId": "01POLICY...",
  "scenario": {
    "threshold": 20000,
    "dateRange": {
      "from": "2024-01-01T00:00:00Z",
      "to": "2024-12-31T23:59:59Z"
    }
  }
}
```

**Response:** `202 Accepted`
```json
{
  "success": true,
  "message": "Simulation started",
  "data": {
    "simulationId": "01SIM...",
    "status": "RUNNING"
  }
}
```

## GET /simulations/{id} - 시뮬레이션 결과 조회

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "simulationId": "01SIM...",
    "type": "WHAT_IF",
    "status": "COMPLETED",
    "result": {
      "eventsGenerated": 120,
      "comparison": {
        "baseline": {
          "events": 150,
          "cases": 45
        },
        "simulated": {
          "events": 120,
          "cases": 38
        },
        "difference": {
          "events": -30,
          "cases": -7,
          "percentChange": -20
        }
      }
    },
    "completedAt": "2025-01-13T16:00:00Z"
  }
}
```

## POST /simulations/backtest - 백테스트 실행

**Request:**
```json
{
  "policySnapshotId": "01POLICY...",
  "historicalDateRange": {
    "from": "2024-01-01T00:00:00Z",
    "to": "2024-12-31T23:59:59Z"
  }
}
```

**Response:** `202 Accepted`

---
