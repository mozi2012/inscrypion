# Project: Inscrypion

## Server & Development Workflow

1.  **Connect to Server**: `ssh wabbit.bumble.fish`
2.  **Access TMUX**: `tmux a`
3.  **Start Watch Process**: `clj -M:cljs watch app`
4.  **Open in Browser**: `http://wabbit.bumble.fish:8020/`

## Project Details

-   **Core File**: `/home/cto/workspace/inscrypion/src/inscrypion/core.cljs`
-   **Card Data Structure**: 
    ```clojure
    [{:name "test0" 
      :health 1 
      :damage 0
      :sigils 0}]
    ```
-   **Board Data Structure**:
    ```clojure
    [
     [{:name "test0" 
       :health 1 
       :damage 0
       :sigils 0}]

     [{:name "test1" 
       :health 1 
       :damage 0
       :sigils 0}]
    ]
    ```
-   **Ideas**: 
    - (empty)

---

# Emacs Configuration & Commands

## General

-   **Open Shell**: `M-x shell`
-   **Find File**: `C-x p f`
-   **Show Line Numbers**: `M-x display-line-numbers-mode`
-   **Spell Check (on-the-fly)**: `M-x flyspell-mode`
-   **Recover from `C-z`**: Type `fg` in the terminal and press Enter.

## Text Editing

-   **Mark**: `C-space`
-   **Copy (Yank)**: `M-w`
-   **Evaluate S-expression**: `C-x C-e`
-   **Delete Parentheses (forward)**: `C-d`

## Buffer Management

-   **List Buffers**: `C-x C-b`
-   **Split Buffer Vertically**: `C-x 3`
-   **Rename Buffer**: `M-x rename-buffer`
-   **Reload Buffer from File**: `C-x C-v`

## Clojure & CIDER

-   **Start REPL**: `M-x shell` (then run `clj -M:cljs watch app`)
-   **Connect CIDER REPL**: `M-x cider-connect`
-   **Select ClojureScript REPL**: `(shadow/repl :app)`

## Git (Magit)

-   **Enter Magit Status**: `C-x g`
-   **Push**: `P p` (in Magit buffer)

## Terminal (vterm)
-   **start AI agents in vterm **
-   **Switch between buffer and terminal**: `C-c C-t`

## Code Folding (hs-minor-mode)

-   **Activate Mode**: `M-x hs-minor-mode`
-   **Show All**: `C-c @ C-a` or `C-c @ C-M-s`
-   **Hide All**: `C-c @ C-t` or `C-c @ C-M-h`
-   **Toggle Hiding**: `C-c @ C-c` or `C-c @ C-e`
-   **Hide Block**: `C-c @ C-d` or `C-c @ C-h`
-   **Show Block**: `C-c @ C-s`
-   **Hide by Level**: `C-c @ C-l`
