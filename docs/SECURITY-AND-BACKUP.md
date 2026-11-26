# Security and backup behavior

## AI provider credentials

The user's NVIDIA API key is encrypted with an AES-GCM key held by Android Keystore. That Keystore key is device-bound and is not portable through Android Auto Backup or device transfer.

The app therefore excludes only `ai_secret_store.xml` from both legacy full backup and Android 12+ cloud/device-transfer backup. Other app data remains eligible for the platform's normal backup behavior.

After restoring app data on a different device, the user must enter the provider API key again in Settings. The app does not silently copy or export the credential.

## Network transport

Cleartext HTTP traffic is disabled at the application manifest level. The NVIDIA client also requires an HTTPS base URL before making a request.
