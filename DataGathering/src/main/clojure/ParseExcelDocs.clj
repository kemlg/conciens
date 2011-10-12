(ns ParseExcelDocs
  
(:import 
	[com.google.gdata.data.spreadsheet SpreadsheetEntry CellFeed CellEntry SpreadsheetFeed WorksheetEntry]
  [com.google.gdata.client.spreadsheet SpreadsheetService]
	[java.io BufferedReader InputStream InputStreamReader]
	[java.net URL]	
)  

(:require clojure.contrib.pprint))
  

(load-file "./contrib/passwd.clj")

;(println "Starting...")

(def feed (. spread_client getFeed (URL. "https://spreadsheets.google.com/feeds/spreadsheets/private/full") SpreadsheetFeed) )

(defn cell-treatment [ce]
  (hash-map
    :row (. (. ce getCell) getRow)
    :col (. (. ce getCell) getCol)
    :value (. (. ce getCell) getValue)
  )
  )

(defn work-sheet-treatment [we]
  (hash-map 
    :name (. (. we getTitle) getPlainText)
    :row-count (. we getRowCount)
    :col-count (. we getColCount)
    :content (partition-by :row (map cell-treatment (. (. spread_client getFeed (. we getCellFeedUrl) CellFeed) getEntries )))
  )
)



(defn spread-sheet-treatment [se]
  (hash-map 
    :url (. (. se getHtmlLink) getHref)
    :name (. (. se getTitle) getPlainText)
    :content (map work-sheet-treatment (. se getWorksheets))
  )
)

(defn google-docs-treatment []
  ;(pprint
  (map spread-sheet-treatment (. feed getEntries)))  
 ; )

;(google-docs-treatment)

;(println "Ending...")
