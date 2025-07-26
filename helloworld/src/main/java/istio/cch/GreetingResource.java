package istio.cch;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

record Information(
    String ip,
    String hostname,
    LocalDateTime timestamp,
    String version
) {

}
@Path("/hello")
public class GreetingResource {

    @Inject
    Logger log;

    @ConfigProperty(name = "QUARKUS_APPLICATION_VERSION")
    String version;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Information> helloK8s() {
        try {
            // Get the InetAddress object for the local host
            InetAddress localHost = InetAddress.getLocalHost();

            // Get the IP address
            String ipAddress = localHost.getHostAddress();

            // Get the hostname
            String hostName = localHost.getHostName();

            log.info("Local IP Address: " + ipAddress);
            log.info("Local Hostname: " + hostName);
            var info = new Information(ipAddress, hostName, LocalDateTime.now(), version);

            return Uni.createFrom().item(info);

        } catch (UnknownHostException e) {
            log.error("Could not determine local host information: " + e.getMessage());
        }
        return Uni.createFrom().failure(new WebApplicationException("System error", 500));
    }
}
