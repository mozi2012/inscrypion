(ns inscrypion.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom-client]))

;; Define app state
(defonce app-state (r/atom {:text "Welcome to Inscrypion!2"
                            :image "/img/squirrel.png"
                            :counter 0}))

;; Example of a for loop in ClojureScript
(defn for-loop-example []
  (doseq [i (range 5)]
    (println i)))
;; Call this function to see output: 0, 1, 2, 3, 4

(defn for-comprehension-example []
  ;; Basic for - returns a sequence
  (for [i (range 5)]
    (* i 2)))
;; Example of 'for' (list comprehension) in ClojureScript

;; Returns: (0 2 4 6 8)

;; For with filtering
(defn for-with-filter-example []
  (for [i (range 10)
        :when (even? i)]
    i))
;; Returns: (0 2 4 6 8)

;; For with multiple sequences (nested)
(defn for-nested-example []
  (for [x [1 2 3]
        y [10 20]]
    [x y]))
;; Returns: ([1 10] [1 20] [2 10] [2 20] [3 10] [3 20])



;;defn image []
;;[:img {:src (:image @app-state) :alt "Cards"}]

(defn create_board []
  [:div
   (for [segment (range 4)]
     ^{:key segment}
     [:button {:style {:background-image "url(img/board_piece_yellow.png)"
                       :width 123
                       :height 195}
               :id segment}])
   [:br]
   (for [segment (range 4)]
     ^{:key segment}
     [:button {:style {:background-image "url(img/board_piece_orange.png)"
                       :width 123
                       :height 195}
                       :id (+ segment 4)}])
   [:br]
   [:br]
    ] )



(def cards [{:name "squirrel"
             :damage 0
             :health 1
             :sigils "none"
             :image "NULL"}

            {:name "stoat"
             :damage 1
             :health 3}
             :sigils ["sniper"]
             :image "NULL" ]

  )

(def board_state [[(cards 0)] ;;The first brackets are yellows's side of the board.
                  
                  [(cards 1)]] ;;The second brackets are orange's side of the board.   
  )  

;;(defn end_turn)


;;"\\wsl.localhost\Ubuntu-24.04\home\cto\workspace\inscrypion\public\img\board.png"

;; Main component
(defn app []
  [:div
   [:p (:text @app-state)]
   [:br]

   [:button {:style {:background-image "url(img/board_piece_orange.png)"
                     :width 123
                     :height 195}
             }]
   (create_board)
   [:h1 "sknlfb 100"]
   
   
   


   ]

  )     

  
  ;;root in an atom
   

(defonce root (atom nil)) 

 
;; Initialize the app
(defn init []
  (let [app-element (.getElementById js/document "app")]
    (when-not @root
      (reset! root (rdom-client/create-root app-element)))
    (rdom-client/render @root [app])))

;; Hot reload handler
(defn ^:dev/after-load reload []
  (init)) 
