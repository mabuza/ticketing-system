package system.ticketing.profile.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.profile.model.Profile;
import system.ticketing.utilities.util.SystemConstants;
import system.ticketing.utilities.util.Utils;

import javax.naming.*;
import javax.naming.directory.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Created by Ncube on 2/5/17.
 */
public class LdapUtil {
    // get the sys config params
    static final Logger log = LoggerFactory.getLogger(LdapUtil.class);
    private static Properties config = Utils.configParams;

    public static Hashtable<String, String> getEnvironmentVariables() {

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,
                "ldap://" + config.getProperty("SYSTEM_LDAP_HOST_NAME") + ":"
                        + config.getProperty("SYSTEM_LDAP_HOST_PORT"));
        env.put(Context.SECURITY_PRINCIPAL,
                config.getProperty("SYSTEM_LDAP_USER"));
        env.put(Context.SECURITY_CREDENTIALS,
                config.getProperty("SYSTEM_LDAP_USER_PASSWORD"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        //log.info("environment variables >>>>>>>"+config.getProperty("SYSTEM_LDAP_USER"));
        // env.put(Context.REFERRAL, "ignore");
        return env;
    }

    public static Profile createLDAPEntry(Profile profile) throws Exception {
        log.info("----------------------> In createLDAPEntry()");
        Hashtable<String, String> env = getEnvironmentVariables();
        env.put(Context.BATCHSIZE, "2000");
        // Get a reference to a directory context
        try {
            DirContext ctx = new InitialDirContext(env);
            User newUser = new User(profile);
            // profile.setCn(newUser.getDn());
            // log.info("dn is :"+newUser.getDn());
            ctx.bind(newUser.getDn(), newUser, newUser.getMyAttrs());
            // profile.setLdapCreationStatus(SystemConstants.LDAP_CREATION_SUCCESS);
        } catch (NameAlreadyBoundException e) {
            // log.info("Exists Exception");
            // profile.setLdapCreationStatus(SystemConstants.LDAP_ENTRY_EXISTS);
            throw e;
        } catch (Exception e) {
            throw e;
        }
        return profile;
    }

    public static String validateUser(String userName, String password) {
        log.info("--------------------------> In validateUser().");
        String status = SystemConstants.AUTH_STATUS_AUTHENTICATED;
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,"ldap://" + config.getProperty("SYSTEM_LDAP_HOST_NAME")
                + ":"+ config.getProperty("SYSTEM_LDAP_HOST_PORT"));
        log.info("Get DN -> " + getDN(userName));
        env.put(Context.SECURITY_PRINCIPAL, getDN(userName));
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.REFERRAL, "follow");
        env.put(Context.BATCHSIZE, "2000");

        InitialDirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
        } catch (AuthenticationException e) {

            status = SystemConstants.AUTH_STATUS_INVALID_CREDENTIALS;
            return status;
        } catch (CommunicationException e) {
            // log.info("Communication Exception thrown message");
            status = SystemConstants.AUTH_STATUS_NETWORK_PROBLEM;
            return status;
        } catch (OperationNotSupportedException e) {
            // log.info("Account Locked");
            status = SystemConstants.AUTH_STATUS_ACCOUNT_LOCKED;

        } catch (NamingException e) {
            // log.info("Naming Exception thrown message");
            status = SystemConstants.AUTH_STATUS_SYSTEM_ERROR;
            e.printStackTrace();
        }
        return status;
    }

    public static String changeUserPassword(String userName,
                                            String oldPassword, String newPassword) {
        String result = SystemConstants.CHANGE_PASSWORD_FAILURE;
        String status = validateUser(userName, oldPassword);

        log.info("Validation status::::::" + status);
        if (SystemConstants.AUTH_STATUS_AUTHENTICATED.equalsIgnoreCase(status)) {
            result = SystemConstants.CHANGE_PASSWORD_SUCCESS;
            InitialDirContext ctx = null;
            try {
                // connect to directory
                Hashtable<String, String> env = getEnvironmentVariables();
                env.put(Context.REFERRAL, "ignore");
                env.put(Context.BATCHSIZE, "2000");
                ctx = new InitialDirContext(env);

                // get the user's LDAP distinguished name (dn)
                SearchControls constraints = new SearchControls();
                String[] returnAttributes = new String[] { "uid", "sn",
                        "givenName", "cn" };
                constraints.setReturningAttributes(returnAttributes);
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> ne = ctx.search(
                        config.getProperty("SYSTEM_LDAP_SEARCHBASE_DN"), "uid="
                                + userName, constraints);
                SearchResult sr;
                String dn = getDN(userName);

                if (dn == null || "".equals(dn.trim())) {
                    throw new Exception(
                            "A full DN could not be retrieved for user id ["
                                    + userName + "].");
                }

                // now change the password
                ModificationItem[] mods = new ModificationItem[1];
                Attribute mod0 = new BasicAttribute("userPassword", newPassword);
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                        mod0);
                ctx.modifyAttributes(dn, mods);
            } catch (InvalidAttributeValueException ie) {
                result = SystemConstants.PASSWORD_IN_HISTORY;
            } catch (Exception e) {
                e.printStackTrace();
                result = SystemConstants.CHANGE_PASSWORD_FAILURE;
            } finally {
                try {
                    if (ctx != null)
                        ctx.close();
                } catch (Exception e) {
                }
            }
        } else {
            result = SystemConstants.INVALID_OLD_PASSWORD;
            // log.info("Password change failed");
        }
        log.info("Final result is " + result);
        return result;
    }

    public static String resetUserPassword(String userName, String newPassword) {
        String result = null;

        result = SystemConstants.RESET_PASSWORD_SUCCESS;
        InitialDirContext ctx = null;
        try {
            // connect to directory
            Hashtable<String, String> env = getEnvironmentVariables();
            env.put(Context.REFERRAL, "ignore");
            env.put(Context.BATCHSIZE, "2000");
            ctx = new InitialDirContext(env);

            // get the user's LDAP distinguished name (dn)
            SearchControls constraints = new SearchControls();
            String[] returnAttributes = new String[] { "uid", "sn",
                    "givenName", "cn" };
            constraints.setReturningAttributes(returnAttributes);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> ne = ctx.search(
                    config.getProperty("SYSTEM_LDAP_SEARCHBASE_DN"), "uid="
                            + userName, constraints);
            SearchResult sr;
            String dn = getDN(userName);

            if (dn == null || "".equals(dn.trim())) {
                throw new Exception(
                        "A full DN could not be retrieved for user id ["
                                + userName + "].");
            }

            // now change the password
            ModificationItem[] mods = new ModificationItem[1];
            Attribute mod0 = new BasicAttribute("userPassword", newPassword);
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
            ctx.modifyAttributes(dn, mods);
        } catch (Exception e) {
            result = SystemConstants.RESET_PASSWORD_FAILURE;
            e.printStackTrace();
        } finally {
            try {
                if (ctx != null)
                    ctx.close();
            } catch (Exception e) {
            }
        }

        log.info("Final result is " + result);
        return result;
    }

    private static String getDN(String userName) {
        String dn = "";
        userName = userName.toLowerCase();
        log.info(">>>>>>>>"+userName);
        Hashtable<String, String> env = getEnvironmentVariables();
        log.info(">>>>>emv"+env);

        try {
            log.info(" in try");
            //InitialDirContext ctx = new InitialLdapContext(env, null);
            InitialDirContext ctx = new InitialDirContext(env);
            log.info("Initial context");
            SearchControls ctls = new SearchControls();
            log.info("Search controlss");
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = "(uid=" + userName + ")"; // Search for objects with
            // these matching
            // attributes
            NamingEnumeration results = ctx.search("", filter, ctls);
            log.info(">>>>>>results length:"+results);
            if (results != null && results.hasMoreElements()) {
                SearchResult sr = (SearchResult) results.nextElement();
                dn = sr.getName();
                log.info("In if >>>>>>>"+dn);
            } else{
                log.info("in else");
                dn = null;
            }
            log.info("Closing >>>>>>>>>>");
            ctx.close();
        } catch (AuthenticationException e) {
            // log.info("Authentication Exception");
            return null;
        } catch (NamingException e) {
            log.info("exception");
            e.printStackTrace();
            return null;
        } catch (Exception e){
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>Exception>>>>>>>>>>>>>>>"+e.getMessage());
        }

        return dn;
    }

    public static boolean shouldChangePassword(String userUid) {
        InitialDirContext ctx = null;
        try {
            // connect to the TDS dir - only users in TDS are allowed to change
            // passwords
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL,"ldap://" + config.getProperty("SYSTEM_LDAP_HOST_NAME") + ":"+ config.getProperty("SYSTEM_LDAP_HOST_PORT"));
            env.put(Context.SECURITY_PRINCIPAL,config.getProperty("SYSTEM_LDAP_USER"));
            env.put(Context.SECURITY_CREDENTIALS,config.getProperty("SYSTEM_LDAP_USER_PASSWORD"));
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.REFERRAL, "ignore");
            env.put(Context.BATCHSIZE, "2000");
            ctx = new InitialDirContext(env);

            // get the user's LDAP distinguished name (dn)
            SearchControls constraints = new SearchControls();
            String[] returnAttributes = new String[] { "pwdMaxAge" };
            constraints.setReturningAttributes(returnAttributes);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> ne = ctx.search("cn=ibmpolicies","cn=pwdpolicy", constraints);
            SearchResult sr;
            Attributes attrs;
            Attribute attr;
            String pwdMaxAgeStr = "";
            while (ne.hasMore()) {
                sr = (SearchResult) ne.next();
                attrs = sr.getAttributes();
                attr = attrs.get("pwdMaxAge");
                if (attr != null) {
                    pwdMaxAgeStr = (String) attr.get();
                }
                // got one result - it's enough for us
                break;
            }
            int pwdMaxAge;
            try {
                pwdMaxAge = (Integer.parseInt(pwdMaxAgeStr)) / (60 * 60 * 24); // in
                // days
            } catch (Exception e) {
                pwdMaxAge = 60; // default to 60 days if there is any exception,
                // e.g. str=null or ""
            }

            // get the user using the uid & determine if pwd should be change
            // now
            constraints = new SearchControls();
            returnAttributes = new String[] { "uid", "modifytimestamp","pwdChangedTime", "pwdReset", "createtimestamp" };
            constraints.setReturningAttributes(returnAttributes);
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ne = ctx.search(config.getProperty("SYSTEM_LDAP_SEARCHBASE_DN"), "uid=" + userUid,constraints);
            String modifytimestamp = "";
            while (ne.hasMore()) {
                sr = (SearchResult) ne.next();
                attrs = sr.getAttributes();
                attr = attrs.get("modifytimestamp");
                if (attr != null) {
                    modifytimestamp = (String) attr.get();
                }

                break;
            }
            // if expiry date (last modified date + max pwd age) < today, then
            // return true
            if (modifytimestamp == null || "".equals(modifytimestamp.trim())) {
                return false;
            } else {
                // expected format of modifytimestamp is 20100504140451.083158Z.
                // get first part
                try {
                    modifytimestamp = modifytimestamp.split("\\.")[0];
                } catch (Exception e) {
                    // we cannot parse modified date, so just return false
                    return false;
                }
                // now parse modifytimestamp into a date
                java.util.Date modifiedDate;
                try {
                    DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                    modifiedDate = df.parse(modifytimestamp);
                } catch (Exception e) {
                    return false;
                }
                // now check if it must be changed
                Calendar cal = Calendar.getInstance();
                cal.setTime(modifiedDate);
                cal.add(Calendar.DATE, pwdMaxAge);
                java.util.Date pwdExpiryDate = cal.getTime();
                log.info("Modified Date: "+modifiedDate);
                log.info("Max Password Age: "+pwdMaxAge);
                log.info("Password Expiry: "+pwdExpiryDate);
                if (pwdExpiryDate.before(new java.util.Date())) {
                    return true;
                } else {
                    return false;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ctx != null)
                    ctx.close();
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] argv) {

        try {
            log.info("debug<<<<<<>>>>"+getDN("sinitiator"));
        } catch (Exception e) {
            log.info(">>>>>>>>>>>nemmessgae>>>>>>>>>>>>>>>>>"+e.getMessage());
            e.printStackTrace();
        }
    }
}
