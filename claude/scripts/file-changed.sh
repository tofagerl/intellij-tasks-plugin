#!/usr/bin/env bash
# FileChanged (asyncRewake): when the user ticks or unticks a (user) line in this session's task file,
# exit 2 so Claude wakes with the change. Other sessions' files and other owners' edits stay silent.
input=$(cat)
session=$(jq -r '.session_id // empty' <<<"$input")
file=$(jq -r '.file_path // empty' <<<"$input")
case "$file" in */"$session"/tasks.md) ;; *) exit 0 ;; esac

snap="$(dirname "$file")/.ticked"
prev=$(cat "$snap" 2>/dev/null)
cur=$(rg -N '^- \[[xX]\] \(user\)' "$file" 2>/dev/null | sort)
printf '%s\n' "$cur" | rg -v '^$' > "$snap"

ticked=$(comm -13 <(printf '%s\n' "$prev") <(printf '%s\n' "$cur") | rg -v '^$' | sed 's/^/TICKED: /')
unticked=$(comm -23 <(printf '%s\n' "$prev") <(printf '%s\n' "$cur") | rg -v '^$' | sed 's/^/UNTICKED: /')
[ -n "$ticked$unticked" ] || exit 0
printf 'Tasks file %s changed:\n%s\n' "$file" "$(printf '%s\n%s' "$ticked" "$unticked" | rg -v '^$')" >&2
exit 2
