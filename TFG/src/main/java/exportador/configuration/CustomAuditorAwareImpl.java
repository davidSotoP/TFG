package exportador.configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CustomAuditorAwareImpl implements AuditorAware<String> {


    @Autowired
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentAuditor = null;
        if (authentication != null && authentication.isAuthenticated()) {
        	currentAuditor = ( (CustomUserDetails) authentication.getPrincipal()).getUsername();
        }
        
        return Optional.ofNullable(currentAuditor);
    }

}
