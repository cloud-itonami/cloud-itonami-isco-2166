(ns design-studio.governor
  "GraphicDesignGovernor — the independent licensing/brand-integrity layer
  for the ISCO-08 2166 independent graphic/multimedia designer actor. Wired
  as its own `:govern` node in `design-studio.actor`'s StateGraph,
  downstream of `:advise` — the Advisor has no notion of project provenance,
  asset licensing, or client brand guidelines, so this MUST be a separate
  system able to reject a proposal (itonami actor pattern, per
  ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. project provenance — the request's project must be registered.
    2. no-actuation       — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off):
    3. any requested asset outside the project's licensed-assets set
       (no unlicensed asset use without license verification).
    4. a client brand-guideline override.
    5. low confidence (< `confidence-floor`)."
  (:require [design-studio.store :as store]))

(def confidence-floor 0.6)

(defn- hard-violations [{:keys [request proposal]} project-record]
  (cond-> []
    (nil? project-record)
    (conj {:rule :no-project :detail (str "未登録 project " (:project-id request))})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn unlicensed-assets
  "Requested assets outside the project's licensed set."
  [project-record request]
  (vec (remove (or (:licensed-assets project-record) #{})
               (or (:assets request) []))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a `store`
  implementing `design-studio.store/Store`. Returns
  `{:ok? bool :violations [...] :escalations [...] :confidence n
    :hard? bool :escalate? bool}`."
  [request _context proposal store]
  (let [project-record (store/project store (:project-id request))
        hard (hard-violations {:request request :proposal proposal} project-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        unlicensed (when project-record (unlicensed-assets project-record request))
        escalations (cond-> []
                      (seq unlicensed)
                      (conj {:rule :unlicensed-asset
                             :detail (str "license 未確認 asset: " unlicensed)})

                      (:brand-override? request)
                      (conj {:rule :brand-guideline-override
                             :detail "クライアント brand guideline の override は人間承認が必要"})

                      low?
                      (conj {:rule :low-confidence :detail conf}))]
    {:ok? (and (not hard?) (empty? escalations))
     :violations hard
     :escalations escalations
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (boolean (seq escalations)))}))
