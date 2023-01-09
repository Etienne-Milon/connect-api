package fr.em.endpoints;

import fr.em.dto.MailDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient
@Path("/mailer")
public interface MailResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response sendMail(MailDto mail, @HeaderParam("ApiKey") String apiKey);
}
