(ns clj-gcloud.duration
  (:import (org.threeten.bp.Duration)))

(defprotocol Duration
  (->duration [object]))

(extend org.threeten.bp.Duration
  Duration
  {:->duration identity})

(extend Long
  Duration
  {:->duration #(org.threeten.bp.Duration/ofMillis %)})
