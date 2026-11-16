#!/usr/bin/env bash
# SessionStart: give this session its own task file (<tasks_dir>/<session_id>/tasks.md, shown by the
# IntelliJ "Tasks" tool window), watch it so file-changed.sh can wake Claude, and load the rules.
input=$(cat)
session=$(jq -r '.session_id // empty' <<<"$input")
cwd=$(jq -r '.cwd // empty' <<<"$input")
[ -n "$session" ] && [ -n "$cwd" ] || exit 0

# Git repos only. The first worktree listed is the main one, so linked worktrees share one list.
root=$(git -C "$cwd" worktree list --porcelain 2>/dev/null | sed -n '1s/^worktree //p')
[ -n "$root" ] || exit 0

tasks_dir="${CLAUDE_PLUGIN_OPTION_TASKS_DIR:-tmp/tasks}"
tasks_dir="${tasks_dir%/}"
case "$tasks_dir" in
  /*) base="$tasks_dir" ;;
  *)
    base="$root/$tasks_dir"
    # Keep task files out of git status with a local ignore, nothing to commit.
    exclude=$(git -C "$root" rev-parse --path-format=absolute --git-path info/exclude)
    mkdir -p "$(dirname "$exclude")"
    grep -qxF "/$tasks_dir/" "$exclude" 2>/dev/null || echo "/$tasks_dir/" >> "$exclude"
    ;;
esac

dir="$base/$session"
mkdir -p "$dir"
file="$dir/tasks.md"
# Snapshot of the user's ticks, so file-changed.sh reports only new changes.
rg -N '^- \[[xX]\] \(user\)' "$file" 2>/dev/null | sort > "$dir/.ticked"

rules=$(cat "$(dirname "$0")/../rules.md")
jq -n --arg f "$file" --arg rules "$rules" '{
  watchPaths: [$f],
  hookSpecificOutput: {
    hookEventName: "SessionStart",
    watchPaths: [$f],
    additionalContext: "\($rules)\n\nThis session'"'"'s task file: \($f)"
  }
}'
