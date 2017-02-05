package system.ticketing.utilities.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.utilities.enums.*;
import system.ticketing.utilities.pojo.ApplicableTariffDTO;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Properties;

import static system.ticketing.utilities.util.SystemConstants.*;

/**
 * Created by Ncube on 2/5/17.
 */
public class Utils {
    static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static Properties configParams;
    public static String SYSTEM_CONFIG_FOLDER;
    public static String SYSTEM_CONFIG_FILE;

    static {
        String fileSep = System.getProperties().getProperty("file.separator");
        String root = fileSep;
        if (fileSep.equals("\\")) {
            fileSep = "\\\\";
            root = "c:\\";
        }
        SYSTEM_CONFIG_FOLDER = root + "opt" + fileSep + "eSolutions" + fileSep + "pundits" + fileSep + "conf" + fileSep;
        SYSTEM_CONFIG_FILE = SYSTEM_CONFIG_FOLDER + "pundits.conf";

        // read the system configurations
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(SYSTEM_CONFIG_FILE));
            configParams = config;
            //System.out.println("config:: " + config);
        } catch (Exception ex) {
            System.out.println("Error: Could not read the system configuration file '" + SYSTEM_CONFIG_FILE + "'. Make sure it exists.");
            System.out.println("Specific error: " + ex);
        }
    }

    public static boolean isTerminalStatus(String status) {
        return TransactionStatus.SUCCESSFUL.name().equals(status)
                || TransactionStatus.FAILED.name().equals(status)
                || TransactionStatus.MANUAL_RESOLVE.name().equals(status)
                || TransactionStatus.REVERSED.name().equals(status);
    }

    public static long getDelayToNextRunInSeconds(String configProperty) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String loadArrearsRunTime = configParams.getProperty(configProperty, "23:59:59");
        int[] tokens = Arrays.stream(loadArrearsRunTime.split(":")).mapToInt(Integer::parseInt).toArray();
        LocalDateTime nextRunTime = LocalDateTime.now()
                .withHour(tokens[0]).withMinute(tokens[1]).withSecond(tokens[2]).withNano(0);
        if (nextRunTime.getHour() <= now.getHour()) {
            log.info("Scheduled time behind or very close to now... verify and reschedule");
            if (nextRunTime.getHour() == now.getHour()) {
                log.info("Same hour ... check minutes");
                if (nextRunTime.getMinute() <= now.getMinute()) {
                    log.info("Scheduled minutes are behind ... must have executed... schedule for next day!");
                    nextRunTime = nextRunTime.plusDays(1);
                }
            } else {
                log.info("Scheduled Hour is behind ... must have executed... schedule for next day! ");
                nextRunTime = nextRunTime.plusDays(1);
            }
        }
        log.info("Now -> " + now);
        log.info("Next Run -> " + nextRunTime);
        long seconds = ChronoUnit.SECONDS.between(now, nextRunTime);
        log.info("Seconds to next settlement run -> " + seconds);
        return seconds;
    }

    public static String getTransactionCategory(String type) {
        if (TariffItemType.COMMISSION.name().equals(type)) {
            return TransactionCategory.COMMISSION.name();
        } else if (TariffItemType.ARREARS.name().equals(type)) {
            return TransactionCategory.ARREARS.name();
        } else {
            return TransactionCategory.CHARGE.name();
        }
    }

    public static BigDecimal parseBigDecimal(String val) {
        if (StringUtils.isEmpty(val)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(val);
    }

    public static boolean isPostableTariff(ApplicableTariffDTO t) {
        return TariffItemType.STEP_FEE.name().equals(t.getType())
                || TariffItemType.MONTHLY_FEE.name().equals(t.getType())
                || TariffItemType.TRANSACTION_FEE.name().equals(t.getType())
                || TariffItemType.ARREARS.name().equals(t.getType())
                || TariffItemType.VENDOR_TRANSACTION.name().equals(t.getType())
                || TariffItemType.VAT.name().equals(t.getType());
    }

    public static boolean isChargeTariff(ApplicableTariffDTO t) {
        return TariffItemType.STEP_FEE.name().equals(t.getType())
                || TariffItemType.MONTHLY_FEE.name().equals(t.getType())
                || TariffItemType.TRANSACTION_FEE.name().equals(t.getType())
                || TariffItemType.ARREARS.name().equals(t.getType())
                || TariffItemType.VAT.name().equals(t.getType());
    }

    public static boolean isFeeTariff(ApplicableTariffDTO t) {
        return TariffItemType.STEP_FEE.name().equals(t.getType())
                || TariffItemType.MONTHLY_FEE.name().equals(t.getType())
                || TariffItemType.TRANSACTION_FEE.name().equals(t.getType());
    }

    public static boolean isTaxTariff(ApplicableTariffDTO t) {
        return TariffItemType.VAT.name().equals(t.getType());
    }

    public static boolean isArrearsTariff(ApplicableTariffDTO t) {
        return TariffItemType.ARREARS.name().equals(t.getType());
    }

    public static TransactionType getTransactionType(String processingCode) {
        TransactionType type;
        switch (processingCode) {
            case PC_CUSTOMER_INFO : type = TransactionType.CUSTOMER_INFO; break;
            case PC_VEND_ADVICE : type = TransactionType.VEND_ADVICE; break;
            case PC_LAST_TOKEN : type = TransactionType.LAST_TOKEN; break;
            case PC_VERY_TOKEN : type = TransactionType.VERIFY_TOKEN; break;
            case PC_FAULT_REPORT : type = TransactionType.FAULT_REPORT; break;
            case PC_CREDIT_TOKEN : type = TransactionType.CREDIT_TOKEN; break;
            case PC_FREE_TOKEN : type = TransactionType.FREE_TOKEN; break;
            case PC_TEST_TOKEN : type = TransactionType.TEST_TOKEN; break;
            case PC_CREDIT_TRANSFER_TOKEN : type = TransactionType.CREDIT_TRANSFER_TOKEN; break;
            case PC_REVERSAL : type = TransactionType.REVERSAL; break;
            default: type = null;
        }
        return type;
    }

    public static boolean isAllowOverdraft(AccountType accountType) {
        return AccountType.UTILITY_CONTROL.equals(accountType)
                || AccountType.VENDOR_CONTROL.equals(accountType)
                || AccountType.METER_MAIN.equals(accountType);
    }

    public static boolean isSuperAdminRole(String roleName) {
        return "SuperAdmin".equals(roleName);
    }
}
