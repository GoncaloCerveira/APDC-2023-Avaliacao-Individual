package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.ModifyPassData;

@Path("/modifyPass")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyPassResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ModifyPassResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyPass(ModifyPassData data) {
		LOG.fine("Attempt to modify password by " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key userTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);

		Transaction txn = datastore.newTransaction();
		
		try {
			Entity user = txn.get(userKey);
			
			if(user == null) {
				txn.rollback();
				LOG.warning("ModifyPass operation canceled, " + data.username + " does not exist.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " does not exist.").build();
			}
			
			Entity userToken = txn.get(userTokenKey);
			
			if(userToken == null) {
				txn.rollback();
				LOG.warning("ModifyPass operation canceled, " + data.username + " is not logged in.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " is not logged in.").build();
			}
			
			if(!isTokenValid(userToken)) {
				LogoutResource invalidTokenAux = new LogoutResource();
				invalidTokenAux.logout(data.username);

				txn.rollback();
				LOG.warning("ModifyPass operation canceled, " + data.username + " session has expired.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " session has expired.").build();
			}
			
			String password = user.getString("password");
			if(!password.equals(DigestUtils.sha512Hex(data.password))) {
				txn.rollback();
				LOG.warning("Wrong password for " + data.username);

				return Response.status(Status.BAD_REQUEST).entity("Wrong Password.").build();
			}
			
			int auxValid = data.isValid();
			
			if(auxValid != 2) {
				if(auxValid == 1)
					return Response.status(Status.BAD_REQUEST).entity("Confirmation is not the same as the password.").build();
				else
					return Response.status(Status.BAD_REQUEST).entity("Missing credenticials.").build();
			}
			
			if(!data.checkPass())
				return Response.status(Status.BAD_REQUEST).entity("New password requires at least a number and an upper case letter.").build();
			
			user = Entity.newBuilder(userKey)
					.set("username", user.getString("username"))
					.set("email", user.getString("email"))
					.set("name", user.getString("name"))
					.set("password", DigestUtils.sha512Hex(data.newPass))
					.set("profile", user.getString("profile"))
					.set("telephone", user.getString("telephone"))
					.set("mobilePhone", user.getString("mobilePhone"))
					.set("profession", user.getString("profession"))
					.set("workPlace", user.getString("workPlace"))
					.set("address", user.getString("address"))
					.set("additionalAddress", user.getString("additionalAddress"))
					.set("city", user.getString("city"))
					.set("zip", user.getString("zip"))
					.set("nif", user.getString("nif"))
					.set("userCreationTime", user.getTimestamp("userCreationTime"))
					.set("role", user.getString("role"))
					.set("state", user.getString("state"))
					.build();
			
			txn.put(user);
			LOG.info("Password of " + data.username + " modifies successfuly.");
			txn.commit();

			return Response.ok("Password has successfuly been modified.").build();
		}
		finally {
			if(txn.isActive()) {
			txn.rollback();
			}
		}
	}
	
	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();
		return !(token.getLong("expirationData") < currentTime);
	}
}
