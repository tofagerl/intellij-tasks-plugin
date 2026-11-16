# Tasks (IntelliJ plugin)

A right-side "Tasks" tool window that renders every `<project>/tmp/tasks/<session>/tasks.md` as one section per session folder. The section title is the file's first `# ` heading, or the first 8 characters of the folder name.

## Task format

```
tmp/tasks/<session>/tasks.md
```

```
# Section title
- [ ] (owner) text
- [x] (owner) text
```

Only tasks owned by `tom` are clickable (toggle). The owner is currently hardcoded. Done tasks disappear 5 minutes after they were ticked (the file keeps them), and sessions with nothing left open disappear with them.

## Install

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
