package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import com.google.cloud.Timestamp;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	
	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Gson g = new Gson();
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public RegisterResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistration(RegisterData data) {
		LOG.fine("Register attempt by " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		
		data.fillOptional();
		
		int auxValid = data.isValid();
				
		if(auxValid != 2) {
			if(auxValid == 1)
				return Response.status(Status.BAD_REQUEST).entity("Confirmation is not the same as the password.").build();
			else
				return Response.status(Status.BAD_REQUEST).entity("Missing or incorrect credenticials.").build();
		}
		
		if(!data.checkPass())
			return Response.status(Status.BAD_REQUEST).entity("Password requires at least a number and an upper case letter.").build();
		
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity user = txn.get(userKey);

			if(user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			}
			else {
				user = Entity.newBuilder(userKey)
						.set("username", data.username)
						.set("email", data.email)
						.set("name", data.name)
						.set("password", DigestUtils.sha512Hex(data.password))
						.set("profile", data.profile)
						.set("telephone", data.telephone)
						.set("mobilePhone", data.mobilePhone)
						.set("profession", data.profession)
						.set("workPlace", data.workPlace)
						.set("address", data.address)
						.set("additionalAddress", data.additionalAddress)
						.set("city", data.city)
						.set("zip", data.zip)
						.set("nif", data.nif)
						.set("userCreationTime", Timestamp.now())
						.set("role", "USER")
						.set("state", "INATIVO")
						.build();
			}

			txn.add(user);

			LOG.info("Registration of " + data.username + " successful.");

			txn.commit();

			return Response.ok("User " + data.username + " created").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
	
	
	@POST
    @Path("/SU")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerSuper(){

        Key userKey = datastore.newKeyFactory().setKind("User").newKey("superuser");

        Entity user = Entity.newBuilder(userKey)
        		.set("username", "superuser")
				.set("email", "g.cerveira@campus.fct.unl.pt")
				.set("name", "Gonçalo Cerveira")
				.set("password", DigestUtils.sha512Hex("bueseguroMano123"))
				.set("profile", "Privado")
				.set("telephone", "9666")
				.set("mobilePhone", "9234")
				.set("profession", "Farmer")
				.set("workPlace", "FCT")
				.set("address", "Caparica")
				.set("additionalAddress", "Lagoa")
				.set("city", "Lisbon")
				.set("zip", "123")
				.set("nif", "7645")
				.set("userCreationTime", Timestamp.now())
				.set("role", "SU")
				.set("state", "ATIVO")
				.build();
        datastore.add(user);
        LOG.info("User successfully registered: superuser");
		return Response.ok(g.toJson(true)).build();
    }
}
