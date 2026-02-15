# inscrypion

## Quick Start TL;DR

1. Start the app watcher:

```bash
clj -M:cljs watch app
```

2. In Emacs, connect CIDER to the Clojure REPL:
- `M-x cider-connect`
- Host defaults to `localhost`
- Port is auto-read from `.shadow-cljs/nrepl.port`

3. In the connected Clojure REPL, switch to the CLJS REPL:

```clojure
(shadow/repl :app)
```

4. Open the game in your browser:
- `http://localhost:8020/`

5. In the CLJS REPL, run:

```clojure
(js/alert :hello)
```

You should see an alert in the browser.

## ClojureScript Workflow (`deps.edn`)

This project runs `shadow-cljs` via `deps.edn` so tooling can be driven from the Clojure CLI.

## `deps.edn` Aliases

### `:fmt`

Formats the codebase using the dev task runner (`cljstyle` + `zprint`).

```bash
clj -X:fmt
```

### `:clean`

Cleans build/cache artifacts and editor temp files.

```bash
clj -X:clean
```

### `:kondo`

Runs `clj-kondo` for linting.

```bash
clj -M:kondo --lint src
```

### `:cljs`

Runs `shadow-cljs` via the Clojure CLI.

```bash
clj -M:cljs watch app
clj -M:cljs compile app
clj -M:cljs release app
```

### Notes

- `deps.edn` is the entry point for dependencies and launching `shadow-cljs`.
- `shadow-cljs.edn` is still required for Shadow build config (`:builds`, `:dev-http`, etc.).
- `:cljs` alias in `deps.edn` uses `shadow.cljs.devtools.cli`, so you can pass any normal `shadow-cljs` command after `clj -M:cljs`.
- Shadow writes nREPL port metadata to `.shadow-cljs/nrepl.port` (and CLJS REPL metadata to `.shadow-cljs/cli-repl.port`).
- `:fmt` and `:clean` are `-X` aliases (exec-fn).
- `:kondo` and `:cljs` are `-M` aliases (main-opts).
