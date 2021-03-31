(ns clj-gcloud.coerce
  (:import
   (com.google.api.gax.paging Page)
   (com.google.api.gax.retrying RetrySettings)
   (com.google.auth.oauth2 ServiceAccountCredentials)
   (com.google.cloud ByteArray ServiceOptions)
   (com.google.common.base CaseFormat Converter)
   (java.util Iterator List Map)
   (java.util.concurrent TimeUnit)
   (org.threeten.bp Duration)))

(defn gc-bytes->bytes
  ^bytes
  [^ByteArray bts]
  (.toByteArray bts))

(defn bytes->gc-bytes
  ^ByteArray
  [^bytes bts]
  (ByteArray/copyFrom bts))

(defn page->seq
  [^Page page]
  (iterator-seq
    (.iterator (.iterateAll page))))

(defprotocol ClojureCoercible
  (->clj [object]))

(extend nil
  ClojureCoercible
  {:->clj (constantly nil)})

(extend Object
  ClojureCoercible
  {:->clj identity})

(extend List
  ClojureCoercible
  {:->clj (fn [o] (map ->clj o))})

(extend Map
  ClojureCoercible
  {:->clj (fn [^Map o] (reduce-kv #(assoc %1 %2 (->clj %3)) {} (into {} o)))})

(extend Page
  ClojureCoercible
  {:->clj (fn [^Page o]
            (map ->clj (page->seq o)))})

(extend ByteArray
  ClojureCoercible
  {:->clj gc-bytes->bytes})

(extend Iterator
  ClojureCoercible
  {:->clj (fn [o] (map ->clj (iterator-seq o)))})

(extend Duration
  ClojureCoercible
  {:->clj (fn [^Duration o] (+ (.toSeconds TimeUnit/NANOSECONDS (.getNano o)) (.getSeconds o)))})

(extend RetrySettings
  ClojureCoercible
  {:->clj (fn [^RetrySettings o]
            {:total-timeout          (->clj (.getTotalTimeout o))
             :initial-retry-delay    (->clj (.getInitialRetryDelay o))
             :retry-delay-multiplier (.getRetryDelayMultiplier o)
             :max-retry-delay        (->clj (.getMaxRetryDelay o))
             :max-attempts           (->clj (.getMaxAttempts o))
             :jittered               (.isJittered o)})})

(defn accessor->name
  [input-string]
  (-> (re-find #"^(get-)?(.*)$" input-string)
      (nth 2)
      (keyword)))

(def ^Converter lh->uc (.converterTo CaseFormat/LOWER_HYPHEN CaseFormat/UPPER_CAMEL))
(defn ->getter [field]
  (str ".get" (.convert lh->uc field)))

(defmacro create-clj-coerce
  [type fields]
  (let [o#          (with-meta (gensym) {:tag type})
        field-list# (eval fields)]
    `(extend ~type
       ClojureCoercible
       {:->clj (fn [~o#]
                 ~(into {} (map (fn [field-name]
                                  [(accessor->name (name field-name))
                                   `(->clj (~(symbol (->getter (name field-name))) ~o#))])
                                field-list#)))})))

(create-clj-coerce ServiceOptions [:project-id :host :credentials :retry-settings])
(create-clj-coerce ServiceAccountCredentials [:project-id :client-id :client-email])
