package fr.em.endpoints;

import fr.em.Entities.UtilisateurEntity;
import fr.em.dto.CreateUserDto;
import fr.em.dto.UtilisateurDto;
import fr.em.dto.MailDto;
import fr.em.repositories.UtilisateurRepository;
import fr.em.security.SecurityTools;
import io.quarkus.elytron.security.common.BcryptUtil;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
import java.util.*;

@Path("/user")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UtilisateurResource {

    @Inject
    UtilisateurRepository utilisateurRepository;

    @Context
    UriInfo uriInfo;

    @Inject
    @RestClient
    MailResource mailResource;

    @Inject
    JsonWebToken jwt;


    @PermitAll
    @POST
    @Path("/authentification")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getJwt(@HeaderParam("login") String login, @HeaderParam("password") String password) {
        UtilisateurEntity utilisateur = utilisateurRepository.findById(login);
        if (utilisateur == null) {
            return Response.ok("login inconnu").status(404).build();
        }
        if (BcryptUtil.matches(password, utilisateur.getPassword())) {
            String token = SecurityTools.getToken(utilisateur);
            return Response.ok(token).build();
        }
        return Response.ok().status(Response.Status.FORBIDDEN).build();
    }

    @Transactional
    @PermitAll
    @POST
    @Path("/creerUtilisateur")
    public Response insert(CreateUserDto utilisateur) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
//        if (utilisateurRepository.findById(utilisateur.getEmail()) != null)
//            return Response.status(417, "Cette adresse existe déjà").build();
//        if (utilisateurRepository.findById(utilisateur.getLogin()) != null)
//            return Response.status(417, "Ce login existe déjà").build();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 120);
        Date expiration = calendar.getTime();
        String url = String.format("%s|%s|%s|%s|%s|%s",
                utilisateur.getLogin(),
                utilisateur.getEmail(),
                BcryptUtil.bcryptHash(utilisateur.getPassword()),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                new SimpleDateFormat("dd-MM-yy-HH:mm:ss").format(expiration));
        String urlEncode = SecurityTools.encrypt(url);
        uriBuilder.path("/user/confirm/" + URI.create(urlEncode));
        StringBuilder body = new StringBuilder("Veuillez cliquer sur le lien suivant pour confirmer la création de votre utilisateur : ");
        body.append(uriBuilder.build());
        MailDto mailDto = new MailDto(utilisateur.getEmail(), "Confirmation de votre inscription", body.toString());
        mailResource.sendMail(mailDto, "ItsOKForYou");
        return Response.ok().status(200).build();
    }

    @Transactional
    @GET
    @Path("/confirm/{code}")
    public Response confirmCreate(@PathParam("code") String code) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String[] params = SecurityTools.decrypt(code).split("\\|");
        System.out.println(params[2] + " " + params[3]+ " " + params[4]);
        UtilisateurEntity user = new UtilisateurEntity();
        user.setLogin(params[0]);
        user.setEmail(params[1]);
        user.setPassword(params[2]);
        user.setNom(params[3]);
        user.setPrenom(params[4]);
        user.setRole("user");
        user.setDateDebutAdhesion(java.sql.Date.valueOf(LocalDate.now()));
        user.setDateFinAdhesion(java.sql.Date.valueOf(LocalDate.now().plusYears(1)));
        try {
            Date expireAt = new SimpleDateFormat("dd-MM-yy-HH:mm:ss").parse(params[5]);
            if (expireAt.before(Calendar.getInstance().getTime()))
                return Response.ok("Le lien n'est plus valide").status(400, "status 400").build();
        } catch (ParseException e) {
            return Response.ok("Le lien n'est pas valide").status(400, "status 400").build();
        }
        utilisateurRepository.persist(user);
        return Response.ok("L'utilisateur est enregistré").build();
    }

    @Transactional
    @RolesAllowed({"admin"})
    @Path("/{userLogin}/upgrade")
    @POST
    public Response upgrade(@PathParam("userLogin") String userLogin) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        UtilisateurEntity utilisateur = utilisateurRepository.findById(userLogin);
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        if (utilisateur == null) {
            return Response.ok("login inconnu").status(404).build();
        }
        String url = userLogin;
        String urlEncode = SecurityTools.encrypt(url);
        uriBuilder.path("/user/confirmUpgrade/" + URI.create(urlEncode));
        StringBuilder body = new StringBuilder("Veuillez cliquer sur le lien suivant pour confirmer l'upgrade de " + userLogin + " comme admin : ");
        body.append(uriBuilder.build());
        MailDto mailDto = new MailDto("etienne.milon@yahoo.fr", userLogin + " souhaite devenir admin", body.toString());
        mailResource.sendMail(mailDto, "ItsOKForYou");
        return Response.ok().status(200).build();
    }

    @Transactional
    @Path("/{userLogin}/delete")
    @RolesAllowed({"admin"})
    @DELETE
    public Response delete(@PathParam("userLogin") String userLogin) {
        UtilisateurEntity user = utilisateurRepository.findById(userLogin);
        utilisateurRepository.delete(user);
        return Response.ok().build();
    }

    @Transactional
    @GET
    @RolesAllowed({"admin"})
    @Path("/confirmUpgrade/{code}")
    public Response confirmUpgrade(@PathParam("code") String code) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String[] params = SecurityTools.decrypt(code).split("\\|");
        UtilisateurEntity user = utilisateurRepository.findById(params[0]);
        user.setRole("admin");
        return Response.ok("L'utilisateur " + user.getLogin() + " est désormais administrateur").build();
    }

    @RolesAllowed({"admin"})
    //@PermitAll
    @GET
    @Path("/allUsers")
    public Response allUserOas(@Context UriInfo uriInfo) {
        List<UtilisateurDto> userList = new ArrayList<>();
        for (UtilisateurEntity user : utilisateurRepository.listAll()) {
            UtilisateurDto utilisateurDto = new UtilisateurDto(user);
            String uri = uriInfo.getRequestUriBuilder().build().toString();
            utilisateurDto.addLink("self", uri.replace("/allUsers", "") + "/" + user.getLogin());
            userList.add(utilisateurDto);
        }
        return Response.ok(userList).build();
    }

    @RolesAllowed({"admin", "user"})
    @GET
    @Path("{login}")
    public Response userByLogin(@PathParam("login") String login, @Context UriInfo uriInfo) {
        UtilisateurEntity utilisateurEntity = utilisateurRepository.findById(login);
        if (utilisateurEntity != null) {
            UtilisateurDto utilisateurDto = new UtilisateurDto(utilisateurEntity);
            if (jwt.getGroups().contains("admin")) {
                String uri = uriInfo.getRequestUriBuilder().build().toString();
                utilisateurDto.addLink("allUser", uri.replace(utilisateurDto.getLogin(), "allUsers"));
                if (!Objects.equals(utilisateurDto.getRole(), "admin"))
                    utilisateurDto.addLink("upgrade", uri + "/upgrade");
                utilisateurDto.addLink("delete", uri + "/delete");
                return Response.ok(utilisateurDto).build();
            }
//            if (jwt.getGroups().contains("user") && jwt.getName() == utilisateurEntity.getLogin()) {
//                return Response.ok(utilisateurDto).build();
//            }
        }
        return Response.ok("login inconnu").build();
    }

}
