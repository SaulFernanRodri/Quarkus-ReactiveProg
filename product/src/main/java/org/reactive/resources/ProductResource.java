package org.reactive.resources;

import org.reactive.entities.Product;
import org.reactive.services.ProductService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;


@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    public Uni<Response> list() {
        return productService.listAllProducts()
                .map(products -> Response.ok(products).build());
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") Long id) {
        return productService.getProductById(id)
                .map(product -> product != null ? Response.ok(product).build() : Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Uni<Response> add(Product p) {
        return productService.createProduct(p)
                .map(product -> Response.ok(product).build());
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Product p) {
        return productService.updateProduct(id, p)
                .map(product -> Response.ok(product).build())
                .onFailure().recoverWithItem(throwable -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(throwable.getMessage()).build());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return productService.deleteProduct(id)
                .replaceWith(Response.ok().build());
    }
}
