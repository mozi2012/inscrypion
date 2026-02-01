(ns inscrypion.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom-client]))

(def cards
  ;; :cost [blood_cost bone_cost]
  {:squirrel {:name "squirrel"
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

;; Define app state
(defonce app-state (r/atom {:dynamic-text nil
                            
                            :images {:cards {}
                                     :board {}}
                            
                            :board-piece-clicked nil
                            :card-clicked nil
                            
                            :deck-shuffled? false 
                            :card-drawn? false 

                            :current-player :orange 

                            :scale {:yellow 0
                                    :orange 0}
                            
                            :yellow {:hand [:squirrel]
                                     :deck [:stoat :squirrel]
                                     :bones 0}
                                     
                            
                            :orange {:hand [:squirrel :stoat :stoat :wolf ]
                                     :deck [:stoat :squirrel :wolf ]
                                     :bones 0}
                            
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
                            

                            } ))




(defn shuffle-deck []
  (prn "old: " (:orange @app-state))
  (swap! app-state (fn [app-state]
                     (let [cp (:current-player app-state)
                           app-state2 (update-in app-state [cp :deck] shuffle)]
                       (assoc-in app-state2 [:deck-shuffled?] true)
                       )))
  (prn "new: " (:orange @app-state))
  
  )

(comment
  (map-indexed list [:a :b :c])

  (map-indexed (fn [idx itm]
                 [idx itm])
               "foobar")
  (map (fn [a]
         [:button a])
       [1 2 3])
  )

(defn create-hand []
  (let [player-data ((:current-player @app-state) @app-state) ]
    [:div
     (map-indexed (fn [index card-key]
                    [:button {:style {:background-image (str "url(" (:image (cards card-key)) ")")
                                      :width 123
                                      :height 195}
                              :on-click (fn [] (swap! app-state assoc-in [:l]))
                              :key index}])
                  (:hand player-data))]
    
    #_[:div
       (for [[index card-key] index-card-pairs]
         [:button {:style {:background-image (str "url(" (:image (cards card-key)) ")")
                           :width 123
                           :height 195}
                   :on-click (fn [] (swap! app-state assoc-in [:l]))
                   :key index}])]) )

(defn create-board []
  [:div
   (doall
    (for [segment (range 4)]
      [:button {:style {:background-image (str "url("
                                               (if-let  [i (:image ((:yellow (:board @app-state)) segment))]
                                                 i
                                                 "img/board_piece_yellow.png") ")")
                        :width 123
                        :height 195}
                
                :on-click (if (or (= (:current-player @app-state) :yellow) (:board-piece-clicked? @app-state))
                            (fn [] (swap! app-state assoc-in [:board-piece-clicked?] true)))
                :id segment
                :key segment}]))
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
                :on-click (if (or (= (:current-player @app-state) :yellow) (:board-piece-clicked? @app-state))
                            (fn [] (swap! app-state assoc-in [:board-piece-clicked?] true)) ) 
                :id (str "segment " (+ segment 4))}]))
   [:br] ] )

(defn play-card []
  (let [])
  )

(defn draw-card [card-key]
  (prn "drawn?" (:card-drawn? @app-state))
  (if (:card-drawn? @app-state)
    (swap! app-state assoc :dynamic-text
           (["You can draw a new card once per turn. You have already drawn one..."
             "Do you understand? Only one."
             "You have drawn a card this turn."
             "One card per turn. Don't be greedy."] (rand-int 4) ))

    (if (not= card-key nil) 
      (let [path [(:current-player @app-state) :hand]]
        (swap! app-state update-in path conj card-key)
        (swap! app-state assoc-in [:card-drawn?] true) )
      (swap! app-state assoc :dynamic-text []
      ) )
  ) )

  #_(defn calculate-board []
    (let [board-state (:board @app-state)]   

      (for [r (range 4)]
        )
      )
    )


#_(defn end-turn []
  (calculate-board)

  )


;;"\\wsl.localhost\Ubuntu-24.04\home\cto\workspace\inscrypion\public\img\board.png"

;; Main component
(defn app [app-state]
  [:div
   (if [(:deck-shuffled? @app-state)]
     nil
     (shuffle-deck))
   
   [:br]
   [create-board]
   [:br]
   [:h1 "hand:"]
   (create-hand)
   [:br]
   [:h1 (:dynamic-text @app-state)]   ;;(prn "test")
   [:br]
   [:button {:style {:background-image "url(/img/squirrel.png)"
                     :width 123
                     :height 195}
             :on-click (fn [] (draw-card :squirrel) )}]
   
   [:button {:style {:background-image nil
                     :width 123
                     :height 195}
             :on-click (fn []
                         (let [current-player (:current-player @app-state)
                               deck (:deck (current-player @app-state)) ]
                           (draw-card (first deck)) 
                           (swap! app-state update-in [current-player :deck] (fn [a]
                                                                               (vec (rest a))) )) )}]
   ]
  
  )     
  
  ;;root in an atom
  
  
(defonce root (atom nil)) 

 
;; Initialize the app
(defn init []
  (let [app-element (.getElementById js/document "app")]
    (when-not @root
      (reset! root (rdom-client/create-root app-element)))
    (rdom-client/render @root [app app-state])))

;; Hot reload handler
(defn ^:dev/after-load reload []
  (init)) 

(comment
  (swap! app-state update-in [:orange :hand] conj :squirrel)
  (swap! app-state update-in [path] conj card-key)
  (first (:deck (:orange @app-state) ) )
  )
