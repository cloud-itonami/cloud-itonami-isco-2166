(ns design-studio.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [design-studio.governor :as governor]
            [design-studio.store :as store]))

(defn- fresh-store []
  (-> (store/mem-store)
      (store/register-project! {:project/id "proj-1"
                                :licensed-assets #{"font-a" "photo-b"}
                                :brand-guidelines "client brandbook v2"})))

(defn- proposal [op] {:op op :effect :propose :stake :low :confidence 0.95})

(deftest ok-on-clean-proof
  (let [v (governor/check {:project-id "proj-1" :assets ["font-a"]}
                          {} (proposal :produce-proof) (fresh-store))]
    (is (:ok? v))))

(deftest hard-holds
  (testing "unregistered project"
    (let [v (governor/check {:project-id "no-such" :assets []}
                            {} (proposal :produce-proof) (fresh-store))]
      (is (:hard? v))
      (is (some #(= :no-project (:rule %)) (:violations v)))))
  (testing "non-propose effect"
    (let [v (governor/check {:project-id "proj-1"}
                            {} (assoc (proposal :produce-proof) :effect :write!)
                            (fresh-store))]
      (is (:hard? v)))))

(deftest escalations
  (testing "unlicensed asset use"
    (let [v (governor/check {:project-id "proj-1" :assets ["font-a" "stock-x"]}
                            {} (proposal :deliver-design) (fresh-store))]
      (is (not (:hard? v)))
      (is (:escalate? v))
      (is (some #(= :unlicensed-asset (:rule %)) (:escalations v)))))
  (testing "brand-guideline override"
    (let [v (governor/check {:project-id "proj-1" :assets [] :brand-override? true}
                            {} (proposal :deliver-design) (fresh-store))]
      (is (:escalate? v))
      (is (some #(= :brand-guideline-override (:rule %)) (:escalations v)))))
  (testing "low confidence"
    (let [v (governor/check {:project-id "proj-1" :assets []}
                            {} (assoc (proposal :produce-proof) :confidence 0.2)
                            (fresh-store))]
      (is (:escalate? v)))))
