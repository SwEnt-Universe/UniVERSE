#!/usr/bin/env bash
# test-suites.sh
# ------------------------------------------------------------------------------
# Maps PR title tags (e.g. [Profile]) to Gradle instrumentation test tasks.
# Supports multiple tags per PR title.
# ------------------------------------------------------------------------------

declare -A SUITES=(
  # ---------------- Screen-based suites ----------------
  ["Profile"]="ProfileRelatedConnectedCheck"
  ["EventList"]="EventRelatedConnectedCheck"
  ["GlobalNav"]="GlobalNavRelatedConnectedCheck"
  ["ProfileCreation"]="ProfileCreationRelatedConnectedCheck"
  ["Map"]="MapRelatedConnectedCheck"
  ["SelectTag"]="TagScreenRelatedConnectedCheck"
  ["Common"]="CommonComposableConnectedCheck"


  # ---------------- Utility / meta tags ----------------
  ["Docs"]="__SKIP__"   # handled separately by the CI (skip heavy jobs)
)

# ------------------------------------------------------------------------------
# Given a PR title, print all Gradle tasks to run
# ------------------------------------------------------------------------------
get_suites_from_title() {
  local title="$1"
  local result=""

  for key in "${!SUITES[@]}"; do
    if [[ "$title" == *"[$key]"* ]]; then
      local task="${SUITES[$key]}"
      if [[ "$task" != "__SKIP__" ]]; then
        result="$result $task"
      fi
    fi
  done

  # Trim leading/trailing whitespace
  result=$(echo "$result" | xargs)
  echo "$result"
}
