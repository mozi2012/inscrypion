(ns inscrypion.core
  (:require
   [clojure.string :as string]
   [reagent.core :as r]
   [reagent.dom.client :as rdom-client]))

(comment
  {:name "",
   :damage 0,
   :health 1,
   :cost {:blood 0, :bones 0},
   :sigils nil,
   :image "/img/ "})

(def cards
  { ;; :cost [blood_cost bone_cost]
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
           :image "/img/stoat_card.png"}
   
   :wolf {:name "wolf",
          :damage 3,
          :health 2,
          :cost {:blood 2, :bones 0},
          :sigils nil,
          :image "/img/nil"}

   :coyote {:name "coyote",
            :damage 2,
            :health 1,
            :cost {:blood 0, :bones 4},
            :sigils nil,
            :image "/img/nil "}})


;; Define app state
(defonce app-state
  (r/atom
    {:dynamic-text nil,
     :images {:cards {}, :board {}},

     :board-piece-clicked nil,
     :card-clicked nil,

     :must-play-card false
     :sacrificed-cards [],
     :sacrifices-required nil,

     :deck-shuffled? false,
     :card-drawn? false,

     :current-player :orange, ; :yellow

     :scale {:yellow 0, :orange 0},

     :yellow {:hand [:squirrel],
              :deck [],                 ; [:stoat :squirrel]
              :bones 0},

     :orange {:hand [{:card-key :coyote
                      :sigils :red} #_[:coyote :sniper] :squirrel :stoat :stoat :wolf],
              :deck [:stoat :squirrel :wolf],
              :bones 4},

     :board {:yellow [{:name "squirrel",
                       :damage 0,
                       :health 1,
                       :cost {:blood 0 :bones 0},
                       :sigils [nil],
                       :image "/img/squirrel.png"}
                      nil
                      nil
                      nil],
             :orange [{:name "stoat",
                       :damage 1,
                       :health 3,
                       :cost [0 0],
                       :sigils [:sniper], ;;worthy-sacrifice
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
    "Do you understand? Only one."
    "You have drawn a card this turn."
    "One card per turn. Don't be greedy."],

   :deck-empty ["You are out of cards..."
                "Nothing left to draw."
                "Your deck is empty."
                "You can only draw a squirrel."
                "Trying to draw cards from thin air?"],

   :lack-blood-squirrel-in-hand
   "You are lacking the sacrifices to play that [CARD], but your squirrel is free.",

   :lack-blood-no-squirrel
   "You lack the sacrifices required to play that [CARD]"

   :lack-bones
   "You lack the bones to play that [CARD]"
   })





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

(defn calculate-sacrificial-cards [cards nested-order]
  (prn (str nested-order " START: calculate-sacrificial-cards"))
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
    (return blood "calculate-sacrificial-cards" nested-order)

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
  (prn "::NOTICE:: card hand ")                        ;;BIG PROBLEM CANNOT ADD SIGILS TO INDUVIAL CARDS IN DECK NOR HAND
  (prn "1 START: on-click-hand")
  (let [bone-cost (get-in card-info [:cost :bone])
        card-name (:name card-info)
        bones-in-hand (-> @app-state
                          current-player
                          :bones)
        blood-cost (get-in card-info [:cost :blood])
        blood-on-board (reduce + (calculate-sacrificial-cards (-> @app-state
                                                                  :board
                                                                  current-player)
                                                              2))]

    (prn "SYMBOL blood-on-board: " blood-on-board)
    (prn "SYMBOL blood-cost:" blood-cost)
    (prn "BOOLEAN <= blood-cost blood-on-board:" (<= blood-cost blood-on-board))
    (prn "SYMBOL card-clicked : " (@app-state :card-clicked))

    
    (if (<= bone-cost bones-in-hand)
      (if (<= blood-cost blood-on-board)

        (do
          (prn "EVENT: card-clicked succeed; :card-clicked changed to:" index )
          (prn "                           ; :sacrifices-required changed changed to:" blood-cost)
          (swap! app-state (fn [state]
                             (-> state
                                 (assoc :card-clicked index)
                                 (assoc :sacrifices-required blood-cost)))))

        (let [number-of-squirrels (check-squirrels current-player)
              msg-template (if (>= (+ number-of-squirrels blood-on-board)
                                   blood-cost)
                             (text-dump :lack-blood-squirrel-in-hand)
                             (text-dump :lack-blood-no-squirrel))]
          (prn "EVENT: card-clicked failed; :dynamic-text changed to: ~" msg-template)
          (swap! app-state assoc
                 :dynamic-text
                 (replace-CARD msg-template card-name))))


      (swap! app-state assoc :dynamic-text (replace-CARD (text-dump :lack-bones) card-name))))
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
  (prn "2 START: play-card")
  (prn "PARAMETER; hand:" hand)
  (prn "         ; card-index:" card-index)
  (prn "         ; board-piece-index:" board-piece-index)
  (prn "         ; current-player:"current-player)
  (let [card-key (hand card-index)
        card-info (cards card-key)]
    (prn "EVENT: play-card succeeded; card-clicked:" nil)
    (prn "                          ; board-piece-clicked:" nil)
    (prn "                          ; card-info:" card-info "moved to board index:" board-piece-index) 
    (prn "::SYMBOL:: card-info:" card-info)
    (swap! app-state
           (fn [app-state]
             (-> app-state
                 (assoc-in [:board current-player board-piece-index] card-info)
                 (assoc :card-clicked nil)
                 (assoc :board-piece-clicked nil)
                 (assoc :sacrifices-required nil)
                 (assoc :sacrificed-cards [])
                 (update-in [current-player :hand]
                            (fn [h]
                              (vec (keep-indexed (fn [idx card]
                                                   (when (not= idx card-index) card))
                                                 h))))))))
  (prn "SYMBOL :sacrifices-required :" (@app-state :sacrifices-required))
  (prn "2 END: play-card"))



(defn can-pay-cost? ;;PROBLEM WITH can-pay-cost 
  [card-index hand board-piece-index current-player]
  (prn "2 START: can-pay-cost?")
  (prn "SYMBOL hand:" hand)
  (prn "SYMBOL card-index:" card-index)
  (prn "SYMBOL board-index:" board-piece-index)
  (let [card-key (hand card-index)
        card-info (cards card-key)
        card-cost (card-info :cost)]

    (prn "SYMBOL sacrifices-required:" (@app-state :sacrifices-required))
    (prn "SYMBOL ")
    
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







(defn update-sacrifices-required [sacrificed-card-index board-current-players-side]
  (prn "2 START: update-sacrifices-required")
  (let [blood-drawn (reduce + (calculate-sacrificial-cards [(board-current-players-side sacrificed-card-index)] 3))
        new-sacrifices-required (- (@app-state :sacrifices-required) blood-drawn)]
    (swap! app-state assoc :sacrifices-required new-sacrifices-required)
    (prn "SYMBOL blood-drawn:" blood-drawn)
    (prn "SYMBOL new-sacrifices-required:" new-sacrifices-required)
    (prn "EVENT: :sacrifices-required changed to: " new-sacrifices-required)) 
  (prn "2 END: update-sacrifices-required")
  )

(comment
  (swap! app-state assoc :sacrifices-required 5)
  
  [{:sigils [:no-effect :bloodless]}
   {:sigils [:no-effect]}        ;;1
   {:sigils nil}                 ;;2
   {:sigils [:worthy-sacrifice]} ;;3
   nil]

  (update-sacrifices-required 1 [{:sigils [:no-effect :bloodless]}
                                 {:sigils [:no-effect]} ;;1
                                 {:sigils nil} ;;2
                                 {:sigils [:worthy-sacrifice]} ;;3
                                 nil])
  )





(defn board-segment-on-click
  [card-index board-piece-index current-player]
  (prn "1 START: board-segment-on-click")
  (let [hand (-> @app-state current-player :hand)
        board-players-side (-> @app-state
                               :board
                               current-player)
        - (prn "SYMBOL board-players-side:" board-players-side)
        - (prn "SYMNOL board-piece-index:" board-piece-index)
        board-piece-info (board-players-side board-piece-index)
        _ (prn "SYMBOL board-piece-info:" board-piece-info)

        sacrificed-cards (@app-state :sacrificed-cards)

        pay-cost? (can-pay-cost? card-index hand board-piece-index current-player)

        can-play-card? (and
                        pay-cost?
                        (space-empty? board-piece-index current-player))

        can-sacrifice-card? (and
                             (and (-> board-piece-info
                                      nil?
                                      not)
                                  
                                  (= (count
                                      (filter (fn [item]
                                                (= item board-piece-index))
                                              sacrificed-cards))
                                     1))
                             
                             (not (bloodless? (board-piece-info :sigils)) ) )]    
    (prn "SYMBOL board-piece-index:" board-piece-index)

    
    
    (cond
      can-play-card?
      (do
        (play-card hand card-index board-piece-index current-player))

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
              (swap! app-state update :sacrificed-cards conj board-piece-index)
              (update-sacrifices-required board-piece-index board-players-side)) 
      
      )
    (prn "SYMBOL :sacrifices-required :" (@app-state :sacrifices-required))
    (prn "BOOLEAN " (<= (@app-state :sacrifices-required) 0))
    
    
    (when (and
           (not (nil? (@app-state :sacrifices-required)))
           (<= (@app-state :sacrifices-required) 0))
      (do
        (prn "EVENT:  ")
        (swap! app-state update-in [:board :orange]
               #(vec (map-indexed (fn [index item]
                                    (if (some (set [index]) (@app-state :sacrificed-cards))
                                      nil
                                      item
                                      ))
                                  %1))))
      )
    
    )
  (prn "1 END: board-segment-on-click")
  (prn " "))

(comment

  (prn (->
        app-state
        :board
        :orange))
  (swap! app-state update-in [:board :orange]
         #(map-indexed (fn [index item]
                         (if (some (set [index]) (@app-state :sacrificed-cards))
                           nil
                           item
                           ))
                       %1))

  (map-indexed (fn [index item]
                 (prn "item: " item)
                 (prn "index:" index)
                 (if (some (set [index]) (@app-state :sacrificed-cards))
                   nil
                   item
                   ))
               )

  )
#_(fn [app-state]
    (-> app-state
        (update :sacrificed-cards
                conj
                board-piece-index)))

(comment
      (def foo1 [1 3 2])

      (def foo2 [{:name "card0"}
                 {:name "card1"}
                 {:name "card2"}
                 {:name "card4"}
                 {:name "card5"}])

      (not (empty?
            (filter (fn [v]
                      (= 1 v))
                    foo1)))

      (filter (fn [v]
                (= index v))
              foo1)

      (def fooB3
        (keep-indexed (fn [index item]
                        #_(if (empty?
                             (filter (fn [v]
                                       (= index v))
                                     foo1))
                          item
                          nil))
                      foo2))
      
      (def foo4
        )

      
      (def foo3
        (map-indexed (fn [index item]  
                       (if (empty?
                            (filter (fn [v]
                                      (= 1 v))
                                    foo2))
                         nil
                         item)
                       )
                     foo1))
      
      (filter (fn [v]
                (not )))
      (some #(when ))
      (some (fn [v]
              ))
      (keep-indexed ))

(comment
  (filter (fn [item]
            (not= [1 2 3 4])) 3)
  )

(defn create-board-segment
  [player]
  
  (doall
   (for [segment (range 4)]
     [:button
      {:style {:background-image (str "url("
                                      (if-let [i (:image ((player (:board @app-state))
                                                          segment))]
                                        i
                                        (str "img/board_piece_" (name player) ".png"))
                                      ")"),
               :width 123,
               :height 195},
       :on-click #(let [card-clicked (:card-clicked @app-state)]
                    (do
                      (prn "::NOTICE:: board-segment state: " (-> @app-state
                                                                  :board
                                                                  player
                                                                  ((fn [v]
                                                                     (v segment)))
                                                                  ))
                      (if (and (= (:current-player @app-state) player) 
                               (not= card-clicked
                                     nil))  

                        ;;PROBLEM IN BOARD-SEGMENT-ON-CLICK
                        (board-segment-on-click card-clicked segment player)
                        
                        (swap! app-state (fn [app-state]
                                           (-> app-state
                                               (assoc :card-clicked nil)
                                               (assoc :board-piece-clicked nil))))))),
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
   [:div

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
                           (fn [a] (vec (rest a))))))}]]])


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



