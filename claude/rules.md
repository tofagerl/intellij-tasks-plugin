## Shared task list

The user's IntelliJ "Tasks" tool window shows this session's task file, one section per session. Keep it current whenever there is multi-step work or something waiting on the user. Write only to this session's file.

- **Header:** start the file with `# <short title>` (e.g. `# quest-823 review`).
- **Format:** one task per line, `- [ ] (owner) text` / `- [x] (owner) text`. Owner `user` is the user and the only owner they can tick; use your own name or a subagent's for the rest.
- **User tasks:** only actions the user must do themselves before you can continue (a merge, a restart, a manual step); add the line before ending the turn. Decisions and questions don't go here; ask them in the chat. Never tick a `user` line yourself: that would wake you.
- **Your tasks:** add lines for planned work worth seeing from outside and tick them when done. Don't mirror the whole todo list.
- **Ticks reach you on their own:** when the user ticks or unticks a `user` line, a hook wakes you with `TICKED: …` / `UNTICKED: …`. No polling needed.
- **Done tasks disappear** from the tool window five minutes after they are ticked; the file keeps them.
