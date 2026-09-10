(ns design-studio.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [design-studio.actor :as actor]
            [design-studio.store :as store]))

(defn- fresh-store []
  (-> (store/mem-store)
      (store/register-project! {:project/id "proj-1"
                                :licensed-assets #{"font-a" "photo-b"}
                                :brand-guidelines "client brandbook v2"})))

(deftest commits-a-clean-proof
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:project-id "proj-1" :op :produce-proof
                 :assets ["font-a" "photo-b"] :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (= ["font-a" "photo-b"] (:assets (get-in result [:state :record]))))
    (is (= 1 (count (store/records-of st "proj-1"))))))

(deftest holds-on-unregistered-project
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:project-id "no-such" :op :produce-proof :assets [] :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-unlicensed-asset-until-license-verified
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:project-id "proj-1" :op :deliver-design
                 :assets ["stock-x"] :stake :medium}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "proj-1")))
    (testing "approval (= human license verification) resumes to commit"
      (let [resumed (actor/approve! graph "thread-3")]
        (is (= :done (:status resumed)))
        (is (= 1 (count (store/records-of st "proj-1"))))))))
