# Dev scripts

This folder contains helper scripts to run and manage the services used for local development.

Files
- `start-dev.sh` — Builds (optional) and starts the backend and gateway in the background, waits for their ports, and writes pid/log files under the configured `LOG_DIR` (defaults to repo `logs/`). Use `--force` to free the ports first.
- `stop-dev.sh` — Stops processes started by `start-dev.sh` using pid files in `scripts/`. It attempts a graceful shutdown with exponential backoff, then falls back to SIGKILL. Use `--force` to stop by port if pid files are absent.
- `install-logrotate.sh` — Renders `scripts/logrotate/email-reg-ms.conf.template` and installs it to `/etc/logrotate.d/email-reg-ms`. Must be run with `sudo`.
- `logrotate/email-reg-ms.conf.template` — Logrotate template with an `@ROOT@` placeholder that the installer replaces with the repository root.

Usage examples

```bash
# Start the services (writes logs to ./logs by default):
LOG_DIR=./logs bash scripts/start-dev.sh

# Force-free the ports then start
bash scripts/start-dev.sh --force

# Stop (reads pid files under scripts/):
bash scripts/stop-dev.sh

# Force stop by port (when pid files are missing):
bash scripts/stop-dev.sh --force

# Install logrotate config (requires root):
sudo bash scripts/install-logrotate.sh

# Test the installed logrotate config (debug only):
sudo logrotate --debug /etc/logrotate.d/email-reg-ms
```

Notes
- `LOG_DIR` environment variable controls where logs are written by the start/stop scripts. If not set, `./logs` is used.
- The installer overwrites `/etc/logrotate.d/email-reg-ms` if it exists. Remove it manually to revert.

Systemd unit example

If you'd like to run the gateway as a systemd service, there's an example unit at `scripts/systemd/gateway.service.example`.
Copy it to `/etc/systemd/system/email-reg-ms-gateway.service`, edit the `User`, `WorkingDirectory` and `ExecStart` (replace `@ROOT@` with the repository path), then enable/start the service:

```bash
sudo cp scripts/systemd/gateway.service.example /etc/systemd/system/email-reg-ms-gateway.service
sudo sed -i "s#@ROOT@#$(pwd)#g" /etc/systemd/system/email-reg-ms-gateway.service
sudo systemctl daemon-reload
sudo systemctl enable --now email-reg-ms-gateway.service
sudo journalctl -u email-reg-ms-gateway.service -f
```

Ensure the jar referenced by `ExecStart` exists (build the gateway with `./gradlew :gateway:bootJar -x test`).

Backend systemd unit example

There is also an example unit for the backend at `scripts/systemd/backend.service.example`. Install it similarly:

```bash
sudo cp scripts/systemd/backend.service.example /etc/systemd/system/email-reg-ms-backend.service
sudo sed -i "s#@ROOT@#$(pwd)#g" /etc/systemd/system/email-reg-ms-backend.service
sudo systemctl daemon-reload
sudo systemctl enable --now email-reg-ms-backend.service
sudo journalctl -u email-reg-ms-backend.service -f
```

Ensure the backend jar exists (build with `./gradlew :bootJar -x test`).

Install both units at once

If you'd like to install both service units in one step (uses the example units shipped in `scripts/systemd/` which already reference the repository path), run:

```bash
sudo cp scripts/systemd/gateway.service.example /etc/systemd/system/email-reg-ms-gateway.service \
	&& sudo cp scripts/systemd/backend.service.example /etc/systemd/system/email-reg-ms-backend.service \
	&& sudo systemctl daemon-reload \
	&& sudo systemctl enable --now email-reg-ms-gateway.service email-reg-ms-backend.service \
	&& sudo journalctl -u email-reg-ms-gateway.service -u email-reg-ms-backend.service -f
```

This will copy the prepared unit files, reload systemd, enable and start both services and stream their journals.
