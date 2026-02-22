(ns inscrypion.core
  (:require
    [clojure.string :as string]
    [reagent.core :as r]
    [reagent.dom.client :as rdom-client]))


(def cards
  {;; :cost [blood_cost bone_cost]
   :squirrel {:name "squirrel",
              :damage 0,
              :health 1,
              :cost {:blood 0, :bones 0},
              :sigils nil,
              :image "/img/squirrel.png"},

   :stoat {:name "stoat",
           :damage 1,
           :health 3,
           :cost {:blood 1, :bones 0},
           :sigils [:sniper],
           :image "/img/stoat_card.png"}})


;; Define app state
(defonce app-state
  (r/atom
    {:dynamic-text nil,
     :images {:cards {}, :board {}},

     :board-piece-clicked nil,
     :card-clicked nil,

     :sacrificed-cards [],
     :sacrifices-required nil,

     :deck-shuffled? false,
     :card-drawn? false,

     :current-player :orange, ; :yellow

     :scale {:yellow 0, :orange 0},

     :yellow {:hand [:squirrel],
              :deck [],                 ; [:stoat :squirrel]
              :bones 0},

     :orange {:hand [:squirrel :stoat :stoat :wolf],
              :deck [:stoat :squirrel :wolf],
              :bones 0},

     :board {:yellow [{:name "squirrel",
                       :damage 0,
                       :health 1,
                       :cost [0 0],
                       :sigils [nil],
                       :image "/img/squirrel.png"}
                      nil
                      nil
                      nil],
             :orange [{:name "stoat",
                       :damage 1,
                       :health 3,
                       :cost [0 0],
                       :sigils [:sniper],
                       :image "/img/stoat_card.png"}
                      nil
                      nil
                      nil
                      #_{:name "squirrel",
                         :damage 0,
                         :health 1,
                         :cost [0 0],
                         :sigils [nil],
                         :image "/img/squirrel.png"}]}}))


(def text-dump
  {:card-already-drawn
   ["You can draw a new card once per turn. You have already drawn one..."
    "Do you understand? Only one." "You have drawn a card this turn."
    "One card per turn. Don't be greedy."],

   :deck-empty ["You are out of cards..." "Nothing left to draw."
                "Your deck is empty." "You can only draw a squirrel."
                "Trying to draw cards from thin air?"],

   :lack-blood-squirrel-in-hand
   "You are lacking the sacrifices to play that [CARD], but your squirrel is free.",

   :lack-blood-no-squirrel
   "You lack the sacrifices required to play the [CARD]"})





(defn return [return function-name nested-order]
  (prn "RETURN:" return)
  (prn (str nested-order " END: " function-name))
  (prn " ")
  return)

(defn replace-CARD
  [str card-name]
  (string/replace str "[CARD]" card-name))


(defn shuffle-decks
  []
  (swap! app-state (fn [app-state]
                     (let [app-state
                           (update-in app-state [:orange :deck] shuffle)
                           app-state
                           (update-in app-state [:yellow :deck] shuffle)]
                       (assoc-in app-state [:deck-shuffled?] true))))
  nil)




(defn worthy-sacrifice? [vector]
  (not (empty?
        (filter (fn [x]
                  (= x :worthy-sacrifice))
                vector))))

(defn bloodless? [vector]
  (not (empty?
        (filter (fn [x]
                  (= x :bloodless))
                vector))))

(comment
  (calculate-sacrificial-cards
   [{:sigils [:no-effect :bloodless]}
    {:sigils [:no-effect]}
    {:sigils nil}
    {:sigils [:worthy-sacrifice]}
    nil]))

(defn calculate-sacrificial-cards [cards]
  (prn "2 START: calculate-sacrificial-cards")
  (prn "PARAMETER cards:" cards)
  (let [nil-filtered (filter (fn [x]
                               (not (nil? x)))
                             cards)
        
        sigils (for [x nil-filtered]
                 (:sigils x))
        p (prn "SYMBOL sigils:" sigils)
        blood         (for [y sigils]
                        (cond
                          (worthy-sacrifice? y)
                          3
                          
                          (bloodless? y)
                          0

                          :else
                          1 
                          ))]
    (prn "SYMBOL blood-list:" blood)
    (return blood "calculate-sacrificial-cards" 2)

    ))



(defn check-squirrels
  [current-player]
  (prn "2 START: check-squirrels")
  (let [hand (-> @app-state
                 current-player
                 :hand)]
    (prn "SYMBOL " hand)
    (return  (count (filter (fn [card] (= card :squirrel)) hand)) "check-squirrels" 2)))


(defn- on-click-hand
  [current-player index card-info]
  (prn "1 START: on-click-hand")
  (let [blood-cost (get-in card-info [:cost :blood])
        blood-on-board (reduce + (calculate-sacrificial-cards (-> @app-state
                                                                  :board
                                                                  current-player)))]

    (prn "SYMBOL blood-on-board: " blood-on-board)
    (prn "SYMBOL blood-cost:" blood-cost)
    (prn "BOOLEAN <= blood-cost blood-on-board:" (<= blood-cost blood-on-board))
    (prn "SYMBOL card-clicked : " (@app-state :card-clicked))
    (if (<= blood-cost blood-on-board)

      (do
        (prn "EVENT: card-clicked changed to:" index )
        (prn "EVENT: sacrifices-required changed changed to:" blood-cost)
        (swap! app-state (fn [state]
                           (-> state
                               (assoc :card-clicked index)
                               (assoc :sacrifices-required blood-cost)))))

      (let [number-of-squirrels (check-squirrels current-player)
            card-name (:name card-info)
            msg-template (if (>= (+ number-of-squirrels blood-on-board)
                                 blood-cost)
                           (text-dump :lack-blood-squirrel-in-hand)
                           (text-dump :lack-blood-no-squirrel))]
        (prn "EVENT: dynamic-text changed to: ~" msg-template)
        (swap! app-state assoc
               :dynamic-text
               (replace-CARD msg-template card-name)))))
  (prn "SYMBOL sacrifices-required : " (@app-state :sacrifices-required))

  (prn "1 END: on-click-hand")
  (prn "")
  )


(defn create-hand
  []
  (let [current-player (:current-player @app-state)
        hand (get-in @app-state [current-player :hand])]
    [:div
     (map-indexed
       (fn [index card-key]
         (let [card-info (cards card-key)]
           [:button
            {:style {:background-image (str "url(" (:image card-info) ")"),
                     :width 123,
                     :height 195},
             :on-click #(on-click-hand current-player index card-info),
             :key index}]))
       hand)]))


(defn play-card
  [hand card-index board-piece-index current-player]
  (prn "2 START: play-card ")
  (let [card-key (hand card-index)
        card-info (cards card-key)]
    (prn "EVENT: card-clicked:" nil)
    (prn "EVENT: board-piece-clicked:" nil)
    (prn "EVENT: card-info:" card-info "moved to board index:" board-piece-index) 
    (swap! app-state
           (fn [app-state]
             (-> app-state
                 (assoc-in [:board current-player board-piece-index] card-info)
                 (assoc :card-clicked nil)
                 (assoc :board-piece-clicked nil)
                 (assoc :sacrifices-required nil)
                 
                 (update-in [current-player :hand]
                            (fn [h]
                              (vec (keep-indexed (fn [idx card]
                                                   (when (not= idx card-index) card))
                                                 h)))))))))



(defn can-pay-cost?
  [card-index hand board-piece-index current-player]
  (prn "2 START: can-pay-cost?")
  (prn "SYMBOL hand:" hand)
  (prn "SYMBOL card-index:" card-index)
  (prn "SYMBOL board-index:" board-piece-index)
  (let [card-key (hand card-index)
        card-info (cards card-key)
        card-cost (card-info :cost)]

    (prn "SYMBOL sacrifices-required:" (@app-state :sacrifices-required))
    
    
    (return
     (if (and 
          (<= 
           (card-cost :bones)
           (-> @app-state
               current-player
               :bones))
          (= (@app-state :sacrifices-required) 0)) ; MIGHT NEED DEBUGING LATER 
       
       true
       false)
     "can-pay-cost" 2) ))

(comment
  ;;if board-segment-clicked:  ✓  
  ;;   if card-clicked\(hand): ✓ 
  ;;      if can-pay-cost and card-not-on-space:     ✓  
  ;;         remove-sacrificed-cards X
  ;;         play-card X                      
  ;;
  ;;      else:
  ;;           if card-on-space\(board):  ?X
  ;;              if board-piece-index in :sacrificed-cards : X  <<---
  ;;                 remove-board-piece-index-from-:sacrificyed-cards      X
  ;;
  ;;              else:                                       X       
  ;;                  add-index-to-:sacrificed-cards          ✓
  ;;
  ;;              check-blood-sigils                          ?✓
  ;;              subtract-blood-from-:sacrifices-required    X
  ;;                   
  ;;
  (shadow/repl :app)

  (not (nil? nil))
  
  (-> @app-state  
      :board
      current-player
      ((fn [current-board]
         (current-board board-piece-index)))
      nil?
      not)



)

(defn space-empty? [board-piece-index current-player]
  (prn "2 START: space-empty?")

  (return
   (-> @app-state
       :board
       current-player
       ((fn [b] 
          (b board-piece-index)))
       nil?
       )
   "space-empty?" 2))

(comment

  
  
  ;;first 
  
  (def sacrificed-cards [1 3 2])

  (def board
    [{:sigils [:no-effect :bloodless]}
     {:sigils [:no-effect]} ;;1
     {:sigils nil} ;;2
     {:sigils [:worthy-sacrifice]} ;;3
     nil])
  ;;C-x C-e

  
  (keep-indexed (fn [idx card]
                  (when (some #{idx} sacrificed-cards)
                    card))
                board)
  (some #{1} [1 3 2])

  (if []
    1
    0)
  
  (def foo1
    (calculate-sacrificial-cards board))

  
  )


#_(defn update-sacrifices-required [sacrificed-cards current-player]
  (prn "2 START: update-sacrifices-required")
  (-> @app-state :board current-player (fn [p]
                                         (p sacrificed-cards))))

(defn board-segment-on-click
  [card-index board-piece-index current-player]
  (prn "1 START: board-segment-on-click")
  (let [hand (-> @app-state current-player :hand)
        sacrificed-cards (@app-state :sacrificed-cards)
        pay-cost? (can-pay-cost? card-index hand board-piece-index current-player)
        can-play-card? (and pay-cost?
                            (space-empty? board-piece-index current-player))
        can-sacrifice-card? (and (-> @app-state  
                                     :board
                                     current-player
                                     ((fn [current-board]
                                        (current-board board-piece-index)))
                                     nil?
                                     not)

                                 (= (count
                                     (filter (fn [item]
                                               (= item board-piece-index))
                                             sacrificed-cards))
                                    1))]    
    (prn "SYMBOL board-piece-index:" board-piece-index)
    
    (cond
      can-play-card?
      (play-card hand card-index board-piece-index current-player)

      pay-cost? (do
                  (prn "EVENT: card-clicked, board-piece-clicked, and sacrifices-required changed to:" nil)
                  (prn "EVENT: sacrificed-cards changed to:" [])
                  (swap! app-state (fn [app-state]
                                     (-> app-state
                                         (assoc :card-clicked nil)
                                         (assoc :board-piece-clicked nil)
                                         (assoc :sacrifices-required nil)
                                         (assoc :sacrificed-cards [])))))
      
      can-sacrifice-card? 
      (do
        (prn "EVENT: board-piece-index:" board-piece-index " removed from sacrificed-cards")
        (swap! app-state update :sacrificed-cards #(filter (fn [item]
                                                             (not= board-piece-index))
                                                           sacrificed-cards)))
      :else (do
              (prn "EVENT: index:" board-piece-index " added to sacrificed-cards")
              ;;NEED TO MAKE FUNCTION THAT CALCULATES THE BLOOD OF EACH CARD IN :sacrificed-cards AND UPDATES :sacrifices-required ACCORDINGLY
              (swap! app-state update :sacrificed-cards conj board-piece-index)) 
      )
    
    )
  (prn "1 END: board-segment-on-click")
  (prn " "))

#_(fn [app-state]
                     (-> app-state
                         (update :sacrificed-cards
                                 conj
                                 board-piece-index)))

(comment
  (filter (fn [item]
            (not= [1 2 3 4])) 3))

(defn create-board-segment
  [player]
  (doall
   (for [segment (range 4)]
     [:button
      {:style {:background-image
               (str "url("
                    (if-let [i (:image ((player (:board @app-state))
                                        segment))]
                      i
                      (str "img/board_piece_" (name player) ".png"))
                    ")"),
               :width 123,
               :height 195},
       :on-click #(let [card-clicked (:card-clicked @app-state)]
                    (if (and (= (:current-player @app-state) player) 
                             (not= card-clicked
                                   nil))  

                      (board-segment-on-click card-clicked segment player)
                      
                      (swap! app-state (fn [app-state]
                                         (-> app-state
                                             (assoc :card-clicked nil)
                                             (assoc :board-piece-clicked nil)))))),
       :key segment}])))





(defn create-board
  []
  (let [current-player (:current-player @app-state)
        opponent (if (= current-player :yellow) :orange :yellow)]
    [:div (create-board-segment opponent) [:br]
     (create-board-segment current-player) [:br]]))


(defn draw-card
  [card-key]
  (prn "drawn?" (:card-drawn? @app-state))
  (if (:card-drawn? @app-state)
    (swap! app-state assoc-in
           [:dynamic-text]
           ((text-dump :card-already-drawn) (rand-int 4)))

    (if (not= card-key nil)
      (let [path [(:current-player @app-state) :hand]]
        (swap! app-state update-in path conj card-key)
        (swap! app-state assoc-in [:card-drawn?] true))
      (swap! app-state assoc-in
             [:dynamic-text]
             ((text-dump :deck-empty) (rand-int 5))))))


#_(defn calculate-board
    []
    (let [board-state (:board @app-state)] (for [r (range 4)])))


#_(defn end-turn [] (calculate-board))


;; "\\wsl.localhost\Ubuntu-24.04\home\cto\workspace\inscrypion\public\img\board.png"

;; Main component
(defn app
  [app-state]
  (when-not (:deck-shuffled? @app-state) (shuffle-decks))
  [:div [:br] [create-board] [:br] [:h1 "hand:"] (create-hand) [:br]
   [:h1 (:dynamic-text @app-state)] ; (prn "test")
   [:br]
   [:button
    {:style
     {:background-image "url(/img/squirrel.png)", :width 123, :height 195},
     :on-click (fn [] (draw-card :squirrel))}]
   [:button
    {:style {:background-image nil, :width 123, :height 195},
     :on-click (fn []
                 (let [current-player (:current-player @app-state)
                       deck (:deck (current-player @app-state))]
                   (draw-card (first deck))
                   (swap! app-state update-in
                          [current-player :deck]
                          (fn [a] (vec (rest a))))))}]])


;; root in an atom

(defonce root (atom nil))


;; Initialize the app
(defn init
  []
  (let [app-element (.getElementById js/document "app")]
    (when-not @root (reset! root (rdom-client/create-root app-element)))
    (rdom-client/render @root [app app-state])))


;; Hot reload handler
(defn ^:dev/after-load reload
  []
  (init))



