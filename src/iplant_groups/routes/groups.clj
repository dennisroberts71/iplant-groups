(ns iplant-groups.routes.groups
  (:use [common-swagger-api.schema]
        [iplant-groups.routes.schemas.group]
        [iplant-groups.routes.schemas.privileges]
        [iplant-groups.routes.schemas.params]
        [ring.util.http-response :only [ok]])
  (:require [iplant-groups.service.groups :as groups]))

(defroutes groups
  (GET "/" []
    :query       [params GroupSearchParams]
    :return      GroupListWithDetail
    :summary     "Group Search"
    :description "This endpoint allows callers to search for groups by name. Only groups that
    are visible to the given user will be listed. The folder name, if provided, contains the
    name of the folder to search. Any folder name provided must exactly match the name of a
    folder in the system."
    (ok (groups/group-search params)))

  (POST "/" []
    :return      GroupWithDetail
    :query       [params StandardUserQueryParams]
    :body        [body (describe BaseGroup "The group to add.")]
    :summary     "Add Group"
    :description "This endpoint allows adding a new group."
    (ok (groups/add-group body params)))

  (context "/id/:group-id" []
    :path-params [group-id :- GroupIdPathParam]

    (GET "/" []
      :query       [params StandardUserQueryParams]
      :return      GroupWithDetail
      :summary     "Get Group Information by Group ID"
      :description "This endpoint allows callers to get detailed information about a single
      group."
      (ok (groups/get-group-by-id group-id params)))

    (GET "/members" []
      :query       [params GroupMemberListingQueryParams]
      :return      GroupMembers
      :summary     "List Group Members by Group ID"
      :description "This endpoint allows callers to list the members of a single group."
      (ok (groups/get-group-members-by-id group-id params))))

  (context "/:group-name" []
    :path-params [group-name :- GroupNamePathParam]

    (GET "/" []
      :query       [params StandardUserQueryParams]
      :return      GroupWithDetail
      :summary     "Get Group Information"
      :description "This endpoint allows callers to get detailed information about a single
      group."
      (ok (groups/get-group group-name params)))

    (PUT "/" []
      :return      GroupWithDetail
      :query       [params StandardUserQueryParams]
      :body        [body (describe GroupUpdate "The group information to update.")]
      :summary     "Update Group"
      :description "This endpoint allows callers to update group information."
      (ok (groups/update-group group-name body params)))

    (DELETE "/" []
      :query       [params StandardUserQueryParams]
      :return      GroupStub
      :summary     "Delete Group"
      :description "This endpoint allows deleting a group if the current user has permissions to do so."
      (ok (groups/delete-group group-name params)))

    (context "/privileges" []
      (GET "/" []
        :query       [params GroupPrivilegeSearchQueryParams]
        :return      GroupPrivileges
        :summary     "List Group Privileges"
        :description "This endpoint allows callers to list the privileges visible to the current user of a single
        group."
        (ok (groups/get-group-privileges group-name params)))

      (POST "/" []
        :query       [params PrivilegeUpdateParams]
        :body        [body (describe GroupPrivilegeUpdates "The privilege updates to process.")]
        :return      GroupPrivileges
        :summary     "Update Group Privileges"
        :description "This endpoint allows callers to update the privileges for a group."
        (ok (groups/update-group-privileges group-name body params)))

      (POST "/deleter" []
        :query       [params StandardUserQueryParams]
        :body        [body (describe GroupPrivilegeRemovals "The privilege updates to process.")]
        :return      GroupPrivileges
        :summary     "Remove Group Privileges"
        :description "This endpoint allows callers to remove group privileges."
        (ok (groups/remove-group-privileges group-name body params)))

      (context "/:subject-id/:privilege-name" []
        :path-params [subject-id :- SubjectIdPathParam
                      privilege-name :- ValidGroupPrivileges]

        (PUT "/" []
          :query       [params StandardUserQueryParams]
          :return      Privilege
          :summary     "Add Group Privilege"
          :description "This endpoint allows callers to add a specific privilege for a specific subject to a
          specific group."
          (ok (groups/add-group-privilege group-name subject-id privilege-name params)))

        (DELETE "/" []
          :query       [params StandardUserQueryParams]
          :return      Privilege
          :summary     "Remove Group Privilege"
          :description "This endpoint allows callers to remove a specific privilege for a specific subject to a
          specific group."
          (ok (groups/remove-group-privilege group-name subject-id privilege-name params)))))

    (context "/members" []
      (GET "/" []
        :query       [params GroupMemberListingQueryParams]
        :return      GroupMembers
        :summary     "List Group Members"
        :description "This endpoint allows callers to list the members of a single group."
        (ok (groups/get-group-members group-name params)))

      (PUT "/" []
        :query       [params StandardUserQueryParams]
        :body        [body (describe GroupMembersUpdate "The new list of group member IDs.")]
        :return      GroupMembersUpdateResponse
        :summary     "Replace Group Members"
        :description "This endpoint allows callers to completely replace the members of a group."
        (ok (groups/replace-members group-name body params)))

      (POST "/" []
        :query       [params StandardUserQueryParams]
        :body        [body (describe GroupMembersUpdate "The list of group member IDs to add.")]
        :return      GroupMembersUpdateResponse
        :summary     "Add Group Members"
        :description "This endpoint allows callers to add multiple members to a group."
        (ok (groups/add-members group-name body params)))

      (POST "/deleter" []
        :query       [params StandardUserQueryParams]
        :body        [body (describe GroupMembersUpdate "The list of group member IDs to remove.")]
        :return      GroupMembersUpdateResponse
        :summary     "Remove Group Members"
        :description "This endpoint allows callers to remove multiple members from a group."
        (ok (groups/remove-members group-name body params)))

      (context "/:subject-id" []
        :path-params [subject-id :- SubjectIdPathParam]

        (PUT "/" []
          :query       [params StandardUserQueryParams]
          :summary     "Add Group members"
          :description "This endpoint allows callers to add members to a group. Note that a request to add a user
          who is already a member of the group is treated as a no-op and no error will be reported."
          (groups/add-member group-name subject-id params)
          (ok))

        (DELETE "/" []
          :query       [params StandardUserQueryParams]
          :summary     "Remove Group members"
          :description "This endpoint allows callers to add members to a group. Note that a request to remove
          someone who is not currently a member of the group (even a non-existent user) is treated as a no-op
          and no error will be reported."
          (groups/remove-member group-name subject-id params)
          (ok))))))
