package grom;

import com.codahale.metrics.health.HealthCheck;

public class MainHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
