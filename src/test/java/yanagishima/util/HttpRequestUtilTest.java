package yanagishima.util;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;
import static yanagishima.util.HttpRequestUtil.getRequiredHeader;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

class HttpRequestUtilTest {
    @Test
    void testGetOrDefaultParameter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("key1")).thenReturn("false");
        assertFalse(getOrDefaultParameter(request, "key1", true));
        assertTrue(getOrDefaultParameter(request, "not-found-key1", true));
    }

    @Test
    void testGetRequiredParameter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("key1")).thenReturn("value1");
        assertEquals("value1", getRequiredParameter(request, "key1"));
    }

    @Test
    void testGetRequiredParameterNotExist() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("key1")).thenReturn("value1");
        assertThatThrownBy(() -> getRequiredParameter(request, "unknown"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGetRequiredHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("key1")).thenReturn("value1");
        assertEquals("value1", getRequiredHeader(request, "key1"));
    }

    @Test
    void testGetRequiredHeaderNotExist() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("key1")).thenReturn("value1");
        assertThatThrownBy(() -> getRequiredParameter(request, "unknown"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
