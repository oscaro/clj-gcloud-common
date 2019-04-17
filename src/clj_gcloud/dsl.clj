(ns clj-gcloud.dsl
  (:import (com.google.common.base CaseFormat Converter)
           (com.google.api.client.json JsonFactory)
           (com.google.api.client.json.jackson2 JacksonFactory)
           (java.lang.reflect Method)))

(def ^:private ^Converter lh->lc
  (.converterTo CaseFormat/LOWER_HYPHEN CaseFormat/LOWER_CAMEL))

(def ^:private ^Converter lh->uu
  (.converterTo CaseFormat/LOWER_HYPHEN CaseFormat/UPPER_UNDERSCORE))

(defn ^String kw->field-name
  "Converts a keyword into a field name"
  [kw]
  (->> kw name (.convert lh->lc)))

(defn ^String kw->enum-str
  "Converts a keyword into a enum string"
  [kw]
  (->> kw name (.convert lh->uu)))

(defn dsl->google-json-map
  "Recursively transforms a DSL map to conform to the Google JSON format:
  - All keyword keys become lower camel strings
  - All keyword values become upper underscore strings"
  [m]
  (let [xform-key (fn [[k v]] (if (keyword? k) [(kw->field-name k) v] [k v]))
        xform-val (fn [[k v]] (if (keyword? v) [k (kw->enum-str v)] [k v]))]
    ;; only apply to maps
    (clojure.walk/postwalk
      (fn [x] (if (map? x) (into {} (map (comp xform-key xform-val) x)) x))
      m)))

(def ^JsonFactory json-factory (JacksonFactory/getDefaultInstance))

(defn ^Method get-static-method
  "Returns a static method"
  [^Class cls n arg-classes]
  (let [m (.getDeclaredMethod cls n (into-array Class arg-classes))]
    (.setAccessible m true)
    m))
