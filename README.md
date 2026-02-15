# inscrypion

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
- `:fmt` and `:clean` are `-X` aliases (exec-fn).
- `:kondo` and `:cljs` are `-M` aliases (main-opts).
