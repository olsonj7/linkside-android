# Linkside Android

Bronze-tier Android client for Linkside.

## Repos & backend

| Repo | What lives there |
|------|------------------|
| **`linkside-ios`** | iOS app + **`linkside-sms-backend/`** (Node API, Supabase, Twilio) |
| **`linkside-android`** (this repo) | Android app only |

All backend/API/DB changes go in **`linkside-ios`** on branch **`develop`** (dev Railway) → merge to **`main`** (prod Railway). This repo only needs client updates when endpoints or payloads change.

**Bronze gap tracker:** see [BRONZE_PARITY.md](./BRONZE_PARITY.md) — rescan against iOS `ApiService.swift` and `Views/` when adding features.

## Week 3 (current)

- **Trips (invitee only)** — view invited trips on Home, open trip detail
- **RSVP** — In / Maybe / Out on trips you're invited to
- **Trip chat** — group messages with auto-refresh
- **Photos** — view trip photo grid, upload from gallery
- **Trip tee times** — see tee times linked to a trip
- **Self payment tracking** — mark deposit/balance paid (when host set costs)

No trip creation or host management (Silver+ on iOS).

## Week 2

- **Golfers** — saved golfers list, manual add, import from device contacts
- **Friend groups** — create/edit/delete (Bronze max 3 groups)
- **Tee times** — create with course search, invite saved golfers, list on Home
- **RSVP** — YES / MAYBE / NO on tee time detail
- Contact status badges (In App / Opted In)

## Week 1

- Phone OTP + Google Sign-In + **email/password** auth
- JWT in EncryptedSharedPreferences
- Main tabs: Home / Golfers / Profile

## Run

1. Open in Android Studio → Sync Gradle → Run (API 26+).
2. Grant **Contacts** permission when adding golfers from your address book.

## Branches & API targets

| Branch | Use | API (debug / dev builds) | API (release) |
|--------|-----|--------------------------|---------------|
| `develop` | day-to-day dev | `https://linkside-backend-dev-production.up.railway.app` | — |
| `main` | production releases | — | `https://linkside-ios-production.up.railway.app` |

Debug builds on `develop` hit the **dev Railway backend** (and its Supabase dev project). Release builds always use production.

Local backend override (emulator → host machine): set `API_BASE_URL` in the `debug` block in `app/build.gradle.kts` to `http://10.0.2.2:3000`.

## Roadmap

| Week | Focus |
|------|--------|
| 1 | Auth, API shell, tabs |
| 2 | Golfers, groups, tee times |
| 3 | Trip invitee flows |
| 4 | Tournament register + Stripe |
