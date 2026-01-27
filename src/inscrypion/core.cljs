(ns inscrypion.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom-client]))

(def cards
  {:squirrel {:name "Squirrel"
              :damage 0
              :health 1
              :cost [0 0]
              :sigils []
              :emoji "🐿️"
              :image "/img/squirrel.png"}

   :stoat {:name "Stoat"
           :damage 1
           :health 3
           :cost [1 0]
           :sigils [{:name "Sniper" :emoji "🎯"}]
           :emoji "🦡"
           :image "/img/stoat_card.png"}

   :wolf {:name "Wolf"
          :damage 3
          :health 2
          :cost [2 0]
          :sigils []
          :emoji "🐺"
          :rare true}

   :raven {:name "Raven"
           :damage 2
           :health 3
           :cost [2 0]
           :sigils [{:name "Airborne" :emoji "🪽"}]
           :emoji "🐦‍⬛"}})

;; Define app state
(defonce app-state (r/atom {:current_turn :orange
                            :card_drawn? false

                            :scale {:yellow 0
                                    :orange 0}

                            :orange {:deck [:stoat :squirrel :wolf]
                                     :hand [:stoat :squirrel :wolf :raven]}

                            :yellow {:deck [:squirrel :squirrel]
                                     :hand [:stoat]}

                            :board {:yellow [nil nil nil nil]
                                    :orange [nil nil nil nil]}}))

;; Components

(defn scale-component []
  [:div.scale
   [:div.scale-post]
   [:div.scale-beam
    [:div.scale-pan.left
     [:div.pan-label "Yellow"]
     [:div.pan-bowl]
     [:div.pan-score (get-in @app-state [:scale :yellow])]]
    [:div.scale-pivot]
    [:div.scale-pan.right
     [:div.pan-label "Orange"]
     [:div.pan-bowl]
     [:div.pan-score (get-in @app-state [:scale :orange])]]]
   [:div.scale-base]])

(defn end-turn-button []
  [:button.end-turn-btn
   {:on-click (fn []
                (let [next-turn (if (= (:current_turn @app-state) :orange) :yellow :orange)]
                  (swap! app-state assoc :current_turn next-turn :card_drawn? false)))}
   "End Turn"])

(defn scale-area []
  [:div.scale-area
   [scale-component]
   [end-turn-button]])

(defn card-component [{:keys [name damage health cost sigils emoji rare]}]
  [:div {:class (str "card" (when rare " rare"))}
   [:div.card-border]
   (when (pos? (first cost))
     [:div.cost
      (for [i (range (first cost))]
        ^{:key i} [:div.blood])])
   [:div.card-image emoji]
   [:div.card-name name]
   [:div.card-sigils
    (for [{:keys [name emoji]} sigils]
      ^{:key name} [:div.sigil {:title name} emoji])]
   [:div.card-stats
    [:div.stat.damage damage]
    [:div.stat.health health]]])

(defn board-slot [player segment]
  (let [card-key (get-in @app-state [:board player segment])
        card (when card-key (cards card-key))]
    [:div.board-slot
     {:on-click (fn [] (println "Clicked" player "slot" segment))}
     (when card
       [card-component card])]))

(defn board-row [player]
  [:div.board-row
   (doall
    (for [segment (range 4)]
      ^{:key segment}
      [board-slot player segment]))])

(defn board-component []
  [:div.board
   [board-row :yellow]
   [board-row :orange]])

(defn decks-component []
  [:div.decks
   [:div {:class "deck squirrel-deck"
          :on-click (fn []
                      (when-not (:card_drawn? @app-state)
                        (swap! app-state update-in [(:current_turn @app-state) :hand] conj :squirrel)
                        (swap! app-state assoc :card_drawn? true)))}
    [:div.deck-cards]
    [:div.deck-label "Squirrels"]]
   [:div {:class "deck main-deck"
          :on-click (fn []
                      (when-not (:card_drawn? @app-state)
                        (let [player (:current_turn @app-state)
                              deck (get-in @app-state [player :deck])]
                          (when (seq deck)
                            (swap! app-state update-in [player :hand] conj (first deck))
                            (swap! app-state update-in [player :deck] (comp vec rest))
                            (swap! app-state assoc :card_drawn? true)))))}
    [:div.deck-cards]
    [:div.deck-label "Draw"]]])

(defn hand-component []
  (let [player (:current_turn @app-state)
        hand (get-in @app-state [player :hand])]
    [:div.card-container
     (doall
      (for [[idx card-key] (map-indexed vector hand)]
        ^{:key (str card-key "-" idx)}
        (let [card (cards card-key)]
          [card-component card])))]))

(defn hand-area []
  [:div.hand-area
   [decks-component]
   [hand-component]])

(defn board-column []
  [:div.board-column
   [board-component]
   [hand-area]])

(defn game-area []
  [:div.game-area
   [scale-area]
   [board-column]])

;; Main component
(defn app []
  [game-area])

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
