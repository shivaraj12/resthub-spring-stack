package org.resthub.web.controller;

import java.lang.reflect.Array;
import java.net.URI;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.resthub.core.util.ClassUtils;

import com.sun.jersey.api.view.ImplicitProduces;
import java.io.Serializable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import org.resthub.core.service.GenericService;
import org.resthub.web.response.PageResponse;
import org.synyx.hades.domain.PageRequest;

/**
 * Generic REST controller
 * @author bouiaw
 * @param <T> Entity to manage
 * @param <ID> Entity key
 */
@Singleton
@ImplicitProduces("text/html;qs=5")
public abstract class GenericController<T, S extends GenericService<T, ID>, ID extends Serializable> {

	protected T[] entityClassArray;
	protected S service;

	@Context
	protected UriInfo uriInfo;

	/**
	 * Allow to custom how the identifier, used in URLs, is generated from the entity parameter
	 * @param entity
	 */
	abstract public ID generateIdentifierFromEntity(T entity);

	@SuppressWarnings("unchecked")
	public GenericController() {
		entityClassArray = (T[]) Array.newInstance(ClassUtils.getGenericTypeFromBean(this), 0);
	}

	public void setService(S service) {
		this.service = service;
	}

	public S getService() {
		return this.service;
	}

	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response create(T entity) {
		T e = this.service.create(entity);
		UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
		URI uri = uriBuilder.path(generateIdentifierFromEntity(e).toString()).build();

		return Response.created(uri).entity(e).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response update(@PathParam("id") ID id, T entity) {
		T e = this.service.findById(id);

		this.service.update(entity);
		return Response.created(uriInfo.getAbsolutePath()).build();
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEntities(
                @QueryParam("page") @DefaultValue("0") Integer page,
                @QueryParam("size") @DefaultValue("5") Integer size) {

            PageResponse<T> entitys = new PageResponse<T>(
                    this.service.findAll(new PageRequest(page, size)));
            return Response.ok(entitys).build();
	}

	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getResource(@PathParam("id") ID id) {
		T entity = this.service.findById(id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.ok(entity).build();
	}

	@DELETE
	@Path("/{id}")
	public void delete(@PathParam("id") ID id) {
		this.service.delete(id);
	}

}