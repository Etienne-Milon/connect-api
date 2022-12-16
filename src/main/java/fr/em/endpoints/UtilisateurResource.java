package fr.em.endpoints;

import fr.em.Entities.UtilisateurEntity;
import fr.em.JwtService;
import fr.em.dto.UtilisateurDto;
import fr.em.dto.MailDto;
import fr.em.repositories.UtilisateurRepository;
import fr.em.security.SecurityTools;
import io.quarkus.elytron.security.common.BcryptUtil;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@Path("/jwt")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_PLAIN)
public class UtilisateurResource {

    UtilisateurRepository utilisateurRepository = new UtilisateurRepository();

    @Context
    UriInfo uriInfo;

    @Inject
    @RestClient
    MailResource mailResource;

    @GET
    public Response getJwt() {
        String jwt = JwtService.generateJwt();
        return Response.ok(jwt).build();
    }

    @POST
    @Path("/authentification")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJwt(@HeaderParam("login") String login, @HeaderParam("password") String password) {
        UtilisateurEntity utilisateur = utilisateurRepository.findById(login);
        if (utilisateur == null) {
            return Response.ok("login inconnu").status(404).build();
        }
        if (BcryptUtil.matches(password, utilisateur.getPasswordhash())) {
            String token = SecurityTools.getToken(utilisateur);
            return Response.ok(token).build();
        }
        return Response.ok().status(Response.Status.FORBIDDEN).build();
    }

    @Transactional
    @POST
    @Path("/creerUtilisateur")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insert(UtilisateurDto utilisateur) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        if (utilisateurRepository.findById(utilisateur.getEmail()) != null)
            return Response.status(417, "Cette adresse existe déjà").build();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 20);
        Date expiration = calendar.getTime();
        String url = String.format("%s|%s|%s|%s|%s",
                utilisateur.getLogin(),
                utilisateur.getEmail(),
                BcryptUtil.bcryptHash(utilisateur.getPassword()),
                "user",
                new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(expiration));
        String urlEncode = SecurityTools.encrypt(url);
        uriBuilder.path(URI.create(urlEncode).toString());

        StringBuilder body = new StringBuilder("Veillez cliquer sur le lien suivant pour confirmer la création de votre utilisateur : ");
        body.append(uriBuilder.build());
        MailDto mailDto = new MailDto(utilisateur.getEmail(), "Confirmation de votre inscription", body.toString());

        mailResource.sendMail(mailDto, "ItsOKForYou");
        return Response.ok().status(200).build();
    }

    @Transactional
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/confirm")
    public Response confirmCreate(@QueryParam("code") String code) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String[] params = SecurityTools.decrypt(code).split("\\|");
        UtilisateurEntity user = new UtilisateurEntity();
        user.setLogin(params[0]);
        user.setEmail(params[1]);
        user.setPasswordhash(params[2]);
        user.setRole(params[3]);
        user.setNom("");
        user.setPrenom("");
        user.setDateDebutAdhesion(java.sql.Date.valueOf(LocalDate.now()));
        user.setDateFinAdhesion(java.sql.Date.valueOf(LocalDate.now().plusYears(1)));
        try {
            Date expireAt = new SimpleDateFormat("dd-MM-yy-HH:mm:ss").parse(params[4]);
            if (expireAt.before(Calendar.getInstance().getTime()))
                return Response.ok("Le lien n'est plus valide").status(400, "status 400").build();
        } catch (ParseException e) {
            return Response.ok("Le lien n'est pas valide").status(400, "status 400").build();
        }
        utilisateurRepository.persist(user);
        return Response.ok("Le utilisateur est confirmé").build();
    }

}
