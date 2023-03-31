(ns clj-gcloud.dsl
  (:require
   [clojure.walk :refer [postwalk]])
  (:import
   (com.google.api.client.json JsonFactory)
   (com.google.api.client.json.gson GsonFactory)
   (com.google.common.base CaseFormat Converter)
   (java.lang.reflect Method)
   (java.util EnumSet)))

(def ^:private ^Converter lh->lc
  (.converterTo CaseFormat/LOWER_HYPHEN CaseFormat/LOWER_CAMEL))

(def ^:private ^Converter lh->uu
  (.converterTo CaseFormat/LOWER_HYPHEN CaseFormat/UPPER_UNDERSCORE))

(def ^:private ^Converter uu->lh
  (.converterTo CaseFormat/UPPER_UNDERSCORE CaseFormat/LOWER_HYPHEN))

(defn ^String kw->field-name
  "Converts a keyword into a field name"
  [kw]
  (->> kw name (.convert lh->lc)))

(defn ^String kw->enum-str
  "Converts a keyword into a enum string"
  [kw]
  (->> kw name (.convert lh->uu)))

(defn enum->kw
  "Converts an enum into a keyword"
  [^Enum e]
  (->> (.name e)
       (.convert uu->lh)
       keyword))

(defn enums-as-map
  "Creates a map of keywords -> enums"
  [c]
  (reduce
   (fn [m e] (assoc m (enum->kw e) e))
   {}
   (EnumSet/allOf c)))

(defn dsl->google-json-map
  "Recursively transforms a DSL map to conform to the Google JSON format:
  - All keyword keys become lower camel strings
  - All keyword values become upper underscore strings"
  [m]
  (let [xform-key (fn [[k v]] (if (keyword? k) [(kw->field-name k) v] [k v]))
        xform-val (fn [[k v]] (if (keyword? v) [k (kw->enum-str v)] [k v]))]
    ;; only apply to maps
    (postwalk
     (fn [x] (if (map? x) (into {} (map (comp xform-key xform-val) x)) x))
     m)))

(def ^JsonFactory json-factory (GsonFactory/getDefaultInstance))

(defn ^Method get-static-method
  "Returns a static method"
  [^Class cls n arg-classes]
  (let [m (.getDeclaredMethod cls n (into-array Class arg-classes))]
    (.setAccessible m true)
    m))
