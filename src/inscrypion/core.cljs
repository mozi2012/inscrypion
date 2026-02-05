(ns inscrypion.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom-client]))

(def cards
  ;; :cost [blood_cost bone_cost]
  {:squirrel {:name "squirrel"
              :damage 0
              :health 1
              :cost [0 0] ;; :cost [blood_cost bone_cost]
              :sigils "none"
              :image "/img/squirrel.png"}

   :stoat {:name "stoat"
           :damage 1
           :health 3
           :cost [1 0]
           :sigils "sniper"
           :image "/img/stoat_card.png"}})

;; Define app state
(defonce app-state (r/atom {:dynamic-text nil

                            :images {:cards {}
                                     :board {}}

                            :board-piece-clicked nil
                            :card-clicked nil

                            :deck-shuffled? false
                            :card-drawn? false

                            :current-player :yellow

                            :scale {:yellow 0
                                    :orange 0}

                            :yellow {:hand [:squirrel]
                                     :deck [] ;;[:stoat :squirrel]
                                     :bones 0}

                            :orange {:hand [:squirrel :stoat :stoat :wolf]
                                     :deck [:stoat :squirrel :wolf]
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
                                              :image "/img/squirrel.png"}]}}))

(def text-dump {:card-already-drawn
                ["You can draw a new card once per turn. You have already drawn one..."
                 "Do you understand? Only one."
                 "You have drawn a card this turn."
                 "One card per turn. Don't be greedy."]

                :deck-empty
                ["You are out of cards..."
                 "Nothing left to draw."
                 "Your deck is empty."
                 "You can only draw a squirrel."
                 "What? Trying to pluck cards from thin air?"]})

(defn shuffle-decks []
  (swap! app-state (fn [app-state]
                     (let [app-state (update-in app-state [:orange :deck] shuffle)
                           app-state (update-in app-state [:yellow :deck] shuffle)]
                       (assoc-in app-state [:deck-shuffled?] true))))
  nil)

(defn create-hand []
  (let [player-data ((:current-player @app-state) @app-state)]
    [:div
     (map-indexed (fn [index card-key]
                    [:button {:style {:background-image (str "url(" (:image (cards card-key)) ")")
                                      :width 123
                                      :height 195}
                              :on-click (fn [] (swap! app-state assoc-in [:card-clicked] index))
                              :key index}])
                  (:hand player-data))]))

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
                :on-click (fn []
                            (if (and (= (:current-player @app-state) :yellow)
                                     (not= (:card-clicked @app-state)
                                           nil))
                              (swap! app-state assoc-in [:board-piece-clicked] segment)

                              (swap! app-state (fn [app-state]
                                                 (let [app-state
                                                       ((swap! app-state assoc-in [:card-clicked] nil))]

                                                   (swap! app-state assoc-in [:board-piece-clicked] nil)))))
                            (prn "board-piece-clicked:" (:board-piece-clicked @app-state)))
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
                :on-click (fn []
                            (if  (and (= (:current-player @app-state) :orange)
                                      (not= (:card-clicked @app-state)
                                            nil))
                              (swap! app-state assoc-in [:board-piece-clicked] (+ segment 4))

                              (swap! app-state (fn [app-state]
                                                 (let [app-state
                                                       ((swap! app-state assoc-in [:card-clicked] nil))]

                                                   (swap! app-state assoc-in [:board-piece-clicked] nil))))
                              #_(do
                                  (swap! app-state assoc-in [:card-clicked] nil)
                                  (swap! app-state assoc-in [:board-piece-clicked] nil)))
                            (prn "board-piece-clicked:" (:board-piece-clicked @app-state)))
                :id (str "segment " (+ segment 4))}]))
   [:br]])

(defn play-card []
  (let [card-index (:card-clicked @app-state)
        board-piece-index (:board-piece-clicked @app-state)]

    (if (and (not= card-index nil)
             (not= board-piece-index nil))
      (do
        (swap! app-state assoc-in [:board (:current-player @app-state) board-piece-index])))))

(defn draw-card [card-key]
  (prn "drawn?" (:card-drawn? @app-state))
  (if (:card-drawn? @app-state)
    (swap! app-state assoc-in [:dynamic-text]
           ((text-dump :card-already-drawn) (rand-int 4)))

    (if (not= card-key nil)
      (let [path [(:current-player @app-state) :hand]]
        (swap! app-state update-in path conj card-key)
        (swap! app-state assoc-in [:card-drawn?] true))

      (swap! app-state assoc-in [:dynamic-text]
             ((text-dump :deck-empty) (rand-int 5))))))

#_(defn calculate-board []
    (let [board-state (:board @app-state)]

      (for [r (range 4)])))

#_(defn end-turn []
    (calculate-board))

;;"\\wsl.localhost\Ubuntu-24.04\home\cto\workspace\inscrypion\public\img\board.png"

;; Main component
(defn app [app-state]
  (when-not (:deck-shuffled? @app-state)
    (shuffle-decks))
  [:div
   [:br]
   [create-board]
   [:br]
   [:h1 "hand:"]
   (create-hand)
   [:br]
   [:h1 (:dynamic-text @app-state)] ;;(prn "test")
   [:br]
   [:button {:style {:background-image "url(/img/squirrel.png)"
                     :width 123
                     :height 195}
             :on-click (fn [] (draw-card :squirrel))}]

   [:button {:style {:background-image nil
                     :width 123
                     :height 195}
             :on-click (fn []
                         (let [current-player (:current-player @app-state)
                               deck (:deck (current-player @app-state))]
                           (draw-card (first deck))
                           (swap! app-state update-in [current-player :deck] (fn [a]
                                                                               (vec (rest a))))))}]])

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
  (first (:deck (:orange @app-state)))

  (def my-list (range 1 10))
  (shuffle my-list)           ; => a shuffled version of the list, e.g., (2 5 7 0 3 9 4 8 1 6)
  )
