#!/bin/bash

# --- Configuration ---
LOG_FILE="firebase-emulator.log"
PROJECT_ID="demo-project"
EMULATOR_PORTS=(8080 9099)
EMULATOR_COMPONENTS="auth,firestore"

# Function to clean up background processes upon exit (Ctrl+C)
cleanup() {
  echo ""
  echo " Shutting down..."
  # Kill the emulator process (assuming it's running from nohup)
  kill "$EMULATOR_PID"

  # Kill the tail process if it exists
  if [ -n "$TAIL_PID" ]; then
    kill "$TAIL_PID" 2>/dev/null
  fi

  echo " Cleanup complete."
  exit 0
}

# Trap SIGINT (Ctrl+C) to run the cleanup function
trap cleanup SIGINT

# 1. Start Firebase Emulators
echo "Starting Firebase emulators for $EMULATOR_COMPONENTS (Project: $PROJECT_ID)..."

# Start emulators in the background, logging output
nohup firebase emulators:start --only $EMULATOR_COMPONENTS --project $PROJECT_ID > "$LOG_FILE" 2>&1 &
EMULATOR_PID=$!

# 2. Wait for Ports to be Ready
for port in "${EMULATOR_PORTS[@]}"; do
  echo "⏳ Waiting for port $port..."
  while ! nc -z localhost "$port"; do
    sleep 1
  done
done

echo "Firebase emulators are running."
echo "--------------------------------------------------------"

# 3. Start tail loop to auto-click verification links
echo "Listening for email verification links"

# Start tail in the background
tail -n 0 -F "$LOG_FILE" | while read line; do
  # Check if the line contains the verification URL pattern
  if [[ "$line" =~ http://127.0.0.1:9099/emulator/action\?mode=verifyEmail.* ]]; then
    url="${BASH_REMATCH[0]}"
    echo "➡ Clicking verification link: $url"
    # Use curl to hit the link without printing output
    curl -s "$url" >/dev/null
  fi
done &

# Save the PID of the background tail process
TAIL_PID=$!

echo "Automated verification link clicking is active (PID: $TAIL_PID)."
echo "Press Ctrl+C to stop the emulators."

# Keep the main script alive so the background tail process can continue
wait