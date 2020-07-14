package io.github.organizationApp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTest {

    @Test
    @DisplayName("Should throw IllegalStateException when referring to request attributes outside of an actual web request")
    void getUserId_RequestFromOutsideThread_throwsIllegalStateException() {
        // given

        // when
        var exception = catchThrowable(() -> User.getUserId());

        // then
        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("occurred while capturing a request");
    }

    @Test
    @DisplayName("Should throw NullPointerException when no user id found")
    void getUserId_noId_throwsNullPointerException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        var exception = catchThrowable(() -> User.getUserId());

        // then
        assertThat(exception)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("client has not been recogn");
    }

}