(ns iplant-groups.service.folders
  (:require [iplant-groups.clients.grouper :as grouper]
            [iplant-groups.service.format :as fmt]
            [iplant-groups.service.util :as util]
            [iplant-groups.util.service :as service]))

(defn folder-search
  [{:keys [user search]}]
  {:folders (mapv fmt/format-folder (grouper/folder-search user search))})

(defn get-folder
  [folder-name {:keys [user]}]
  (if-let [folder (grouper/get-folder user folder-name)]
    (fmt/format-folder folder)
    (service/not-found "folder" folder-name)))

(defn get-folder-privileges
  [folder-name {:keys [user]}]
  (let [[privileges attribute-names] (grouper/get-folder-privileges user folder-name)]
    {:privileges (mapv #(fmt/format-privilege attribute-names %) privileges)}))

(defn add-folder
  [{:keys [name description display_extension]} {:keys [user]}]
  (let [folder (grouper/add-folder user name display_extension description)]
    (fmt/format-folder folder)))

(defn add-folder-privilege
  [folder-name subject-id privilege-name {:keys [user]}]
  (let [[privilege attribute-names] (grouper/add-folder-privileges user folder-name [subject-id] [privilege-name])]
    (fmt/format-privilege attribute-names privilege)))

(defn remove-folder-privilege
  [folder-name subject-id privilege-name {:keys [user]}]
  (util/verify-not-removing-own-privileges user [subject-id])
  (let [[privilege attribute-names] (grouper/remove-folder-privileges user folder-name [subject-id] [privilege-name])]
    (fmt/format-privilege attribute-names privilege)))

(defn update-folder-privileges
  [folder-name {:keys [updates]} {:keys [user] :as params}]
  (util/verify-not-removing-own-privileges user (map :subject_id updates))
  (doseq [[privileges vs] (group-by (comp set :privileges) updates)]
    (grouper/update-folder-privileges user folder-name (mapv :subject_id vs) privileges))
  (get-folder-privileges folder-name params))

(defn update-folder
  [folder-name {:keys [name description display_extension]} {:keys [user]}]
  (let [folder (grouper/update-folder user folder-name name display_extension description)]
    (fmt/format-folder folder)))

(defn delete-folder
  [folder-name {:keys [user]}]
  (fmt/format-folder (grouper/delete-folder user folder-name)))
