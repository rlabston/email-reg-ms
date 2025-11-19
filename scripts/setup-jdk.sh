#!/usr/bin/env bash
set -euo pipefail

# scripts/setup-jdk.sh
# Helper to download and install Temurin / Adoptium JDK into ~/.jdks
# Usage: ./scripts/setup-jdk.sh [--version 21] [--target-dir /path/to/dir] [--persist] [--dry-run]
# Examples:
#   ./scripts/setup-jdk.sh --version 21            # installs Temurin 21 to ~/.jdks/jdk-21-temurin
#   ./scripts/setup-jdk.sh --version 17 --persist # installs JDK 17 and appends exports to ~/.bashrc

VERSION=21
TARGET_DIR="$HOME/.jdks/jdk-${VERSION}-temurin"
PERSIST=false
DRY_RUN=false

print_usage() {
  cat <<EOF
Usage: $0 [--version <n>] [--target-dir <path>] [--persist] [--dry-run]

Options:
  --version <n>      JDK major version to install (default: ${VERSION})
  --target-dir <p>   Installation directory (default: ${TARGET_DIR})
  --persist          Append JAVA_HOME and PATH exports to ~/.bashrc
  --dry-run          Print chosen download URL and paths, do not download
  -h|--help          Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version)
      VERSION="$2"; shift 2;;
    --target-dir)
      TARGET_DIR="$2"; shift 2;;
    --persist)
      PERSIST=true; shift 1;;
    --dry-run)
      DRY_RUN=true; shift 1;;
    -h|--help)
      print_usage; exit 0;;
    *)
      echo "Unknown arg: $1" >&2; print_usage; exit 1;;
  esac
done

# Normalize TARGET_DIR if it contains ~
TARGET_DIR="$(eval echo $TARGET_DIR)"

# Try common Adoptium API URL patterns. Prefer linux/x64 which is commonly used.
ARCH_PATHS=("linux/x64" "linux-x64")
API_BASE="https://api.adoptium.net/v3/binary/latest"
VARIANT="hotspot"
RELEASE_TYPE="normal"
PROVIDER="eclipse"
FEATURE="ga"

attempt_urls=()
for arch in "${ARCH_PATHS[@]}"; do
  attempt_urls+=("${API_BASE}/${VERSION}/${FEATURE}/${arch}/jdk/${VARIANT}/${RELEASE_TYPE}/${PROVIDER}")
done

# Pick the first URL that returns 200/302/307 (redirect to asset)
chosen_url=""
for url in "${attempt_urls[@]}"; do
  if curl -I -sS -L "$url" >/dev/null 2>&1; then
    chosen_url="$url"
    break
  fi
done

if [[ -z "$chosen_url" ]]; then
  echo "Could not determine a working Adoptium download URL for JDK ${VERSION}. Tried:" >&2
  for u in "${attempt_urls[@]}"; do echo "  - $u" >&2; done
  exit 2
fi

echo "Selected download URL: $chosen_url"
echo "Target installation directory: $TARGET_DIR"

if [[ "$DRY_RUN" == "true" ]]; then
  echo "Dry run requested; exiting without downloading."
  exit 0
fi

TMP_ARCHIVE="/tmp/openjdk-${VERSION}.tar.gz"

mkdir -p "$TARGET_DIR"

echo "Downloading JDK ${VERSION}... (this may take a minute)"
curl -L --fail -o "$TMP_ARCHIVE" "$chosen_url"

# Basic sanity check
if [[ ! -s "$TMP_ARCHIVE" ]]; then
  echo "Download failed or archive is empty: $TMP_ARCHIVE" >&2
  exit 3
fi

echo "Extracting to $TARGET_DIR"
# Remove existing content and extract into target
rm -rf "$TARGET_DIR"/* || true
mkdir -p "$TARGET_DIR"

# Attempt to extract; the archive may contain a top-level folder
tar -xzf "$TMP_ARCHIVE" -C "$TARGET_DIR" --strip-components=1
rm -f "$TMP_ARCHIVE"

# Verify java exists
if [[ ! -x "$TARGET_DIR/bin/java" ]]; then
  echo "Extraction failed: $TARGET_DIR/bin/java not found or not executable" >&2
  exit 4
fi

echo "Installation complete: $TARGET_DIR"

echo "To use this JDK in the current shell session run:"
echo "  export JAVA_HOME=\"$TARGET_DIR\""
echo "  export PATH=\"$TARGET_DIR/bin:\$PATH\""

if [[ "$PERSIST" == "true" ]]; then
  SHELL_RC="$HOME/.bashrc"
  if [[ -f "$HOME/.zshrc" && -n "${ZSH_VERSION-}" ]]; then
    SHELL_RC="$HOME/.zshrc"
  fi
  echo "Appending export lines to $SHELL_RC (will not overwrite existing JAVA_HOME lines)"
  # Add a marker block to the rc file
  cat >> "$SHELL_RC" <<EOF

# Added by email-reg-ms/scripts/setup-jdk.sh: set Temurin JDK ${VERSION}
export JAVA_HOME="$TARGET_DIR"
export PATH="$TARGET_DIR/bin:">$SHELL_RC
  fi
fi

# Print java version
echo "Java version now available:" 
"$TARGET_DIR/bin/java" -version

echo "Done." 
