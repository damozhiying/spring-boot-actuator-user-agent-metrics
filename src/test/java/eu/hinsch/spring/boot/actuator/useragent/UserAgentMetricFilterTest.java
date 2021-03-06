package eu.hinsch.spring.boot.actuator.useragent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.actuate.metrics.CounterService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by lukas.hinsch on 02.06.2015.
 */
public class UserAgentMetricFilterTest {

    private static final String CHROME = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
            "Chrome/41.0.2228.0 Safari/537.36";

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private CounterService counterService;

    @Mock
    private UserAgentMetricFilterConfiguration configuration;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequest servletRequest;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private UserAgentMetricFilter filter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        // TODO mock userAgentParser and adjust test accordingly for proper unit testing
        filter = new UserAgentMetricFilter(counterService, beanFactory, configuration, new UserAgentParser());
    }

    @Test
    public void shouldLogUserAgentWithDefaultConfig() throws Exception {
        // given
        mockUserAgentHeader(CHROME);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(counterService).increment("user-agent.chrome.41");
    }

    @Test
    public void shouldLogUserAgentWithConfiguredKeys() throws Exception {
        // given
        mockUserAgentHeader(CHROME);
        mockKeyConfiguration("#this.name", "#this.operatingSystem.name");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(counterService).increment("user-agent.chrome");
        verify(counterService).increment("user-agent.windows-7");
    }

    @Test
    public void shouldLogRequestData() throws Exception {
        // given
        mockUserAgentHeader(CHROME);
        mockKeyConfiguration("@currentRequest.getHeader('MyHeader')");
        when(request.getHeader("MyHeader")).thenReturn("MyHeaderValue");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(counterService).increment("user-agent.myheadervalue");
    }

    @Test
    public void shouldUseOtherBean() throws Exception {
        // given
        mockUserAgentHeader(CHROME);
        when(beanFactory.getBean("myBean")).thenReturn("value");
        mockKeyConfiguration("@myBean");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(counterService).increment("user-agent.value");
    }

    @Test
    public void shouldNotLogAnythingIfRequestIsNotHttpServletRequest() throws Exception {
        // when
        filter.doFilter(servletRequest, response, filterChain);

        // then
        verify(counterService, never()).increment(anyString());
    }

    @Test
    public void shouldCallFilterChain() throws Exception {
        // when
        filter.doFilter(servletRequest, response, filterChain);

        // then
        verify(filterChain).doFilter(servletRequest, response);
    }

    @Test
    public void shouldGenerateCoverageOnUnusedMethods() throws ServletException {
        filter.init(null);
        filter.destroy();
    }

    private void mockKeyConfiguration(String... keys) {
        when(configuration.getKeys()).thenReturn(asList(keys));
    }

    private void mockUserAgentHeader(String userAgent) {
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
    }
}
