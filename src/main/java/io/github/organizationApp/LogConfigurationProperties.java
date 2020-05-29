package io.github.organizationApp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "console")
public class LogConfigurationProperties {
    private Show show;

    public Show getShow() {return show;}
    public void setShow(final Show show) {this.show = show;}

    public static class Show {
        private boolean allowAroundEachClassServiceFunctionTrackExecuteTime;

        public boolean isAllowAroundEachClassServiceFunctionTrackExecuteTime() {
            return allowAroundEachClassServiceFunctionTrackExecuteTime;
        }

        public void setAllowAroundEachClassServiceFunctionTrackExecuteTime(final boolean allowAroundEachClassServiceFunctionTrackExecuteTime) {
            this.allowAroundEachClassServiceFunctionTrackExecuteTime = allowAroundEachClassServiceFunctionTrackExecuteTime;
        }
    }
}
