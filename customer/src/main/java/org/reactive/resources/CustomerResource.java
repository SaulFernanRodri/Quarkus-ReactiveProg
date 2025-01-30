package org.reactive.resources;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.reactive.dtos.CustomerDTO;
import org.reactive.entities.Customer;
import org.reactive.services.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/v1/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @GET
    public Uni<Response> getAllCustomers() {
        return customerService.listAllCustomers()
                .onItem().transform(customers -> Response.ok(customers).build());
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getCustomerById(@PathParam("id") Long id) {
        return customerService.getCustomer(id)
                .onItem().transform(customer -> {
                    if (customer != null) {
                        return Response.ok(customer).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });
    }

    @POST
    public Uni<Response> createCustomer(Customer customer) {
        return customerService.createCustomer(customer)
                .onItem().transform(c -> Response.ok(c).build());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteCustomer(@PathParam("id") Long id) {
        return customerService.deleteCustomer(id)
                .replaceWith(Response.ok().build());
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateCustomer(@PathParam("id") Long id, Customer customer) {
        return customerService.updateCustomer(id, customer)
                .onItem().transform(c -> Response.ok(c).build());
    }

    @PUT
    @Path("/{id}/products/{productId}")
    public Uni<Response> addProductToCustomer(@PathParam("id") Long id, @PathParam("productId") Long productId) {
        return customerService.addProductToCustomer(id, productId)
                .onItem().transform(customer -> {
                    if (customer != null) {
                        return Response.ok(customer).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });
    }

    @GET
    @Path("/{id}/products")
    public Uni<Response> getCustomerWithProducts(@PathParam("id") Long id) {
        return customerService.getCustomerWithProducts(id)
                .onItem().transform(customer -> {
                    if (customer != null) {
                        return Response.ok(customer).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                });
    }
}
