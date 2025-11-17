package in.zeta.constants;

public class Messages {

    private Messages() {}

    public static class Auth {
        public static final String REGISTERING_NEW_USER = "Registering new user";
        public static final String USER_REGISTERED_SUCCESSFULLY = "User registered successfully";
        public static final String LOGGING_IN_USER = "User trying to log in";
        public static final String LOGIN_SUCCESSFUL = "Login successful";
        public static final String AUTHENTICATING_USER = "Authenticating user";
        public static final String USER_AUTHENTICATED_SUCCESSFULLY = "User authenticated successfully";
    }

    public static class User {
        public static final String FETCHING_USER_DETAILS = "Fetching user details";
        public static final String FETCHING_USER_BY_ROLE = "Fetching user by role";
        public static final String UPDATING_USER_DETAILS = "Updating user details";
        public static final String FETCHED_USER_DETAILS_SUCCESSFULLY = "Fetched user details successfully";
        public static final String UPDATING_USER_ROLE = "Updating user role";
        public static final String USER_ROLE_UPDATED_SUCCESSFULLY = "User role updated successfully";
        public static final String UPDATE_USER_ROLE = "Update user role";
        public static final String USER_CREATED_WITH_ROLE = "User created with role";
    }

    public static class VerificationRequestLimit {
        public static final String FETCHING_REQUESTOR_REQUESTS_TO_CUSTOMER = "Fetching requestor requests to customer for year:";
        public static final String FETCHED_REQUESTOR_REQUESTS_TO_CUSTOMER = "Fetched requestor requests to customer for year:";
        public static final String FETCHING_TOTAL_REQUESTS_TO_CUSTOMER = "Fetching total requests to customer for current year:";
        public static final String FETCHED_TOTAL_REQUESTS_TO_CUSTOMER = "Fetched total requests to customer for current year:";
        public static final String FETCHING_ALL_REQUESTORS_COUNT = "Fetching all requestors count for customer:";
        public static final String FETCHED_ALL_REQUESTORS_COUNT = "Fetched all requestors count for customer:";
    }

    public static class Verification {
        public static final String REQUEST_CREATED_SUCCESSFULLY = "Verification request created successfully";
        public static final String VERIFICATION_STATUS = "Verification status fetched successfully";
        public static final String MULTIPLE_REQUESTS_CREATED = "Verification requests created for multiple customers successfully";
        public static final String ASSIGNED_SUCCESSFULLY = "Verification request assigned successfully";
        public static final String STATUS_UPDATED_SUCCESSFULLY = "Status updated successfully";
        public static final String STATUS_FETCHED = "Verification status fetched successfully";
        public static final String REQUESTS_BY_REQUESTOR = "All verification requests by requestor";
        public static final String REQUEST_FOR_VERIFICATION_ID = "Verification request for verification ID";
        public static final String REQUESTS_FOR_CUSTOMER = "Verification requests for customer";
        public static final String REQUESTS_ASSIGNED_TO_OFFICER = "Verification requests assigned to officer";
        public static final String VERIFICATION_REQUEST_CREATED_SUCCESSFULLY = "Verification request created successfully";
        public static final String VERIFICATION_REQUEST_CREATED_FOR_MULTIPLE_CUSTOMER_SUCCESSFULLY = "Verification requests created for multiple customers successfully";
        public static final String ALL_VERIFICATION_REQUESTS_BY_REQUESTOR = "All verification requests fetched for requestor";
        public static final String VERIFICATION_REQUEST_FOR_VERIFICATION_ID = "Verification request fetched for verification ID";
        public static final String VERIFICATION_REQUESTS_FOR_CUSTOMER = "Verification requests fetched for customer";
        public static final String ASSIGNED_VERIFICATION_REQUEST_SUCCESSFULLY = "Verification request assigned to officer successfully";
        public static final String FETCHING_VERIFICATION_REQUEST = "Fetching verification request for requestorId:";
        public static final String CREATING_VERIFICATION_REQUEST = "Creating verification request for requestorId:";
        public static final String VERIFICATION_REQUEST_CREATED = "Verification request created successfully with id:";
        public static final String CREATING_MULTIPLE_VERIFICATION_REQUESTS = "Creating multiple verification requests for requestorId:";
        public static final String MULTIPLE_VERIFICATION_REQUESTS_CREATED = "Multiple verification requests created successfully";
        public static final String FETCHING_VERIFICATION_REQUESTS_BY_REQUESTOR = "Fetching verification requests for requestorId:";
        public static final String FETCHED_VERIFICATION_REQUESTS_BY_REQUESTOR = "Fetched verification requests for requestorId:";
        public static final String FETCHING_VERIFICATION_REQUEST_BY_ID = "Fetching verification request for verificationId:";
        public static final String FETCHED_VERIFICATION_REQUEST_BY_ID = "Fetched verification request for verificationId:";
        public static final String FETCHING_VERIFICATION_REQUESTS_FOR_CUSTOMER = "Fetching verification requests for customerId:";
        public static final String FETCHED_VERIFICATION_REQUESTS_FOR_CUSTOMER = "Fetched verification requests for customerId:";
        public static final String FETCHING_VERIFICATION_REQUESTS_FOR_OFFICER = "Fetching all verification requests assigned to officer:";
        public static final String FETCHED_VERIFICATION_REQUESTS_FOR_OFFICER = "Fetched verification requests assigned to officer with officerId:";
        public static final String FETCHING_PENDING_VERIFICATION_REQUESTS = "Fetching all pending verification requests";
        public static final String ASSIGNING_TO_OFFICER = "Assigning verification request to officer:";
        public static final String UPDATING_VERIFICATION_STATUS = "Updating status of verification request:";
        public static final String UPDATED_VERIFICATION_STATUS = "Updated status of verification request:";
    }

    public static class Document {
        public static final String UPLOAD_SUCCESS = "Document uploaded successfully";
        public static final String MULTIPLE_UPLOAD_SUCCESS = "Documents uploaded successfully";
        public static final String DELETE_SUCCESS = "Document deleted successfully";
        public static final String MULTIPLE_DELETE_SUCCESS = "Documents deleted successfully";
        public static final String FETCH_LIST_SUCCESS = "Fetched list of documents successfully";
        public static final String UPDATED_SUCCESSFULLY = "Document updated successfully";
        public static final String UPLOADING_DOCUMENT = "Uploading document:";
        public static final String DOCUMENT_UPLOADED = "Document uploaded:";
        public static final String DELETING_DOCUMENT = "Deleting document:";

    }

    public static class Comment {
        public static final String ADD_SUCCESS = "Comment added successfully";
        public static final String ADDING_COMMENT = "Adding comment to verification request:";
        public static final String COMMENT_ADDED = "Comment added to verification request:";
        public static final String READING_COMMENTS_FOR_CUSTOMER = "Reading comments for customer:";
        public static final String COMMENTS_RETRIEVED_FOR_CUSTOMER = "Comments retrieved for customer:";
        public static final String READING_COMMENT_BY_ID = "Reading comment by ID:";
        public static final String COMMENT_RETRIEVED_BY_ID = "Comment retrieved by ID:";
        public static final String UPDATING_COMMENT = "Updating comment:";
        public static final String COMMENT_UPDATED = "Comment updated:";
        public static final String DELETING_COMMENT = "Deleting comment:";
        public static final String COMMENT_DELETED = "Comment deleted:";
        public static final String READING_COMMENTS_BY_OFFICER = "Reading comments by officer:";
        public static final String COMMENTS_RETRIEVED_BY_OFFICER = "Comments retrieved by officer:";
    }

    public static class Status{
        public static final String FETCHING_VERIFICATION_STATUS = "Fetching verification status for verificationId:";
        public static final String FETCHED_VERIFICATION_STATUS = "Fetched verification status for verificationId:";
        public static final String FETCHING_STATUS_HISTORY = "Fetching status history for verificationId:";
        public static final String FETCHED_STATUS_HISTORY = "Fetched status history for verificationId:";
    }

    public static class Notification {
        public static final String CREATED_SUCCESSFULLY = "Notification created successfully";
        public static final String SENT_SUCCESSFULLY = "Notification sent successfully";
        public static final String FETCH_SUCCESSFULLY = "Notifications fetched successfully";
        public static final String MARKING_AS_READ = "Marking notification as read:";
        public static final String MARKED_AS_READ_SUCCESSFULLY = "Notification marked as read successfully";
        public static final String UNREAD_COUNT_FETCHED = "Unread notification count fetched successfully";
        public static final String FETCHING_NOTIFICATIONS_FOR_USER = "Fetching notifications for user:";
        public static final String FETCHED_NOTIFICATIONS_FOR_USER = "Fetched notifications for user:";
        public static final String FETCHING_UNREAD_COUNT_FOR_USER = "Fetching unread notification count for user:";
        public static final String UNREAD_COUNT_FOR_USER_FETCHED = "Unread notification count for user fetched:";
        public static final String DOCUMENT_UPLOADED = "A new document has been uploaded.";
        public static final String DOCUMENT_UPDATED = "A document has been updated.";
        public static final String STATUS_CHANGED = "The status of your verification request has changed.";
        public static final String ASSIGNED_TO_OFFICER = "You have been assigned a new verification request.";
        public static final String VERIFICATION_REQUESTED = "A new verification request has been created.";
        public static final String VERIFICATION_APPROVED = "Your verification request has been approved.";
        public static final String VERIFICATION_REJECTED = "Your verification request has been rejected.";
        public static final String SENT_BACK_FOR_DETAILS = "Your verification request has been sent back for more details.";
        public static final String COMMENT_ADDED = "A new comment has been added to your verification request.";
    }

    public static class Audit {
        public static final String FETCHED_TRAIL_SUCCESSFULLY = "Fetched audit trail successfully";
        public static final String FETCHED_LOGS_FOR_USER = "Fetched audit logs of user successfully";
        public static final String FETCHED_ALL_LOGS = "Fetched all audit logs successfully";
        public static final String FETCHING_AUDIT_TRAIL = "Fetching audit trail";
        public static final String FETCHING_ALL_AUDIT_LOGS = "Fetching all audit logs";
    }

    public static class Keys {
        public static final String LOG_COUNT = "logCount";
        public static final String USER_ID = "userId";
        public static final String YEAR = "year";
        public static final String VERIFICATION_ID = "verificationId";
        public static final String CUSTOMER_ID = "customerId";
        public static final String TENANT_ID = "tenantId";
        public static final String COMMENT_ID = "commentId";
        public static final String DOCUMENT_ID = "documentId";
        public static final String REQUESTOR_ID = "requestorId";
        public static final String OFFICER_ID = "officerId";
        public static final String STATUS = "status";
        public static final String REASON = "reason";
        public static final String REQUEST_COUNT = "requestCount";
        public static final String MAX_ALLOWED_REQUESTS = "maxAllowedRequests";
        public static final String RESPONSE_SIZE = "responseSize";
        public static final String ROLE = "role";
        public static final String EMAIL = "email";
        public static final String USERNAME = "username";
        public static final String ENTITY_ID = "entityId";
        public static final String ACTION = "action";
        public static final String ENTITY_TYPE = "entityType";
        public static final String TIMESTAMP = "timestamp";
        public static final String DETAILS = "details";
        public static final String COMMENT_COUNT = "commentCount";
        public static final String NOTIFICATION_COUNT = "notificationCount";
        public static final String NOTIFICATION_ID = "notificationId";
        public static final String HISTORY_COUNT = "historyCount";
        public static final String NEW_ROLE = "newRole";
        public static final String FILE_NAME = "fileName";
        public static final String FILE_SIZE = "fileSize";
        public static final String VERIFICATION_REQUEST_COUNT = "verificationRequestCount";
    }

    public static class Errors {
        public static final String USER_NOT_FOUND = "User not found";
        public static final String VERIFICATION_REQUEST_NOT_FOUND = "Verification request not found";
        public static final String DOCUMENT_NOT_FOUND = "Document not found";
        public static final String COMMENT_NOT_FOUND = "Comment not found";
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String ROLE_UPDATE_FAILED = "Failed to update user role";
        public static final String VERIFICATION_ASSIGNMENT_FAILED = "Failed to assign verification request";
        public static final String STATUS_UPDATE_FAILED = "Failed to update status";
        public static final String NOTIFICATION_SENDING_FAILED = "Failed to send notification";
        public static final String AUDIT_LOG_FETCH_FAILED = "Failed to fetch audit logs";
        public static final String SINGLE_FILE_WARNING = "Single file upload expected";
        public static final String FILE_EXPECTED = "At least one file is required";
        public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
        public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
        public static final String INVALID_PASSWORD = "Invalid password";
        public static final String USER_WITH_ROLE_NOT_FOUND = "User with specified role not found";
        public static final String FAILED_LOGIN_ATTEMPT = "Failed login attempt for email";
    }

    public static class Exceptions {
        public static final String RESOURCE_NOT_FOUND = "Requested resource not found";
        public static final String INVALID_FILE = "Invalid file provided";
        public static final String LIMIT_EXCEEDED = "Operation limit exceeded";
        public static final String INVALID_STATUS_TRANSITION = "Invalid status transition";
        public static final String INVALID_OPERATION = "Invalid operation";
        public static final String FILE_SIZE_EXCEEDED = "File size exceeds maximum allowed limit";
        public static final String VALIDATION_ERROR = "Validation error";
        public static final String ACCESS_DENIED = "Access denied";
        public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
        public static final String DATABASE_ERROR = "Database error occurred";

    }
}
