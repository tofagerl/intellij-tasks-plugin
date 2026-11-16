# Tasks (IntelliJ + Claude Code plugins)

A right-side IntelliJ "Tasks" tool window that renders every `<project>/tmp/tasks/<session>/tasks.md` as one section per session folder. The section title is the file's first `# ` heading, or the first 8 characters of the folder name.

## Task format

```
tmp/tasks/<session>/tasks.md
```

```
# Section title
- [ ] (owner) text
- [x] (owner) text
```

Only tasks owned by `user` are clickable (toggle). Done tasks disappear 5 minutes after they were ticked (the file keeps them), and sessions with nothing left open disappear with them.

The directory is configurable: IntelliJ under Settings → Tools → Tasks, Claude Code through the plugin's `tasks_dir` option (`/config`). Relative paths resolve against the project root (IntelliJ) and the repo's main worktree (Claude Code), and both settings must point at the same place.

## Claude Code plugin

The `claude/` folder is a Claude Code plugin that gives each session its own task file, tells Claude how to use it, and wakes Claude when you tick or untick one of your tasks:

```
/plugin install tasks --marketplace tofagerl/intellij-tasks-plugin
```

It works in git repos only, and adds the tasks directory to the repo's local ignore (`.git/info/exclude`).

## Install (IntelliJ)

Settings → Plugins → ⚙ → Manage Plugin Repositories → add:

```
https://github.com/tofagerl/intellij-tasks-plugin/releases/latest/download/updatePlugins.xml
```

Then install "Tasks" from the Marketplace tab. Requires IntelliJ IDEA 2026.3 (build 263) or newer.

## Build

```
./gradlew buildPlugin
```

The zip lands in `build/distributions/`. By default this downloads IntelliJ IDEA 2026.3 as the target platform. To build against a local installation instead:

```
./gradlew buildPlugin -PlocalIde="/path/to/IntelliJ IDEA.app"
```

## Release

Push a `vX.Y.Z` tag. CI builds and tests with that version, then creates a GitHub Release with the plugin zip and `updatePlugins.xml`.

## License

MIT
