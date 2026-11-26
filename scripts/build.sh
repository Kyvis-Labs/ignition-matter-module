#!/usr/bin/env bash
# Builds the module into matter-tag-provider-build/target/: Matter-Tag-Provider-unsigned.modl,
# plus Matter-Tag-Provider.modl signed with the Kyvis Labs key when it is available.
# SIGNING_DIR moves the key (module-signing.pfx, .crt, .password); without it, unsigned only.
set -euo pipefail
here="$(cd "$(dirname "$0")/.." && pwd)"
cd "$here"

keys="${SIGNING_DIR:-$HOME/data/kyvis}"
args=(package -B -q "$@")
if [ -f "$keys/module-signing.pfx" ]; then
  SIGNING_PASSWORD="$(cat "$keys/module-signing.password")"
  export SIGNING_PASSWORD
  args+=("-Dsigning.dir=$keys")
else
  echo "no signing key in $keys: building unsigned only" >&2
fi

mvn "${args[@]}"

target="$here/matter-tag-provider-build/target"
ls -1 "$target"/*.modl
