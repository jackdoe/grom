package grom;
import io.dropwizard.setup.Environment;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
public class MainApplication extends Application<MainConfiguration> {
    public static void main(String[] args) throws Exception {
        new MainApplication().run(args);
    }

    @Override
    public String getName() {
        return "grom";
    }

    @Override
    public void initialize(Bootstrap<MainConfiguration> bootstrap) {
    }

    @Override
    public void run(MainConfiguration configuration, Environment environment) {
        final MainHealthCheck healthCheck = new MainHealthCheck();
        environment.healthChecks().register("main", healthCheck);

        environment.jersey().register(new ClassifyResource());
        environment.jersey().register(new PingResource());
    }
}
