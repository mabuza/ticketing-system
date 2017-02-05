package system.ticketing.utilities.audit;

import java.util.Map;

/**
 * Created by Ncube on 2/5/17.
 */
public interface Auditable {
    default String getEntityName() {
        return getClass().getSimpleName();
    }

    public Map< String, String> getAuditableAttributesMap();

    public String getAuditableAttributesString();

    public String getInstanceName();
}
