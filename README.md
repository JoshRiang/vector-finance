# Vector Finance

**How much runway is left, and what did today actually cost?**

Part of the **VECTOR Suite** — three apps, one private database.

---

## The number that changes behaviour

A balance tells you what you have. **Runway** tells you what to do about it.

So the headline is not the balance — it is `days remaining at current burn`.
Balance sits underneath as context. A balance of 3,500,000 reads as comfortable;
"9 days" reads as urgent, and only one of those makes you act.

Under a week, it turns red and says so.

## What it shows

- **Runway** — days left at the current average daily burn
- **Today** — spent vs daily budget, with what is left free
- **Last 30 days** — total spend, average per day
- **Log an expense** — amount and an optional note

Until you log spending there is no honest burn rate, so the app says
"No spending logged yet" instead of inventing a figure.

## Home-screen widget

A native Android widget (`RunwayWidget`) shows days of runway and what is left
free today — without opening the app.

## Why it is server-backed

The numbers live on the server so they survive a reinstall, and so the calendar
can see real daily burn. A finance tracker that forgets everything when you
switch phones is not a tracker.

## Architecture

```
Flutter app  ──HTTP──▶  VECTOR Suite API  ──▶  private database
  (this repo)             (finance + store)
```

Runway maths, computed server-side:

```python
avg_daily  = total_spent_30d / days_with_spend
runway_days = balance / avg_daily
```

## Build

```bash
flutter pub get
flutter test
flutter build apk --release --target-platform android-arm64 --split-per-abi
```

```bash
flutter build apk --dart-define=API_BASE=https://your-host
```

CI injects `API_BASE` from the repository variable of the same name.

## The VECTOR Suite

| App | Question it answers |
|---|---|
| [Vector Tasks](https://github.com/JoshRiang/vector-tasks) | What is the one thing to start right now? |
| [Vector Calendar](https://github.com/JoshRiang/vector-calendar) | How is today going? |
| **Vector Finance** (this) | How much runway is left? |

All three share one private database.

## Privacy

Every table is row-level-security gated on the authenticated user. The backend
runs on the owner's own server.

## Not a licensed advisor

This app **computes and reports**. It does not recommend trades or investments.

## Licence

MIT
