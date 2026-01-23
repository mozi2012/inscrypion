(ns inscrypion.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom-client]))

;; Define app state
(defonce app-state (r/atom {:text "Welcome to Inscrypion!2"
                            :images {:cards {}
                                     :board {}}
                            
                            :current_turn :orange 
                            
                            :board {:yellow [{:name "squirrel"
                                              :damage 0
                                              :health 1
                                              :cost [0 0]
                                              :sigils "none"
                                              :image "/img/squirrel.png"}
                                             nil
                                             nil
                                             nil]
                                    
                                    :orange [{:name "stoat"
                                              :damage 1
                                              :health 3
                                              :cost [0 0]
                                              :sigils "sniper"
                                              :image "/img/stoat_card.png"}
                                             nil
                                             nil
                                             {:name "squirrel"
                                              :damage 0
                                              :health 1
                                              :cost [0 0]
                                              :sigils "none"
                                              :image "/img/squirrel.png"}]}
                            
                            :scale {:yellow 0
                                    :orange 0}
                            
                            :hand {:yellow [1 2 3]
                                   :orange [{:name "stoat"
                                             :damage 1
                                             :health 3
                                             :cost [0 0]
                                             :sigils "sniper"
                                             :image "/img/stoat_card.png"}
                                            {:name "stoat"
                                             :damage 1
                                             :health 3
                                             :cost [0 0]
                                             :sigils "sniper"
                                             :image "/img/stoat_card.png"}]}
                            } ))

(def cards
  ;; :cost [blood_cost bone_cost]
  {:squirel {:name "squirrel"
             :damage 0
             :health 1
             :cost [0 0]  ;; :cost [blood_cost bone_cost]
             :sigils "none"
             :image "/img/squirrel.png"}
            
   :stoat {:name "stoat"
           :damage 1
           :health 3
           :cost [1 0]
           :sigils "sniper"
           :image "/img/stoat_card.png"}} )

(defn create_hand []
  (for [card  ((:current_turn @app-state) (:hand @app-state))   ]
    [:button {:style {:background-image (str "url(" (:image card) ")")
                      :width 123
                      :height 195}
              }]) )







(defn create_board []
  [:div
   (doall
    (for [segment (range 4)]
      ^{:key segment}
      [:button {:style {:background-image (str "url("
                                               (if-let  [i (:image ((:yellow (:board @app-state)) segment))]
                                                 i
                                                 "img/board_piece_yellow.png") ")")
                        :width 123
                        :height 195}
                :id segment}]))
   [:br]
   (doall
    (for [segment (range 4)]
      ^{:key segment}
      [:button {:style {:background-image (str "url("
                                               ;;if orange board 
                                               (if-let  [i (:image ((:orange (:board @app-state)) segment))]
                                                 i
                                                 "img/board_piece_orange.png") ")")
                        :width 123
                        :height 195}
                :on-click (fn [] (print "you clicked."))
                :id (str "segment " (+ segment 4))}]))
   [:br] ] )




 


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
   [create_board]
   [:br]
   (create_hand)

   [:br]
   [:button {:style {:background-image "url(/img/squirrel.png)"
                     :width 123
                     :height 195}
             :on-click (fn [] [ ])}] 
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
