package system.ticketing.utilities.util;

/**
 * Created by Ncube on 2/5/17.
 */
public class SystemConstants {
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_APPROVAL = "PENDING APPROVAL";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_DISAPPROVED = "DISAPPROVED";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_DELETED = "DELETED";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_BLOCKED = "BLOCKED";

    public static final String STATUS_POSTING_REQUEST = "POSTING REQUEST";
    public static final String STATUS_POSTING_RESPONSE = "POSTING RESPONSE";
    public static final String STATUS_SUCCESSFUL = "SUCCESSFUL";
    public static final String STATUS_RETRY = "RETRY";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_MANUAL_RESOLVE = "MANUAL_RESOLVE";

    public static final String STATUS_PENDING_EXPORT = "PENDING EXPORT";
    public static final String STATUS_PENDING_IMPORT = "PENDING IMPORT";

    public static final String AUTH_STATUS_AUTHENTICATED = "Authenticated";
    public static final String AUTH_STATUS_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String AUTH_STATUS_NETWORK_PROBLEM = "Network Problem";
    public static final String AUTH_STATUS_ACCOUNT_LOCKED = "Account has been locked";
    public static final String AUTH_STATUS_CHANGE_PASSWORD = "Should change password";
    public static final String AUTH_STATUS_SYSTEM_ERROR = "System error occured";
    public static final String CHANGE_PASSWORD_SUCCESS = "Change password success";
    public static final String CHANGE_PASSWORD_FAILURE = "Change password failure";
    public static final String INVALID_OLD_PASSWORD = "Incorrect old password";
    public static final String PASSWORD_IN_HISTORY = "Password has already been used";
    public static final String RESET_PASSWORD_SUCCESS = "Reset password success";
    public static final String RESET_PASSWORD_FAILURE = "Reset password failure";
    public static final String AUTH_STATUS_PROFILE_EXPIRED = "Profile Expired";

    public static final String PC_CUSTOMER_INFO = "300000";
    public static final String PC_VEND_ADVICE = "310000";
    public static final String PC_LAST_TOKEN = "320000";
    public static final String PC_VERY_TOKEN = "330000";
    public static final String PC_FAULT_REPORT = "340000";

    public static final String PC_CREDIT_TOKEN = "400000";
    public static final String PC_FREE_TOKEN = "410000";
    public static final String PC_TEST_TOKEN = "420000";
    public static final String PC_CREDIT_TRANSFER_TOKEN = "430000";
    public static final String PC_REVERSAL = "440000";
}
