 
CONNECT TO SERVER: ssh wabbit.bumble.fish

ACCESS TMUX: tmux a

run(in shell): npx shadow-cljs watch app 
open: http://wabbit.bumble.fish:8020/

open shell:              M-x shell
open file:               C-x p f
 
start claude:            M-x vterm 
change:
start REPL:              M-x cider-connect 
start clojure script repl:   (shadow/repl :app)

show line number:        M-x display-line-numbers-mode

mark: C-space
copy: M-w




rename buffer:           M-x rename-buffer 
split buffer:            C-x 3
reload buffer:           C-x C-v
list all buffers: C-x C-b

if C-z: input fg

core.cljs: /home/cto/workspace/inscrypion/srnscrypion/core.cljs


activate hs-minor-mode:  M-x hs-minor-mode
C-c @ C-a	hs-show-all
C-c @ C-c	hs-toggle-hiding
C-c @ C-d	hs-hide-block
C-c @ C-e	hs-toggle-hiding
C-c @ C-h	hs-hide-block
C-c @ C-l	hs-hide-level
C-c @ C-s	hs-show-block
C-c @ C-t	hs-hide-all

C-c @ C-M-h	hs-hide-all
C-c @ C-M-s	hs-show-all


notes:

card data structure: [{:name "test0" 
                       :health 1 
                       :damage 0
                       :sigils 0}]

board data structure: [
                       [{:name "test0" 
                         :health 1 
                         :damage 0
                         :sigils 0}]

                       [{:name "test1" 
                         :health 1 
                         :damage 0
                         :sigils 0}]
                        ] 

idea: 
