(ns Mongo2CSV
    (:require [somnium.congomongo :as cm]))

(cm/make-connection "test"
                   :host "147.83.200.97"
                   :port 27017)