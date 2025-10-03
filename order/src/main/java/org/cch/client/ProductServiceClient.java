package org.cch.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.cch.dto.ProductDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@Path("/products")
@RegisterRestClient(configKey="product-service-client")
public interface ProductServiceClient {

    @GET
    @Path("/{id}")
    Uni<ProductDTO> findById(@PathParam("id") String id);
}