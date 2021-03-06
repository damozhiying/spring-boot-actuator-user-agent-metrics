package eu.hinsch.spring.boot.actuator.useragent;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    private final UserAgentStringParser resourceModuleParser;

    public UserAgentParser() {
        resourceModuleParser = UADetectorServiceFactory.getResourceModuleParser();
    }

    @Cacheable("user-agent-parser")
    public ReadableUserAgent parseUserAgentString(String userAgentString) {
        return resourceModuleParser.parse(userAgentString);
    }
}