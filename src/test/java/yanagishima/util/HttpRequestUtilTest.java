package yanagishima.util;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;
import static yanagishima.util.HttpRequestUtil.getRequiredHeader;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

public class HttpRequestUtilTest {
    @Test
    public void testGetOrDefaultParameter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("key1")).thenReturn("false");
        assertFalse(getOrDefaultParameter(request, "key1", true));
        assertTrue(getOrDefaultParameter(request, "not-found-key1", true));
    }

    @Test
    public void testGetRequiredParameter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("key1")).thenReturn("value1");
        assertEquals("value1", getRequiredParameter(request, "key1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequiredParameterNotExist() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("key1")).thenReturn("value1");
        getRequiredParameter(request, "unknown");
    }

    @Test
    public void testGetRequiredHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("key1")).thenReturn("value1");
        assertEquals("value1", getRequiredHeader(request, "key1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequiredHeaderNotExist() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("key1")).thenReturn("value1");
        getRequiredParameter(request, "unknown");
    }
}
