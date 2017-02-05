package system.ticketing.profile.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import system.ticketing.profile.dto.*;
import system.ticketing.profile.model.*;

/**
 * Created by Ncube on 2/5/17.
 */
public class DTOMapper {
    private static ModelMapper mapper;

    public static ModelMapper getMapper() {
        if (mapper == null) {
            mapper = new ModelMapper();
            mapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
        }
        return mapper;
    }

    public static ProfileDTO toProfileDTO(Profile profile){
        ProfileDTO profileDTO = getMapper().map(profile, ProfileDTO.class);
        profileDTO.setRoleName(profile.getRole().getName());
        profileDTO.setRoleId(profile.getRole().getId());
        return profileDTO;
    }

    public static Profile toProfileEntity(ProfileDTO dto){
        Profile profile = getMapper().map(dto, Profile.class);
        return profile;
    }

    public static RoleDTO toRoleDTO(Role role){
        return getMapper().map(role, RoleDTO.class);
    }

    public static Role toRoleEntity(RoleDTO dto){
        return getMapper().map(dto, Role.class);
    }

    public static AccessRightDTO toAccessRightDTO(AccessRight accessRight){
        return getMapper().map(accessRight, AccessRightDTO.class);
    }

    public static AccessRight toAccessRightEntity(AccessRightDTO dto){
        return getMapper().map(dto, AccessRight.class);
    }

    public static RoleAccessRightDTO toRoleAccessRightDTO(RoleAccessRight roleAccessRight){
        if (roleAccessRight == null) return null;
        RoleAccessRightDTO roleAccessRightDTO = getMapper().map(roleAccessRight, RoleAccessRightDTO.class);
        roleAccessRightDTO.setRoleName(roleAccessRight.getRole().getName());
        roleAccessRightDTO.setActionName(roleAccessRight.getAccessRight().getActionName());
        return roleAccessRightDTO;
    }

    public static RoleAccessRight toRoleAccessRightEntity(RoleAccessRightDTO dto){
        RoleAccessRight roleAccessRight = getMapper().map(dto, RoleAccessRight.class);
        return roleAccessRight;
    }

    public static AuditTrailDTO toAuditTrailDTO(AuditTrail auditTrail){
        AuditTrailDTO dto = getMapper().map(auditTrail, AuditTrailDTO.class);
        dto.setActivityId(auditTrail.getActivity().getId());
        return dto;
    }

    public static AuditTrail toAuditTrailEntity(AuditTrailDTO dto) {
        return getMapper().map(dto, AuditTrail.class);
    }

    public static ActivityDTO toActivityDTO(Activity activity){
        return getMapper().map(activity, ActivityDTO.class);
    }

    public static Activity toActivityEntity(ActivityDTO dto){
        return getMapper().map(dto, Activity.class);
    }

}
